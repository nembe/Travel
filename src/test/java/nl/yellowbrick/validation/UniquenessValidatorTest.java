package nl.yellowbrick.validation;

import nl.yellowbrick.BaseSpringTestCase;
import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.domain.CustomerStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.time.*;
import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class UniquenessValidatorTest extends BaseSpringTestCase {

    @InjectMocks
    UniquenessValidator uniquenessValidator;

    @Mock
    CustomerDao customerDao;

    Customer customer;
    Errors errors;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        customer = customer();
        errors = new BindException(customer, "customer");
    }

    @Test
    public void invalidates_match_on_name_and_date_of_birth() {
        mockFuzzyQuery(customer);

        customer.setCustomerStatusIdfk(CustomerStatus.ACTIVE.code());

        invokeValidator();

        assertThat(errors.getGlobalError().getCode(), equalTo("duplicate"));
    }

    @Test
    public void ignores_pre_active_results() {
        mockFuzzyQuery(customer);

        customer.setCustomerStatusIdfk(CustomerStatus.REGISTERED.code());
        invokeValidator();
        assertThat(errors.getAllErrors(), empty());

        customer.setCustomerStatusIdfk(CustomerStatus.ACTIVATION_FAILED.code());
        invokeValidator();
        assertThat(errors.getAllErrors(), empty());
    }

    private void mockFuzzyQuery(Customer... results) {
        when(customerDao.findAllByFuzzyNameAndDateOfBirth(
                eq(customer.getFirstName()),
                eq(customer.getLastName()),
                eq(customer.getDateOfBirth())
        )).thenReturn(Arrays.asList(results));
    }

    private Customer customer() {
        Customer cust = new Customer();
        cust.setFirstName("John");
        cust.setLastName("Doe");

        Instant dob = LocalDate.of(1990, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        cust.setDateOfBirth(Date.from(dob));

        return cust;
    }

    private void invokeValidator() {
        ValidationUtils.invokeValidator(uniquenessValidator, customer, errors);
    }
}
