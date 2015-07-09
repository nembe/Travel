package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.dao.ConfigDao;
import nl.yellowbrick.data.dao.ConfigSection;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Config;
import nl.yellowbrick.data.domain.Customer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;

import java.util.Optional;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SleeveOrderValidatorTest {

    SleeveOrderValidator validator;

    ConfigDao configDao;
    CustomerDao customerDao;

    CardOrder order;
    Customer customer;
    Errors errors;

    @Before
    public void setUp() {
        configDao = mock(ConfigDao.class);
        customerDao = mock(CustomerDao.class);
        validator = new SleeveOrderValidator(configDao, customerDao);

        customer = new Customer();
        customer.setCustomerId(1l);

        order = new CardOrder();
        order.setCustomerId(1l);
        order.setCardType(CardType.SLEEVE);

        errors = new UnboundErrors(order, "order");
    }

    @Test
    public void rejects_business_cust_order_above_certain_amount() {
        customer.setBusiness("Y");
        stubCustomer(customer);
        stubConfig("sleeveOrderValidation.businessCust.maxAmount", "5");

        order.setAmount(5);
        validator.validate(order, errors);
        assertThat(errors.getAllErrors(), empty());

        order.setAmount(6);
        validator.validate(order, errors);
        assertThat(errors.getGlobalError().getCode(), is("errors.order.high.quantity"));
    }

    @Test
    public void rejects_personal_cust_order_above_certain_amount() {
        stubCustomer(customer);
        stubConfig("sleeveOrderValidation.privateCust.maxAmount", "5");

        order.setAmount(5);
        validator.validate(order, errors);
        assertThat(errors.getAllErrors(), empty());

        order.setAmount(6);
        validator.validate(order, errors);
        assertThat(errors.getGlobalError().getCode(), is("errors.order.high.quantity"));
    }

    @Test(expected = IllegalStateException.class)
    public void raises_exception_when_customer_cant_be_found() {
        stubCustomer(null);

        validator.validate(order, errors);
    }

    @Test(expected = IllegalStateException.class)
    public void raises_exception_when_threshold_config_cant_be_found() {
        stubCustomer(customer);
        when(configDao.findSectionField(any(), anyString())).thenReturn(Optional.empty());

        validator.validate(order, errors);
    }

    @Test(expected = NumberFormatException.class)
    public void raises_exception_when_threshold_config_isnt_numeric() {
        stubCustomer(customer);
        stubConfig("sleeveOrderValidation.privateCust.maxAmount", "not numeric");

        validator.validate(order, errors);
    }

    private void stubCustomer(Customer customer) {
        when(customerDao.findById(1l)).thenReturn(Optional.ofNullable(customer));
    }

    private void stubConfig(String key, String value) {
        Config cfg = new Config();
        cfg.setValue(value);

        when(configDao.findSectionField(ConfigSection.BRICKWALL, key)).thenReturn(Optional.ofNullable(cfg));
    }
}
