package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MembershipDao;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.*;
import nl.yellowbrick.data.errors.ActivationException;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class AccountActivationServiceTest extends BaseSpringTestCase {

    private static final long CUSTOMER_ID = 4776;

    @Autowired @InjectMocks AccountActivationService activationService;

    @Autowired @Spy CustomerDao customerDao;
    @Autowired @Spy MembershipDao membershipDao;
    @Autowired @Spy CardOrderDao cardOrderDao;
    @Autowired @Spy CardAssignmentService cardAssignmentService;
    @Autowired @Spy CustomerNotificationService emailNotificationService;
    @Autowired @Spy AdminNotificationService notificationService;

    @Autowired DbHelper db;

    Customer customer;
    PriceModel priceModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // change status from our sample customer back to "registered"
        db.accept(t -> t.update("update customer set customerstatusidfk = 1 where customerid = ?", CUSTOMER_ID));

        // make sure his card orders haven't been processed yet
        db.accept(t -> t.update("update cardorder set orderstatus = '1' where customerid = ?", CUSTOMER_ID));

        customer = customerDao.findById(CUSTOMER_ID).get();
        priceModel = new PriceModel();
    }

    @Test
    public void assigns_next_customer_nr() {
        activationService.activateCustomerAccount(customer, priceModel);

        verify(customerDao).assignNextCustomerNr(any());
    }

    @Test
    public void saves_special_tarif_and_validates_card_orders_and_then_assigns_cards_to_customer() {
        activationService.activateCustomerAccount(customer, priceModel);

        // saves specialtarif
        verify(cardOrderDao).saveSpecialTarifIfApplicable(any());

        // validates card orders
        verify(cardOrderDao, times(1)).validateCardOrder(argThat(isTransponderCardOrder()));
        verify(cardOrderDao, times(1)).validateCardOrder(argThat(isQparkCardOrder()));

        // assigns transponder cards
        verify(cardAssignmentService).assignTransponderCard(argThat(isTransponderCardOrder()));
    }

    @Test
    public void saves_new_membership_and_notifies_customer() {
        activationService.activateCustomerAccount(customer, priceModel);

        Matcher<Membership> isExpectedMembership = new ArgumentMatcher<Membership>() {
            @Override
            public boolean matches(Object o) {
                Membership m = (Membership) o;
                return m.getPriceModel().equals(priceModel) && m.getCustomer().getCustomerId() == CUSTOMER_ID;
            }
        };

        verify(membershipDao).saveValidatedMembership(argThat(isExpectedMembership));
        verify(emailNotificationService).notifyAccountAccepted(any());
    }

    @Test
    public void no_op_if_customer_lacks_transponder_cards() {
        db.accept(t -> t.update("update customer set numberoftcards = 0 where customerid = ?", CUSTOMER_ID));

        try {
            activationService.activateCustomerAccount(customer, priceModel);
            fail("expected exception to be thrown");
        } catch(ActivationException e) {
            verifyZeroInteractions(membershipDao, emailNotificationService);
        }
    }

    @Test(expected = ExhaustedCardPoolException.class)
    public void raises_exception_when_transponder_cards_cant_be_assigned() {
        doReturn(false).when(cardAssignmentService).canAssignTransponderCards(any(), anyInt());

        activationService.activateCustomerAccount(customer, priceModel);
    }

    private Matcher<CardOrder> isOrderOf(CardType cardType) {
        return new ArgumentMatcher<CardOrder>() {
            @Override
            public boolean matches(Object o) {
                return ((CardOrder) o).getCardType().equals(cardType);
            }
        };
    }

    private Matcher<CardOrder> isTransponderCardOrder() {
        return isOrderOf(CardType.TRANSPONDER_CARD);
    }

    private Matcher<CardOrder> isQparkCardOrder() {
        return isOrderOf(CardType.QPARK_CARD);
    }
}
