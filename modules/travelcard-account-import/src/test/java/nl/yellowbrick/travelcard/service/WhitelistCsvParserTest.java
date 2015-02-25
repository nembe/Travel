package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.domain.WhitelistEntry;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WhitelistCsvParserTest {

    WhitelistCsvParser parser = new WhitelistCsvParser(',');

    @Test
    public void readsCsvEntriesFromFile() throws Exception {
        List<WhitelistEntry> entries = parser.parseFile(testFile());

        assertEquals(new WhitelistEntry("tc111111111", "AA-BB-CC"), entries.get(0));
        assertEquals(new WhitelistEntry("tc222222222", "DD-EE-FF"), entries.get(1));
    }

    private Path testFile() throws Exception {
        URL csv = getClass().getClassLoader().getResource("whitelist.csv");
        return Paths.get(csv.toURI());
    }
}
