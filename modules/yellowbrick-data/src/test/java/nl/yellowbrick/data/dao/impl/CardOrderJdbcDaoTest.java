package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static nl.yellowbrick.data.database.Functions.CALL_RECORDERS;
import static nl.yellowbrick.data.database.Functions.FunctionCall;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
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
        updateCardType("transponderkaart");

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
        updateCardType("rtp kaart");

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
        updateCardType("qcard");

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

    @Test
    public void returns_next_transponder_card_numbers_from_pool() {
        // should return the single first accepted number
        List<String> cardNumbers = cardOrderDao.nextTransponderCardNumbers(1, 1, Optional.empty());
        assertThat(cardNumbers, contains("162826"));

        // should return the single following accepted number
        cardNumbers = cardOrderDao.nextTransponderCardNumbers(1, 1, Optional.of("162826"));
        assertThat(cardNumbers, contains("162827"));

        // should return both the available accepted numbers
        cardNumbers = cardOrderDao.nextTransponderCardNumbers(1, 5, Optional.empty());
        assertThat(cardNumbers, contains("162826", "162827"));

        // should be empty as there are no cards in the pool for the supplied product group id
        cardNumbers = cardOrderDao.nextTransponderCardNumbers(12345, 1, Optional.empty());
        assertThat(cardNumbers, empty());
    }

    @Test
    public void returns_card_orders_per_customer() {
        CardOrder expectedCardOrder = new CardOrder();
        expectedCardOrder.setId(72031);
        expectedCardOrder.setDate(Date.valueOf("2010-12-23"));
        expectedCardOrder.setStatus(CardOrderStatus.INSERTED);
        expectedCardOrder.setCustomerId(4776);
        expectedCardOrder.setCardType(CardType.QPARK_CARD);
        expectedCardOrder.setBriefCode("2");
        expectedCardOrder.setAmount(1);
        expectedCardOrder.setPricePerCard(0);

        List<CardOrder> cardOrders = cardOrderDao.findForCustomer(customer, CardOrderStatus.INSERTED, CardType.QPARK_CARD);
        assertThat(cardOrders, contains(expectedCardOrder));
    }

    @Test
    public void delegates_processing_transponder_card_to_stored_procedure() throws Exception {
        CountDownLatch lock = new CountDownLatch(1);
        LinkedList<FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        cardOrderDao.processTransponderCard("123456", customer, true);

        lock.await(2, TimeUnit.SECONDS);

        FunctionCall call = calls.getFirst();

        assertThat(call.functionName, equalTo("PROCESS_TRANSPONDERCARDS"));
        assertThat(call.getNumericArg(0).longValue(), equalTo(customer.getCustomerId()));
        assertThat(call.arguments[1], equalTo("123456"));
        assertThat(call.arguments[2], equalTo("TEST MUTATOR"));
        assertThat(call.arguments[3], is(1));
    }

    private void updateCardType(String cardType) {
        db.accept((template) -> template.update("UPDATE CARDORDER SET cardtype = ?", cardType));
    }
}
