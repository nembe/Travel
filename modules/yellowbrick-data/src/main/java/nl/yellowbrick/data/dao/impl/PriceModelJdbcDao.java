package nl.yellowbrick.data.dao.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import nl.yellowbrick.data.dao.PriceModelDao;
import nl.yellowbrick.data.domain.PriceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class PriceModelJdbcDao implements PriceModelDao {

    @Autowired
    private JdbcTemplate template;

    private Logger log = LoggerFactory.getLogger(PriceModelDao.class);

    public Optional<PriceModel> findForCustomer(long customerId) {
        try {
            long subGroupId = getSubgroupIdForCustomer(customerId);
            PriceModel model = getPriceModelForSubGroup(subGroupId);
            return Optional.of(model);
        } catch (Exception e) {
            log.error("Failed to find PriceModel for customer ID: " + customerId, e);
            return Optional.empty();
        }
    }

    private PriceModel getPriceModelForSubGroup(long subGroupId) throws SQLException {
        String sql = Joiner.on(' ').join(ImmutableList.of(
                "SELECT p.*, ps.default_issue_physical_card FROM PRICEMODEL p",
                "INNER JOIN PRODUCT_SUBGROUP_PRICEMODEL sm ON sm.pricemodel_id = p.id",
                "INNER JOIN PRODUCT_SUBGROUP ps ON ps.id = sm.product_subgroup_id",
                "WHERE sm.product_subgroup_id = ?",
                "AND sm.APPLY_DATE = (",
                "SELECT MAX(APPLY_DATE) FROM PRODUCT_SUBGROUP_PRICEMODEL",
                "WHERE PRODUCT_SUBGROUP_ID = ?",
                "AND apply_date < SYSDATE",
                ")"
        ));

        List<PriceModel> models = template.query(sql, new Object[]{subGroupId, subGroupId}, this::mapToPriceModel);

        if(models.size() > 1) {
            throw new IllegalStateException("Expected to get no more than 1 PriceModel but got " + models.size());
        }

        return models.get(0);
    }

    private long getSubgroupIdForCustomer(long customerId) throws SQLException {
        String sql = Joiner.on(' ').join(ImmutableList.of(
                "SELECT ps.id FROM PRODUCT_SUBGROUP ps",
                "INNER JOIN CUSTOMER c ON c.productgroup_id = ps.product_group_id AND c.business = ps.business",
                "INNER JOIN PRODUCT_GROUP pg ON pg.id = c.productgroup_id",
                "WHERE c.customerid = ?"
        ));

        return template.queryForObject(sql, Long.class, customerId);
    }

    private PriceModel mapToPriceModel(ResultSet rs, int rowNum) throws SQLException {
        PriceModel model = new PriceModel();

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
        model.setInitVehicleProfileCost(rs.getInt("INIT_VEHICLE_PROFILE_COST"));
        model.setVehicleProfileCost(rs.getInt("VEHICLE_PROFILE_COST"));
        model.setDefaultIssuePhysicalCard(rs.getString("DEFAULT_ISSUE_PHYSICAL_CARD").equals("Y"));

        return model;
    }
}
