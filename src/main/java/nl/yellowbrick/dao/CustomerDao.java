package nl.yellowbrick.dao;

import nl.yellowbrick.domain.Customer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CustomerDao {

    List<Customer> findAllPendingActivation();

    List<Customer> findAllByFuzzyNameAndDateOfBirth(String firstName, String lastName, Date dateOfBirth);

    void markAsPendingHumanReview(Customer customer);

    void assignNextCustomerNr(Customer customer);

    Optional<String> getRegistrationLocale(Customer customer);
}
