package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.dao.ConfigDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.validation.ClassValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import static nl.yellowbrick.activation.validation.CardOrderValidator.AMOUNT_TOO_HIGH;
import static nl.yellowbrick.data.dao.ConfigSection.BRICKWALL;

@Component
public class QParkCardOrderValidator extends ClassValidator<CardOrder> {

    private static final String BUSINESS_CUST_MAX_AMOUNT = "qparkCardOrderValidation.businessCust.maxAmount";
    private static final String PRIVATE_CUST_MAX_AMOUNT = "qparkCardOrderValidation.privateCust.maxAmount";

    private final ConfigDao configDao;
    private final CustomerDao customerDao;

    @Autowired
    protected QParkCardOrderValidator(ConfigDao configDao, CustomerDao customerDao) {
        super(CardOrder.class);

        this.configDao = configDao;
        this.customerDao = customerDao;
    }

    @Override
    protected void doValidate(CardOrder order, Errors errors) {
        if(order.getCardType() != CardType.QPARK_CARD)
            return;

        Customer customer = customerDao
                .findById(order.getCustomerId())
                .orElseThrow(() -> new IllegalStateException("Couldn't find customer with id " + order.getCustomerId()));

        if(customer.isBusinessCustomer())
            validateForBusinessCustomer(order, errors);
        else
            validateForPrivateCustomer(order, errors);
    }

    private void validateForBusinessCustomer(CardOrder order, Errors errors) {
        int orderThreshold = configDao.mustFindSectionField(BRICKWALL, BUSINESS_CUST_MAX_AMOUNT).getValueAsInt();

        if(order.getAmount() > orderThreshold)
            errors.reject(AMOUNT_TOO_HIGH, new Object[] { orderThreshold }, "Amount too high");
    }

    private void validateForPrivateCustomer(CardOrder order, Errors errors) {
        int orderThreshold = configDao.mustFindSectionField(BRICKWALL, PRIVATE_CUST_MAX_AMOUNT).getValueAsInt();

        if(order.getAmount() > orderThreshold)
            errors.reject(AMOUNT_TOO_HIGH, new Object[] { orderThreshold }, "Amount too high");
    }
}
