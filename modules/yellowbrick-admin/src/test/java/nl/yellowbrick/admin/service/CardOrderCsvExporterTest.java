package nl.yellowbrick.admin.service;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import nl.yellowbrick.data.domain.ProductGroup;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CardOrderCsvExporterTest {

    private static final List<String> TEST_FILES = Lists.newArrayList("123/file1", "123/file2");

    CardOrderCsvExporter exporter;
    Path baseDir;

    @Before
    public void setUp() {
        baseDir = Files.createTempDir().toPath();
        exporter = new CardOrderCsvExporter(baseDir.toAbsolutePath().toString());
    }

    @Test
    public void lists_exports_per_product_group() throws Exception {
        List<Path> testFiles = createTestFiles();

        assertThat(exporter.listExports(productGroup(123l)), equalTo(testFiles));
        assertThat(exporter.listExports(productGroup(456l)), empty());
    }

    private ProductGroup productGroup(long id) {
        ProductGroup productGroup = new ProductGroup();
        productGroup.setId(id);

        return productGroup;
    }

    private List<Path> createTestFiles() throws Exception {
        return TEST_FILES.stream().map(this::createTestFile).collect(Collectors.toList());
    }

    private Path createTestFile(String pathStr) {
        try {
            Path path = baseDir.resolve(pathStr);
            Files.createParentDirs(path.toFile());
            Files.write("something", path.toFile(), Charsets.UTF_8);
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
