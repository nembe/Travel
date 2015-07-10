package nl.yellowbrick.admin.service;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import nl.yellowbrick.admin.domain.SleeveOrderExportRecord;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class SleeveOrderCsvExporterTest {

    private static final List<String> TEST_FILES = Lists.newArrayList("file1", "file2");

    SleeveOrderCsvExporter exporter;
    Path exportsDir;

    @Before
    public void setUp() throws IOException {
        exportsDir = com.google.common.io.Files.createTempDir().toPath();
        exporter = new SleeveOrderCsvExporter(exportsDir.toAbsolutePath().toString());
    }

    @Test
    public void exports_to_exports_dir() throws Exception {
        exporter.exportRecords(Arrays.asList(record()));

        assertThat(Files.list(exportsDir).count(), is(1l));
    }

    @Test
    public void lists_files_from_export_dir() throws Exception {
        assertThat(exporter.listExports().count(), is(0l));

        List<Path> testFiles = createTestFiles();
        assertThat(exporter.listExports().collect(Collectors.toList()), equalTo(testFiles));
    }

    private List<Path> createTestFiles() throws Exception {
        return TEST_FILES.stream().map(this::createTestFile).collect(Collectors.toList());
    }

    private Path createTestFile(String pathStr) {
        try {
            Path path = exportsDir.resolve(pathStr);
            Files.write(path, Arrays.asList("something"), Charsets.UTF_8);
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SleeveOrderExportRecord record() {
        final CardOrder order = new CardOrder();
        order.setCardType(CardType.SLEEVE);
        order.setAmount(1);

        return new SleeveOrderExportRecord(order, new Customer(), new CustomerAddress(), null);
    }
}
