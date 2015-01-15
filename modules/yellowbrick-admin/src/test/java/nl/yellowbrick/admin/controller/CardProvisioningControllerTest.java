package nl.yellowbrick.admin.controller;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebAppConfiguration
public class CardProvisioningControllerTest extends BaseSpringTestCase {

    private static final String BASE = "/provisioning/cards/";

    @Autowired WebApplicationContext wac;
    @Autowired @InjectMocks CardProvisioningController controller;

    @Autowired @Spy CardOrderDao cardOrderDao;

    MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void displays_message_when_no_card_orders() throws Exception {
        // interpect call to dao and return 0 orders
        when(cardOrderDao.findByStatus(CardOrderStatus.INSERTED)).thenReturn(new ArrayList<CardOrder>());

        MvcResult res = mockMvc.perform(get(BASE)).andReturn();

        Document html = Jsoup.parse(res.getResponse().getContentAsString());

        assertThat(html.select(".section").size(), is(1));
        assertThat(html.select(".section").get(0).text(), is("No card orders pending validation."));
    }

    @Test
    public void lists_card_orders() throws Exception {
        MvcResult res = mockMvc.perform(get(BASE)).andReturn();

        Document html = Jsoup.parse(res.getResponse().getContentAsString());

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
        assertThat(row.select("td:nth-child(8)").text(), is("1")); // order amount
        assertThat(row.select("td:last-child a").attr("href"), is("/provisioning/cards/72031")); // link to validation
    }
}
