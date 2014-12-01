package nl.yellowbrick.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class CustomerMembershipValidator implements AccountRegistrationValidator {

    @Override
    public void validate(Object o, Errors errors) {
        // TODO implement
    }
}
