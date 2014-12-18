package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.dao.DirectDebitDetailsDao;
import nl.yellowbrick.data.domain.DirectDebitDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Component
public class DirectDebitDetailsJdbcDao implements DirectDebitDetailsDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public Optional<DirectDebitDetails> findForCustomer(long customerId) {
        String sql = "SELECT * FROM PAYMENT_DIRECT_DEBIT_DETAILS WHERE CUSTOMERID = ? AND ROWNUM <= 1";

        return template.query(sql, beanRowMapper(), customerId).stream().findFirst();
    }

    private RowMapper<DirectDebitDetails> beanRowMapper() {
        return new RowMapper<DirectDebitDetails>() {
            @Override
            public DirectDebitDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                DirectDebitDetails details = new DirectDebitDetails();

                details.setId(rs.getLong("ID"));
                details.setSepaNumber(rs.getString("SEPANUMBER"));
                details.setBic(rs.getString("BIC"));
                details.setVerified(rs.getString("VERIFIED").equals("Y"));

                return details;
            }
        };
    }
}
