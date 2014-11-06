package nl.yellowbrick.dao.impl;

import nl.yellowbrick.dao.PriceModelDao;
import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.domain.PriceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.Optional;

@Component
public class PriceModelJdbcDao implements PriceModelDao {

    @Autowired
    private JdbcTemplate template;

    private Connection connection;

    public Optional<PriceModel> findForCustomer(Customer customer) {
        try {
            long subGroupId = getSubgroupIdForCustomer(customer.getCustomerId());
            PriceModel model = getPriceModelForSubGroup(subGroupId);
            return Optional.of(model);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private PriceModel getPriceModelForSubGroup(long subGroupId) throws SQLException {
        PriceModel model = new PriceModel();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            Connection conn = getConnection();
            String sql = "select p.* from pricemodel p, PRODUCT_SUBGROUP_PRICEMODEL sm"
                    + " WHERE sm.PRODUCT_SUBGROUP_ID = ?" + " AND sm.PRICEMODEL_ID = p.ID" + " AND sm.APPLY_DATE = "
                    + " (select max(APPLY_DATE) from product_subgroup_pricemodel "
                    + " WHERE PRODUCT_SUBGROUP_ID = ? AND apply_date < SYSDATE)";

            prep = conn.prepareStatement(sql);
            prep.setLong(1, subGroupId);
            prep.setLong(2, subGroupId);
            rs = prep.executeQuery();

            while (rs.next()) {
                model.setId(rs.getLong("ID"));
                model.setDescription(rs.getString("DESCRIPTION"));
                model.setSubscriptionCostEuroCents(rs.getInt("SUBSCRIPTION_COST"));
                model.setTransactionCostMaximumEuroCents(rs.getInt("TRANS_COSTS_MAX"));
                model.setTransactionCostMinimumEuroCents(rs.getInt("TRANS_COSTS_MIN"));
                model.setTransactionCostPercentage(rs.getInt("TRANS_COSTS_PERC"));
                model.setKortingenGeldigheidsduur(rs.getInt("SIGNUP_ACTION_EXP_IN_DAYS"));
                model.setRegistratiekosten(rs.getInt("REG_COSTS"));
                model.setSleevePrice(rs.getInt("SLEEVE_PRICE"));
                model.setMaxAmountCards(rs.getInt("MAX_AMNT_CARDS"));
                model.setInitRtpCardCost(rs.getInt("INIT_RTPCARD_COSTS"));
                model.setRtpCardCost(rs.getInt("RTPCARD_COSTS"));
                model.setInitTranspCardCost(rs.getInt("INIT_TCARD_COSTS"));
                model.setTranspCardCost(rs.getInt("TRANSPCARD_COSTS"));
                model.setQparkPassCost(rs.getInt("QPARK_PASS_COSTS"));
            }
        } finally {
            close(prep, rs);
            closeConnection();
        }

        return model;
    }

    private long getSubgroupIdForCustomer(long customerId) throws SQLException {
        long subGroupId = -1;
        PreparedStatement prep1 = null;
        ResultSet rs1 = null;
        try {
            Connection conn = getConnection();
            String sql = "select ps.ID from product_subgroup ps, product_group pg, customer c "
                    + " WHERE pg.ID = c.PRODUCTGROUP_ID " + " AND ps.PRODUCT_GROUP_ID = c.PRODUCTGROUP_ID "
                    + " AND c.BUSINESS = ps.BUSINESS " + " AND c.CUSTOMERID = ?";
            prep1 = conn.prepareStatement(sql);
            prep1.setLong(1, customerId);
            rs1 = prep1.executeQuery();
            if (rs1.next()) {
                subGroupId = rs1.getLong("ID");
            }
        } finally {
            close(prep1, rs1);
            closeConnection();
        }
        return subGroupId;
    }

    public Connection getConnection() {
        if(connection == null) {
            try {
                connection = template.getDataSource().getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return connection;
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
        if (getConnection() != null) {
            try {
                getConnection().close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }
}
