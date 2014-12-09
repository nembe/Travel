package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.domain.Customer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class GeneralCustomerValidatorTest {

    GeneralCustomerValidator customerValidator;

    Customer customer;
    Errors errors;

    Instant sixteenYearsAgo = LocalDate.now().minusYears(16).atStartOfDay(ZoneId.systemDefault()).toInstant();

    @Before
    public void setUp() {
        customerValidator = new GeneralCustomerValidator();
        customer = validCustomer();
        errors = new BindException(customer, "customer");
    }

    @Test
    public void invalidates_missing_dob()  {
        customer.setDateOfBirth(null);

        invokeValidator();
        assertThat(errors.getFieldError("dateOfBirth").getCode(), equalTo("errors.missing"));
    }

    @Test
    public void invalidates_younger_than_16() {
        Instant tooYoung = sixteenYearsAgo.plusSeconds(60);

        customer.setDateOfBirth(Date.from(tooYoung));

        invokeValidator();
        assertThat(errors.getFieldError("dateOfBirth").getCode(), equalTo("errors.too.young"));
    }

    @Test
    public void validates_otherwise() {
        invokeValidator();
        assertThat(errors.getAllErrors(), empty());
    }


    private Customer validCustomer() {
        Customer cust = new Customer();
        cust.setDateOfBirth(Date.from(sixteenYearsAgo));

        return cust;
    }

    private void invokeValidator() {
        ValidationUtils.invokeValidator(customerValidator, customer, errors);
    }
}
