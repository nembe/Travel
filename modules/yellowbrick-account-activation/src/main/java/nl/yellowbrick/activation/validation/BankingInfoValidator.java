package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.dao.DirectDebitDetailsDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.DirectDebitDetails;
import nl.yellowbrick.data.domain.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Optional;

@Component
public class BankingInfoValidator extends AccountRegistrationValidator {

    @Autowired
    private DirectDebitDetailsDao directDebitDetailsDao;

    @Override
    protected void doValidate(Customer customer, Errors errors) {
        if(!customer.getPaymentMethodType().equals(PaymentMethod.DIRECT_DEBIT))
            return;

        Optional<DirectDebitDetails> directDebitInfo = directDebitDetailsDao.findForCustomer(customer.getCustomerId());

        directDebitInfo.ifPresent(details -> validateUniqueSepaNumber(details, errors));
    }

    private void validateUniqueSepaNumber(DirectDebitDetails details, Errors errors) {
        directDebitDetailsDao.findBySepaNumber(details.getSepaNumber())
                .stream()
                .filter(it -> !it.equals(details))
                .findAny()
                .ifPresent(it -> errors.rejectValue("iban", "errors.duplicate"));
    }
}
