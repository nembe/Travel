package nl.yellowbrick.admin.validation;

import com.google.common.collect.Lists;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.List;

public class ValidatorChain implements Validator {

    private final List<Validator> validators = Lists.newLinkedList();

    private ValidatorChain(Validator... validators) {
        Collections.addAll(this.validators, validators);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return validators.stream().anyMatch(v -> v.supports(clazz));
    }

    @Override
    public void validate(Object target, Errors errors) {
        validators.stream()
                .filter(v -> v.supports(target.getClass()))
                .findFirst()
                .ifPresent(v -> v.validate(target, errors));
    }

    public static ValidatorChain of(Validator... validators) {
        return new ValidatorChain(validators);
    }
}
