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
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@WebAppConfiguration
public class CardOrderExportServiceIntegrationTest extends BaseMvcTestCase {

    // from test dataset
    private static final long TRANSPONDERCARD_ORDER_ID = 72032l;

    @Autowired @InjectMocks CardOrderExportService exportService;

    @Autowired @Spy CardOrderDao cardOrderDao;
    @Autowired ProductGroupDao productGroupDao;
    @Autowired CardOrderCsvExporter csvExporter;

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

        File export = latestFileFromOutputDir();
        CardOrder order = cardOrderDao.findByStatusAndType(CardOrderStatus.EXPORTED, CardType.TRANSPONDER_CARD).get(0);

        assertThat(export.getName(), endsWith("_YELLOWBRICK_Transponderkaart.csv"));
        assertThat(readFile(export), equalTo(sampleTransponderCardExport()));
        assertThat(order.getId(), is(TRANSPONDERCARD_ORDER_ID));
    }

    @Test
    public void writes_qcard_orders_to_their_own_file() throws Exception {
        ProductGroup pg = internalProductGroup();
        CardOrder qcardOrder = sampleOrder(CardType.QPARK_CARD);

        // stub test data
        when(cardOrderDao.findPendingExport(pg)).thenReturn(Arrays.asList(qcardOrder));

        exportService.exportForProductGroup(pg);

        File export = latestFileFromOutputDir();

        assertThat(export.getName(), endsWith("_TESTPG_QCARD.csv"));
        assertThat(readFile(export), equalTo(sampleQCardExport()));
    }

    @Test
    public void writes_rtpcard_orders_to_their_own_file() throws Exception {
        ProductGroup pg = internalProductGroup();
        CardOrder rtpCardOrder = sampleOrder(CardType.RTP_CARD);

        // stub test data
        when(cardOrderDao.findPendingExport(pg)).thenReturn(Arrays.asList(rtpCardOrder));

        exportService.exportForProductGroup(pg);

        File export = latestFileFromOutputDir();

        assertThat(export.getName(), endsWith("_TESTPG_RTP-Kaart.csv"));
    }

    @Test
    public void writes_externally_provisioned_cards_to_combined_file() throws Exception {
        // product group with external provisioning of cards
        ProductGroup pg = externalProductGroup();

        CardOrder rtpCardOrder = sampleOrder(CardType.RTP_CARD);
        CardOrder qCardOrder = sampleOrder(CardType.QPARK_CARD);
        CardOrder tCardOrder = sampleOrder(CardType.TRANSPONDER_CARD);

        // stub test data
        when(cardOrderDao.findPendingExport(pg)).thenReturn(Arrays.asList(rtpCardOrder, tCardOrder, qCardOrder));

        exportService.exportForProductGroup(pg);

        File export = latestFileFromOutputDir();

        assertThat(export.getName(), endsWith("_TESTPG_Transponderkaart.csv"));
        assertThat(Files.readLines(export, Charsets.UTF_8), hasSize(4)); // 3 records and the columns header
    }

    private ProductGroup internalProductGroup() {
        ProductGroup productGroup = new ProductGroup();
        productGroup.setInternalCardProvisioning(true);
        productGroup.setId(1l);
        productGroup.setDescription("TESTPG");

        return productGroup;
    }

    private ProductGroup externalProductGroup() {
        ProductGroup productGroup = internalProductGroup();
        productGroup.setInternalCardProvisioning(false);

        return productGroup;
    }

    private String readFile(File file) throws Exception {
        return Files.toString(file, Charsets.UTF_8);
    }

    private File latestFileFromOutputDir() {
        return outputDir.toFile().listFiles()[0];
    }

    private CardOrder sampleOrder(CardType cardType) {
        CardOrder order = new CardOrder();
        order.setCardType(cardType);
        order.setCustomerId(4776);
        order.setBriefCode("bc");

        return order;
    }

    private String sampleTransponderCardExport() throws Exception {
        return fileFromClasspath("transpondercard_export.csv");
    }

    private String sampleQCardExport() throws Exception {
        return fileFromClasspath("qcard_export.csv");
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
