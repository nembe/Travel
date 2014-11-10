package nl.yellowbrick.dao.impl;

import nl.yellowbrick.dao.CardOrderDao;
import nl.yellowbrick.domain.CardOrder;
import nl.yellowbrick.domain.Customer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.util.Date;

@Component
public class CardOrderJdbcDao implements CardOrderDao, InitializingBean {

    private static final String PACKAGE = "WEBAPP";
    private static final String SAVE_SPECIAL_TARIF_PROC = "saveSignupSpecialRate";

    @Autowired
    private JdbcTemplate template;

    private SimpleJdbcCall saveSpecialTarifCall;
    private Log log = LogFactory.getLog(CardOrderJdbcDao.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        compileJdbcCall();
    }

    @Override
    public void saveSpecialTarifIfApplicable(Customer customer) {
        saveSpecialTarifCall.execute(customer.getCustomerId());
    }

    @Override
    public void validateCardOrders(Customer customer) {
        Object[] param = new Object[]{CardOrder.STATUS_INSERTED, customer.getCustomerId()};
        double pricepcard = -1.0;

        String sql = "SELECT co.ORDERID, co.ORDERDATE, c.BUSINESS, c.LASTNAME, co.CARDTYPE, co.AMOUNT , co.PRICEPERCARD "
                + "FROM CARDORDER co, CUSTOMER c  "
                + "WHERE co.ORDERSTATUS = ? "
                + "AND co.CUSTOMERID = c.CUSTOMERID "
                + "AND co.CARDTYPE != 'Hoesje' "
                + "AND co.CUSTOMERID = ? ";

        CachedRowSet rs = null;
        try {
            rs = ExecuteSql(sql, param);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            while (rs.next()) {
                pricepcard = rs.getDouble("pricepercard");
                double orderid = rs.getDouble("orderid");
                int amount = new Double(rs.getDouble("amount")).intValue();
                if (saveAndAcceptCardOrder(orderid, pricepcard, amount)) {
                    String cardTypeCode = rs.getString("CARDTYPE");
                    //ticket #115: cardType is a description, must be a code
                    cardTypeCode = (cardTypeCode.toLowerCase()
                            .startsWith("transponder")) ? ""
                            + CardOrder.TRANSPONDER_CARD : (cardTypeCode
                            .toLowerCase().startsWith("rtp")) ? ""
                            + CardOrder.RTP_CARD : (cardTypeCode.toLowerCase().startsWith("qcard")) ?
                            "" + CardOrder.QPARK_CARD :
                            "" + CardOrder.UNKNOWN_CARD;
                    cardOrderValidate(customer.getCustomerId(), pricepcard, amount, cardTypeCode);
                }
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    /**
     * This method updates the card orders that were validated.
     *
     * @return true if update was successful, false otherwise
     */
    private boolean saveAndAcceptCardOrder(double orderId, double pricepercard, int amount) {
        PreparedStatement prep = null;

        boolean result = false;

        try {
            String sql = "{call WEBAPP.cardorderUpdate( ?, ?, ?, ? )}";
            prep = getConnection().prepareStatement(sql);
            prep.setDouble(1, orderId);
            prep.setString(2, CardOrder.STATUS_ACCEPTED + "");
            prep.setDouble(3, pricepercard);
            prep.setInt(4, amount);
            prep.execute();
            close(prep, null);
            result = true;

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        closeConnection();
        return result;
    }

    /**
     * This method runs CreateFinancialTransaction on an ordered card.
     *
     * @return true if transaction was successful, false otherwise
     */
    public boolean cardOrderValidate(long customerId, double pricepercard,
                                     int amount, String typeOfCard) {

        CallableStatement prep = null;

        boolean retval = false;
        try {
            String sql = "{call WEBAPP.CardOrderValidate( ?, ?, ?, ?, ? )}";
            prep = getConnection().prepareCall(sql);
            prep.setDouble(1, customerId);
            prep.setDouble(2, pricepercard);
            prep.setInt(3, amount);
            prep.setString(4, typeOfCard);
            prep.registerOutParameter(5, Types.INTEGER);
            prep.executeUpdate();
            if (prep.getInt(5) == -1) {
                retval = true;
            } else {
                log.debug("CardOrderSQLHelper.cardOrderValidate return value: " + prep.getInt(5));
            }
            close(prep, null);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            log.error("CardOrderSQLHelper.cardOrderValidate has caused and SQLException." + sqle.getMessage());
            retval = false;
        }
        closeConnection();
        return retval;
    }

    private void compileJdbcCall() {
        saveSpecialTarifCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(SAVE_SPECIAL_TARIF_PROC)
                .declareParameters(new SqlParameter("Customer_in", Types.NUMERIC));

        saveSpecialTarifCall.compile();
    }

    public CachedRowSet ExecuteSql(String sqlStr, Object[] parameters) throws SQLException {
        final String sql = sqlStr;
        PreparedStatement prep = null;
        ResultSet rs = null;
        CachedRowSet result;
        try {
            result = (CachedRowSet) Class.forName("com.sun.rowset.CachedRowSetImpl").newInstance();
        } catch (Exception e) {
            return null;
        }

        try {
            prep = getConnection().prepareStatement(sql);
            int i = 1;
            if (parameters != null) {
                for (Object p : parameters) {
                    if (null == p) {
                        break;
                    }
                    if (p instanceof String) {
                        String d = (String) p;
                        prep.setString(i, d);
                    } else if (p instanceof Long) {
                        Long d = (Long) p;
                        prep.setLong(i, d);
                    } else if (p instanceof Date) {
                        Date d = (Date) p;
                        java.sql.Date dd = new java.sql.Date(d.getTime());
                        prep.setDate(i, dd);
                    } else {
                        prep.setObject(i, p);
                    }
                    i++;
                }
            }
            rs = prep.executeQuery();
            result.populate(rs);

            close(prep, rs);
            closeConnection();
        } catch (SQLException e) {
            closeConnection();
            throw e;
        } catch (Exception e) {
            closeConnection();
        }
        return result;
    }

    public void close(Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            // ignore
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }

    public void closeConnection() {
    }

    protected java.sql.Connection getConnection() throws SQLException {
        return template.getDataSource().getConnection();
    }
}
