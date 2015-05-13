package nl.yellowbrick.activation.validation;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BusinessCustomerValidatorTest {

    BusinessCustomerValidator validator;

    CustomerDao customerDao;
    Customer customer;
    Errors errors;
    BusinessIdentifier bi;

    @Before
    public void setUp() {
        customer = new Customer();
        customer.setCustomerId(123l);
        customer.setBusiness("Y");

        errors = new UnboundErrors(customer, "customer");

        bi = new BusinessIdentifier();
        bi.setLabel("vatNumber");
        bi.setValue("123456789");

        customerDao = mock(CustomerDao.class);
        validator = new BusinessCustomerValidator(customerDao);
    }

    @Test
    public void rejects_business_identifiers_equal_to_those_of_other_users() {
        when(customerDao.getBusinessIdentifiers(123l)).thenReturn(Arrays.asList(bi));
        when(customerDao.findAllByBusinessIdentifier(bi.getLabel(), bi.getValue()))
                // return multiple customers matching the BIs
                .thenReturn(Arrays.asList(new Customer(), new Customer()));

        invokeValidator();

        assertThat(errors.getFieldError("businessIdentifiers[0].value").getCode(), is("errors.duplicate"));
    }

    @Test
    public void rejects_large_orders_for_customers_lacking_business_identifiers() {
        when(customerDao.getBusinessIdentifiers(123l)).thenReturn(Lists.newArrayList());
        customer.setNumberOfTCards(2);
        customer.setNumberOfQCards(2);

        invokeValidator();
        assertThat(errors.getAllErrors(), empty());

        // increment any of the card counts
        doOneOf(() -> { customer.setNumberOfTCards(3); },
                () -> { customer.setNumberOfQCards(3); });

        invokeValidator();
        assertThat(errors.getGlobalError().getCode(), is("errors.businessCustomer.large.order"));
    }

    private void invokeValidator() {
        ValidationUtils.invokeValidator(validator, customer, errors);
    }

    private void doOneOf(Runnable... actions) {
        actions[(new Random()).nextInt(actions.length)].run();
    }
}
