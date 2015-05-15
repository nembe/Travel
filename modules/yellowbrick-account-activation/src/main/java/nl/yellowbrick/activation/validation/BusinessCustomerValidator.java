package nl.yellowbrick.activation.validation;

import com.google.common.base.Strings;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class BusinessCustomerValidator extends AccountRegistrationValidator {

    private static final int INITIAL_ORDER_THRESHOLD = 4;
    private static final String INITIAL_ORDER_TOO_LARGE = "errors.businessCustomer.large.order";

    private static final Pattern KVK_LABEL_PATTERN = Pattern.compile("(?i).*businessRegistrationNumber.*");
    private static final Pattern VAT_LABEL_PATTERN = Pattern.compile("(?i).*vatNumber.*");

    private final CustomerDao customerDao;

    @Autowired
    public BusinessCustomerValidator(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    @Override
    protected void doValidate(Customer customer, Errors errors) {
        if(!customer.isBusinessCustomer())
            return;

        List<BusinessIdentifier> businessIdentifiers = customerDao.getBusinessIdentifiers(customer.getCustomerId());

        validateUniquenessByBusinessIdentifiers(businessIdentifiers, errors);
        validateReasonableInitialOrderSize(businessIdentifiers, customer, errors);
    }

    private void validateReasonableInitialOrderSize(List<BusinessIdentifier> businessIdentifiers,
                                                    Customer customer,
                                                    Errors errors) {
        if(hasVatOrKvk(businessIdentifiers))
            return;

        if(customer.getNumberOfTCards() + customer.getNumberOfQCards() > INITIAL_ORDER_THRESHOLD)
            errors.reject(
                    INITIAL_ORDER_TOO_LARGE,
                    new Object[] { INITIAL_ORDER_THRESHOLD },
                    "Initial order too large for business account lacking KvK or VAT numbers");
    }

    private boolean hasVatOrKvk(List<BusinessIdentifier> businessIdentifiers) {
        List<Pattern> identifierPatterns = Arrays.asList(KVK_LABEL_PATTERN, VAT_LABEL_PATTERN);

        for(BusinessIdentifier bi: businessIdentifiers) {
            for(Pattern identifierPattern: identifierPatterns) {
                if(identifierPattern.matcher(bi.getLabel()).matches() && !Strings.isNullOrEmpty(bi.getValue()))
                    return true;
            }
        }
        return false;
    }

    private void validateUniquenessByBusinessIdentifiers(List<BusinessIdentifier> businessIdentifiers,
                                                         Errors errors) {

        businessIdentifiers.forEach(bi -> {
            if (customerDao.findAllByBusinessIdentifier(bi.getLabel(), bi.getValue()).size() > 1) {
                String field = String.format("businessIdentifiers[%d].value", businessIdentifiers.indexOf(bi));
                errors.rejectValue(field, "errors.duplicate");
            }
        });
    }
}
