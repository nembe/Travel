package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebAppConfiguration
public class CardProvisioningListControllerTest extends BaseMvcTestCase {

    private static final String ORDER_ID = "72031";
    private static final String URL = "/provisioning/cards/";

    @Autowired @InjectMocks CardProvisioningListController controller;
    @Autowired @Spy CardOrderDao cardOrderDao;

    @Test
    public void displays_message_when_no_card_orders() throws Exception {
        // interpect call to dao and return 0 orders
        when(cardOrderDao.findByStatus(CardOrderStatus.INSERTED)).thenReturn(new ArrayList<CardOrder>());

        MvcResult res = mockMvc.perform(get(URL)).andReturn();

        Document html = parseHtml(res);

        assertThat(html.select(".section").size(), is(1));
        assertThat(html.select(".section").get(0).text(), is("No card orders pending validation."));
    }

    @Test
    public void lists_card_orders() throws Exception {
        MvcResult res = mockMvc.perform(get(URL)).andReturn();

        Document html = parseHtml(res);

        Elements tableRows = html.select(".section table tbody tr");
        Element row = tableRows.get(0);

        assertThat(tableRows.size(), is(1));
        assertThat(row.select("td:first-child").text(), is("YELLOWBRICK")); // product group
        assertThat(row.select("td:nth-child(2)").text(), is("2010-12-23 16:26:39.0")); // order date
        assertThat(row.select("td:nth-child(3)").text(), is("false")); // business
        assertThat(row.select("td:nth-child(4)").text(), is("203126")); // customer number
        assertThat(row.select("td:nth-child(5)").text(), is("Mathijn Slomp")); // customer name
        assertThat(row.select("td:nth-child(6)").text(), is("QPARK_CARD")); // card type
        assertThat(row.select("td:nth-child(7)").text(), is("false")); // export
        assertThat(row.select("td:nth-child(8)").text(), is("2")); // order amount
        assertThat(row.select("td:last-child a").attr("href"), is(URL + ORDER_ID)); // link to validation
    }
}
