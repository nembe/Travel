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

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static nl.yellowbrick.data.database.Functions.CALL_RECORDERS;
import static nl.yellowbrick.data.database.Functions.FunctionCall;
import static nl.yellowbrick.data.database.Functions.TEST_QCARD_NUMBER;
import static nl.yellowbrick.data.domain.CardOrderStatus.*;
import static nl.yellowbrick.data.domain.CardType.*;
import static org.exparity.hamcrest.date.DateMatchers.after;
import static org.hamcrest.Matchers.*;
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

        CardOrder someOrder = cardOrderDao.findForCustomer(customer, INSERTED, QPARK_CARD).get(0);

        cardOrderDao.validateCardOrder(someOrder);

        lock.await(2, TimeUnit.SECONDS);
        assertThat("not all expected stored procedures have been called", lock.getCount(), equalTo(0l));

        // starts by calling WEBAPP.cardorderupdate
        FunctionCall firstCall = calls.getFirst();
        assertThat(firstCall.functionName, equalTo("cardOrderUpdate"));
        assertThat(firstCall.getNumericArg(0).longValue(), equalTo(72031l)); // card order id
        assertThat(firstCall.arguments[1], equalTo("2")); // STATUS_ACCEPTED
        assertThat(firstCall.getNumericArg(2).longValue(), equalTo(600l));
        assertThat(firstCall.getNumericArg(3).intValue(), equalTo(2));

        // then calls WEBAPP.CardOrderValidate
        FunctionCall secondCall = calls.get(1);
        assertThat(secondCall.functionName, equalTo("cardOrderValidate"));
        assertThat(secondCall.getNumericArg(0).longValue(), equalTo(72031l)); // card order id
        assertThat(secondCall.arguments[1], equalTo("3")); // type of card (qpark maps to "3")
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

        CardOrder order = cardOrderDao.findForCustomer(customer, INSERTED, TRANSPONDER_CARD).get(0);
        cardOrderDao.validateCardOrder(order);

        lock.await(2, TimeUnit.SECONDS);

        assertThat(calls.getLast().arguments[1], equalTo("1"));
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

        CardOrder order = cardOrderDao.findForCustomer(customer, INSERTED, RTP_CARD).get(0);
        cardOrderDao.validateCardOrder(order);

        lock.await(2, TimeUnit.SECONDS);

        assertThat(calls.getLast().arguments[1], equalTo("2"));
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
        CardOrder orderA = new CardOrder();
        orderA.setId(72031);
        orderA.setDate(Timestamp.valueOf("2010-12-23 16:26:39"));
        orderA.setStatus(INSERTED);
        orderA.setCustomerId(4776);
        orderA.setCardType(QPARK_CARD);
        orderA.setBriefCode("2");
        orderA.setAmount(2);
        orderA.setPricePerCard(600);
        orderA.setExport(false);
        
        CardOrder orderB = new CardOrder();
        orderB.setId(72032);
        orderB.setDate(Timestamp.valueOf("2010-12-23 16:00:00"));
        orderB.setStatus(ACCEPTED);
        orderB.setCustomerId(4776);
        orderB.setCardType(TRANSPONDER_CARD);
        orderB.setBriefCode("1");
        orderB.setAmount(2);
        orderB.setPricePerCard(500);
        orderB.setExport(true);

        List<CardOrder> cardOrders = cardOrderDao.findForCustomer(customer, INSERTED, QPARK_CARD);
        assertThat(cardOrders, contains(orderA));

        cardOrders = cardOrderDao.findForCustomer(customer, ACCEPTED, TRANSPONDER_CARD);
        assertThat(cardOrders, contains(orderB));
    }

    @Test
    public void delegates_processing_transponder_card_to_stored_procedure() throws Exception {
        CountDownLatch lock = new CountDownLatch(1);
        LinkedList<FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        CardOrder order = new CardOrder();
        order.setId(123l);

        cardOrderDao.processTransponderCard("123456", customer, order, true);

        lock.await(2, TimeUnit.SECONDS);

        FunctionCall call = calls.getFirst();

        assertThat(call.functionName, equalTo("PROCESS_TRANSPONDERCARDS"));
        assertThat(call.getNumericArg(0).longValue(), equalTo(customer.getCustomerId()));
        assertThat(call.getNumericArg(1).longValue(), equalTo(order.getId()));
        assertThat(call.arguments[2], equalTo("123456"));
        assertThat(call.arguments[3], equalTo("TEST MUTATOR"));
        assertThat(call.arguments[4], is(1));
    }

    @Test
    public void fetches_orders_by_status_and_card_type() {
        assertThat(cardOrderDao.findByStatusAndType(INSERTED, TRANSPONDER_CARD), empty());
        assertThat(cardOrderDao.findByStatusAndType(ACCEPTED, TRANSPONDER_CARD), hasSize(1));

        assertThat(cardOrderDao.findByStatusAndType(INSERTED, QPARK_CARD), hasSize(1));
        assertThat(cardOrderDao.findByStatusAndType(ACCEPTED, QPARK_CARD), empty());

        updateCardType(CardType.RTP_CARD.description());

        assertThat(cardOrderDao.findByStatusAndType(INSERTED, RTP_CARD), hasSize(1));
        assertThat(cardOrderDao.findByStatusAndType(ACCEPTED, RTP_CARD), hasSize(1));
        assertThat(cardOrderDao.findByStatusAndType(EXPORTED, RTP_CARD), empty());
    }

    @Test
    public void fetches_orders_by_status() {
        assertThat(cardOrderDao.findByStatus(EXPORTED), empty());

        updateCardStatus(CardOrderStatus.EXPORTED.code());

        List<CardOrder> orders = cardOrderDao.findByStatus(EXPORTED);

        assertThat(orders, hasSize(2));
        assertThat(orders.get(0).getDate(), after(orders.get(1).getDate()));
    }

    @Test
    public void deletes_orders_by_id() {
        CardOrder cardOrder = cardOrderDao.findById(72031).get();

        cardOrderDao.delete(cardOrder.getId());

        assertThat(cardOrderDao.findById(72031).isPresent(), is(false));
    }

    @Test
    public void counts_cards_in_stock_per_product_group() {
        assertThat(cardOrderDao.cardsAvailableForProductGroup(1), is(2));
        assertThat(cardOrderDao.cardsAvailableForProductGroup(2), is(0));
    }

    @Test
    public void delegates_retrieval_of_qcardnumber_to_procedure() throws Exception {
        assertThat(cardOrderDao.nextQCardNumber(123l), is(TEST_QCARD_NUMBER));
    }

    private void updateCardType(String cardType) {
        db.accept((template) -> template.update("UPDATE CARDORDER SET cardtype = ?", cardType));
    }

    private void updateCardStatus(int status) {
        db.accept((template) -> template.update("UPDATE CARDORDER SET orderstatus = ?", status));
    }
}
