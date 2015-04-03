package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.SystemUserDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.UserAccountType;
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

    @Autowired
    private Mutator mutator;

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

    @Override
    public void createAppUser(TransponderCard card, String username, String password, UserAccountType accountType) {
        // TODO use named param template here for clarity ?
        String sql = "insert into SYSTEMUSER " +
            "(systemuserid, username, password, mutator, account_type, customeridfk, transpondercardidfk) " +
            "values(SYSTEMUSER_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?)";

        template.update(sql, username, password, mutator.get(), accountType.value(), card.getCustomerId(), card.getId());
    }

    @Override
    public void deleteAppUserByCardId(long transponderCardId) {
        String sql = "delete from SYSTEMUSER where transpondercardidfk = ?";

        template.update(sql, transponderCardId);
    }

    @Override
    public boolean existsAppUserForCard(long transponderCardId) {
        String sql = "select case " +
                "when exists (select * from systemuser where transpondercardidfk = ?) then 1 " +
                "else 0 end from dual";

        return template.queryForObject(sql, Boolean.class, transponderCardId);
    }
}
