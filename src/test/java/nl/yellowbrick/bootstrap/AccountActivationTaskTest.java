package nl.yellowbrick.bootstrap;

import nl.yellowbrick.dao.CustomerDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class AccountActivationTaskTest {

    @InjectMocks
    AccountActivationTask accountActivationTask;

    @Mock
    CustomerDao customerDao;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    // really dumb test to just make sure test harness is ok
	@Test
	public void fetchesAccountsPendingActivation() {
        when(customerDao.findAllPendingActivation()).thenReturn(new ArrayList<>());

        accountActivationTask.validateAndActivateAccounts();

        verify(customerDao).findAllPendingActivation();
        verifyNoMoreInteractions(customerDao);
	}
}
