package nl.yellowbrick.activation.service;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MembershipDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.Membership;
import nl.yellowbrick.data.domain.PriceModel;
import nl.yellowbrick.data.errors.ActivationException;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import static nl.yellowbrick.data.domain.CardOrderStatus.INSERTED;
import static nl.yellowbrick.data.domain.CardType.QPARK_CARD;
import static nl.yellowbrick.data.domain.CardType.TRANSPONDER_CARD;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AccountActivationServiceTest {

    @InjectMocks AccountActivationService activationService;

    @Mock CustomerDao customerDao;
    @Mock MembershipDao membershipDao;
    @Mock CardOrderDao cardOrderDao;
    @Mock CardAssignmentService cardAssignmentService;
    @Mock CustomerNotificationService emailNotificationService;
    @Mock AdminNotificationService notificationService;

    Customer customer;
    PriceModel priceModel;

    CardOrder tCardOrder;
    CardOrder qparkCardOrder;

    @Before
    public void setUp() {
        customer = new Customer();
        customer.setCustomerId(123);
        customer.setNumberOfTCards(1);

        priceModel = new PriceModel();

        tCardOrder = new CardOrder();
        tCardOrder.setCardType(TRANSPONDER_CARD);

        qparkCardOrder = new CardOrder();
        qparkCardOrder.setCardType(QPARK_CARD);

        MockitoAnnotations.initMocks(this);

        when(cardOrderDao.findForCustomer(customer, INSERTED, TRANSPONDER_CARD))
                .thenReturn(Lists.newArrayList(tCardOrder));
        when(cardOrderDao.findForCustomer(customer, INSERTED, QPARK_CARD))
                .thenReturn(Lists.newArrayList(qparkCardOrder));
        when(cardAssignmentService.canAssignTransponderCards(customer, customer.getNumberOfTCards()))
                .thenReturn(true);
    }

    @Test
    public void assigns_next_customer_nr() {
        mockCollaborations();
        activationService.activateCustomerAccount(customer, priceModel);

        assertThat(customer.getCustomerNr(), equalTo("ABC123"));
    }

    @Test
    public void saves_special_tarif_and_validates_card_orders_and_then_assigns_cards_to_customer() {
        mockCollaborations();
        activationService.activateCustomerAccount(customer, priceModel);

        // saves specialtarif
        verify(cardOrderDao).saveSpecialTarifIfApplicable(eq(customer));

        // validates card orders
        verify(cardOrderDao).validateCardOrder(tCardOrder);
        verify(cardOrderDao).validateCardOrder(qparkCardOrder);

        // assigns transponder cards
        verify(cardAssignmentService).assignTransponderCard(tCardOrder);
    }

    @Test
    public void saves_new_membership_and_notifies_customer() {
        mockCollaborations();
        activationService.activateCustomerAccount(customer, priceModel);

        Membership expectedMembership = new Membership(customer, priceModel);

        verify(membershipDao).saveValidatedMembership(eq(expectedMembership));
        verify(emailNotificationService).notifyAccountAccepted((eq(customer)));
    }

    @Test
    public void no_op_if_customer_lacks_transponder_cards() {
        mockCollaborations();
        customer.setNumberOfTCards(0);

        try {
            activationService.activateCustomerAccount(customer, priceModel);
            fail("expected exception to be thrown");
        } catch(ActivationException e) {
            verifyZeroInteractions(membershipDao, emailNotificationService);
        }
    }

    @Test(expected = ExhaustedCardPoolException.class)
    public void raises_exception_when_transponder_cards_cant_be_assigned() {
        reset(cardAssignmentService);
        when(cardAssignmentService.canAssignTransponderCards(any(), anyInt())).thenReturn(false);

        activationService.activateCustomerAccount(customer, priceModel);
    }

    private void mockCollaborations() {
        doAnswer(setCustomerNr()).when(customerDao).assignNextCustomerNr(eq(customer));
    }

    private Answer setCustomerNr() {
        return invocationOnMock -> {
            Customer cust = (Customer) invocationOnMock.getArguments()[0];
            cust.setCustomerNr("ABC123");
            return null;
        };
    }

}
