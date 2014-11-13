package nl.yellowbrick.dao.impl;

import nl.yellowbrick.dao.SystemUserDao;
import nl.yellowbrick.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
public class SystemUserJdbcDao implements SystemUserDao {

    private static final ZoneId TIMEZONE = ZoneId.of("Europe/Amsterdam");

    @Autowired
    private JdbcTemplate template;

    @Override
    public String createAndStoreUserToken(Customer customer, LocalDateTime validity) {
        String sql = "UPDATE SYSTEMUSER SET token = ?, token_date = ? " +
                "WHERE account_type = ? " +
                "AND customeridfk = ?";
        String token = UUID.randomUUID().toString();
        Date tokenDate = Date.from(validity.atZone(TIMEZONE).toInstant());

        template.update(sql, token, tokenDate, 0, customer.getCustomerId());

        return token;
    }
}
