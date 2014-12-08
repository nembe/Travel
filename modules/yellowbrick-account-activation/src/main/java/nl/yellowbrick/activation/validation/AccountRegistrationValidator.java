package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.domain.Customer;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class AccountRegistrationValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(Customer.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        if(!(o instanceof Customer))
            throw new IllegalArgumentException(String.format("can only validate Customer objects, got %s instead",
                    o.getClass().getCanonicalName()));

        validate((Customer) o, errors);
    }

    protected abstract void validate(Customer customer, Errors errors);
}
