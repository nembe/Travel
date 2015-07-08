package nl.yellowbrick.admin.service;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Joiner;
import nl.yellowbrick.admin.domain.CardOrderExportRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Component
public class SleeveOrderCsvExporter {

    private static final Logger LOG = LoggerFactory.getLogger(SleeveOrderCsvExporter.class);
    private static final char SEPARATOR = ';';
    private static final String SUFFIX = "Sleeves";

    private Path baseExportPath;

    @Autowired
    public SleeveOrderCsvExporter(@Value("${sleeveexport.path}") String sleeveExportPath) throws IOException {
        setBaseExportPath(sleeveExportPath);
    }

    public void setBaseExportPath(String baseExportPath) throws IOException {
        this.baseExportPath = Paths.get(baseExportPath).toAbsolutePath();

        if(!this.baseExportPath.toFile().exists())
            Files.createDirectory(this.baseExportPath);
    }

    public void exportRecords(List<CardOrderExportRecord> exports) {
        if(exports.isEmpty()) {
            LOG.warn("Export requested with empty exports list");
            return;
        }

        try {
            Path path = resolveFilePath();
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.schemaFor(CardOrderExportRecord.class)
                    .withHeader()
                    .withoutQuoteChar()
                    .withColumnSeparator(SEPARATOR);

            LOG.info("writing sleeve order export with {} items to file {}", exports.size(), path.toString());

            csvMapper.writer(csvSchema).writeValue(path.toFile(), exports);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<Path> listExports() {
        try {
            return Files.list(baseExportPath).sorted();
        } catch (IOException e) {
            LOG.error("Failed to retrieve sleeve exports lists", e);
            return Stream.empty();
        }
    }

    private Path resolveFilePath() throws IOException {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm"));
        String filename = Joiner.on("_").join(time, SUFFIX).concat(".csv").replaceAll("\\s", "");

        return baseExportPath.resolve(filename);
    }
}
