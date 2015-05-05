package nl.yellowbrick.activation.service;

import nl.yellowbrick.activation.validation.AccountRegistrationValidator;
import nl.yellowbrick.activation.validation.UnboundErrors;
import nl.yellowbrick.data.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Responsible for validating newly registered accounts
 */
@Component
public class AccountValidationService {

    private final AccountRegistrationValidator[] accountRegistrationValidators;

    @Autowired
    public AccountValidationService(AccountRegistrationValidator... accountRegistrationValidators) {
        this.accountRegistrationValidators = accountRegistrationValidators;
    }

    public Errors validate(Customer customer, String targetName) {
        return doValidate(customer, new UnboundErrors(customer, targetName));
    }

    public Errors validate(Customer customer) {
        return doValidate(customer, new UnboundErrors(customer));
    }

    private Errors doValidate(Customer customer, Errors errors) {
        for(Validator validator: accountRegistrationValidators) {
            ValidationUtils.invokeValidator(validator, customer, errors);
        }

        return errors;
    }
}
