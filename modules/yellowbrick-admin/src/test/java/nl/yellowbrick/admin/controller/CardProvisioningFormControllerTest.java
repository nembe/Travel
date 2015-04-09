package nl.yellowbrick.admin.controller;


import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.data.dao.CardOrderDao;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebAppConfiguration
public class CardProvisioningFormControllerTest extends BaseMvcTestCase {

    private static final String ORDER_ID = "72031";
    private static final String ORDER_URL = "/provisioning/cards/" + ORDER_ID;

    @Autowired @InjectMocks CardProvisioningFormController controller;
    @Autowired @Spy CardOrderDao cardOrderDao;

    @Test
    public void loads_card_order_data() throws Exception {
        MvcResult res = mvcGet(ORDER_URL);

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
        MvcResult res = mockMvc.perform(post(ORDER_URL)
                        .param("pricePerCard", "5.0") // correct entry
                        .param("surcharge", "not an integer") // wrong type. should have binding error
                        .param("validateCardOrder", "Submit")
        ).andReturn();

        Document html = parseHtml(res);

        assertThat(html.select("[name=surcharge] + .field-error").text(), not(isEmptyOrNullString()));
    }

    @Test
    public void validates_card_order_converting_charges_to_cents() throws Exception {
        mockMvc.perform(post(ORDER_URL)
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

    @Test
    public void deletes_card_orders() throws Exception {
        MvcResult result = mockMvc.perform(post(ORDER_URL).param("deleteCardOrder", "Submit")).andReturn();

        verify(cardOrderDao).delete(Long.parseLong(ORDER_ID));

        assertThat(result.getResponse().getHeader("Location"), is("/provisioning/cards"));
    }
}
