package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.dao.BillingDetailsDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import nl.yellowbrick.data.domain.DirectDebitDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Optional;

@Component
public class BankingInfoValidator extends AccountRegistrationValidator {

    private static final String IBAN_FIELD = "iban";
    private static final String CC_FIELD = "ccname";
    private static final String PAYMENT_METHOD_FIELD = "paymentMethod";

    @Autowired
    private BillingDetailsDao billingDetailsDao;

    @Autowired
    private CustomerDao customerDao;

    @Override
    protected void doValidate(Customer customer, Errors errors) {
        switch(customer.getPaymentMethod()) {
            case DIRECT_DEBIT:
                verifyDirectDebitDetails(customer, errors);
                break;
            case MASTERCARD:
                verifyCreditCardDetails(customer, errors);
                break;
            case VISA:
                verifyCreditCardDetails(customer, errors);
                break;
            case INVOICED:
                warnAboutPaymentTerms(errors);
                break;
        }
    }

    private void warnAboutPaymentTerms(Errors errors) {
        errors.rejectValue(PAYMENT_METHOD_FIELD, "errors.invoiced.setPaymentTerms");
    }

    private void verifyCreditCardDetails(Customer customer, Errors errors) {
        if(!billingDetailsDao.existsCreditCardReferenceForCustomer(customer.getCustomerId()))
            errors.rejectValue(CC_FIELD, "errors.missing");
    }

    private void verifyDirectDebitDetails(Customer customer, Errors errors) {
        Optional<DirectDebitDetails> directDebitInfo = billingDetailsDao.findDirectDebitDetailsForCustomer(customer.getCustomerId());

        if(directDebitInfo.isPresent())
            validateUniqueSepaNumber(directDebitInfo.get(), customer, errors);
        else
            errors.rejectValue(IBAN_FIELD, "errors.missing");
    }

    private void validateUniqueSepaNumber(DirectDebitDetails details, Customer customer, Errors errors) {
        billingDetailsDao.findDirectDebitDetailsBySepaNumber(details.getSepaNumber())
                .stream()
                .map(ddd -> customerDao.findById(ddd.getCustomerId()))
                .filter(Optional::isPresent).map(Optional::get) // essentially "flatten"
                .filter(c -> c.getCustomerId() != customer.getCustomerId())
                .map(Customer::getStatus)
                .distinct()
                .forEach(status -> {
                    if (status.equals(CustomerStatus.BLACKLISTED)) {
                        errors.rejectValue(IBAN_FIELD, "errors.matches.blacklisted");
                        errors.reject("errors.iban.matches.blacklisted");
                    } else if (status.equals(CustomerStatus.IRRECOVERABLE)) {
                        errors.rejectValue(IBAN_FIELD, "errors.matches.unbillable");
                        errors.reject("errors.iban.matches.unbillable");
                    } else if (!errors.hasFieldErrors(IBAN_FIELD)) {
                        errors.rejectValue(IBAN_FIELD, "errors.duplicate");
                    }
                });
    }
}
