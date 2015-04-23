package nl.yellowbrick.admin.validation;

import nl.yellowbrick.admin.form.BusinessAccountProvisioningForm;
import nl.yellowbrick.data.validation.ClassValidator;
import org.springframework.validation.Errors;

import static nl.yellowbrick.admin.validation.Validations.validateDutchZipCode;
import static nl.yellowbrick.admin.validation.Validations.validateMinSize;
import static nl.yellowbrick.admin.validation.Validations.validatePresence;

public class BusinessAccountProvisioningFormValidator extends ClassValidator<BusinessAccountProvisioningForm> {

    public BusinessAccountProvisioningFormValidator() {
        super(BusinessAccountProvisioningForm.class);
    }

    @Override
    protected void doValidate(BusinessAccountProvisioningForm form, Errors errors) {
        validatePresence(errors, "gender");
        validatePresence(errors, "initials");
        validatePresence(errors, "email");
        validatePresence(errors, "phoneNr");
        validatePresence(errors, "street");
        validatePresence(errors, "houseNr");
        validatePresence(errors, "postalCode");
        validatePresence(errors, "city");
        validatePresence(errors, "dateOfBirth");

        validateMinSize(2, errors, "businessName");
        validateMinSize(2, errors, "firstName");
        validateMinSize(2, errors, "lastName");

        if("NL".equalsIgnoreCase(form.getCountry()))
            validateDutchZipCode(errors, "postalCode");

        if("NL".equalsIgnoreCase(form.getBillingAddressCountry()))
            validateDutchZipCode(errors, "billingAddressPostalCode");
    }
}
