package nl.yellowbrick.activation.validation;


import nl.yellowbrick.data.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;

/**
 * Looks for the word "TEST" leading or trailing any of the customer properties.
 * That would indicate this was likely an account created by testers and so shouldn't be automatically
 * activated as it's likely it will be discarded.
 */
@Component
public class NonTestAccountValidator extends AccountRegistrationValidator {

    private static final Logger log = LoggerFactory.getLogger(NonTestAccountValidator.class);

    @Override
    public void validate(Customer customer, Errors errors) {
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(Customer.class);

            Arrays.asList(beanInfo.getPropertyDescriptors()).forEach((property) -> {
                validateProperty(property, customer, errors);
            });

        } catch (Exception e) {
            log.error("an error occurred during automatic validation: {}", e.getMessage());
            errors.reject("errors.validation.failure");
        }
    }

    private void validateProperty(PropertyDescriptor prop, Customer customer, Errors errors) {
        try {
            Object propVal = prop.getReadMethod().invoke(customer);
            if(propVal == null)
                return;

            String propText = propVal.toString().toLowerCase();

            if(propText.startsWith("test") || propText.endsWith("test"))
                errors.rejectValue(prop.getName(), "errors.test.data");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
