package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.dao.SubscriptionDao;
import nl.yellowbrick.data.domain.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubscriptionJdbcDao implements SubscriptionDao {

    @Autowired
    private JdbcTemplate template;


    @Override
    public Optional<Subscription> findForCustomer(long customerId) {
        String sql = "SELECT s.id, s.customer_id, s.begin_time, s.end_time, s.subscription_type_id type_id, t.description " +
                "FROM subscription s, subscription_type t " +
                "WHERE customer_id = ? " +
                "AND s.subscription_type_id = t.id " +
                "AND ROWNUM <= 1";

        return template.query(sql, new BeanPropertyRowMapper(Subscription.class), customerId).stream().findFirst();
    }
}
