package nl.yellowbrick.dao.impl;

import nl.yellowbrick.BaseSpringTestCase;
import nl.yellowbrick.database.DbHelper;
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

    @Autowired
    DbHelper db;

    Customer customer;

    @Before
    public void setUp() {
        customer = new Customer();
        customer.setCustomerId(4776);
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

    @Test
    public void validates_card_orders() throws Exception {
        CountDownLatch lock = new CountDownLatch(2);
        LinkedList<FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        cardOrderDao.validateCardOrders(customer);

        lock.await(2, TimeUnit.SECONDS);
        assertThat("not all expected stored procedures have been called", lock.getCount(), equalTo(0l));

        // starts by calling WEBAPP.cardorderupdate
        FunctionCall firstCall = calls.getFirst();
        assertThat(firstCall.functionName, equalTo("cardOrderUpdate"));
        assertThat(firstCall.getNumericArg(0).longValue(), equalTo(72031l));
        assertThat(firstCall.arguments[1], equalTo("2")); // STATUS_ACCEPTED
        assertThat(firstCall.getNumericArg(2).longValue(), equalTo(0l));
        assertThat(firstCall.getNumericArg(3).intValue(), equalTo(1));

        // then calls WEBAPP.CardOrderValidate
        FunctionCall secondCall = calls.get(1);
        assertThat(secondCall.functionName, equalTo("cardOrderValidate"));
        assertThat(secondCall.getNumericArg(0).longValue(), equalTo(customer.getCustomerId()));
        assertThat(secondCall.getNumericArg(1).longValue(), equalTo(0l)); // price per card
        assertThat(secondCall.getNumericArg(2).intValue(), equalTo(1)); // amount
        assertThat(secondCall.arguments[3], equalTo("3")); // type of card
    }

    @Test
    public void maps_transponder_card_description_to_type_1() throws Exception {
        updateCardType("transponderSOMETHING_OR_OTHER");

        CountDownLatch lock = new CountDownLatch(2);
        LinkedList<FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        cardOrderDao.validateCardOrders(customer);
        lock.await(2, TimeUnit.SECONDS);

        assertThat(calls.getLast().arguments[3], equalTo("1"));
    }

    @Test
    public void maps_rtp_card_description_to_type_2() throws Exception {
        updateCardType("rtp_AND_SOME_IRRELEVANT_STUFF");

        CountDownLatch lock = new CountDownLatch(2);
        LinkedList<FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        cardOrderDao.validateCardOrders(customer);
        lock.await(2, TimeUnit.SECONDS);

        assertThat(calls.getLast().arguments[3], equalTo("2"));
    }

    @Test
    public void maps_q_card_description_to_type_3() throws Exception {
        updateCardType("qcard_SOMETHING_OR_OTHER");

        CountDownLatch lock = new CountDownLatch(2);
        LinkedList<FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        cardOrderDao.validateCardOrders(customer);
        lock.await(2, TimeUnit.SECONDS);

        assertThat(calls.getLast().arguments[3], equalTo("3"));
    }

    @Test
    public void maps_other_descriptions_to_type_0() throws Exception {
        updateCardType("BAZINGA?");

        CountDownLatch lock = new CountDownLatch(2);
        LinkedList<FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        cardOrderDao.validateCardOrders(customer);
        lock.await(2, TimeUnit.SECONDS);

        assertThat(calls.getLast().arguments[3], equalTo("0"));
    }

    private void updateCardType(String cardType) {
        db.withTemplate((template) -> {
            template.update("UPDATE CARDORDER SET cardtype = ?", cardType);
            return null;
        });
    }
}