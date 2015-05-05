package nl.yellowbrick.admin.controller;


import com.google.common.net.HttpHeaders;
import nl.yellowbrick.activation.service.CardAssignmentService;
import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebAppConfiguration
public class CardProvisioningFormControllerTest extends BaseMvcTestCase {

    private static final long ORDER_ID = 72031;
    private static final String ORDER_URL = "/provisioning/cards/" + ORDER_ID;

    @Autowired @InjectMocks CardProvisioningFormController controller;
    @Autowired @Spy CardOrderDao cardOrderDao;
    @Autowired @Spy CardAssignmentService cardAssignmentService;

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
    }

    @Test
    public void shows_form_binding_errors() throws Exception {
        MvcResult res = mockMvc.perform(post(ORDER_URL)
                        .param("pricePerCard", "not an integer") // wrong type. should have binding error
                        .param("validateCardOrder", "Submit")
        ).andReturn();

        Document html = parseHtml(res);

        assertThat(html.select("[name=pricePerCard] + .field-error").text(), not(isEmptyOrNullString()));
    }

    @Test
    public void validates_card_order_converting_charges_to_cents() throws Exception {
        doCorrectValidateRequest();

        verify(cardOrderDao).validateCardOrder(argThat(allOf(
                hasProperty("id", is(ORDER_ID)),
                hasProperty("pricePerCard", is(1.23 * 100))
        )));
    }

    @Test
    public void assigns_transponder_cards() throws Exception {
        CardOrder order = cardOrderDao.findById(ORDER_ID).get();

        when(cardOrderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));

        doCorrectValidateRequest();
        verifyZeroInteractions(cardAssignmentService);

        order.setCardType(CardType.TRANSPONDER_CARD);
        doCorrectValidateRequest();

        verify(cardAssignmentService).assignTransponderCard(order);
    }

    @Test
    public void deletes_card_orders() throws Exception {
        MvcResult result = mockMvc.perform(post(ORDER_URL).param("deleteCardOrder", "Submit")).andReturn();

        verify(cardOrderDao).delete(ORDER_ID);

        assertThat(result.getResponse().getHeader("Location"), is("/provisioning/cards"));
    }

    @Test
    public void redirects_back_to_cards_list_if_order_status_not_inserted() throws Exception {
        CardOrder order = cardOrderDao.findById(ORDER_ID).get();
        order.setStatus(CardOrderStatus.ACCEPTED);

        when(cardOrderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));

        MockHttpServletResponse response = mvcGet(ORDER_URL).getResponse();

        assertThat(response.getStatus(), is(302));
        assertThat(response.getHeader(HttpHeaders.LOCATION), is("/provisioning/cards"));
    }

    private MvcResult doCorrectValidateRequest() throws Exception {
        return mockMvc.perform(post(ORDER_URL)
                        .param("pricePerCard", "1.23")
                        .param("validateCardOrder", "Submit")
        ).andReturn();
    }
}
