package nl.yellowbrick.admin.service;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import nl.yellowbrick.admin.domain.CardOrderExportRecord;
import nl.yellowbrick.admin.domain.CardOrderExportTarget;
import nl.yellowbrick.data.domain.ProductGroup;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CardOrderCsvExporter {

    private static final Logger LOG = LoggerFactory.getLogger(CardOrderCsvExporter.class);
    private static final char SEPARATOR = ';';
    private static final Map<CardOrderExportTarget, String> SUFFIXES = new HashMap<>();

    static {
        SUFFIXES.put(CardOrderExportTarget.TRANSPONDER_CARDS_FILE, "Transponderkaart");
        SUFFIXES.put(CardOrderExportTarget.QPARK_CARDS_FILE, "QCARD");
        SUFFIXES.put(CardOrderExportTarget.RTP_CARDS_FILE, "RTP-Kaart");
    }

    private Path baseExportPath;

    @Autowired
    public CardOrderCsvExporter(@Value("${orderexport.path}") String baseExportPath) {
        setBaseExportPath(baseExportPath);
    }

    public void setBaseExportPath(String baseExportPath) {
        this.baseExportPath = Paths.get(baseExportPath).toAbsolutePath();
    }

    public void exportRecords(CardOrderExportTarget target, ProductGroup productGroup, List<CardOrderExportRecord> exports) {
        if(target.equals(CardOrderExportTarget.OTHER)) {
            LOG.warn("Export requested for unsupported target {}", target.name());
            return;
        } else if(exports.isEmpty()) {
            LOG.warn("Export requested with empty exports list");
            return;
        }

        try {
            Path path = resolveFilePath(target, productGroup);
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.schemaFor(CardOrderExportRecord.class)
                    .withHeader()
                    .withoutQuoteChar()
                    .withLineSeparator(System.lineSeparator())
                    .withColumnSeparator(SEPARATOR);

            LOG.info("writing card order export for product group {} with {} items to file {}",
                    productGroup.getDescription(), exports.size(), path.toString());

            csvMapper.writer(csvSchema).writeValue(path.toFile(), exports);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Path> listExports(ProductGroup productGroup) {
        try {
            return Files.list(exportsDir(productGroup))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Failed to retrieve exports lists for group " + productGroup.getDescription(), e);
            return Lists.newArrayList();
        }
    }

    private Path resolveFilePath(CardOrderExportTarget target, ProductGroup productGroup) throws IOException {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm"));
        Path dir = exportsDir(productGroup);

        String filename = Joiner
                .on("_").join(time, productGroup.getDescription(), SUFFIXES.get(target))
                .concat(".csv")
                .replaceAll("\\s", "");

        return dir.resolve(filename);
    }

    private Path exportsDir(ProductGroup productGroup) throws IOException {
        Path path = baseExportPath.resolve(productGroup.getId().toString());

        return path.toFile().exists()
                ? path
                : Files.createDirectory(path);
    }
}
