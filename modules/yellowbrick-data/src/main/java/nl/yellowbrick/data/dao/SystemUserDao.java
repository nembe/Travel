package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Customer;

import java.time.LocalDateTime;

public interface SystemUserDao {

    String createAndStoreUserToken(Customer customer, LocalDateTime validity);
}
