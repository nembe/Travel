package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

public class CardAssignmentServiceTest {

    @InjectMocks
    CardAssignmentService cardAssignmentService;

    @Mock CardOrderDao cardOrderDao;
    @Mock CustomerDao customerDao;

    Customer customer;
    CardOrder tCardOrder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        customer = new Customer();
        customer.setCustomerId(12345);
        customer.setProductGroupId(1);
        customer.setFirstCardMobile("123456789");

        when(customerDao.findById(customer.getCustomerId())).thenReturn(Optional.of(customer));

        tCardOrder = new CardOrder();
        tCardOrder.setCardType(CardType.TRANSPONDER_CARD);
        tCardOrder.setCustomerId(customer.getCustomerId());

        when(cardOrderDao.nextTransponderCardNumbers(eq(customer.getProductGroupId()), anyInt(), any()))
                .thenReturn(Arrays.asList("1", "2"));
    }

    @Test(expected = ExhaustedCardPoolException.class)
    public void raises_exception_if_not_enough_card_numbers_available() {
        tCardOrder.setAmount(5);

        cardAssignmentService.assignTransponderCard(tCardOrder);
    }

    @Test
    public void assigns_card_numbers_in_order_and_updates_mobile_only_for_first() {
        tCardOrder.setAmount(2);
        cardAssignmentService.assignTransponderCard(tCardOrder);

        InOrder inOrder = inOrder(cardOrderDao);
        inOrder.verify(cardOrderDao).processTransponderCard("1", customer, tCardOrder, true);
        inOrder.verify(cardOrderDao).processTransponderCard("2", customer, tCardOrder, false);
    }
}
