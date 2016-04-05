package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.dao.SystemUserDao;
import nl.yellowbrick.data.dao.WhitelistImportDao;
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

@Component
public class WhitelistFileImporter implements WhitelistFileWatchListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistFileImporter.class);
    private static final String USERNAME_PREFIX = "tc";

    private final WhitelistImportDao importDao;
    private final WhitelistCsvParser parser;
    private final SystemUserDao systemUserDao;
    private final CardBindingService cardBindingService;
    private final EmailNotificationService emailNotificationService;
    private final Path doneDir;

    @Autowired
    public WhitelistFileImporter(WhitelistImportDao importDao,
                                 WhitelistCsvParser parser,
                                 SystemUserDao systemUserDao,
                                 CardBindingService cardBindingService,
                                 EmailNotificationService emailNotificationService,
                                 @Value("${tc.import.doneDir}") String doneDir) throws IOException {
        this.importDao = importDao;
        this.parser = parser;
        this.systemUserDao = systemUserDao;
        this.cardBindingService = cardBindingService;
        this.emailNotificationService = emailNotificationService;
        this.doneDir = Paths.get(doneDir).toAbsolutePath();

        checkDoneDirectory();
    }

    @Override
    public void fileCreated(Path path) {
        Exception failure = null;

        try {
            List<WhitelistEntry> csvEntries = parser.parseFile(path);
            LOGGER.info("processing {} entries from file {}", csvEntries.size(), path.getFileName());
            importEntries(csvEntries);
        } catch (Exception e) {
            failure = e;
        }

        Path doneFile = moveToDoneDirectory(path);

        if(failure == null) {
            emailNotificationService.notifyFileImported(doneFile);
        } else if(failure instanceof IOException) {
            LOGGER.error("Failed to parse file " + doneFile.toString(), failure);
            emailNotificationService.notifyImportFailed(doneFile, "Parse error");
        } else {
            LOGGER.error("Error importing file " + doneFile.toString(), failure);
            emailNotificationService.notifyImportFailed(doneFile, "Import error");
        }
    }

    @Transactional
    public void importEntries(List<WhitelistEntry> entries) {
        importDao.markAllAsObsolete();

        entries.forEach(this::createOrUpdateEntry);

        importDao.scanObsolete((entry) -> {
            cardBindingService.cancelTransponderCard(entry);
            systemUserDao.deleteAppUserByCardId(entry.getTransponderCardId());
        });

        importDao.deleteAllObsolete();
    }

    private void createOrUpdateEntry(WhitelistEntry newEntry) {
        WhitelistEntry entry = importDao.findByTravelcardNumber(newEntry.getTravelcardNumber()).orElse(newEntry);

        TransponderCard card = cardBindingService.assignActiveTransponderCard(entry);
        entry.setTransponderCardId(card.getId());

        if(!entry.equals(newEntry)) {
            entry.setLicensePlate(newEntry.getLicensePlate());
            entry.setObsolete(false);
            importDao.updateEntry(entry);
        } else {
            importDao.createEntry(entry);
        }

        if(!systemUserDao.existsAppUserForCard(card.getId()))
            systemUserDao.createAppUser(card, username(entry), password(entry), UserAccountType.APPLOGIN);
    }

    private void checkDoneDirectory() throws IOException {
        if(!Files.exists(doneDir))
            Files.createDirectories(doneDir);
    }

    private Path moveToDoneDirectory(Path path) {
        LOGGER.info("moving processed file {} to directory {}", path, doneDir);

        try {
            Path doneFile = doneDir.resolve(doneFileName(path));
            Files.move(path, doneFile, StandardCopyOption.REPLACE_EXISTING);

            return doneFile;
        } catch (IOException e) {
            LOGGER.error(String.format("could not move %s to done directory %s", path, doneDir), e);
        }
        return path;
    }

    private String doneFileName(Path path) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

        return path.getFileName().toString() + "." + formatter.format(LocalDateTime.now());
    }

    private String username(WhitelistEntry entry) {
        return (USERNAME_PREFIX + entry.getTravelcardNumber()).toLowerCase();
    }

    private String password(WhitelistEntry entry) {
        return entry.getLicensePlate().toLowerCase().replaceAll("\\W|_", "");
    }
}
