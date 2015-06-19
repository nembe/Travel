package nl.yellowbrick.activation.validation;

import org.apache.commons.beanutils.BeanMap;
import org.springframework.validation.MapBindingResult;

/**
 * Binds errors to a map instead of attempting javaBean binding.
 * Allows errors to be added even if the target objects lacks some
 * of the rejected fields
 */
public class UnboundErrors extends MapBindingResult {

    private static final String DEFAULT_TARGET_NAME = "target";

    public UnboundErrors(Object target, String targetName) {
        super(new BeanMap(target), targetName);
    }

    public UnboundErrors(Object target) {
        this(target, DEFAULT_TARGET_NAME);
    }
}
