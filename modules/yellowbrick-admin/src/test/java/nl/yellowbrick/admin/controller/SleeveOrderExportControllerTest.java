package nl.yellowbrick.admin.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.admin.service.SleeveOrderCsvExporter;
import nl.yellowbrick.admin.service.SleeveOrderExportScheduler;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static nl.yellowbrick.admin.matchers.HtmlMatchers.hasAttr;
import static nl.yellowbrick.admin.matchers.HtmlMatchers.includesText;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@WebAppConfiguration
public class SleeveOrderExportControllerTest extends BaseMvcTestCase {

    private static final String TEST_FILE_NAME = "testfile.csv";
    private static final String TEST_FILE_CONTENT = "something";

    @Autowired
    @InjectMocks
    SleeveOrderExportController controller;

    @Autowired @Mock
    SleeveOrderCsvExporter csvExporter;
    @Autowired @Spy
    SleeveOrderExportScheduler exportScheduler;

    @Test
    public void shows_message_when_lacking_exports() throws Exception {
        stubCsvExporter();

        MvcResult res = mvcGet("/provisioning/sleeve_exports");
        Document html = parseHtml(res);

        assertThat(res.getResponse().getStatus(), is(200));
        assertThat(html.select("table"), empty());
        assertThat(html.select(".section h3").text(), is("No exports available."));
    }

    @Test
    public void lists_exports_displaying_download_link() throws Exception {
        stubCsvExporter(testFile());

        MvcResult res = mvcGet("/provisioning/sleeve_exports");
        Document html = parseHtml(res);

        Elements tableRows = html.select("table tbody tr");

        assertThat(res.getResponse().getStatus(), is(200));
        assertThat(tableRows, hasSize(1));
        assertThat(tableRows.select("td").first().text(), is(TEST_FILE_NAME));
        assertThat(
                tableRows.select("a").first(),
                hasAttr("href", "/provisioning/sleeve_exports/?fileName=" + TEST_FILE_NAME));
    }

    @Test
    public void downloads_export_file() throws Exception {
        stubCsvExporter(testFile());

        MvcResult res = mvcGet("/provisioning/sleeve_exports/?fileName=" + TEST_FILE_NAME);

        assertThat(res.getResponse().getStatus(), is(200));
        assertThat(res.getResponse().getContentAsString(), is(TEST_FILE_CONTENT));
    }

    @Test
    public void shows_next_export_tile() throws Exception {
        stubCsvExporter();
        stubScheduler(LocalDateTime.of(2015, 4, 10, 13, 30, 0));

        Document html = parseHtml(mvcGet("/provisioning/sleeve_exports"));

        assertThat(html.select("#export-schedule span").first(), includesText("2015-04-10 13:30:00"));
    }

    private void stubScheduler(LocalDateTime nextScheduledExport) {
        when(exportScheduler.nextScheduledExport()).thenReturn(nextScheduledExport);
    }

    private void stubCsvExporter(Path file) throws Exception {
        when(csvExporter.listExports()).thenReturn(Stream.of(file));
    }

    private void stubCsvExporter() throws Exception {
        when(csvExporter.listExports()).thenReturn(Stream.empty());
    }

    private Path testFile() throws Exception {
        Path path = Files.createTempDir().toPath().resolve(TEST_FILE_NAME);
        Files.write(TEST_FILE_CONTENT, path.toFile(), Charsets.UTF_8);

        return path;
    }
}
