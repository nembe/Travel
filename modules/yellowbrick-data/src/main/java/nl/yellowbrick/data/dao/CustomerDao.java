package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Customer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CustomerDao {

    List<Customer> findAllPendingActivation();

    List<Customer> findAllByFuzzyNameAndDateOfBirth(String firstName, String lastName, Date dateOfBirth);

    List<Customer> findAllByEmail(String email);

    void markAsPendingHumanReview(Customer customer);

    void assignNextCustomerNr(Customer customer);

    Optional<String> getRegistrationLocale(Customer customer);

    void savePrivateCustomer(Customer customer);

    void saveBusinessCustomer(Customer customer);
}
