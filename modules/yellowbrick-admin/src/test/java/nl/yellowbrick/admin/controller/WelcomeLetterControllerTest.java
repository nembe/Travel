package nl.yellowbrick.admin.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.admin.service.WelcomeLetterExportService;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static nl.yellowbrick.admin.matchers.HtmlMatchers.hasAttr;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebAppConfiguration
public class WelcomeLetterControllerTest extends BaseMvcTestCase {
    
    private static final String TEST_FILE_NAME = "testfile.csv";
    private static final String TEST_FILE_CONTENT = "something";

    @Autowired
    @InjectMocks
    WelcomeLetterController controller;

    @Autowired
    ProductGroupDao productGroupDao;
    @Autowired @Mock
    WelcomeLetterExportService exportService;

    ProductGroup productGroup;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        stubExportService(Stream.of(testResource()));

        productGroup = randomProductGroup();
    }

    @Test
    public void shows_message_when_lacking_exports() throws Exception {
        stubExportService();

        MvcResult res = mvcGet("/provisioning/welcome_letters?productGroup=1");
        Document html = parseHtml(res);

        assertThat(res.getResponse().getStatus(), is(200));
        assertThat(html.select("table"), empty());
        assertThat(html.select(".section h3").text(), containsString("No exports available."));
    }

    @Test
    public void lists_exports_per_product_group_displaying_download_link() throws Exception {
        stubExportService(Stream.of(testResource()));

        MvcResult res = mvcGet("/provisioning/welcome_letters?productGroup=1");
        Document html = parseHtml(res);

        Elements tableRows = html.select("table tbody tr");

        assertThat(res.getResponse().getStatus(), is(200));
        assertThat(tableRows, hasSize(1));
        assertThat(tableRows.select("td").first().text(), is(TEST_FILE_NAME));
        assertThat(
                tableRows.select("a").first(),
                hasAttr("href", "/provisioning/welcome_letters/?productGroup=1&fileName=" + TEST_FILE_NAME));
    }

    @Test
    public void downloads_export_file() throws Exception {
        stubExportService(Stream.of(testResource()));

        MvcResult res = mvcGet("/provisioning/welcome_letters/?productGroup=1&fileName=" + TEST_FILE_NAME);

        assertThat(res.getResponse().getStatus(), is(200));
        assertThat(res.getResponse().getContentAsString(), is(TEST_FILE_CONTENT));
    }

    @Test
    public void triggers_next_batch_export() throws Exception {
        when(exportService.exportForProductGroup(productGroup, 123l)).thenReturn(Optional.of(testPath()));

        mockMvc.perform(post("/provisioning/welcome_letters")
                        .param("productGroup", productGroup.getId().toString())
                        .param("customer", "123")
                        .param("action", "exportBatch"));

        verify(exportService).exportForProductGroup(productGroup, 123l);
    }

    @Test
    public void validates_form_binding_errors_on_next_batch_form() throws Exception {
        MvcResult result = mockMvc.perform(post("/provisioning/welcome_letters")
                        .param("productGroup", productGroup.getId().toString())
                        .param("customer", "totally not a number")
                        .param("action", "exportBatch")
        ).andReturn();

        Document html = parseHtml(result);

        assertTrue(html.getElementById("customer").hasClass("field-error"));
        verify(exportService, never()).exportForProductGroup(any(), anyLong());
    }

    private void stubExportService(Stream<FileSystemResource> stream) {
        when(exportService.listExports(any())).thenReturn(stream);
    }

    private void stubExportService() {
        stubExportService(Stream.empty());
    }

    private Path testPath() throws Exception {
        Path path = Files.createTempDir().toPath().resolve(TEST_FILE_NAME);
        Files.write(TEST_FILE_CONTENT, path.toFile(), Charsets.UTF_8);

        return path;
    }

    private FileSystemResource testResource() throws Exception {
        return new FileSystemResource(testPath().toFile());
    }

    private ProductGroup randomProductGroup() {
        List<ProductGroup> productGroups = productGroupDao.all();

        return productGroups.get(new Random().nextInt(productGroups.size()));
    }
}
