package nl.yellowbrick.activation.validation;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.DirectDebitDetailsDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.DirectDebitDetails;
import nl.yellowbrick.data.domain.PaymentMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.util.Optional.of;
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
        stubPaymentMethod(PaymentMethod.DIRECT_DEBIT);

        errors = new UnboundErrors(customer, "customer");
    }

    @Test
    public void no_op_if_sepa_number_is_unknown() {
        stubDirectDebitDetailsByCustomer(of(details(123)));
        stubDirectDebitDetailsBySepaNumber();

        invokeValidator();

        assertThat(errors.getAllErrors(), empty());
    }

    @Test
    public void rejects_previously_used_iban() {
        stubDirectDebitDetailsByCustomer(of(details(123)));
        stubDirectDebitDetailsBySepaNumber(details(456));

        invokeValidator();

        assertThat(errors.getFieldError("iban").getCode(), equalTo("errors.duplicate"));
    }

    @Test
    public void flags_direct_debit_customer_lacking_bank_account_info() {
        stubDirectDebitDetailsByCustomer(Optional.empty());

        invokeValidator();

        assertThat(errors.getFieldError("iban").getCode(), equalTo("errors.missing"));
    }

    @Test
    public void flags_credit_card_customer_lacking_credit_card_info() {
        stubDirectDebitDetailsByCustomer(Optional.empty());
        stubPaymentMethod(randomFrom(PaymentMethod.MASTERCARD, PaymentMethod.VISA));

        invokeValidator();

        assertThat(errors.getFieldError("ccname").getCode(), equalTo("errors.missing"));
    }

    private void invokeValidator() {
        ValidationUtils.invokeValidator(validator, customer, errors);
    }

    private void stubDirectDebitDetailsByCustomer(Optional<DirectDebitDetails> details) {
        when(directDebitDetailsDao.findForCustomer(anyLong())).thenReturn(details);
    }

    private void stubDirectDebitDetailsBySepaNumber(DirectDebitDetails... details) {
        when(directDebitDetailsDao.findBySepaNumber(anyString())).thenReturn(Lists.newArrayList(details));
    }

    private void stubPaymentMethod(PaymentMethod paymentMethod) {
        // ugly but there's no incentive besides tests to make the codes visible
        List<Integer> billingAgents = (List<Integer>) ReflectionTestUtils.getField(paymentMethod, "codes");
        customer.setBillingAgentId(billingAgents.get(0));
    }

    private DirectDebitDetails details(long id) {
        DirectDebitDetails details = new DirectDebitDetails();
        details.setId(id);

        return details;
    }

    @SafeVarargs
    private final <T> T randomFrom(T... options) {
        int randomizedIndex = new Random(options.length).nextInt();

        return options[randomizedIndex];
    }
}
