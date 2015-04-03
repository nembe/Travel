package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.UserAccountType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import static nl.yellowbrick.data.domain.UserAccountType.RESTRICTED_SUBACCOUNT;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SystemUserJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    SystemUserJdbcDao systemUserDao;

    @Autowired
    DbHelper db;

    @Before
    public void setUserAccountType() {
        db.accept((template) -> template.update("UPDATE SYSTEMUSER SET account_type = 0"));
    }

    @Test
    public void sets_token_and_token_date_for_customer() {
        Customer cust = new Customer();
        cust.setCustomerId(2364);

        LocalDateTime validity = LocalDateTime.of(2014, 11, 11, 22, 52);
        Date valdityTime = Date.from(
                validity.atZone(ZoneId.of("Europe/Amsterdam")).toInstant()
        );

        systemUserDao.createAndStoreUserToken(cust, validity);

        db.accept((template) -> {
            Map<String, Object> res = template.queryForMap("SELECT * FROM SYSTEMUSER WHERE customeridfk = ?", 2364);

            assertThat(res.get("token").toString(), not(isEmptyOrNullString()));
            assertThat(((Timestamp)res.get("token_date")).getTime(), equalTo(valdityTime.getTime()));
        });
    }

    @Test
    public void creates_app_user() {
        TransponderCard card = testCard();

        systemUserDao.createAppUser(card, "user", "pass", RESTRICTED_SUBACCOUNT);

        db.accept((t) -> {
            Map<String, Object> rec = fetchLatestUser(t);

            assertThat(rec.get("systemuserid"), notNullValue());
            assertThat(rec.get("username"), is("user"));
            assertThat(rec.get("password"), is("pass"));
            assertThat(rec.get("mutator"), is("TEST MUTATOR"));
            assertThat(rec.get("account_type").toString(), equalTo(String.valueOf(RESTRICTED_SUBACCOUNT.value())));
            assertThat(Long.valueOf(rec.get("customeridfk").toString()), is(card.getCustomerId()));
            assertThat(Long.valueOf(rec.get("transpondercardidfk").toString()), is(card.getId()));
        });
    }

    @Test
    public void deletes_user_by_card_id() {
        db.accept(t -> {
            assertThat(t.update("update systemuser set transpondercardidfk = 123"), greaterThan(0));

            systemUserDao.deleteAppUserByCardId(123);

            assertThat(userCount(t), is(0));
        });
    }

    @Test
    public void checks_existence_of_user_associated_with_card() {
        assertThat(systemUserDao.existsAppUserForCard(123), is(false));

        // create some user ...
        systemUserDao.createAppUser(testCard(), "user", "pass", UserAccountType.APPLOGIN);

        // and re-assert
        assertThat(systemUserDao.existsAppUserForCard(123), is(true));
    }

    private TransponderCard testCard() {
        TransponderCard card = new TransponderCard();
        card.setId(123l);
        card.setCustomerId(456l);

        return card;
    }

    private Map<String, Object> fetchLatestUser(JdbcTemplate template) {
         return template.queryForMap("select * from " +
                 "(select * from systemuser order by systemuserid desc) " +
                 "where rownum = 1");
    }

    private int userCount(JdbcTemplate template) {
        return template.queryForObject("select count(*) from systemuser", Integer.class);
    }
}
