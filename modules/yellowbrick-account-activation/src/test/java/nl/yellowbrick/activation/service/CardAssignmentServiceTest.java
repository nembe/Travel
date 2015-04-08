package nl.yellowbrick.activation.service;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.errors.ActivationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static nl.yellowbrick.data.domain.CardOrderStatus.ACCEPTED;
import static nl.yellowbrick.data.domain.CardType.TRANSPONDER_CARD;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;

public class CardAssignmentServiceTest {

    @InjectMocks
    CardAssignmentService cardAssignmentService;

    @Mock
    CardOrderDao cardOrderDao;

    Customer customer;

    CardOrder tCardOrderA;
    CardOrder tCardOrderB;

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

        when(cardOrderDao.findForCustomer(customer, ACCEPTED, TRANSPONDER_CARD))
                .thenReturn(Arrays.asList(tCardOrderA, tCardOrderB));
    }

    @Test(expected = ActivationException.class)
    public void fails_if_not_enough_card_numbers_available() {
        when(cardOrderDao.nextTransponderCardNumbers(eq(customer.getProductGroupId()), eq(5), any()))
                .thenReturn(Arrays.asList("1", "2"));

        tCardOrderA.setAmount(5);

        cardAssignmentService.assignAllOrderedByCustomer(customer);
    }

    @Test
    public void skips_orders_that_already_have_assigned_card_number() {
        tCardOrderA.setCardNumber("12345");
        tCardOrderB.setCardNumber("67890");

        cardAssignmentService.assignAllOrderedByCustomer(customer);

        verify(cardOrderDao, never()).processTransponderCard(any(), any(), anyBoolean());
    }

    @Test
    public void assigns_card_numbers_in_order_and_updates_mobile_only_for_first() {
        //noinspection unchecked
        when(cardOrderDao.nextTransponderCardNumbers(eq(customer.getProductGroupId()), anyInt(), any()))
                .thenReturn(Arrays.asList("1"), Arrays.asList("2"));

        cardAssignmentService.assignAllOrderedByCustomer(customer);

        InOrder inOrder = inOrder(cardOrderDao);
        inOrder.verify(cardOrderDao).processTransponderCard("1", customer, true);
        inOrder.verify(cardOrderDao).updateCardNumber(anyLong(), eq("1"));
        inOrder.verify(cardOrderDao).processTransponderCard("2", customer, false);
        inOrder.verify(cardOrderDao).updateCardNumber(anyLong(), eq("2"));
    }

    @Test
    public void assigning_qcard_number_rejects_cards_other_than_qcard() {
        List<CardType> cardTypes = Lists.newArrayList(CardType.values());
        cardTypes.remove(CardType.QPARK_CARD);

        cardTypes.forEach(cardType -> {
            CardOrder cardOrder = new CardOrder();
            cardOrder.setCardType(cardType);

            try {
                cardAssignmentService.assignQcardNumber(cardOrder);
                fail("exception should have been raised");
            } catch(IllegalArgumentException e) {
                // just as expected
            }
        });
    }

    @Test
    public void delegates_qcard_number_retrieval_to_dao() {
        CardOrder cardOrder = new CardOrder();
        cardOrder.setCardType(CardType.QPARK_CARD);
        cardOrder.setCustomerId(1l);

        when(cardOrderDao.nextQCardNumber(1l)).thenReturn("test");

        assertThat(cardAssignmentService.assignQcardNumber(cardOrder), is("test"));
    }
}
