package nl.yellowbrick.data.dao.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.Map;

@Component
public class CardOrderJdbcDao implements CardOrderDao, InitializingBean {

    private static final String PACKAGE = "WEBAPP";
    private static final String SAVE_SPECIAL_TARIF_PROC = "saveSignupSpecialRate";
    private static final String CARD_ORDER_UPDATE_PROC = "cardorderUpdate";
    private static final String CARD_ORDER_VALIDATE_PROC = "CardOrderValidate";

    private static final String PROSPECT_CARD_TYPE = "Hoesje";

    @Autowired
    private JdbcTemplate template;

    private SimpleJdbcCall saveSpecialTarifCall;
    private SimpleJdbcCall cardOrderUpdateCall;
    private SimpleJdbcCall cardOrderValidateCall;

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
    public void validateCardOrders(Customer customer) {
        String sql = Joiner.on(' ').join(ImmutableList.of(
                "SELECT co.ORDERID, co.ORDERDATE, c.BUSINESS, c.LASTNAME, co.CARDTYPE, co.AMOUNT, co.PRICEPERCARD",
                "FROM CARDORDER co",
                "INNER JOIN CUSTOMER c ON c.CUSTOMERID = co.CUSTOMERID",
                "WHERE co.ORDERSTATUS = ?",
                "AND co.CARDTYPE != ? ",
                "AND co.CUSTOMERID = ? "
        ));

        RowCallbackHandler processCardOrder = (rs) -> {
            double pricePerCard = rs.getDouble("PricePerCard");
            double orderid = rs.getDouble("OrderId");

            int amount = new Double(rs.getDouble("Amount")).intValue();

            saveAndAcceptCardOrder(orderid, pricePerCard, amount);

            CardType cardType = CardType.fromDescription(rs.getString("CardType"));
            validateCardOrder(customer.getCustomerId(), pricePerCard, amount, cardType.code());
        };

        template.query(sql, processCardOrder,
                CardOrderStatus.INSERTED.code(), PROSPECT_CARD_TYPE, customer.getCustomerId());
    }

    private void saveAndAcceptCardOrder(double orderId, double pricePerCard, int amount) {
        cardOrderUpdateCall.execute(orderId, CardOrderStatus.ACCEPTED.code(), pricePerCard, amount);
    }

    private void validateCardOrder(long customerId, double pricePerCard, int amount, String typeOfCard) {
        Map<String, Object> res = cardOrderValidateCall.execute(customerId, pricePerCard, amount, typeOfCard);
        String returnStr = res.get("Return_out").toString();

        Runnable logUnmetExpectation = () -> {
            log.error(String.format("Expected %s.%s to return -1 but instead got %s",
                    PACKAGE, CARD_ORDER_UPDATE_PROC, returnStr));
        };

        try {
            if(Integer.parseInt(returnStr) != -1)
                logUnmetExpectation.run();
        } catch(NumberFormatException e) {
            logUnmetExpectation.run();
        }
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
                        new SqlParameter("CustomerId_in", Types.NUMERIC),
                        new SqlParameter("CardFee_in", Types.NUMERIC),
                        new SqlParameter("NumberOfTCards_in", Types.NUMERIC),
                        new SqlParameter("TypeOfCard_in", Types.VARCHAR),
                        new SqlOutParameter("Return_out", Types.INTEGER)
                );

        saveSpecialTarifCall.compile();
        cardOrderUpdateCall.compile();
        cardOrderValidateCall.compile();
    }
}
