package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.dao.WelcomeLetterSettingsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WelcomeLetterSettingsJdbcDao implements WelcomeLetterSettingsDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public Optional<Long> latestExportedCustomer() {
        String sql = "SELECT LATEST_CUSTOMER_ID FROM WELCOME_LETTER_SETTINGS WHERE ROWNUM <= 1";

        try {
            return Optional.ofNullable(template.queryForObject(sql, Long.class));
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateLatestExportedCustomer(long customerId) {
        // note: avoided using MERGE statement as syntax is not totally compatible with in memory database
        if(template.update("UPDATE WELCOME_LETTER_SETTINGS SET LATEST_CUSTOMER_ID = ?", customerId) == 0)
            template.update("INSERT INTO WELCOME_LETTER_SETTINGS VALUES(?)", customerId);
    }
}
