package nl.yellowbrick.dao;

import nl.yellowbrick.domain.Customer;

import java.util.List;

public interface CustomerDao {

    List<Customer> findAllPendingActivation();
}
