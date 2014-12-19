package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.dao.SpecialRateTemplateDao;
import nl.yellowbrick.data.domain.SpecialRateTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static nl.yellowbrick.data.domain.SpecialRateTemplate.TRANSACTION_TYPE;

@Component
public class SpecialRateTemplateJdbcDao implements SpecialRateTemplateDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public Optional<SpecialRateTemplate> findForProductGroup(long productGroupId, TRANSACTION_TYPE transactionType) {
        String sql = "SELECT * FROM specialrate_template " +
                "WHERE productgroup_id = ?" +
                "AND transactiontypeidfk = ? " +
                "AND ordinality = 1";

        return template.query(sql, rowMapper(), productGroupId, transactionType.code()).stream().findFirst();
    }

    private RowMapper<SpecialRateTemplate> rowMapper() {
        return new RowMapper<SpecialRateTemplate>() {
            @Override
            public SpecialRateTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
                SpecialRateTemplate template = new SpecialRateTemplate();
                template.setId(rs.getLong("id"));
                template.setProductGroupId(rs.getLong("productgroup_id"));
                template.setBalanceTotal(rs.getLong("balance_total"));
                template.setSpecialRateNumber(rs.getLong("specialrate_number"));
                template.setSpecialRateBase(rs.getString("specialrate_base"));

                return template;
            }
        };
    }
}
