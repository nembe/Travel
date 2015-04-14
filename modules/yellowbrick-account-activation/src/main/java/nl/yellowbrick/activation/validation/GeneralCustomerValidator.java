package nl.yellowbrick.activation.validation;

import com.google.common.base.Strings;
import nl.yellowbrick.data.dao.MarketingActionDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.MarketingAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Component
public class GeneralCustomerValidator extends AccountRegistrationValidator {

    @Autowired
    private MarketingActionDao marketingActionDao;

    @Override
    protected void doValidate(Customer customer, Errors errors) {
        if(customer.getDateOfBirth() == null) {
            errors.rejectValue("dateOfBirth", "errors.missing");
            return;
        }

        if(youngerThanSixteen(customer.getDateOfBirth())) {
            errors.rejectValue("dateOfBirth", "errors.too.young");
        }

        if(!Strings.isNullOrEmpty(customer.getActionCode()) && !validActionCode(customer.getActionCode())) {
            errors.rejectValue("actionCode", "errors.invalid.action.code");
        }
    }

    private boolean validActionCode(String actionCode) {
        return marketingActionDao.findByActionCode(actionCode)
                .map(MarketingAction::isCurrentlyValid)
                .orElse(false);
    }

    private boolean youngerThanSixteen(Date dateOfBirth) {
        Instant sixteenYearsAgo = LocalDate.now().minusYears(16).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return sixteenYearsAgo.isBefore(Instant.ofEpochMilli(dateOfBirth.getTime()));
    }
}
