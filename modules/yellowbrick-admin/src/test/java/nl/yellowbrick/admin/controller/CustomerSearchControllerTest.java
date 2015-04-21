package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.BaseMvcTestCase;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.springframework.test.context.web.WebAppConfiguration;

import static nl.yellowbrick.admin.matchers.HtmlMatchers.includesText;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@WebAppConfiguration
public class CustomerSearchControllerTest extends BaseMvcTestCase {

    @Test
    public void searches_by_customer_number() throws Exception {
        Document html = parseHtml(mvcGet("/customers?customerNumber=203126"));

        assertThat(html.select("tbody tr"), hasSize(1));
        assertThat(html.select("tbody tr").get(0), includesText("Mathijn Slomp"));
    }

    @Test
    public void searches_by_email() throws Exception {
        Document html = parseHtml(mvcGet("/customers?email=bestaatniet@taxameter.nl"));

        assertThat(html.select("tbody tr"), hasSize(3));
    }

    @Test
    public void searches_by_phone_number() throws Exception  {
        Document html = parseHtml(mvcGet("/customers?phoneNumber=+31641017015"));

        assertThat(html.select("tbody tr"), hasSize(1));
    }

    @Test
    public void searches_by_transponder_card_number() throws Exception {
        Document html = parseHtml(mvcGet("/customers?transponderCardNumber=108142"));

        assertThat(html.select("tbody tr"), hasSize(1));
        assertThat(html.select("tbody tr").get(0), includesText("Mathijn Slomp"));
    }

    @Test
    public void joins_results_of_multiple_filters() throws Exception  {
        Document html = parseHtml(mvcGet("/customers?email=bestaatniet@taxameter.nl&customerNumber=203126"));

        assertThat(html.select("tbody tr"), hasSize(3));
    }
}
