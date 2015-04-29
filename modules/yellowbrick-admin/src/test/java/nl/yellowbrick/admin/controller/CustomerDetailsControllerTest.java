package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.data.dao.CardOrderDao;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Optional;

import static nl.yellowbrick.admin.matchers.HtmlMatchers.includesText;
import static nl.yellowbrick.admin.matchers.HtmlMatchers.isChecked;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@WebAppConfiguration
public class CustomerDetailsControllerTest extends BaseMvcTestCase {

    private static final String CARD_NUMBER = "108142";

    @Autowired @InjectMocks CustomerDetailsController controller;
    @Autowired @Spy CardOrderDao cardOrderDao;

    @Test
    public void shows_card_order_info() throws Exception {
        assertThatCardDataIsShown();
    }

    @Test
    public void assumes_card_without_card_order_has_plastic_counterpart() throws Exception {
        when(cardOrderDao.findById(anyLong())).thenReturn(Optional.empty());

        assertThatCardDataIsShown();
    }

    private void assertThatCardDataIsShown() throws Exception {
        Document html = parseHtml(mvcGet("/customers/4776"));

        Element firstTableRow = html.select("tbody tr").first();

        assertThat(firstTableRow, includesText(CARD_NUMBER));
        assertThat(firstTableRow.select("input[type=checkbox]").first(), isChecked(true));
    }
}
