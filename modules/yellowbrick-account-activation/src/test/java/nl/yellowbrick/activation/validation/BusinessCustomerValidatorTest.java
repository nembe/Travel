package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.Arrays;

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

    private void invokeValidator() {
        ValidationUtils.invokeValidator(validator, customer, errors);
    }
}
