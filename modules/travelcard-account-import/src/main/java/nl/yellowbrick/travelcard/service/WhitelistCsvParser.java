package nl.yellowbrick.travelcard.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import nl.yellowbrick.data.domain.WhitelistEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class WhitelistCsvParser {

    private final char delimiter;

    @Autowired
    public WhitelistCsvParser(@Value("${tc.import.csvDelimiter}") char delimiter) {
        this.delimiter = delimiter;
    }

    public List<WhitelistEntry> parseFile(Path path) throws IOException {
        CsvMapper mapper = new CsvMapper();
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);

        MappingIterator<String[]> rowIterator = mapper.reader(String[].class).readValues(path.toFile());
        Iterable<String[]> iterable = () -> rowIterator;

        return StreamSupport
                .stream(iterable.spliterator(), false)
                .filter(row -> row.length > 1)
                .map(row -> new WhitelistEntry(row[0], row[1]))
                .collect(Collectors.toList());
    }

}
