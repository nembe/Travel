package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Subscription;

import java.util.Optional;

public interface SubscriptionDao {

    Optional<Subscription> findForCustomer(long customerId);
}
