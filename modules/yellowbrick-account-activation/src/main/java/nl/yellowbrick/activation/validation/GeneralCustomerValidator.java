package nl.yellowbrick.activation.validation;

import com.google.common.base.Strings;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MarketingActionDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import nl.yellowbrick.data.domain.MarketingAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import static nl.yellowbrick.data.domain.CustomerStatus.*;

@Component
public class GeneralCustomerValidator extends AccountRegistrationValidator {

    private static final List<CustomerStatus> VALID_STATUSES = Arrays.asList(ACTIVATION_FAILED, REGISTERED, ACTIVE);

    private static final String INITIAL_ORDER_TOO_LARGE = "errors.customer.large.order";

    @Autowired
    private MarketingActionDao marketingActionDao;

    @Autowired
    private CustomerDao customerDao;

    @Value("${customervalidation.thresholds.initialOrderAmount}")
    private int initialOrderAmountThreshold;

    @Override
    protected void doValidate(Customer customer, Errors errors) {
        validateAge(customer, errors);
        validateActionCode(customer, errors);
        validateRegistrationMobile(customer, errors);
        validateUniquenessAgainstClosedAccounts(customer, errors);
        validateInitialOrderSize(customer, errors);
    }

    private void validateInitialOrderSize(Customer customer, Errors errors) {
        if(customer.getNumberOfTCards() + customer.getNumberOfQCards() > initialOrderAmountThreshold)
            errors.reject(
                    INITIAL_ORDER_TOO_LARGE,
                    new Object[] {initialOrderAmountThreshold},
                    "Initial card order is too large");
    }

    private void validateUniquenessAgainstClosedAccounts(Customer customer, Errors errors) {
        Predicate<Customer> hasInvalidStatus = it -> !VALID_STATUSES.contains(it.getStatus());

        long matchesByFuzzyName = customerDao.findAllByFuzzyName(customer.getFirstName(), customer.getLastName())
                .stream()
                .filter(hasInvalidStatus)
                .count();

        if(matchesByFuzzyName > 1)
            errors.reject("errors.name.matches.closed");

        long matchesByEmail = customerDao.findAllByEmail(customer.getEmail())
                .stream()
                .filter(hasInvalidStatus)
                .count();

        if(matchesByEmail > 1)
            errors.rejectValue("email", "errors.matches.closed");
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
