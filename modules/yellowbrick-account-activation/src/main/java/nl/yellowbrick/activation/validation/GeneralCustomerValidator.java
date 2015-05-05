package nl.yellowbrick.activation.validation;

import com.google.common.base.Strings;
import nl.yellowbrick.data.dao.CustomerDao;
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

    @Autowired
    private CustomerDao customerDao;

    @Override
    protected void doValidate(Customer customer, Errors errors) {
        validateAge(customer, errors);
        validateActionCode(customer, errors);
        validateRegistrationMobile(customer, errors);
    }

    private void validateRegistrationMobile(Customer customer, Errors errors) {
        if(customer.getFirstCardMobile() == null)
            return;

        String mobile = customer.getFirstCardMobile().replaceAll("\\s", "");

        customerDao.findAllByMobile(mobile)
                .stream()
                .filter(it -> it.getCustomerId() != customer.getCustomerId())
                .findAny()
                .ifPresent(it -> errors.rejectValue("firstCardMobile", "errors.duplicate"));
    }

    private void validateActionCode(Customer customer, Errors errors) {
        if(Strings.isNullOrEmpty(customer.getActionCode()))
            return;

        boolean validActionCode = marketingActionDao.findByActionCode(customer.getActionCode())
                .map(MarketingAction::isCurrentlyValid)
                .orElse(false);

        if(!validActionCode)
            errors.rejectValue("actionCode", "errors.invalid.action.code");
    }

    private void validateAge(Customer customer, Errors errors) {
        if(customer.getDateOfBirth() == null) {
            errors.rejectValue("dateOfBirth", "errors.missing");
            return;
        }

        if(youngerThanSixteen(customer.getDateOfBirth()))
            errors.rejectValue("dateOfBirth", "errors.too.young");
    }

    private boolean youngerThanSixteen(Date dateOfBirth) {
        Instant sixteenYearsAgo = LocalDate.now().minusYears(16).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return sixteenYearsAgo.isBefore(Instant.ofEpochMilli(dateOfBirth.getTime()));
    }
}
