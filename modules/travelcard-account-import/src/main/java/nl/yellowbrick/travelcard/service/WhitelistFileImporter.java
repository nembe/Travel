package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.dao.SystemUserDao;
import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.dao.WhitelistImportDao;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.UserAccountType;
import nl.yellowbrick.data.domain.WhitelistEntry;
import nl.yellowbrick.travelcard.WhitelistFileWatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class WhitelistFileImporter implements WhitelistFileWatchListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistFileImporter.class);
    private static final String CARD_COUNTRY = "NL";
    private static final String USERNAME_PREFIX = "tc";

    private final WhitelistImportDao importDao;
    private final WhitelistCsvParser parser;
    private final TransponderCardDao transponderCardDao;
    private final SystemUserDao systemUserDao;
    private final Path doneDir;
    private final Long mainAccountId;

    @Autowired
    public WhitelistFileImporter(WhitelistImportDao importDao,
                                 WhitelistCsvParser parser,
                                 TransponderCardDao transponderCardDao,
                                 SystemUserDao systemUserDao,
                                 @Value("${tc.import.doneDir}") String doneDir,
                                 @Value("${tc.import.mainAccountId}") Long mainAccountId) throws IOException {
        this.importDao = importDao;
        this.parser = parser;
        this.transponderCardDao = transponderCardDao;
        this.systemUserDao = systemUserDao;
        this.doneDir = Paths.get(doneDir).toAbsolutePath();
        this.mainAccountId = mainAccountId;

        checkDoneDirectory();
    }

    @Override
    @Transactional
    public void fileCreated(Path path) {
        try {
            List<WhitelistEntry> csvEntries = parser.parseFile(path);
            LOGGER.info("processing {} entries from file {}", csvEntries.size(), path.getFileName());

            importDao.markAllAsObsolete();

            csvEntries.forEach(this::createOrUpdateEntry);

            importDao.scanObsolete((entry) -> {
                transponderCardDao.cancelCard(entry.getTransponderCardId());
                systemUserDao.deleteAppUserByCardId(entry.getTransponderCardId());
            });

            importDao.deleteAllObsolete();

        } catch (IOException e) {
            LOGGER.error("Failed to parse file " + path.toString(), e);
        } finally {
            moveToDoneDirectory(path);
        }
    }

    private void createOrUpdateEntry(WhitelistEntry entry) {
        Optional<WhitelistEntry> maybeExistingEntry = importDao.findByTravelcardNumber(entry.getTravelcardNumber());

        if(maybeExistingEntry.isPresent()) {
            WhitelistEntry existingEntry = maybeExistingEntry.get();
            existingEntry.setLicensePlate(entry.getLicensePlate());
            existingEntry.setObsolete(false);

            importDao.updateEntry(existingEntry);
            transponderCardDao.updateLicensePlate(existingEntry.getTransponderCardId(), entry.getLicensePlate());
        } else {
            TransponderCard card = createTransponderCard(entry);
            entry.setTransponderCardId(card.getId());

            importDao.createEntry(entry);
            systemUserDao.createAppUser(card, username(entry), password(entry), UserAccountType.RESTRICTED_SUBACCOUNT);
        }
    }

    private TransponderCard createTransponderCard(WhitelistEntry entry) {
        TransponderCard card = new TransponderCard();

        card.setCustomerId(mainAccountId);
        card.setCardNumber(entry.getTravelcardNumber());
        card.setLicenseplate(entry.getLicensePlate());
        card.setCountry(CARD_COUNTRY);
        card.setStatus(CardStatus.ACTIVE);

        return transponderCardDao.createCard(card);
    }

    private void checkDoneDirectory() throws IOException {
        if(!Files.exists(doneDir))
            Files.createDirectories(doneDir);
    }

    private void moveToDoneDirectory(Path path) {
        try {
            LOGGER.info("moving processed file {} to directory {}", path, doneDir);
            Files.move(path, doneDir.resolve(doneFileName(path)), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error(String.format("could not move %s to done directory %s", path, doneDir), e);
        }
    }

    private String doneFileName(Path path) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

        return path.getFileName().toString() + "." + formatter.format(LocalDateTime.now());
    }

    private String username(WhitelistEntry entry) {
        return (USERNAME_PREFIX + entry.getTravelcardNumber()).toLowerCase();
    }

    private String password(WhitelistEntry entry) {
        return entry.getLicensePlate().toLowerCase();
    }
}
