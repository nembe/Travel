package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.dao.WhitelistImportDao;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
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

    private final WhitelistImportDao importDao;
    private final WhitelistCsvParser parser;
    private final TransponderCardDao transponderCardDao;
    private final Path doneDir;
    private final Long mainAccountId;

    @Autowired
    public WhitelistFileImporter(WhitelistImportDao importDao,
                                 WhitelistCsvParser parser,
                                 TransponderCardDao transponderCardDao,
                                 @Value("${tc.import.doneDir}") String doneDir,
                                 @Value("${tc.import.mainAccountId}") Long mainAccountId) throws IOException {
        this.importDao = importDao;
        this.parser = parser;
        this.transponderCardDao = transponderCardDao;
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
                // TODO delete system user
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
            addTransponderCard(entry);
            // TODO add system user
            importDao.createEntry(entry);
        }
    }

    private void addTransponderCard(WhitelistEntry entry) {
        TransponderCard card = new TransponderCard();

        card.setCustomerId(mainAccountId);
        card.setCardNumber(entry.getTravelcardNumber());
        card.setLicenseplate(entry.getLicensePlate());
        card.setCountry(CARD_COUNTRY);
        card.setStatus(CardStatus.ACTIVE);

        card = transponderCardDao.createCard(card);

        entry.setTransponderCardId(card.getId());
    }

    private void checkDoneDirectory() throws IOException {
        if(!Files.exists(doneDir))
            Files.createDirectory(doneDir);
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
}
