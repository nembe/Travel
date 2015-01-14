package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
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

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private Mutator mutator;

    private SimpleJdbcCall saveSpecialTarifCall;
    private SimpleJdbcCall cardOrderUpdateCall;
    private SimpleJdbcCall cardOrderValidateCall;
    private SimpleJdbcCall processTransponderCardsCall;

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
    public void processTransponderCard(String cardNumber, Customer customer, boolean updateMobileWithCard) {
        processTransponderCardsCall.execute(
                customer.getCustomerId(),
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

    private RowMapper<CardOrder> cardOrderRowMapper() {
        return new RowMapper<CardOrder>() {
            @Override
            public CardOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
                CardOrder co = new CardOrder();

                co.setId(rs.getLong("ORDERID"));
                co.setDate(rs.getDate("ORDERDATE"));
                co.setStatus(CardOrderStatus.byCode(rs.getInt("ORDERSTATUS")));
                co.setCustomerId(rs.getLong("CUSTOMERID"));
                co.setCardType(CardType.fromDescription(rs.getString("CARDTYPE")));
                co.setBriefCode(rs.getString("BRIEFCODE"));
                co.setAmount(rs.getInt("AMOUNT"));
                co.setPricePerCard(rs.getDouble("PRICEPERCARD"));
                co.setSurcharge(rs.getDouble("SURCHARGE"));
                co.setExport(rs.getString("EXPORT").equals("Y"));
                co.setCardNumber(rs.getString("CARD_NUMBER"));

                return co;
            }
        };
    }

    private void acceptCardOrder(double orderId, double pricePerCard, int amount) {
        cardOrderUpdateCall.execute(orderId, CardOrderStatus.ACCEPTED.code(), pricePerCard, amount);
    }

    @Override
    public void validateCardOrders(Customer customer, CardType... cardTypes) {
        Arrays.asList(cardTypes).forEach((cardType) -> {
            findForCustomer(customer, CardOrderStatus.INSERTED, cardType).forEach(this::validateCardOrder);
        });
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
                        new SqlParameter("p_cardnr", Types.VARCHAR),
                        new SqlParameter("p_mutator", Types.VARCHAR),
                        new SqlParameter("p_updateMobileWithCard", Types.NUMERIC)
                );

        saveSpecialTarifCall.compile();
        cardOrderUpdateCall.compile();
        cardOrderValidateCall.compile();
    }
}
