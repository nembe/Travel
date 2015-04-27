package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.BaseMvcTestCase;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.springframework.test.context.web.WebAppConfiguration;

import static nl.yellowbrick.admin.matchers.HtmlMatchers.includesText;
import static nl.yellowbrick.admin.matchers.HtmlMatchers.isChecked;
import static org.junit.Assert.assertThat;

@WebAppConfiguration
public class CustomerDetailsControllerTest extends BaseMvcTestCase {

    private static final String CARD_NUMBER = "108142";

    @Test
    public void shows_card_order_info() throws Exception {
        Document html = parseHtml(mvcGet("/customers/4776"));

        Element firstTableRow = html.select("tbody tr").first();

        assertThat(firstTableRow, includesText(CARD_NUMBER));
        assertThat(firstTableRow.select("input[type=checkbox]").first(), isChecked(true));
    }
}
