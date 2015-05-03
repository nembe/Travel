package nl.yellowbrick.data.validation;

import com.google.common.reflect.AbstractInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class ClassValidator<T> implements Validator {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
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

        doValidate(supportedClass.cast(o), unsafeProxy(errors));
    }

    protected abstract void doValidate(T target, Errors errors);

    // tolerates binding exceptions, doing nothing
    // and returning null instead of propagating the exception
    private Errors unsafeProxy(Errors errors) {
        ClassLoader classLoader = Errors.class.getClassLoader();
        Class<?>[] interfaces = { Errors.class };

        return (Errors) Proxy.newProxyInstance(classLoader, interfaces, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(Object proxy, Method method, Object[] arguments) throws Throwable {
                try {
                    return method.invoke(errors, arguments);
                } catch(Exception e) {
                    if(e.getCause() != null && e.getCause() instanceof InvalidPropertyException)
                        return null;
                    else
                        throw e;
                }
            }
        });
    }

}
