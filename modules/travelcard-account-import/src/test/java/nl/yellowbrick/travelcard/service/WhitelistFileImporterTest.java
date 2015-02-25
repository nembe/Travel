package nl.yellowbrick.travelcard.service;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.dao.WhitelistImportDao;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.WhitelistEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class WhitelistFileImporterTest {

    private static final String TC_NUMBER = "tc111111111";
    private static final long CARD_ID = 123456l;

    WhitelistFileImporter fileImporter;

    WhitelistImportDao importDao;
    WhitelistCsvParser parser;
    TransponderCardDao transponderCardDao;
    Path doneDir;
    Long mainAccountId = 1l;

    Path inboundDir;
    Path testFile;

    @Before
    public void setUp() throws Exception {
        importDao = mock(WhitelistImportDao.class);
        parser = mock(WhitelistCsvParser.class);
        transponderCardDao = mock(TransponderCardDao.class);
        doneDir = Files.createTempDirectory("done");

        String doneDirStr = doneDir.toAbsolutePath().toString();
        fileImporter = new WhitelistFileImporter(importDao, parser, transponderCardDao, doneDirStr, mainAccountId);

        inboundDir = Files.createTempDirectory("inbound");
        testFile = placeTestFile();

        // just set some ID when creating a card
        doAnswer(invocationOnMock -> {
            TransponderCard card = (TransponderCard) invocationOnMock.getArguments()[0];
            card.setId(CARD_ID);
            return card;
        }).when(transponderCardDao).createCard(Matchers.any());
    }

    @Test(expected = IOException.class)
    public void throws_early_exception_if_cant_create_done_dir() throws Exception {
        new WhitelistFileImporter(importDao, parser, transponderCardDao, "/dev/null/something", mainAccountId);
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

        InOrder inOrder = inOrder(importDao, transponderCardDao);
        inOrder.verify(importDao).markAllAsObsolete();
        inOrder.verify(importDao).updateEntry(expected);
        inOrder.verify(transponderCardDao).updateLicensePlate(expected.getTransponderCardId(), expected.getLicensePlate());
        inOrder.verify(importDao).deleteAllObsolete();
    }

    @Test
    public void creates_new_entries_removing_others() throws Exception {
        WhitelistEntry newEntry = new WhitelistEntry(TC_NUMBER, "AA-BB-CC");

        when(parser.parseFile(testFile)).thenReturn(Lists.newArrayList(newEntry));
        when(importDao.findByTravelcardNumber(TC_NUMBER)).thenReturn(Optional.empty());

        fileImporter.fileCreated(testFile);

        WhitelistEntry expectedEntry = new WhitelistEntry(TC_NUMBER, newEntry.getLicensePlate(), CARD_ID);
        expectedEntry.setObsolete(false);

        TransponderCard expectedCard = new TransponderCard();
        expectedCard.setId(CARD_ID);
        expectedCard.setCustomerId(mainAccountId);
        expectedCard.setCardNumber(TC_NUMBER);
        expectedCard.setLicenseplate(newEntry.getLicensePlate());
        expectedCard.setCountry("NL");
        expectedCard.setStatus(CardStatus.ACTIVE);

        InOrder inOrder = inOrder(importDao, transponderCardDao);
        inOrder.verify(importDao).markAllAsObsolete();
        inOrder.verify(transponderCardDao).createCard(expectedCard);
        inOrder.verify(importDao).createEntry(expectedEntry);
        inOrder.verify(importDao).deleteAllObsolete();
    }

    private Path placeTestFile() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("whitelist.csv").toURI());
        File destination = new File(inboundDir.toFile(), file.getName());

        return Files.copy(file.toPath(), destination.toPath());
    }

    private TransponderCard testCard() {
        TransponderCard card = new TransponderCard();
        card.setId(123456l);

        return card;
    }
}
