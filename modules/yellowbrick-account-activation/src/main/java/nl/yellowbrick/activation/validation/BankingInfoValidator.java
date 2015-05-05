package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.dao.DirectDebitDetailsDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.DirectDebitDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Optional;

@Component
public class BankingInfoValidator extends AccountRegistrationValidator {

    private static final String IBAN_FIELD = "iban";

    @Autowired
    private DirectDebitDetailsDao directDebitDetailsDao;

    @Override
    protected void doValidate(Customer customer, Errors errors) {
        switch(customer.getPaymentMethodType()) {
            case DIRECT_DEBIT:
                verifyDirectDebitDetails(customer, errors);
                break;
            case MASTERCARD:
                verifyCreditCardDetails(customer, errors);
                break;
            case VISA:
                verifyCreditCardDetails(customer, errors);
                break;
        }
    }

    private void verifyCreditCardDetails(Customer customer, Errors errors) {
        // TODO implement
    }

    private void verifyDirectDebitDetails(Customer customer, Errors errors) {
        Optional<DirectDebitDetails> directDebitInfo = directDebitDetailsDao.findForCustomer(customer.getCustomerId());

        if(directDebitInfo.isPresent())
            validateUniqueSepaNumber(directDebitInfo.get(), errors);
        else
            errors.rejectValue(IBAN_FIELD, "errors.missing");
    }

    private void validateUniqueSepaNumber(DirectDebitDetails details, Errors errors) {
        directDebitDetailsDao.findBySepaNumber(details.getSepaNumber())
                .stream()
                .filter(it -> !it.equals(details))
                .findAny()
                .ifPresent(it -> errors.rejectValue(IBAN_FIELD, "errors.duplicate"));
    }
}
