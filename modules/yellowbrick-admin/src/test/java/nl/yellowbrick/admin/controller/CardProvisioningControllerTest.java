package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebAppConfiguration
public class CardProvisioningControllerTest extends BaseMvcTestCase {

    private static final String BASE = "/provisioning/cards/";
    private static final String ORDER_ID = "72031";


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

        Document html = parseHtml(res);

        assertThat(html.select(".section").size(), is(1));
        assertThat(html.select(".section").get(0).text(), is("No card orders pending validation."));
    }

    @Test
    public void lists_card_orders() throws Exception {
        MvcResult res = mockMvc.perform(get(BASE)).andReturn();

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
        assertThat(row.select("td:last-child a").attr("href"), is(BASE + ORDER_ID)); // link to validation
    }

    @Test
    public void loads_card_order_data() throws Exception {
        MvcResult res = mockMvc.perform(get(BASE + ORDER_ID)).andReturn();

        Document html = parseHtml(res);
        Elements fields = html.select(".field");

        assertThat(fields.select("[name=cardType]").text(), is("QPARK_CARD"));
        assertThat(fields.select("[name=orderDate]").text(), is("2010-12-23 16:26:39.0"));
        assertThat(fields.select("[name=businessCustomer]").text(), is("false"));
        assertThat(fields.select("[name=customerName]").text(), is("Mathijn Slomp"));
        assertThat(fields.select("[name=export][checked]").val(), is("false"));
        assertThat(fields.select("[name=amount] option[selected]").val(), is("2"));
        assertThat(fields.select("[name=pricepercard]").val(), is("6.0"));
        assertThat(fields.select("[name=surcharge]").val(), is("3.0"));
    }

    @Test
    public void shows_form_binding_errors() throws Exception {
        MvcResult res = mockMvc.perform(post(BASE + ORDER_ID)
                        .param("pricePerCard", "5.0") // correct entry
                        .param("surcharge", "not an integer") // wrong type. should have binding error
                        .param("validateCardOrder", "Submit")
        ).andReturn();

        Document html = parseHtml(res);

        assertThat(html.select("[name=surcharge] + .field-error").text(), not(isEmptyOrNullString()));
    }

    @Test
    public void validates_card_order_converting_charges_to_cents() throws Exception {
        mockMvc.perform(post(BASE + ORDER_ID)
                        .param("pricePerCard", "1.23")
                        .param("surcharge", "4.56")
                        .param("validateCardOrder", "Submit")
        ).andReturn();

        verify(cardOrderDao).validateCardOrder(argThat(allOf(
                hasProperty("id", is(Long.parseLong(ORDER_ID))),
                hasProperty("pricePerCard", is(1.23 * 100)),
                hasProperty("surcharge", is(4.56 * 100))
        )));
    }
}
