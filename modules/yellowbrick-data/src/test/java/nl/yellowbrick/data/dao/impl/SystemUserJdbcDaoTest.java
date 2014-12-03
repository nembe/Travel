package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.Customer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

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
}
