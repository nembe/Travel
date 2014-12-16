package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.AddressType;
import nl.yellowbrick.data.domain.CustomerAddress;

import java.util.Optional;

public interface CustomerAddressDao {

    Optional<CustomerAddress> findByCustomerId(long customerId);

    void savePrivateCustomerAddress(long customerId, CustomerAddress address);

    void saveBusinessCustomerAddress(long customerId, CustomerAddress address, AddressType addressType);
}
