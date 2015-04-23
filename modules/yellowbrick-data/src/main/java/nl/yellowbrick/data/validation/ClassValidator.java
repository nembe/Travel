package nl.yellowbrick.data.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class ClassValidator<T> implements Validator {

    private final Class<T> supportedClass;

    protected ClassValidator(Class<T> supportedClass) {
        this.supportedClass = supportedClass;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(supportedClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        if(!supports(o.getClass()))
            throw new IllegalArgumentException(String.format("can only validate %s objects, got %s instead",
                    supportedClass.getCanonicalName(),
                    o.getClass().getCanonicalName()));

        doValidate(supportedClass.cast(o), errors);
    }

    protected abstract void doValidate(T target, Errors errors);
}
