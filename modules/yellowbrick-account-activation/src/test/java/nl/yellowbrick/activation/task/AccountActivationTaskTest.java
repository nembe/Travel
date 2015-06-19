package nl.yellowbrick.activation.task;

import com.google.common.collect.Lists;
import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.activation.service.AccountValidationService;
import nl.yellowbrick.activation.service.AdminNotificationService;
import nl.yellowbrick.activation.validation.UnboundErrors;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MarketingActionDao;
import nl.yellowbrick.data.dao.PriceModelDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import nl.yellowbrick.data.domain.MarketingAction;
import nl.yellowbrick.data.domain.PriceModel;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AccountActivationTaskTest {

    AccountActivationTask accountActivationTask;

    CustomerDao customerDao;
    PriceModelDao priceModelDao;
    MarketingActionDao marketingActionDao;
    AccountActivationService activationService;
    AccountValidationService accountValidationService;
    AdminNotificationService notificationService;

    Customer customerA = testCustomer();
    Customer customerB = testCustomer();

    PriceModel priceModel = new PriceModel();

    @Before
    public void initMocks() {
        customerDao = mock(CustomerDao.class);
        priceModelDao = mock(PriceModelDao.class);
        marketingActionDao = mock(MarketingActionDao.class);
        activationService = mock(AccountActivationService.class);
        accountValidationService = mock(AccountValidationService.class);
        notificationService = mock(AdminNotificationService.class);

        accountActivationTask = new AccountActivationTask(customerDao, priceModelDao, marketingActionDao,
                activationService, accountValidationService, notificationService);
    }

    @Test
    public void no_op_if_there_are_no_accounts_pending_activation() {
        stubFindCustomersPendingActivation();

        accountActivationTask.validateAndActivateAccounts();

        verify(customerDao).findAllPendingActivation();
        verifyNoMoreInteractions(customerDao, activationService);
    }

    @Test
    public void activates_valid_accounts() {
        stubFindCustomersPendingActivation(customerA, customerB);
        stubPriceModel(of(priceModel));
        stubActionCode(empty());

        doAnswer(emptyErrors()).when(accountValidationService).validate(any());

        accountActivationTask.validateAndActivateAccounts();

        verify(activationService).activateCustomerAccount(same(customerA), eq(priceModel));
        verify(activationService).activateCustomerAccount(same(customerB), eq(priceModel));
        verifyNoMoreInteractions(activationService);
    }

    @Test
    public void marks_invalid_accounts_as_pending_review() {
        // return a couple of customers
        stubFindCustomersPendingActivation(customerA, customerB);
        // customerA doesn't pass validation
        doAnswer(recordAnError()).when(accountValidationService).validate(eq(customerA));
        // customerB doesn't pass validation
        doAnswer(recordAnError()).when(accountValidationService).validate(eq(customerB));

        accountActivationTask.validateAndActivateAccounts();

        // they both are marked for review
        verify(customerDao).markAsPendingHumanReview(same(customerA));
        verify(customerDao).markAsPendingHumanReview(same(customerB));
        verifyZeroInteractions(activationService);
    }

    @Test
    public void marks_invalid_account_if_missing_price_model() {
        stubFindCustomersPendingActivation(customerA);
        stubPriceModel(empty());

        doAnswer(emptyErrors()).when(accountValidationService).validate(any());

        accountActivationTask.validateAndActivateAccounts();

        verify(customerDao).markAsPendingHumanReview(eq(customerA));
        verifyZeroInteractions(activationService);
    }

    @Test
    public void applies_marketing_action() {
        stubFindCustomersPendingActivation(customerA);
        stubPriceModel(of(priceModel));
        stubActionCode(of(validMarketingAction()));

        doAnswer(emptyErrors()).when(accountValidationService).validate(any());

        accountActivationTask.validateAndActivateAccounts();

        verify(activationService).activateCustomerAccount(customerA, priceModel);
        assertThat(priceModel.getRegistratiekosten(), equalTo(12345));
    }

    @Test
    public void notifies_admin_when_activation_fails_due_to_exhausted_cards() {
        stubFindCustomersPendingActivation(customerA);
        stubPriceModel(of(priceModel));
        stubActionCode(empty());
        doAnswer(emptyErrors()).when(accountValidationService).validate(any());

        // raise error when trying to activate customerA
        doThrow(new ExhaustedCardPoolException(customerA))
                .when(activationService)
                .activateCustomerAccount(customerA, priceModel);

        accountActivationTask.validateAndActivateAccounts();

        // check that admin is notified
        verify(notificationService).notifyCardPoolExhausted(customerA.getProductGroupId());
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
        return invocationOnMock -> {
            Customer customer = (Customer) invocationOnMock.getArguments()[0];
            Errors errors = new UnboundErrors(customer, "customer");

            errors.reject("customerId");

            return errors;
        };
    }

    private Answer emptyErrors() {
        return invocationOnMock -> {
            Customer customer = (Customer) invocationOnMock.getArguments()[0];

            return new UnboundErrors(customer, "customer");
        };
    }

    private void stubActionCode(Optional<MarketingAction> marketingAction) {
        when(marketingActionDao.findByActionCode(any())).thenReturn(marketingAction);
    }

    private void stubFindCustomersPendingActivation(Customer... customers) {
        when(customerDao.findAllPendingActivation()).thenReturn(Lists.newArrayList(customers));
    }

    private void stubPriceModel(Optional<PriceModel> priceModel) {
        when(priceModelDao.findForCustomer(anyLong())).thenReturn(priceModel);
    }
}
