package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.domain.Subscription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SubscriptionJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    SubscriptionJdbcDao subscriptionDao;

    @Test
    public void returns_subscription_or_empty() {
        assertThat(subscriptionDao.findForCustomer(12345), equalTo(Optional.empty()));

        Subscription sub = new Subscription();
        sub.setId(1);
        sub.setBeginTime(Timestamp.valueOf("2011-01-19 00:00:00.0"));
        sub.setEndTime(null);
        sub.setCustomerId(394744);
        sub.setDescription("WEKELIJKS");
        sub.setTypeId(Subscription.TYPE_WEEKLY);

        Subscription actualSub = subscriptionDao.findForCustomer(394744).get();
        assertThat(actualSub, equalTo(sub));
        assertThat(actualSub.isSubscriptionActive(), is(true));
    }
}
