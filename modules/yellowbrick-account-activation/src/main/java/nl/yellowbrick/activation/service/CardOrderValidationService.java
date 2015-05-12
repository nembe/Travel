package nl.yellowbrick.activation.service;

import nl.yellowbrick.activation.validation.CardOrderValidator;
import nl.yellowbrick.activation.validation.UnboundErrors;
import nl.yellowbrick.data.domain.CardOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@Component
public class CardOrderValidationService {

    private final CardOrderValidator validator;

    @Autowired
    public CardOrderValidationService(CardOrderValidator validator) {
        this.validator = validator;
    }

    public Errors validate(CardOrder order, String objectName) {
        return doValidate(order, new UnboundErrors(order, objectName));
    }

    public Errors validate(CardOrder order) {
        return doValidate(order, new UnboundErrors(order));
    }

    private Errors doValidate(CardOrder order, Errors errors) {
        ValidationUtils.invokeValidator(validator, order, errors);

        return errors;
    }
}
