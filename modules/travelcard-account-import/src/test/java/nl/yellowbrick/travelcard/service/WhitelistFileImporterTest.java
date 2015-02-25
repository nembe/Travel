package nl.yellowbrick.travelcard.service;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.dao.WhitelistImportDao;
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

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class WhitelistFileImporterTest {

    WhitelistFileImporter fileImporter;

    WhitelistImportDao importDao;
    WhitelistCsvParser parser;
    Path doneDir;

    Path inboundDir;
    Path testFile;

    @Before
    public void setUp() throws Exception {
        importDao = mock(WhitelistImportDao.class);
        parser = mock(WhitelistCsvParser.class);
        doneDir = Files.createTempDirectory("done");
        fileImporter = new WhitelistFileImporter(importDao, parser, doneDir.toAbsolutePath().toString());

        inboundDir = Files.createTempDirectory("inbound");
        testFile = placeTestFile();
    }

    @Test(expected = IOException.class)
    public void throws_early_exception_if_cant_create_done_dir() throws Exception {
        new WhitelistFileImporter(importDao, parser, "/dev/null/something");
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
        WhitelistEntry entry = new WhitelistEntry("tc111111111", "DD-EE-FF");
        entry.setObsolete(false);

        WhitelistEntry existing = new WhitelistEntry("tc111111111", "AA-BB-CC");
        existing.setObsolete(true);

        when(parser.parseFile(testFile)).thenReturn(Lists.newArrayList(entry));
        when(importDao.findByTravelcardNumber(entry.getTravelcardNumber())).thenReturn(Optional.of(existing));

        fileImporter.fileCreated(testFile);

        InOrder inOrder = inOrder(importDao);
        inOrder.verify(importDao).markAllAsObsolete();
        inOrder.verify(importDao).updateEntry(entry);
        inOrder.verify(importDao).deleteAllObsolete();
    }

    @Test
    public void creates_new_entries_removing_others() throws Exception {
        WhitelistEntry entry = new WhitelistEntry("tc111111111", "AA-BB-CC");

        when(parser.parseFile(testFile)).thenReturn(Lists.newArrayList(entry));
        when(importDao.findByTravelcardNumber(entry.getTravelcardNumber())).thenReturn(Optional.empty());

        fileImporter.fileCreated(testFile);

        InOrder inOrder = inOrder(importDao);
        inOrder.verify(importDao).markAllAsObsolete();
        inOrder.verify(importDao).createEntry(entry);
        inOrder.verify(importDao).deleteAllObsolete();
    }

    private Path placeTestFile() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("whitelist.csv").toURI());
        File destination = new File(inboundDir.toFile(), file.getName());

        return Files.copy(file.toPath(), destination.toPath());
    }
}
