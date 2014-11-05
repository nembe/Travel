package nl.yellowbrick.bootstrap;

import com.google.common.collect.ImmutableList;
import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.service.AccountActivationService;
import nl.yellowbrick.validation.CustomerMembershipValidator;
import nl.yellowbrick.validation.GeneralCustomerValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.validation.Errors;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class AccountActivationTaskTest {

    @InjectMocks
    AccountActivationTask accountActivationTask;

    @Mock
    CustomerDao customerDao;

    @Mock
    AccountActivationService accountActivationService;

    @Spy
    GeneralCustomerValidator generalCustomerValidator;

    @Spy
    CustomerMembershipValidator customerMembershipValidator;

    Customer customerA = new Customer();
    Customer customerB = new Customer();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

	@Test
	public void noOpIfThereAreNoAccountsPendingActivation() {
        when(customerDao.findAllPendingActivation()).thenReturn(new ArrayList<>());

        accountActivationTask.validateAndActivateAccounts();

        verify(customerDao).findAllPendingActivation();
        verifyNoMoreInteractions(customerDao, accountActivationService);
	}

    @Test
    public void activatesValidAccounts() {
        // ensure validators won't record any errors
        doNothing().when(generalCustomerValidator).validate(any(), any());
        doNothing().when(customerMembershipValidator).validate(any(), any());

        // return a couple of customers
        when(customerDao.findAllPendingActivation()).thenReturn(ImmutableList.of(customerA, customerB));

        accountActivationTask.validateAndActivateAccounts();

        verify(accountActivationService).activateCustomerAccount(customerA);
        verify(accountActivationService).activateCustomerAccount(customerB);
        verifyNoMoreInteractions(accountActivationService);
    }

    @Test
    public void marksInvalidAccountsAsPendingReview() {
        // customerA doesn't pass general validation
        doAnswer(recordAnError()).when(generalCustomerValidator).validate(eq(customerA), any());
        doNothing().when(customerMembershipValidator).validate(eq(customerA), any());

        // customerB doesn't pass membership validation
        doAnswer(recordAnError()).when(customerMembershipValidator).validate(eq(customerB), any());
        doNothing().when(generalCustomerValidator).validate(eq(customerB), any());

        // return a couple of customers
        when(customerDao.findAllPendingActivation()).thenReturn(ImmutableList.of(customerA, customerB));

        accountActivationTask.validateAndActivateAccounts();

        // they both are marked for review
        verify(customerDao).markAsPendingHumanReview(eq(customerA));
        verify(customerDao).markAsPendingHumanReview(eq(customerB));
        verifyZeroInteractions(accountActivationService);
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
}
