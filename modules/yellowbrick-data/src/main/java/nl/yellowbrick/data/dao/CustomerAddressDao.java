package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.CustomerAddress;

import java.util.Optional;

public interface CustomerAddressDao {

    Optional<CustomerAddress> findByCustomerId(long customerId);

    void savePrivateCustomerAddress(long customerId, CustomerAddress address);
}
