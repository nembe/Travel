package nl.yellowbrick.validation;

import nl.yellowbrick.domain.Customer;
import org.springframework.validation.Validator;

public interface AccountRegistrationValidator extends Validator {

    @Override
    default boolean supports(Class<?> aClass) {
        return aClass.equals(Customer.class);
    }
}
