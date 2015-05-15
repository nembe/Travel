package nl.yellowbrick.activation.service;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MembershipDao;
import nl.yellowbrick.data.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static nl.yellowbrick.data.domain.CardOrderStatus.*;
import static nl.yellowbrick.data.domain.CardType.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AccountActivationServiceTest {

    @InjectMocks AccountActivationService activationService;

    @Mock CustomerDao customerDao;
    @Mock MembershipDao membershipDao;
    @Mock CardOrderDao cardOrderDao;
    @Mock CardAssignmentService cardAssignmentService;
    @Mock CustomerNotificationService emailNotificationService;

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
        verifyNoMoreInteractions(cardAssignmentService);
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
        activationService.activateCustomerAccount(customer, priceModel);

        verifyZeroInteractions(membershipDao, emailNotificationService);
    }

    private void mockCollaborations() {
        doAnswer(setCustomerNr()).when(customerDao).assignNextCustomerNr(eq(customer));
    }

    private Answer setCustomerNr() {
        return new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Customer cust = (Customer) invocationOnMock.getArguments()[0];
                cust.setCustomerNr("ABC123");
                return null;
            }
        };
    }

}
