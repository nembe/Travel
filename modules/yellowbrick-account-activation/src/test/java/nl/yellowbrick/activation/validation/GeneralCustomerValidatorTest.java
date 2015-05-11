package nl.yellowbrick.activation.validation;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MarketingActionDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static nl.yellowbrick.data.domain.CustomerStatus.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class GeneralCustomerValidatorTest extends BaseSpringTestCase {

    private static final String KNOWN_MOBILE = "+31614992123";

    @InjectMocks GeneralCustomerValidator customerValidator;

    @Autowired @Spy CustomerDao customerDao;
    @Autowired @Spy MarketingActionDao marketingActionDao;

    Customer customer;
    Errors errors;

    Instant sixteenYearsAgo = LocalDate.now().minusYears(16).atStartOfDay(ZoneId.systemDefault()).toInstant();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

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

    @Test
    public void validates_action_code() {
        customer.setActionCode("TOTALLY BOGUS");

        invokeValidator();

        assertThat(errors.getFieldError("actionCode").getCode(), equalTo("errors.invalid.action.code"));
    }

    @Test
    public void checks_whether_first_mobile_is_already_known() {
        customer.setFirstCardMobile(KNOWN_MOBILE);

        invokeValidator();

        assertThat(errors.getFieldError("firstCardMobile").getCode(), is("errors.duplicate"));
    }

    @Test
    public void rejects_customers_with_name_matching_that_of_multiple_closed_accounts() {
        customer.setFirstName("John");
        customer.setLastName("Doe");

        when(customerDao.findAllByFuzzyName("John", "Doe")).thenReturn(
                Arrays.asList(customerWithClosedAccount(), customerWithClosedAccount())
        );

        invokeValidator();

        assertThat(errors.getGlobalError().getCode(), is("errors.name.matches.closed"));
    }

    @Test
    public void rejects_customers_with_email_matching_that_of_multiple_closed_accounts() {
        customer.setEmail("john@thedoes.com");

        when(customerDao.findAllByEmail("john@thedoes.com")).thenReturn(
                Arrays.asList(customerWithClosedAccount()), // at first return a single account
                Arrays.asList(customerWithClosedAccount(), customerWithClosedAccount()) // then a couple
        );

        invokeValidator();
        assertFalse(errors.hasFieldErrors("email")); // a single match should not be flagged

        invokeValidator();
        // multiple matches should be flagged
        assertThat(errors.getFieldError("email").getCode(), is("errors.matches.closed"));
    }

    private Customer validCustomer() {
        Customer cust = new Customer();
        cust.setDateOfBirth(Date.from(sixteenYearsAgo));

        return cust;
    }

    private Customer customerWithClosedAccount() {
        ArrayList<CustomerStatus> closedAccountStatuses = Lists.newArrayList(BLACKLISTED, IRRECOVERABLE, UNREGISTERED);
        CustomerStatus randomStatus = closedAccountStatuses.get(new Random().nextInt(closedAccountStatuses.size()));

        Customer customer = new Customer();
        customer.setStatus(randomStatus);

        return customer;
    }

    private void invokeValidator() {
        ValidationUtils.invokeValidator(customerValidator, customer, errors);
    }
}
