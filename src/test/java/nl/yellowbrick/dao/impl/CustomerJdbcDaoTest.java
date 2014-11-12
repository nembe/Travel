package nl.yellowbrick.dao.impl;

import nl.yellowbrick.BaseSpringTestCase;
import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.database.DbHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

public class CustomerJdbcDaoTest extends BaseSpringTestCase {

    @Autowired CustomerJdbcDao customerDao;
    @Autowired DbHelper db;

    @Test
    public void returns_empty_collection_if_no_data() {
        db.truncateTable("CUSTOMER");

        assertThat(customerDao.findAllPendingActivation().size(), equalTo(0));
    }

    @Test
    public void returns_customers_if_data_is_in_place() {
        List<Customer> customers = customerDao.findAllPendingActivation();

        assertThat(customers.size(), equalTo(2));
    }

    @Test
    public void fills_in_customer_bean() {
        Customer c = customerDao.findAllPendingActivation().get(0);

        assertThat(c.getAccountCity(), equalTo("Amsterdam"));
        assertThat(c.getAccountHolderName(), equalTo("M.C.  Slomp"));
        assertThat(c.getAccountNr(), equalTo("539161179"));
        assertThat(c.getAccountType(), equalTo(""));
        assertThat(c.getActionCode(), nullValue());
        assertThat(c.getAgentName(), equalTo("automatische incasso per week"));
        assertThat(c.getApplicationDate(), nullValue());
        assertThat(c.getBillingAgentId(), equalTo(0l));
        assertThat(c.getBusinessName(), equalTo(""));
        assertThat(c.getBusinessTypeId(), equalTo(0l));
        assertThat(c.getBusiness(), equalTo("N"));
        assertThat(c.getCardName(), equalTo(""));
        assertThat(c.getCreditLimit(), equalTo(5000l));
        assertThat(c.getCustomerId(), equalTo(4776l));
        assertThat(c.getCustomerNr(), equalTo("203126"));
        assertThat(c.getCustomerStatusIdfk(), equalTo(1));
        assertThat(c.getDateOfBirth(), nullValue());
        assertThat(c.getDisplayCard(), equalTo(""));
        assertThat(c.getEmail(), equalTo("bestaatniet@taxameter.nl"));
        assertThat(c.getExitDate(), nullValue());
        assertThat(c.getFax(), equalTo(""));
        assertThat(c.getFirstCardLicensePlate(), nullValue());
        assertThat(c.getFirstCardMobile(), nullValue());
        assertThat(c.getFirstName(), equalTo("Mathijn"));
        assertThat(c.getGender(), equalTo("M"));
        assertThat(c.getInfix(), equalTo(""));
        assertThat(c.getInitials(), equalTo("M.C."));
        assertThat(c.getLastName(), equalTo("Slomp"));
        assertThat(c.getMemberDate(), nullValue());
        assertThat(c.getNumberOfQCards(), equalTo(0));
        assertThat(c.getNumberOfRTPCards(), equalTo(0));
        assertThat(c.getNumberOfTCards(), equalTo(1));
        assertThat(c.getParkadammerTotal(), equalTo(0));
        assertThat(c.getPaymentMethod(), equalTo(""));
        assertThat(c.getPhoneNr(), equalTo("0614992123"));
        assertThat(c.getPincode(), equalTo("6858"));
        assertThat(c.getProductGroup(), equalTo(""));
        assertThat(c.getProductGroupID(), equalTo(-1));
        assertThat(c.getStatus(), equalTo(""));
        assertThat(c.getInvoiceAttn(), nullValue());
        assertThat(c.getInvoiceEmail(), nullValue());
        assertThat(c.isExtraInvoiceAnnotations(), equalTo(false));
    }

    @Test
    public void marks_customer_as_pending_review() {
        Customer customer = customerDao.findAllPendingActivation().get(0);
        int activationFailedStatus = 0;

        assertThat(customer.getCustomerStatusIdfk(), equalTo(1));

        customerDao.markAsPendingHumanReview(customer);

        assertThat(customer.getCustomerStatusIdfk(), equalTo(activationFailedStatus));
        assertThat(fetchCustomerStatus(customer.getCustomerId()), equalTo(activationFailedStatus));
    }

    @Test
    public void retrieves_next_value_from_customer_nr_sequence() {
        Customer cust = new Customer();

        customerDao.assignNextCustomerNr(cust);

        assertThat(cust.getCustomerNr(), equalTo("370761"));
    }

    @Test
    public void returns_empty_for_locale_missing() {
        Customer cust = new Customer();
        cust.setCustomerId(12345l);

        assertThat(customerDao.getRegistrationLocale(cust), equalTo(Optional.empty()));
    }

    @Test
    public void returns_empty_if_locale_is_null() {
        db.withTemplate((t) -> t.update("UPDATE CUSTOMER_REGISTRATION SET locale = NULL"));

        assertThat(customerDao.getRegistrationLocale(testCustomer()), equalTo(Optional.empty()));
    }

    @Test
    public void returns_found_locale() {
        assertThat(customerDao.getRegistrationLocale(testCustomer()), equalTo(Optional.of("nl_NL")));
    }

    private int fetchCustomerStatus(long customerId) {
        return db.withTemplate((template) -> {
            return template.queryForObject("SELECT customerstatusidfk FROM CUSTOMER WHERE customerid = ?",
                Integer.class,
                customerId);
        });
    }

    private Customer testCustomer() {
        Customer cust = new Customer();
        cust.setCustomerId(4776);

        return cust;
    }
}