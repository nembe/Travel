package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.Functions;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.Membership;
import nl.yellowbrick.data.domain.PriceModel;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class MembershipJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    MembershipJdbcDao membershipDao;

    Customer customer;
    PriceModel priceModel;

    @Before
    public void setUp() {
        customer = new Customer();
        customer.setCustomerId(123l);
        customer.setCustomerNr("ABC123");
        customer.setParkadammerTotal(1);
        customer.setNumberOfTCards(2);
        customer.setNumberOfQCards(4);
        customer.setCreditLimit(0);

        priceModel = new PriceModel();
        priceModel.setInitRtpCardCost(50);
        priceModel.setInitTranspCardCost(55);
        priceModel.setTranspCardCost(60);
        priceModel.setRtpCardCost(65);
        priceModel.setRegistratiekosten(10 * 100); // cents
        priceModel.setSubscriptionCostEuroCents(8 * 100); // cents
    }

    @Test
    public void delegates_to_stored_procedure() throws Exception {
        CountDownLatch lock = new CountDownLatch(1);
        LinkedList<Functions.FunctionCall> calls = new LinkedList<>();

        Functions.CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        membershipDao.saveValidatedMembership(new Membership(customer, priceModel));

        lock.await(2, TimeUnit.SECONDS);
        assertThat("customerValidateMembership was never called", lock.getCount(), equalTo(0l));
        assertThat(calls.getFirst().functionName, equalTo("customerValidateMembership"));

        Object[] args = calls.getFirst().arguments;

        assertThat(Long.parseLong(args[0].toString()), equalTo(customer.getCustomerId()));
        assertThat(args[1], equalTo(customer.getCustomerNr()));
        assertThat(args[2], equalTo(customer.getParkadammerTotal()));
        assertThat(args[3], equalTo(customer.getNumberOfTCards()));
        assertThat(args[4], equalTo(customer.getNumberOfQCards()));
        assertThat(args[5], equalTo((int)customer.getCreditLimit()));
        assertThat(args[6], equalTo(800)); // membership fee
        assertThat(args[7], equalTo(1000)); // registration fee
        assertThat(args[8], equalTo(priceModel.getInitTranspCardCost()));
        assertThat(args[9], equalTo(priceModel.getTranspCardCost()));
        assertThat(args[10], equalTo(priceModel.getInitRtpCardCost()));
        assertThat(args[11], equalTo(priceModel.getRtpCardCost()));
        assertThat(args[12].toString().length(), equalTo(4)); // 4 char pincode
        assertThat(args[13].toString().length(), equalTo(60)); // 60 char password
        assertThat(args[14], equalTo("TEST MUTATOR"));
    }
}
