package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CardOrderJdbcDao implements CardOrderDao, InitializingBean {

    private static final String PACKAGE = "WEBAPP";
    private static final String SAVE_SPECIAL_TARIF_PROC = "saveSignupSpecialRate";
    private static final String CARD_ORDER_UPDATE_PROC = "cardorderUpdate";
    private static final String CARD_ORDER_VALIDATE_PROC = "CardOrderValidate";
    private static final String PROCESS_TRANSPONDERCARDS_PROC = "PROCESS_TRANSPONDERCARDS";
    private static final String GET_QCARD_NUMBER_PROC = "getQcardNr";

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private Mutator mutator;

    private SimpleJdbcCall saveSpecialTarifCall;
    private SimpleJdbcCall cardOrderUpdateCall;
    private SimpleJdbcCall cardOrderValidateCall;
    private SimpleJdbcCall processTransponderCardsCall;
    private SimpleJdbcCall getQCardNumber;

    private Logger log = LoggerFactory.getLogger(CardOrderJdbcDao.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        compileJdbcCalls();
    }

    @Override
    public void saveSpecialTarifIfApplicable(Customer customer) {
        saveSpecialTarifCall.execute(customer.getCustomerId());
    }

    @Override
    public List<String> nextTransponderCardNumbers(int productGroupId, int numberOfCards, Optional<String> lastUsedCardNumber) {
        String sql = "SELECT * FROM ( " +
                "SELECT CARDNR FROM TRANSPONDERCARDPOOL " +
                "WHERE CARDSTATUS_ID = ? " +
                "AND PRODUCTGROUP_ID = ? " +
                "AND CARDNR > ? " +
                "ORDER BY RANGE_INDEX, CARDNR ASC " +
                ") WHERE ROWNUM <= ?";

        return template.query(sql, new SingleColumnRowMapper<>(String.class),
                3, // TODO confirm that this is correct. Note that PROCESS_TRANSPONDERCARDS sets CARDSTATUS_ID to 1
                productGroupId,
                lastUsedCardNumber.orElse("0"),
                numberOfCards);
    }

    @Override
    public List<CardOrder> findForCustomer(Customer customer, CardOrderStatus orderStatus, CardType cardType) {
        String sql = "SELECT * FROM CARDORDER " +
                "WHERE CUSTOMERID = ? " +
                "AND ORDERSTATUS = ? " +
                "AND UPPER(CARDTYPE) = UPPER(?)";

        return template.query(sql, cardOrderRowMapper(),
                customer.getCustomerId(),
                orderStatus.code(),
                cardType.description());
    }

    @Override
    public void processTransponderCard(String cardNumber, Customer customer, CardOrder order, boolean updateMobileWithCard) {
        processTransponderCardsCall.execute(
                customer.getCustomerId(),
                order.getId(),
                cardNumber,
                mutator.get(),
                updateMobileWithCard ? 1 : 0);
    }

    public void validateCardOrder(CardOrder cardOrder) {
        acceptCardOrder(cardOrder.getId(), cardOrder.getPricePerCard(), cardOrder.getAmount());

        Map<String, Object> res = cardOrderValidateCall.execute(cardOrder.getId(), cardOrder.getCardType().code());
        String returnStr = res.get("Return_out").toString();

        Runnable logUnmetExpectation = () -> {
            log.error("Expected {}.{} to return -1 but instead got {}", PACKAGE, CARD_ORDER_UPDATE_PROC, returnStr);
        };

        try {
            if(Integer.parseInt(returnStr) != -1)
                logUnmetExpectation.run();
        } catch(NumberFormatException e) {
            logUnmetExpectation.run();
        }
    }

    @Override
    public List<CardOrder> findByStatusAndType(CardOrderStatus status, CardType type) {
        String sql = "SELECT * FROM CARDORDER " +
                "WHERE ORDERSTATUS = ? " +
                "AND UPPER(CARDTYPE) = UPPER(?)";

        return template.query(sql, cardOrderRowMapper(), status.code(), type.description());
    }

    @Override
    public List<CardOrder> findByStatus(CardOrderStatus status) {
        String sql = "SELECT * FROM CARDORDER WHERE ORDERSTATUS = ? ORDER BY ORDERDATE DESC";

        return template.query(sql, cardOrderRowMapper(), status.code());
    }

    @Override
    public List<CardOrder> findPendingExport(ProductGroup productGroup) {
        String sql = "SELECT * FROM CARDORDER CO " +
                "INNER JOIN CUSTOMER C ON C.CUSTOMERID = CO.CUSTOMERID AND C.PRODUCTGROUP_ID = ? " +
                "WHERE CO.ORDERSTATUS = ? AND CO.EXPORT = 'Y' ORDER BY CO.ORDERDATE DESC";

        return template.query(sql, cardOrderRowMapper(), productGroup.getId(), CardOrderStatus.ACCEPTED.code());
    }

    @Override
    public List<CardOrder> findPendingExport() {
        String sql = "SELECT * FROM CARDORDER CO " +
                "INNER JOIN CUSTOMER C ON C.CUSTOMERID = CO.CUSTOMERID " +
                "WHERE CO.ORDERSTATUS = ? AND CO.EXPORT = 'Y' ORDER BY CO.ORDERDATE DESC";

        return template.query(sql, cardOrderRowMapper(), CardOrderStatus.ACCEPTED.code());
    }

    @Override
    public Optional<CardOrder> findById(long id) {
        String sql = "SELECT * FROM CARDORDER WHERE ORDERID = ?";

        return template.query(sql, cardOrderRowMapper(), id).stream().findFirst();
    }

    @Override
    public void delete(long id) {
        log.info("Deleting order id {}", id);

        template.update("DELETE FROM CARDORDER WHERE ORDERID = ?", id);
    }

    @Override
    public void updateOrderStatus(long cardOrderId, CardOrderStatus status) {
        template.update("UPDATE CARDORDER SET ORDERSTATUS = ? WHERE ORDERID = ?", status.code(), cardOrderId);
    }

    @Override
    public String nextQCardNumber(long customerId) {
        Map<String, Object> res = getQCardNumber.execute(customerId);

        return res.get("qcardNR").toString();
    }

    @Override
    public int transponderCardsAvailableForProductGroup(long productGroupId) {
        String sql = "SELECT COUNT(*) FROM TRANSPONDERCARDPOOL WHERE PRODUCTGROUP_ID = ? AND CARDSTATUS_ID = ?";

        return template.queryForObject(sql, Integer.class, productGroupId, CardStatus.INSTOCK.code());
    }

    @Override
    public int transponderCardsIssuedForProductGroup(long productGroupId, LocalDate since) {
        String sql = "SELECT COUNT(*) " +
                "FROM TRANSPONDERCARDPOOL p " +
                "INNER JOIN TRANSPONDERCARD t ON t.CARDNR = p.CARDNR and p.PRODUCTGROUP_ID = ? " +
                "INNER JOIN CARDORDER c ON c.ORDERID = t.ORDERIDFK AND c.ORDERDATE > ? " +
                "WHERE p.CARDSTATUS_ID = ?";

        return template.queryForObject(sql, Integer.class,
                productGroupId,
                Date.valueOf(since),
                CardStatus.ACTIVE.code());
    }

    private RowMapper<CardOrder> cardOrderRowMapper() {
        return (rs, rowNum) -> {
            CardOrder co = new CardOrder();

            co.setId(rs.getLong("ORDERID"));
            co.setDate(rs.getTimestamp("ORDERDATE"));
            co.setStatus(CardOrderStatus.byCode(rs.getInt("ORDERSTATUS")));
            co.setCustomerId(rs.getLong("CUSTOMERID"));
            co.setCardType(CardType.fromDescription(rs.getString("CARDTYPE")));
            co.setBriefCode(rs.getString("BRIEFCODE"));
            co.setAmount(rs.getInt("AMOUNT"));
            co.setPricePerCard(rs.getDouble("PRICEPERCARD"));
            co.setExport("Y".equals(rs.getString("EXPORT")));

            return co;
        };
    }

    private void acceptCardOrder(double orderId, double pricePerCard, int amount) {
        cardOrderUpdateCall.execute(orderId, CardOrderStatus.ACCEPTED.code(), pricePerCard, amount);
    }

    private void compileJdbcCalls() {
        saveSpecialTarifCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(SAVE_SPECIAL_TARIF_PROC)
                .declareParameters(new SqlParameter("Customer_in", Types.NUMERIC));

        cardOrderUpdateCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(CARD_ORDER_UPDATE_PROC)
                .declareParameters(
                        new SqlParameter("neworderid", Types.NUMERIC),
                        new SqlParameter("neworderstatus", Types.VARCHAR),
                        new SqlParameter("newpricepercard", Types.NUMERIC),
                        new SqlParameter("newamount", Types.INTEGER)
                );

        cardOrderValidateCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(CARD_ORDER_VALIDATE_PROC)
                .declareParameters(
                        new SqlParameter("CardOrderId_in", Types.NUMERIC),
                        new SqlParameter("TypeOfCard_in", Types.VARCHAR),
                        new SqlOutParameter("Return_out", Types.INTEGER)
                );

        processTransponderCardsCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(PROCESS_TRANSPONDERCARDS_PROC)
                .declareParameters(
                        new SqlParameter("p_customerID", Types.NUMERIC),
                        new SqlParameter("p_orderID", Types.NUMERIC),
                        new SqlParameter("p_cardnr", Types.VARCHAR),
                        new SqlParameter("p_mutator", Types.VARCHAR),
                        new SqlParameter("p_updateMobileWithCard", Types.NUMERIC)
                );

        getQCardNumber = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(GET_QCARD_NUMBER_PROC)
                .declareParameters(
                        new SqlParameter("Customer_in", Types.NUMERIC),
                        new SqlOutParameter("qcardNR", Types.VARCHAR)
                );

        saveSpecialTarifCall.compile();
        cardOrderUpdateCall.compile();
        cardOrderValidateCall.compile();
        getQCardNumber.compile();
    }
}
