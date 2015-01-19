package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.Functions;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.Membership;
import nl.yellowbrick.data.domain.PriceModel;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static nl.yellowbrick.data.database.Functions.FunctionCall;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class MembershipJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    MembershipJdbcDao membershipDao;

    Customer customer;
    PriceModel priceModel;

    FunctionCall latestCall;
    CountDownLatch lock;

    @Before
    public void setUp() {
        customer = new Customer();
        customer.setCustomerId(123l);
        customer.setCustomerNr("ABC123");
        customer.setNumberOfTCards(2);
        customer.setNumberOfQCards(4);
        customer.setCreditLimit(0);

        priceModel = new PriceModel();
        priceModel.setDefaultIssuePhysicalCard(true);
        priceModel.setRegistratiekosten(10 * 100); // cents

        lock = new CountDownLatch(1);
        Functions.CALL_RECORDERS.add((functionCall) -> {
            latestCall = (functionCall);
            lock.countDown();
        });
    }

    @Test
    public void delegates_to_stored_procedure() throws Exception {
        membershipDao.saveValidatedMembership(new Membership(customer, priceModel));

        FunctionCall call = latestFunctionCall();

        assertThat("customerValidateMembership was never called", lock.getCount(), equalTo(0l));
        assertThat(call.functionName, equalTo("customerValidateMembership"));

        Object[] args = call.arguments;

        assertThat(call.getNumericArg(0).longValue(), equalTo(customer.getCustomerId()));
        assertThat(args[1], equalTo(customer.getCustomerNr()));
        assertThat(args[2], equalTo(customer.getNumberOfTCards()));
        assertThat(args[3], equalTo(customer.getNumberOfQCards()));
        assertThat(args[4], equalTo("Y"));
        assertThat(args[5], equalTo(1000)); // registration fee
        assertThat(args[6].toString().length(), equalTo(4)); // 4 char pincode
        assertThat(args[7].toString().length(), equalTo(60)); // 60 char password
        assertThat(args[8], equalTo("TEST MUTATOR"));
    }

    @Test
    public void takes_issuing_of_physical_cards_into_account() throws Exception {
        priceModel.setDefaultIssuePhysicalCard(false); // dont issue physical cards!

        membershipDao.saveValidatedMembership(new Membership(customer, priceModel));

        FunctionCall call = latestFunctionCall();

        assertThat(call.arguments[4], equalTo("N"));
    }

    private FunctionCall latestFunctionCall() throws InterruptedException {
        lock.await(2, TimeUnit.SECONDS);

        return latestCall;
    }
}
