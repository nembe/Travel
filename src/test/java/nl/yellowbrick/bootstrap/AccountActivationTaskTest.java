package nl.yellowbrick.bootstrap;

import com.google.common.collect.ImmutableList;
import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.service.AccountActivationService;
import nl.yellowbrick.validation.AccountRegistrationValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.validation.Errors;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class AccountActivationTaskTest {

    AccountActivationTask accountActivationTask;

    CustomerDao customerDao;
    AccountActivationService activationService;

    // use a couple of validators for tests
    AccountRegistrationValidator validatorA;
    AccountRegistrationValidator validatorB;

    Customer customerA = new Customer();
    Customer customerB = new Customer();

    @Before
    public void initMocks() {
        customerDao = mock(CustomerDao.class);
        activationService = mock(AccountActivationService.class);
        validatorA = spy(new NoOpValidator());
        validatorB = spy(new NoOpValidator());

        accountActivationTask = new AccountActivationTask(customerDao, activationService, validatorA, validatorB);
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
        // ensure validators won't record any errors
        doNothing().when(validatorA).validate(any(), any());
        doNothing().when(validatorB).validate(any(), any());

        // return a couple of customers
        when(customerDao.findAllPendingActivation()).thenReturn(ImmutableList.of(customerA, customerB));

        accountActivationTask.validateAndActivateAccounts();

        verify(activationService).activateCustomerAccount(customerA);
        verify(activationService).activateCustomerAccount(customerB);
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
        verify(customerDao).markAsPendingHumanReview(eq(customerA));
        verify(customerDao).markAsPendingHumanReview(eq(customerB));
        verifyZeroInteractions(activationService);
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

    private class NoOpValidator implements AccountRegistrationValidator {

        @Override
        public boolean supports(Class<?> aClass) {
            return true;
        }

        @Override
        public void validate(Object o, Errors errors) {
        }
    }
}
