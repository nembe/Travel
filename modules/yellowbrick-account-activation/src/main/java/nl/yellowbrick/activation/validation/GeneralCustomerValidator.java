package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.domain.Customer;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class GeneralCustomerValidator extends AccountRegistrationValidator {

    @Override
    protected void validate(Customer customer, Errors errors) {
        if(customer.getDateOfBirth() == null) {
            errors.rejectValue("dateOfBirth", "errors.missing");
            return;
        }

        boolean youngerThanSixteen = sixteenYearsAgo().isBefore(Instant.ofEpochMilli(customer.getDateOfBirth().getTime()));

        if(youngerThanSixteen) {
            errors.rejectValue("dateOfBirth", "errors.too.young");
        }
    }

    private Instant sixteenYearsAgo() {
        return LocalDate.now().minusYears(16).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
