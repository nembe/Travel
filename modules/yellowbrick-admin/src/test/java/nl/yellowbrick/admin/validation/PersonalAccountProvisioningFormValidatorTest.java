package nl.yellowbrick.admin.validation;

import com.google.common.collect.Lists;
import nl.yellowbrick.admin.form.PersonalAccountProvisioningForm;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PersonalAccountProvisioningFormValidatorTest {

    PersonalAccountProvisioningFormValidator validator;
    PersonalAccountProvisioningForm form;

    Errors errors;

    @Before
    public void setUp() {
        validator = new PersonalAccountProvisioningFormValidator();
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
        form.setFirstName("A");
        form.setLastName("A");

        validate();

        assertThat(errors.getFieldError("firstName").getCode(), is("errors.too.short"));
        assertThat(errors.getFieldError("firstName").getArguments()[0], is(2));
        assertThat(errors.getFieldError("lastName").getCode(), is("errors.too.short"));
        assertThat(errors.getFieldError("lastName").getArguments()[0], is(2));
    }

    @Test
    public void validates_dutch_postal_codes() {
        Consumer<String> validate = postalCode -> {
            setUp();
            form.setPostalCode(postalCode);
            validate();
        };

        Lists.newArrayList("CODFISH", "6003DDD", "603DD").forEach(validate.andThen(invalidPostalCode -> {
            assertThat(errors.getFieldError("postalCode").getCode(), is("errors.invalid.nlPostalCode"));
        }));

        Lists.newArrayList("6003 DD", "6003DD").forEach(validate.andThen(validPostalCode -> {
            assertThat(errors.getAllErrors(), empty());
        }));
    }

    private static PersonalAccountProvisioningForm validObject() {
        Customer cust = new Customer();
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

        return new PersonalAccountProvisioningForm(cust, ca);
    }

    private void validate() {
        validator.validate(form, errors);
    }
}
