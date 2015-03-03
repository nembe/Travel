package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.domain.WhitelistEntry;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WhitelistCsvParserTest {

    WhitelistCsvParser parser = new WhitelistCsvParser(',');

    @Test
    public void readsCsvEntriesFromFile() throws Exception {
        List<WhitelistEntry> entries = parser.parseFile(testFile("whitelist.csv"));

        assertEquals(new WhitelistEntry("111111111", "AA-BB-CC"), entries.get(0));
        assertEquals(new WhitelistEntry("222222222", "DD-EE-FF"), entries.get(1));
    }

    @Test
    public void ignoresEmptyLines() throws Exception {
        List<WhitelistEntry> entries = parser.parseFile(testFile("whitelist_with_empty_line.csv"));

        assertThat(entries, hasSize(2));
    }

    private Path testFile(String fileName) throws Exception {
        URL csv = getClass().getClassLoader().getResource(fileName);
        return Paths.get(csv.toURI());
    }
}
