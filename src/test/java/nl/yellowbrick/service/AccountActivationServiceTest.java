package nl.yellowbrick.service;

import nl.yellowbrick.dao.CardOrderDao;
import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.dao.MembershipDao;
import nl.yellowbrick.dao.PriceModelDao;
import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.domain.Membership;
import nl.yellowbrick.domain.PriceModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AccountActivationServiceTest {

    @InjectMocks AccountActivationService activationService;

    @Mock PriceModelDao priceModelDao;
    @Mock CustomerDao customerDao;
    @Mock MembershipDao membershipDao;
    @Mock CardOrderDao cardOrderDao;
    @Mock EmailNotificationService emailNotificationService;

    Customer customer;
    PriceModel priceModel;

    @Before
    public void setUp() {
        customer = new Customer();
        customer.setCustomerId(123);
        customer.setNumberOfTCards(1);

        priceModel = new PriceModel();

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void noOpIfMissingPriceModel() {
        when(priceModelDao.findForCustomer(eq(customer))).thenReturn(Optional.empty());

        activationService.activateCustomerAccount(customer);

        verifyZeroInteractions(customerDao, membershipDao, cardOrderDao, emailNotificationService);
    }

    @Test
    public void assignsNextCustomerNr() {
        mockCollaborations();
        activationService.activateCustomerAccount(customer);

        assertThat(customer.getCustomerNr(), equalTo("ABC123"));
    }

    @Test
    public void savesSpecialTarifAndValidatesCardOrders() {
        mockCollaborations();
        activationService.activateCustomerAccount(customer);

        verify(cardOrderDao).saveSpecialTarifIfApplicable(eq(customer));
        verify(cardOrderDao).validateCardOrders(eq(customer));
    }

    @Test
    public void savesNewMembershipAndNotifiesCustomer() {
        mockCollaborations();
        activationService.activateCustomerAccount(customer);

        Membership expectedMembership = new Membership(customer, priceModel);

        verify(membershipDao).saveValidatedMembership(eq(expectedMembership));
        verify(emailNotificationService).notifyAccountAccepted((eq(customer)));
    }

    @Test
    public void noOpIfCustomerLacksTransponderCards() {
        mockCollaborations();

        customer.setNumberOfTCards(0);
        activationService.activateCustomerAccount(customer);

        verifyZeroInteractions(membershipDao, emailNotificationService);
    }

    private void mockCollaborations() {
        when(priceModelDao.findForCustomer(eq(customer))).thenReturn(Optional.of(priceModel));
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