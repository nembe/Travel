package nl.yellowbrick.admin.service;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Joiner;
import nl.yellowbrick.admin.domain.CardOrderExportRecord;
import nl.yellowbrick.data.domain.ProductGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Component
public class WelcomeLetterCsvExporter {

    private static final Logger LOG = LoggerFactory.getLogger(WelcomeLetterCsvExporter.class);

    private static final char SEPARATOR = ';';

    private Path baseExportPath;

    @Autowired
    public WelcomeLetterCsvExporter(@Value("${welcomeLetterExport.path}") String baseExportPath) {
        setBaseExportPath(baseExportPath);
    }

    public void setBaseExportPath(String baseExportPath) {
        this.baseExportPath = Paths.get(baseExportPath).toAbsolutePath();
    }

    public Appender createAppender(ProductGroup productGroup, String name) {
        try {
            return new Appender(resolveFilePath(productGroup, name));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<Path> listExports(ProductGroup productGroup) {
        try {
            return Files.list(exportsDir(productGroup)).sorted();
        } catch (IOException e) {
            LOG.error("Failed to retrieve exports lists for group " + productGroup.getDescription(), e);
            return Stream.empty();
        }
    }

    private Path resolveFilePath(ProductGroup productGroup, String name) throws IOException {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm"));
        Path dir = exportsDir(productGroup);

        String filename = Joiner
                .on("_").join(time, productGroup.getDescription(), name)
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

    public static class Appender implements AutoCloseable {

        private enum State {
            INITIAL, WRITING, CLOSED
        }

        private final Path path;
        private ObjectWriter writer;
        private CsvGenerator generator;
        private State state;

        private Appender(Path path) {
            this.path = path;
            this.state = State.INITIAL;
        }

        public void append(CardOrderExportRecord record) {
            if(state == State.CLOSED)
                throw new IllegalStateException("Appender has already been closed");

            try {
                if(state == State.INITIAL)
                    initialize();

                state = State.WRITING;
                writer.writeValue(generator, record);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isWriting() {
            return state == State.WRITING;
        }

        public void close() {
            if(!isWriting())
                return;

            try {
                generator.flush();
                generator.close();
            } catch(IOException e) {
                throw new RuntimeException(e);
            } finally {
                writer = null;
                generator = null;
            }
        }

        public Path getPath() {
            return path;
        }

        private void initialize() throws IOException {
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = mapper.schemaFor(CardOrderExportRecord.class)
                    .withHeader()
                    .withoutQuoteChar()
                    .withColumnSeparator(SEPARATOR);

            writer = mapper.writer(schema);
            generator = mapper.getFactory().createGenerator(new FileWriter(path.toFile()));
        }
    }
}
