package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.dao.ConfigDao;
import nl.yellowbrick.data.dao.ConfigSection;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Config;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.validation.ClassValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import static nl.yellowbrick.activation.validation.CardOrderValidator.AMOUNT_TOO_HIGH;

@Component
public class SleeveOrderValidator extends ClassValidator<CardOrder> {

    private static final String BUSINESS_CUST_MAX_AMOUNT = "sleeveOrderValidation.businessCustomer.thresholds.amount";
    private static final String PRIVATE_CUST_MAX_AMOUNT = "sleeveOrderValidation.privateCustomer.thresholds.amount";

    private final ConfigDao configDao;
    private final CustomerDao customerDao;

    @Autowired
    protected SleeveOrderValidator(ConfigDao configDao, CustomerDao customerDao) {
        super(CardOrder.class);

        this.configDao = configDao;
        this.customerDao = customerDao;
    }

    @Override
    protected void doValidate(CardOrder order, Errors errors) {
        if(order.getCardType() != CardType.SLEEVE)
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
        int orderThreshold = readConfigOrFail(BUSINESS_CUST_MAX_AMOUNT);

        if(order.getAmount() > orderThreshold)
            errors.reject(AMOUNT_TOO_HIGH, new Object[] { orderThreshold }, "Amount too high");
    }

    private void validateForPrivateCustomer(CardOrder order, Errors errors) {
        int orderThreshold = readConfigOrFail(PRIVATE_CUST_MAX_AMOUNT);

        if(order.getAmount() > orderThreshold)
            errors.reject(AMOUNT_TOO_HIGH, new Object[] { orderThreshold }, "Amount too high");
    }

    private int readConfigOrFail(String key) {
        Config cfg = configDao
                .findSectionField(ConfigSection.BRICKWALL, key)
                .orElseThrow(() -> new IllegalStateException("Couldn't find config with key " + key));

        return Integer.parseInt(cfg.getValue());
    }
}
