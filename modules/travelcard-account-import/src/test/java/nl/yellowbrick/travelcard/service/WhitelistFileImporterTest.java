package nl.yellowbrick.travelcard.service;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.dao.SystemUserDao;
import nl.yellowbrick.data.dao.WhitelistImportDao;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.WhitelistEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

import static nl.yellowbrick.data.domain.UserAccountType.RESTRICTED_SUBACCOUNT;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WhitelistFileImporterTest {

    private static final String TC_NUMBER = "111111111";
    private static final TransponderCard EXAMPLE_CARD = new TransponderCard();

    static {
        EXAMPLE_CARD.setId(123l);
    }

    WhitelistFileImporter fileImporter;

    WhitelistImportDao importDao;
    WhitelistCsvParser parser;
    CardBindingService cardBindingService;
    SystemUserDao systemUserDao;
    EmailNotificationService emailNotificationService;
    Path doneDir;

    Path inboundDir;
    Path testFile;

    @Before
    public void setUp() throws Exception {
        importDao = mock(WhitelistImportDao.class);
        parser = mock(WhitelistCsvParser.class);
        cardBindingService = mock(CardBindingService.class);
        systemUserDao = mock(SystemUserDao.class);
        emailNotificationService = mock(EmailNotificationService.class);

        doneDir = Files.createTempDirectory("done");
        fileImporter = newFileImporter(doneDir.toAbsolutePath().toString());

        inboundDir = Files.createTempDirectory("inbound");
        testFile = placeTestFile();

        // just set some ID when creating a card
        when(cardBindingService.assignActiveTransponderCard(any())).thenReturn(EXAMPLE_CARD);
    }

    @Test(expected = IOException.class)
    public void throws_early_exception_if_cant_create_done_dir() throws Exception {
        newFileImporter("/dev/null/something");
    }

    @Test
    public void moves_file_to_done_dir() throws Exception {
        fileImporter.fileCreated(testFile);

        Optional<Path> doneFile = Files.list(doneDir).findFirst();

        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        assertTrue(doneFile.isPresent());
        assertThat(doneFile.get().getFileName().toString(), startsWith("whitelist.csv." + date));
    }

    @Test
    public void moves_unparseable_file_to_done_dir() throws Exception {
        when(parser.parseFile(testFile)).thenThrow(IOException.class);

        fileImporter.fileCreated(testFile);

        assertTrue(Files.list(doneDir).findFirst().isPresent());
    }

    @Test
    public void updates_license_plate_for_existing_entries_removing_others() throws Exception {
        WhitelistEntry existing = new WhitelistEntry(TC_NUMBER, "AA-BB-CC", 1l);
        existing.setObsolete(true);

        WhitelistEntry newEntry = new WhitelistEntry(TC_NUMBER, "DD-EE-FF");

        when(parser.parseFile(testFile)).thenReturn(Lists.newArrayList(newEntry));
        when(importDao.findByTravelcardNumber(TC_NUMBER)).thenReturn(Optional.of(existing));

        fileImporter.fileCreated(testFile);

        WhitelistEntry expected = new WhitelistEntry(TC_NUMBER, newEntry.getLicensePlate(), existing.getTransponderCardId());
        expected.setObsolete(false);

        InOrder inOrder = inOrder(importDao, cardBindingService);
        inOrder.verify(importDao).markAllAsObsolete();
        inOrder.verify(importDao).updateEntry(expected);
        inOrder.verify(cardBindingService).updateLicensePlate(expected);
        inOrder.verify(importDao).deleteAllObsolete();
    }

    @Test
    public void creates_new_entries_removing_others() throws Exception {
        WhitelistEntry newEntry = new WhitelistEntry(TC_NUMBER, "AA-BB-CC ");

        when(parser.parseFile(testFile)).thenReturn(Lists.newArrayList(newEntry));
        when(importDao.findByTravelcardNumber(TC_NUMBER)).thenReturn(Optional.empty());

        fileImporter.fileCreated(testFile);

        WhitelistEntry expectedEntry = new WhitelistEntry(TC_NUMBER, newEntry.getLicensePlate(), EXAMPLE_CARD.getId());
        expectedEntry.setObsolete(false);

        String expectedUsername = "tc" + TC_NUMBER;
        String expectedPassword = "aabbcc";

        InOrder inOrder = inOrder(importDao, cardBindingService, systemUserDao);
        inOrder.verify(importDao).markAllAsObsolete();
        inOrder.verify(cardBindingService).assignActiveTransponderCard(expectedEntry);
        inOrder.verify(importDao).createEntry(expectedEntry);
        inOrder.verify(systemUserDao).createAppUser(EXAMPLE_CARD, expectedUsername, expectedPassword, RESTRICTED_SUBACCOUNT);
        inOrder.verify(importDao).deleteAllObsolete();
    }

    @Test
    public void cancels_cards_and_removes_users_of_obsolete_accounts() throws Exception {
        WhitelistEntry entry = new WhitelistEntry(TC_NUMBER, "AA-BB-CC");
        entry.setObsolete(true);
        entry.setTransponderCardId(EXAMPLE_CARD.getId());

        when(parser.parseFile(testFile)).thenReturn(Lists.newArrayList());
        doAnswer(invocationOnMock -> {
            ((Consumer<WhitelistEntry>) invocationOnMock.getArguments()[0]).accept(entry);
            return null;
        }).when(importDao).scanObsolete(any());

        fileImporter.fileCreated(testFile);

        verify(cardBindingService).cancelTransponderCard(entry);
        verify(systemUserDao).deleteAppUserByCardId(EXAMPLE_CARD.getId());
    }

    @Test
    public void notifies_of_successes() throws Exception {
        fileImporter.fileCreated(testFile);

        Path doneFile = Files.list(doneDir).findFirst().get();

        verify(emailNotificationService).notifyFileImported(doneFile);
    }

    @Test
    public void notifies_of_failures() throws Exception {
        when(parser.parseFile(any())).thenThrow(Exception.class);

        fileImporter.fileCreated(testFile);

        Path doneFile = Files.list(doneDir).findFirst().get();

        verify(emailNotificationService).notifyImportFailed(eq(doneFile), anyString());
    }

    private Path placeTestFile() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("whitelist.csv").toURI());
        File destination = new File(inboundDir.toFile(), file.getName());

        return Files.copy(file.toPath(), destination.toPath());
    }

    private WhitelistFileImporter newFileImporter(String doneDir) throws Exception {
        return new WhitelistFileImporter(importDao, parser, systemUserDao, cardBindingService,
                emailNotificationService, doneDir);
    }
}
