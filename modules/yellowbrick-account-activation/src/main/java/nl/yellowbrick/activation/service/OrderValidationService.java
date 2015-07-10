package nl.yellowbrick.activation.service;

import nl.yellowbrick.activation.validation.UnboundErrors;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.validation.ClassValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.List;

@Component
public class OrderValidationService {

    private final List<ClassValidator<CardOrder>> validators;

    @Autowired
    public OrderValidationService(List<ClassValidator<CardOrder>> validators) {
        this.validators = validators;
    }

    public Errors validate(CardOrder order, String objectName) {
        return doValidate(order, new UnboundErrors(order, objectName));
    }

    public Errors validate(CardOrder order) {
        return doValidate(order, new UnboundErrors(order));
    }

    private Errors doValidate(CardOrder order, Errors errors) {
        validators.forEach(v -> v.validate(order, errors));

        return errors;
    }
}
