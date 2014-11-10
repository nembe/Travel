package nl.yellowbrick.dao.impl;

import nl.yellowbrick.BaseSpringTestCase;
import nl.yellowbrick.domain.Customer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static nl.yellowbrick.database.Functions.CALL_RECORDERS;
import static nl.yellowbrick.database.Functions.FunctionCall;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CardOrderJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    CardOrderJdbcDao cardOrderDao;

    Customer customer;

    @Before
    public void setUp() {
        customer = new Customer();
        customer.setCustomerId(1234);
    }

    @Test
    public void delegates_saving_special_tarif_to_stored_procedure() throws Exception {
        CountDownLatch lock = new CountDownLatch(1);
        LinkedList<FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        cardOrderDao.saveSpecialTarifIfApplicable(customer);

        lock.await(2, TimeUnit.SECONDS);
        assertThat("saveSignupSpecialRate was never called", lock.getCount(), equalTo(0l));

        FunctionCall firstCall = calls.getFirst();

        assertThat(firstCall.functionName, equalTo("saveSignupSpecialRate"));
        assertThat(firstCall.getNumericArg(0).longValue(), equalTo(customer.getCustomerId()));
    }
}