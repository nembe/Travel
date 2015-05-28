package nl.yellowbrick.admin.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.admin.service.WelcomeLetterExportService;
import nl.yellowbrick.data.dao.ProductGroupDao;
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
import java.util.stream.Stream;

import static nl.yellowbrick.admin.matchers.HtmlMatchers.hasAttr;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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

    @Before
    public void setUp() throws Exception {
        super.setUp();
        stubExportService(Stream.of(testFile()));
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
        stubExportService(Stream.of(testFile()));

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
        stubExportService(Stream.of(testFile()));

        MvcResult res = mvcGet("/provisioning/welcome_letters/?productGroup=1&fileName=" + TEST_FILE_NAME);

        assertThat(res.getResponse().getStatus(), is(200));
        assertThat(res.getResponse().getContentAsString(), is(TEST_FILE_CONTENT));
    }

    private void stubExportService(Stream<FileSystemResource> stream) {
        when(exportService.listExports(any())).thenReturn(stream);
    }

    private void stubExportService() {
        stubExportService(Stream.empty());
    }

    private FileSystemResource testFile() throws Exception {
        Path path = Files.createTempDir().toPath().resolve(TEST_FILE_NAME);
        Files.write(TEST_FILE_CONTENT, path.toFile(), Charsets.UTF_8);

        return new FileSystemResource(path.toFile());
    }

}
