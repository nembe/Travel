package nl.yellowbrick.activation.validation;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.List;

/**
 * Attempts to determine whether new customer account might be a duplicate of existing account
 */
@Component
public class UniquenessValidator extends AccountRegistrationValidator {

    @Autowired
    private CustomerDao customerDao;

    @Override
    protected void validate(Customer customer, Errors errors) {
        if(existsByNameAndDob(customer)) {
            errors.reject("errors.duplicate");
        }

        if (existsByEmail(customer.getEmail())) {
            errors.rejectValue("email", "errors.duplicate");
        }
    }

    private boolean existsByEmail(String email) {
        return Iterables.tryFind(customerDao.findAllByEmail(email), activatedCustomers()).isPresent();
    }

    private boolean existsByNameAndDob(Customer customer) {
        List<Customer> customers = customerDao.findAllByFuzzyNameAndDateOfBirth(
                customer.getFirstName(),
                customer.getLastName(),
                customer.getDateOfBirth());

        Optional<Customer> match = Iterables.tryFind(customers, activatedCustomers());

        return match.isPresent();
    }

    private Predicate<Customer> activatedCustomers() {
        return (cust) -> cust.getCustomerStatusIdfk() != CustomerStatus.ACTIVATION_FAILED.code()
                && cust.getCustomerStatusIdfk() != CustomerStatus.REGISTERED.code();
    }
}
