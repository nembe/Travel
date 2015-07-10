package nl.yellowbrick.activation.service;

import nl.yellowbrick.activation.validation.CardOrderValidator;
import nl.yellowbrick.activation.validation.SleeveOrderValidator;
import nl.yellowbrick.activation.validation.UnboundErrors;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@Component
public class OrderValidationService {

    private final CardOrderValidator cardOrderValidator;
    private final SleeveOrderValidator sleeveOrderValidator;

    @Autowired
    public OrderValidationService(CardOrderValidator cardOrderValidator,
                                  SleeveOrderValidator sleeveOrderValidator) {
        this.cardOrderValidator = cardOrderValidator;
        this.sleeveOrderValidator = sleeveOrderValidator;
    }

    public Errors validate(CardOrder order, String objectName) {
        return doValidate(order, new UnboundErrors(order, objectName));
    }

    public Errors validate(CardOrder order) {
        return doValidate(order, new UnboundErrors(order));
    }

    private Errors doValidate(CardOrder order, Errors errors) {
        if(order.getCardType() == CardType.SLEEVE)
            ValidationUtils.invokeValidator(sleeveOrderValidator, order, errors);
        else
            ValidationUtils.invokeValidator(cardOrderValidator, order, errors);

        return errors;
    }
}
