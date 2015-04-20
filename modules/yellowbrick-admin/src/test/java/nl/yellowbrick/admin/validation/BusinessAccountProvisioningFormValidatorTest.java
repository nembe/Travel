package nl.yellowbrick.admin.validation;

import com.google.common.collect.Lists;
import nl.yellowbrick.admin.form.BusinessAccountProvisioningForm;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BusinessAccountProvisioningFormValidatorTest {

    BusinessAccountProvisioningFormValidator validator;
    BusinessAccountProvisioningForm form;

    Errors errors;

    @Before
    public void setUp() {
        validator = new BusinessAccountProvisioningFormValidator();
        form = validObject();
        errors = new BindException(form, "form");
    }

    @Test
    public void validates_valid_object() {
        validate();

        assertThat(errors.getAllErrors(), empty());
    }

    @Test
    public void validates_non_null_or_empty_fields() {
        List<String> requiredFields = Lists.newArrayList(
                "gender", "initials", "email", "phoneNr", "street",
                "houseNr", "postalCode", "city", "dateOfBirth");

        requiredFields.forEach(fieldName -> {
            Object field = ReflectionTestUtils.getField(form, fieldName);
            ReflectionTestUtils.setField(form, fieldName, field instanceof String ? "" : null);

            validate();

            assertThat(errors.getFieldError(fieldName).getCode(), is("errors.missing"));
        });
    }

    @Test
    public void validates_length_of_name_fields() {
        form.setBusinessName("A");
        form.setFirstName("A");
        form.setLastName("A");

        validate();

        Lists.newArrayList("businessName", "firstName", "lastName").forEach(field -> {
            assertThat(errors.getFieldError(field).getCode(), is("errors.too.short"));
            assertThat(errors.getFieldError(field).getArguments()[0], is(2));
        });
    }

    @Test
    public void validates_dutch_postal_codes() {
        Consumer<String> validate = postalCode -> {
            setUp();
            form.setPostalCode(postalCode);
            form.setBillingAddressPostalCode(postalCode);
            validate();
        };

        Lists.newArrayList("CODFISH", "6003DDD", "603DD").forEach(validate.andThen(invalidPostalCode -> {
            assertThat(errors.getFieldError("postalCode").getCode(), is("errors.invalid.nlPostalCode"));
            assertThat(errors.getFieldError("billingAddressPostalCode").getCode(), is("errors.invalid.nlPostalCode"));
        }));

        Lists.newArrayList("6003 DD", "6003DD").forEach(validate.andThen(validPostalCode -> {
            assertThat(errors.getAllErrors(), empty());
        }));
    }

    private static BusinessAccountProvisioningForm validObject() {
        Customer cust = new Customer();
        cust.setBusinessName("Kabisa BV");
        cust.setGender("M");
        cust.setInitials("Mr");
        cust.setFirstName("Rui");
        cust.setLastName("Salgado");
        cust.setDateOfBirth(new Date());
        cust.setEmail("rui.salgado@kabisa.nl");
        cust.setPhoneNr("06123456789");

        CustomerAddress ca = new CustomerAddress();
        ca.setAddress("Marconilaan");
        ca.setHouseNr("8");
        ca.setZipCode("6003 DD");
        ca.setCity("Weert");
        ca.setCountryCode("NL");

        Optional<CustomerAddress> billingAddress = Optional.of(ca);
        List<BusinessIdentifier> businessIdentifiers = Lists.newArrayList();

        return new BusinessAccountProvisioningForm(cust, ca, billingAddress, businessIdentifiers);
    }

    private void validate() {
        validator.validate(form, errors);
    }
}
