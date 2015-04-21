package nl.yellowbrick.activation.task;

import com.google.common.collect.ImmutableList;
import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.activation.validation.AccountRegistrationValidator;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MarketingActionDao;
import nl.yellowbrick.data.dao.PriceModelDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import nl.yellowbrick.data.domain.MarketingAction;
import nl.yellowbrick.data.domain.PriceModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AccountActivationTaskTest {

    AccountActivationTask accountActivationTask;

    CustomerDao customerDao;
    PriceModelDao priceModelDao;
    MarketingActionDao marketingActionDao;
    AccountActivationService activationService;

    // use a couple of validators for tests
    AccountRegistrationValidator validatorA;
    AccountRegistrationValidator validatorB;

    Customer customerA = testCustomer();
    Customer customerB = testCustomer();

    PriceModel priceModel = new PriceModel();

    @Before
    public void initMocks() {
        customerDao = mock(CustomerDao.class);
        priceModelDao = mock(PriceModelDao.class);
        marketingActionDao = mock(MarketingActionDao.class);
        activationService = mock(AccountActivationService.class);
        validatorA = spy(new NoOpValidator());
        validatorB = spy(new NoOpValidator());

        accountActivationTask = new AccountActivationTask(customerDao, priceModelDao, marketingActionDao,
                activationService, validatorA, validatorB);
    }

    @Test
    public void no_op_if_there_are_no_accounts_pending_activation() {
        when(customerDao.findAllPendingActivation()).thenReturn(new ArrayList<>());

        accountActivationTask.validateAndActivateAccounts();

        verify(customerDao).findAllPendingActivation();
        verifyNoMoreInteractions(customerDao, activationService);
    }

    @Test
    public void activates_valid_accounts() {
        // return a couple of customers
        when(customerDao.findAllPendingActivation()).thenReturn(ImmutableList.of(customerA, customerB));
        when(priceModelDao.findForCustomer(anyLong())).thenReturn(Optional.of(priceModel));
        when(marketingActionDao.findByActionCode(any())).thenReturn(Optional.empty());

        accountActivationTask.validateAndActivateAccounts();

        verify(activationService).activateCustomerAccount(same(customerA), eq(priceModel));
        verify(activationService).activateCustomerAccount(same(customerB), eq(priceModel));
        verifyNoMoreInteractions(activationService);
    }

    @Test
    public void marks_invalid_accounts_as_pending_review() {
        // customerA doesn't pass validation A
        doAnswer(recordAnError()).when(validatorA).validate(eq(customerA), any());
        doNothing().when(validatorB).validate(eq(customerA), any());

        // customerB doesn't pass validation B
        doAnswer(recordAnError()).when(validatorB).validate(eq(customerB), any());
        doNothing().when(validatorA).validate(eq(customerB), any());

        // return a couple of customers
        when(customerDao.findAllPendingActivation()).thenReturn(ImmutableList.of(customerA, customerB));

        accountActivationTask.validateAndActivateAccounts();

        // they both are marked for review
        verify(customerDao).markAsPendingHumanReview(same(customerA));
        verify(customerDao).markAsPendingHumanReview(same(customerB));
        verifyZeroInteractions(activationService);
    }

    @Test
    public void marks_invalid_account_if_missing_price_model() {
        // return a couple of customers
        when(customerDao.findAllPendingActivation()).thenReturn(ImmutableList.of(customerA));
        when(priceModelDao.findForCustomer(anyLong())).thenReturn(Optional.empty());

        accountActivationTask.validateAndActivateAccounts();

        verify(customerDao).markAsPendingHumanReview(eq(customerA));
        verifyZeroInteractions(activationService);
    }

    @Test
    public void applies_marketing_action() {
        when(customerDao.findAllPendingActivation()).thenReturn(ImmutableList.of(customerA));
        when(priceModelDao.findForCustomer(anyLong())).thenReturn(Optional.of(priceModel));
        when(marketingActionDao.findByActionCode(customerA.getActionCode()))
                .thenReturn(Optional.of(validMarketingAction()));

        accountActivationTask.validateAndActivateAccounts();

        verify(activationService).activateCustomerAccount(customerA, priceModel);
        assertThat(priceModel.getRegistratiekosten(), equalTo(12345));
    }

    private MarketingAction validMarketingAction() {
        long day = 1440 * 60 * 1000;

        Date validFrom = new Date(System.currentTimeMillis() - day);
        Date validTo = new Date(System.currentTimeMillis() + day);

        return new MarketingAction("BOOYAKASHA", 12345, validFrom, validTo);
    }

    private Customer testCustomer() {
        Customer customer = new Customer();
        customer.setStatus(CustomerStatus.REGISTERED);
        customer.setActionCode("BOOYAKASHA");

        return customer;
    }

    private Answer recordAnError() {
        return new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Errors errors = (Errors) invocationOnMock.getArguments()[1];
                errors.reject("customerId");

                return null;
            }
        };
    }

    private class NoOpValidator extends AccountRegistrationValidator {

        @Override
        protected void doValidate(Customer customer, Errors errors) {
        }
    }
}
