package nl.yellowbrick.dao;

import nl.yellowbrick.domain.Customer;

import java.time.LocalDateTime;

public interface SystemUserDao {

    String createAndStoreUserToken(Customer customer, LocalDateTime validity);
}
