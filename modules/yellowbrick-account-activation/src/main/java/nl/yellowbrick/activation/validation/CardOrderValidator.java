package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.validation.ClassValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class CardOrderValidator extends ClassValidator<CardOrder> {

    private static final String COST_TOO_HIGH = "errors.order.high.cost";
    private static final String AMOUNT_TOO_HIGH = "errors.order.high.quantity";

    protected CardOrderValidator() {
        super(CardOrder.class);
    }

    @Value("${ordervalidation.thresholds.priceEurocent}")
    private int priceThresholdEurocent;

    @Value("${ordervalidation.thresholds.amount}")
    private int amountThreshold;

    @Override
    protected void doValidate(CardOrder order, Errors errors) {
        if(order.totalCost() > priceThresholdEurocent)
            rejectCostTooHigh(errors);

        if(order.getAmount() > amountThreshold)
            rejectAmountTooHigh(errors);
    }

    private void rejectCostTooHigh(Errors errors) {
        errors.reject(COST_TOO_HIGH, new Object[] { priceThresholdEurocent / 100 }, "Cost quite high");
    }

    private void rejectAmountTooHigh(Errors errors) {
        errors.reject(AMOUNT_TOO_HIGH, new Object[] { amountThreshold }, "Amount quite high");
    }
}
