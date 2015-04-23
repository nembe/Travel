package nl.yellowbrick.admin.validation;

import org.springframework.validation.Errors;

public class Validations {

    public static void validateDutchZipCode(Errors errors, String fieldName) {
        Object fieldValue = errors.getFieldValue(fieldName);

        if(fieldValue == null || !fieldValue.toString().matches("^[0-9]{4}\\s?[a-zA-Z]{2}$"))
            errors.rejectValue(fieldName, "errors.invalid.nlPostalCode");
    }

    public static void validatePresence(Errors errors, String fieldName) {
        Object fieldValue = errors.getFieldValue(fieldName);

        if(fieldValue == null || fieldValue.toString().isEmpty())
            errors.rejectValue(fieldName, "errors.missing");
    }

    public static void validateMinSize(int min, Errors errors, String fieldName) {
        Object fieldValue = errors.getFieldValue(fieldName);

        if(fieldValue == null || fieldValue.toString().length() < min) {
            String defaultMessage = String.format("must be at least %s characters long", min);
            errors.rejectValue(fieldName, "errors.too.short", new Object[] { min }, defaultMessage);
        }
    }
}
