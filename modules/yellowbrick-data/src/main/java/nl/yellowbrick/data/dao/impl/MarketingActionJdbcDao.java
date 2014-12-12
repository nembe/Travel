package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.dao.MarketingActionDao;
import nl.yellowbrick.data.domain.MarketingAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

@Component
public class MarketingActionJdbcDao implements MarketingActionDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public Optional<MarketingAction> findByActionCode(String actionCode) {
        String sql = "SELECT * FROM MARKETINGACTION WHERE ACTIONCODE = ? AND ROWNUM <= 1";

        return template.query(sql, beanRowMapper(), actionCode).stream().findFirst();
    }

    private RowMapper<MarketingAction> beanRowMapper() {
        return new RowMapper<MarketingAction>() {
            @Override
            public MarketingAction mapRow(ResultSet rs, int rowNum) throws SQLException {
                String actionCode = rs.getString("ACTIONCODE");
                int registrationCostEuroCent = Math.round(rs.getFloat("REGISTRATION_COST") * 100);
                Date validFrom = rs.getDate("VALID_FROM");
                Date validTo = rs.getDate("VALID_TO");

                return new MarketingAction(actionCode, registrationCostEuroCent, validFrom, validTo);
            }
        };
    }
}
