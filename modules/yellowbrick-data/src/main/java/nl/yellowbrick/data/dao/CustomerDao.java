package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CustomerDao {

    Optional<Customer> findById(long id);

    Optional<Customer> findByCustomerNr(String customerNr);

    List<Customer> findAllPendingActivation();

    List<Customer> findAllByFuzzyNameAndDateOfBirth(String firstName, String lastName, Date dateOfBirth);

    List<Customer> findAllByFuzzyName(String firstName, String lastName);

    List<Customer> findAllByEmail(String email);

    List<Customer> findAllByMobile(String mobile);

    void markAsPendingHumanReview(Customer customer);

    void assignNextCustomerNr(Customer customer);

    Optional<String> getRegistrationLocale(Customer customer);

    void savePrivateCustomer(Customer customer);

    void saveBusinessCustomer(Customer customer);

    List<BusinessIdentifier> getBusinessIdentifiers(long customerId);

    void updateBusinessIdentifier(BusinessIdentifier businessIdentifier);

    List<Customer> findAllByBusinessIdentifier(String label, String value);

    List<String> getMobileNumbers(long customerId);

    void deleteAllCustomerData(long customerId);
}
