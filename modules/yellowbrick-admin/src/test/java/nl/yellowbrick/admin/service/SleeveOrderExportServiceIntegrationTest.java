package nl.yellowbrick.admin.service;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@WebAppConfiguration
public class SleeveOrderExportServiceIntegrationTest extends BaseMvcTestCase {

    // from test dataset
    private static final long SLEEVE_ORDER_ID = 367366l;

    @Autowired
    @InjectMocks
    SleeveOrderExportService exportService;

    @Autowired @Spy
    CardOrderDao cardOrderDao;
    @Autowired
    SleeveOrderCsvExporter csvExporter;

    Path outputDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        outputDir = Files.createTempDir().toPath().toAbsolutePath();
        csvExporter.setBaseExportPath(outputDir.toString());
    }

    @Test
    public void writes_one_entry_per_order_in_expected_format() throws Exception {
        exportService.export();

        File export = latestFileFromOutputDir();
        CardOrder order = cardOrderDao.findByStatusAndType(CardOrderStatus.EXPORTED, CardType.SLEEVE).get(0);

        assertThat(export.getName(), endsWith("_Sleeves.csv"));
        assertThat(readFile(export), containsString(sampleSleeveOrderExport()));
        assertThat(order.getId(), is(SLEEVE_ORDER_ID));
    }

    private String readFile(File file) throws Exception {
        return Files.toString(file, Charsets.UTF_8);
    }

    private File latestFileFromOutputDir() {
        return outputDir.toFile().listFiles()[0];
    }

    private String sampleSleeveOrderExport() throws Exception {
        return fileFromClasspath("sleeve_export.csv");
    }

    private String fileFromClasspath(String filename) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
        return CharStreams.toString(new InputStreamReader(stream));
    }
}
