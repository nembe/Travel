package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.errors.ActivationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static nl.yellowbrick.data.domain.CardOrderStatus.ACCEPTED;
import static nl.yellowbrick.data.domain.CardType.TRANSPONDER_CARD;
import static nl.yellowbrick.data.domain.CardType.VECHILE_PROFILE;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

public class CardAssignmentServiceTest {

    @InjectMocks
    CardAssignmentService cardAssignmentService;

    @Mock
    CardOrderDao cardOrderDao;

    Customer customer;

    CardOrder tCardOrderA;
    CardOrder tCardOrderB;
    CardOrder vehicleProfileOrder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        customer = new Customer();
        customer.setCustomerId(12345);
        customer.setProductGroupId(1);

        tCardOrderA = new CardOrder();
        tCardOrderA.setAmount(1);

        tCardOrderB = new CardOrder();
        tCardOrderB.setAmount(1);

        vehicleProfileOrder = new CardOrder();
        vehicleProfileOrder.setAmount(1);

        when(cardOrderDao.findForCustomer(customer, ACCEPTED, TRANSPONDER_CARD)).thenReturn(Arrays.asList(tCardOrderA, tCardOrderB));
        when(cardOrderDao.findForCustomer(customer, ACCEPTED, VECHILE_PROFILE)).thenReturn(Arrays.asList(vehicleProfileOrder));
    }

    @Test(expected = ActivationException.class)
    public void fails_if_not_enough_card_numbers_available() {
        when(cardOrderDao.nextTransponderCardNumbers(eq(customer.getProductGroupId()), eq(5), any()))
                .thenReturn(Arrays.asList("1", "2"));

        tCardOrderA.setAmount(5);

        cardAssignmentService.assignToCustomer(customer);
    }

    @Test
    public void assigns_card_numbers_in_order_and_updates_mobile_only_for_first() {
        //noinspection unchecked
        when(cardOrderDao.nextTransponderCardNumbers(eq(customer.getProductGroupId()), anyInt(), any()))
                .thenReturn(Arrays.asList("1"), Arrays.asList("2"), Arrays.asList("3"));

        cardAssignmentService.assignToCustomer(customer);

        InOrder inOrder = inOrder(cardOrderDao);

        inOrder.verify(cardOrderDao).processTransponderCard("1", customer, true);
        inOrder.verify(cardOrderDao).processTransponderCard("2", customer, false);
        inOrder.verify(cardOrderDao).processTransponderCard("3", customer, false);
    }
}
