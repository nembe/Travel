package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.domain.MarketingAction;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MarketingActionJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    MarketingActionJdbcDao marketingActionDao;

    @Test
    public void converts_registration_cost_from_euros_to_cents() {
        MarketingAction code = marketingActionDao.findByActionCode("AFCIJBURG").get();

        // main assertion
        assertThat(code.getRegistrationCost(), equalTo(10 * 100));

        assertThat(code.getActionCode(), equalTo("AFCIJBURG"));
        assertThat(code.getValidFrom(), equalTo(Date.valueOf("2012-09-24")));
        assertThat(code.getValidTo(), equalTo(Date.valueOf("2013-09-30")));
        assertThat(code.isCurrentlyValid(), is(false));
    }

    @Test
    public void returns_empty_if_action_code_not_found() {
        assertThat(marketingActionDao.findByActionCode("BOOYAKASHA"), equalTo(Optional.empty()));
    }

    @Test
    public void also_returns_still_valid_actions() {
        MarketingAction code = marketingActionDao.findByActionCode("FACEBOOK").get();

        assertThat(code.isCurrentlyValid(), is(true));
    }
}
