package nl.yellowbrick.admin.service;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.ProductGroup;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@WebAppConfiguration
public class CardOrderExportServiceIntegrationTest extends BaseMvcTestCase {

    private static final long TRANSPONDERCARD_ORDER_ID = 72032l;

    @Autowired @InjectMocks CardOrderExportService exportService;

    @Autowired ProductGroupDao productGroupDao;
    @Autowired CardOrderCsvExporter csvExporter;
    @Autowired CardOrderDao cardOrderDao;

    Path outputDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        outputDir = Files.createTempDir().toPath().toAbsolutePath();
        csvExporter.setBaseExportPath(outputDir.toString());
    }

    @Test
    public void noop_when_lacking_orders_to_export() {
        ProductGroup abnPg = productGroupDao.findByDescription("ABN").get();

        exportService.exportForProductGroup(abnPg);

        assertThat(outputDir, emptyDir());
    }

    @Test
    public void writes_transpondercard_orders_to_their_own_file() throws Exception {
        ProductGroup yellowbrickPg = productGroupDao.findByDescription("Yellowbrick").get();

        exportService.exportForProductGroup(yellowbrickPg);

        File export = outputDir.toFile().listFiles()[0];
        CardOrder order = cardOrderDao.findByStatusAndType(CardOrderStatus.EXPORTED, CardType.TRANSPONDER_CARD).get(0);

        assertThat(export.getName(), endsWith("_YELLOWBRICK_Transponderkaart.csv"));
        assertThat(Files.toString(export, Charsets.UTF_8), equalTo(sampleTransponderCardExport()));
        assertThat(order.getId(), is(TRANSPONDERCARD_ORDER_ID));
    }

    @Test
    public void writes_qcard_orders_to_their_own_file() {
        // need to mock out order data
        fail("not implemented");
    }

    @Test
    public void writes_rtpcard_orders_to_their_own_file() {
        // need to mock out order data
        fail("not implemented");
    }

    @Test
    public void writes_externally_provisioned_cards_to_combined_file() {
        // need to mock out order data
        fail("not implemented");
    }

    private String sampleTransponderCardExport() throws Exception {
        return fileFromClasspath("transpondercard_export.csv");
    }

    private String fileFromClasspath(String filename) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
        return CharStreams.toString(new InputStreamReader(stream));
    }

    private Matcher<? super Path> emptyDir() {
        return new ArgumentMatcher<Path>() {
            @Override
            public boolean matches(Object o) {
                File dir = ((Path) o).toFile();
                return dir.isDirectory() && dir.listFiles().length == 0;
            }
        };
    }
}
