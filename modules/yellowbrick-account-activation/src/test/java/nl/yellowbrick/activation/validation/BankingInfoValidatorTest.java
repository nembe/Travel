package nl.yellowbrick.activation.validation;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.DirectDebitDetailsDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.DirectDebitDetails;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.Optional;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class BankingInfoValidatorTest extends BaseSpringTestCase {

    @InjectMocks BankingInfoValidator validator;
    @Mock DirectDebitDetailsDao directDebitDetailsDao;

    Customer customer;
    Errors errors;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        customer = new Customer();
        customer.setBillingAgentId(602); // DIRECT DEBIT

        errors = new UnboundErrors(customer, "customer");
    }

    @Test
    public void no_op_if_sepa_number_is_unknown() {
        stubDirectDebitDetailsByCustomer(details(123));
        stubDirectDebitDetailsBySepaNumber();

        invokeValidator();

        assertThat(errors.getAllErrors(), empty());
    }

    @Test
    public void rejects_previously_used_iban() {
        stubDirectDebitDetailsByCustomer(details(123));
        stubDirectDebitDetailsBySepaNumber(details(456));

        invokeValidator();

        assertThat(errors.getFieldError("iban").getCode(), equalTo("errors.duplicate"));
    }

    private void invokeValidator() {
        ValidationUtils.invokeValidator(validator, customer, errors);
    }

    private void stubDirectDebitDetailsByCustomer(DirectDebitDetails details) {
        when(directDebitDetailsDao.findForCustomer(anyLong())).thenReturn(Optional.of(details));
    }

    private void stubDirectDebitDetailsBySepaNumber(DirectDebitDetails... details) {
        when(directDebitDetailsDao.findBySepaNumber(anyString())).thenReturn(Lists.newArrayList(details));
    }

    private DirectDebitDetails details(long id) {
        DirectDebitDetails details = new DirectDebitDetails();
        details.setId(id);

        return details;
    }
}
