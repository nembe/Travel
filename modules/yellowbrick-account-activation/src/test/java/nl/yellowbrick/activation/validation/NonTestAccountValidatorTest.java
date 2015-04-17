package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.Customer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class NonTestAccountValidatorTest extends BaseSpringTestCase {

    @Autowired
    NonTestAccountValidator nonTestAccountValidator;

    @Autowired
    CustomerDao customerDao;

    Customer customer;
    Errors errors;

    @Before
    public void fetchCustomerFromDb() {
        customer = customerDao.findAllPendingActivation().get(0);
        errors = new BindException(customer, "customer");
    }

    @Test
    public void no_op_on_valid_customer() throws Exception {
        invokeValidator();
        assertThat(errors.getAllErrors(), empty());
    }

    @Test
    public void handles_nulls() {
        // just make some field null
        customer.setFirstName(null);

        invokeValidator();
        assertThat(errors.getAllErrors(), empty());
    }

    @Test
    public void detects_leading_test_text() {
        customer.setFirstName("testsomething");

        invokeValidator();
        assertThat(errors.getFieldError("firstName").getCode(), equalTo("errors.test.data"));
    }

    @Test
    public void detects_trailing_test_text() {
        customer.setLastName("somethingtest");

        invokeValidator();
        assertThat(errors.getFieldError("lastName").getCode(), equalTo("errors.test.data"));
    }

    @Test
    public void is_case_insensitive() {
        customer.setFirstName("MUMBLETEST");

        invokeValidator();
        assertTrue(errors.hasFieldErrors("firstName"));

        customer.setFirstName("TESTMUMBLE");

        invokeValidator();
        assertTrue(errors.hasFieldErrors("firstName"));
    }

    private void invokeValidator() {
        ValidationUtils.invokeValidator(nonTestAccountValidator, customer, errors);
    }
}
