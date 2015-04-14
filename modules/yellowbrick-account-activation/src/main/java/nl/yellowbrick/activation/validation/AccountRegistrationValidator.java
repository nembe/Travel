package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.validation.ClassValidator;

public abstract class AccountRegistrationValidator extends ClassValidator<Customer> {

    protected AccountRegistrationValidator() {
        super(Customer.class);
    }
}
