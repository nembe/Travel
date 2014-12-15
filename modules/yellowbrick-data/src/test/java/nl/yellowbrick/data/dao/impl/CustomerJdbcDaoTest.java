package nl.yellowbrick.data.dao.impl;

import com.google.common.base.Function;
import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.database.Functions;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

public class CustomerJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    CustomerJdbcDao customerDao;
    @Autowired DbHelper db;

    @Test
    public void returns_empty_collection_if_no_data() {
        db.truncateTable("CUSTOMER");

        assertThat(customerDao.findAllPendingActivation().size(), equalTo(0));
    }

    @Test
    public void returns_customers_if_data_is_in_place() {
        List<Customer> customers = customerDao.findAllPendingActivation();

        assertThat(customers.size(), equalTo(3));
    }

    @Test
    public void fills_in_customer_bean() {
        Customer c = customerDao.findAllPendingActivation().get(1);

        Function<LocalDateTime, Timestamp> toTs = (localDateTime) -> {
            return Timestamp.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        };

        assertThat(c.getAccountCity(), equalTo("Amsterdam"));
        assertThat(c.getAccountHolderName(), equalTo("M.C.  Slomp"));
        assertThat(c.getAccountNr(), equalTo("539161179"));
        assertThat(c.getAccountType(), equalTo(""));
        assertThat(c.getActionCode(), nullValue());
        assertThat(c.getAgentName(), equalTo("automatische incasso per week"));
        assertThat(c.getApplicationDate(), equalTo(toTs.apply(LocalDateTime.of(2007, 4, 8, 18, 26, 22))));
        assertThat(c.getBillingAgentId(), equalTo(602l));
        assertThat(c.getBusinessName(), equalTo(""));
        assertThat(c.getBusinessTypeId(), equalTo(0l));
        assertThat(c.getBusiness(), equalTo("N"));
        assertThat(c.getCreditLimit(), equalTo(5000l));
        assertThat(c.getCustomerId(), equalTo(4776l));
        assertThat(c.getCustomerNr(), equalTo("203126"));
        assertThat(c.getCustomerStatusIdfk(), equalTo(1));
        assertThat(c.getDateOfBirth(), nullValue());
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
        assertThat(c.getMemberDate(), equalTo(toTs.apply(LocalDateTime.of(2007, 4, 11, 8, 33, 37))));
        assertThat(c.getNumberOfQCards(), equalTo(0));
        assertThat(c.getNumberOfRTPCards(), equalTo(0));
        assertThat(c.getNumberOfTCards(), equalTo(1));
        assertThat(c.getParkadammerTotal(), equalTo(0));
        assertThat(c.getPaymentMethod(), equalTo(""));
        assertThat(c.getPhoneNr(), equalTo("0614992123"));
        assertThat(c.getPincode(), equalTo("6858"));
        assertThat(c.getProductGroup(), equalTo("YELLOWBRICK"));
        assertThat(c.getProductGroupId(), equalTo(1));
        assertThat(c.getStatus(), equalTo("lblSignedIt"));
        assertThat(c.getInvoiceAttn(), equalTo("bar"));
        assertThat(c.getInvoiceEmail(), equalTo("foo"));
        assertThat(c.isExtraInvoiceAnnotations(), is(true));
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
        db.accept((t) -> t.update("UPDATE CUSTOMER_REGISTRATION SET locale = NULL"));

        assertThat(customerDao.getRegistrationLocale(testCustomer()), equalTo(Optional.empty()));
    }

    @Test
    public void returns_found_locale() {
        assertThat(customerDao.getRegistrationLocale(testCustomer()), equalTo(Optional.of("nl_NL")));
    }

    @Test
    public void fetches_customers_by_fuzzy_match_on_name_and_date() {
        Date date = Date.from(Instant.parse("1965-11-15T12:34:56.789Z")); // time portion is expected to be ignored

        assertThat(customerDao.findAllByFuzzyNameAndDateOfBirth("Rinze", "Opstal", date), not(empty()));
        assertThat(customerDao.findAllByFuzzyNameAndDateOfBirth("rInZe", "oPsTaL", date), not(empty()));
        assertThat(customerDao.findAllByFuzzyNameAndDateOfBirth("Rinze ", "   Opstal", date), not(empty()));
        assertThat(customerDao.findAllByFuzzyNameAndDateOfBirth("Something", "Other", date), empty());
    }

    @Test
    public void fetches_customers_by_email() {
        assertThat(customerDao.findAllByEmail("bestaatniet@taxameter.nl"), hasSize(3));
        assertThat(customerDao.findAllByEmail("something@other.com"), empty());
    }

    @Test
    public void delegates_saving_private_customer_to_stored_procedure() throws Exception {
        CountDownLatch lock = new CountDownLatch(1);
        LinkedList<Functions.FunctionCall> calls = new LinkedList<>();

        Functions.CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        Customer customer = testCustomer();
        customerDao.savePrivateCustomer(customer);

        lock.await(2, TimeUnit.SECONDS);

        Object[] args = calls.getFirst().arguments;
        String fnName = calls.getFirst().functionName;

        assertThat(fnName, Matchers.equalTo("customerSavePrivateData"));
        assertThat(Long.parseLong(args[0].toString()), equalTo(customer.getCustomerId()));
        assertThat(args[1], equalTo(customer.getGender()));
        assertThat(args[2], equalTo(customer.getInitials()));
        assertThat(args[3], equalTo(customer.getFirstName()));
        assertThat(args[4], equalTo(customer.getInfix()));
        assertThat(args[5], equalTo(customer.getLastName()));
        assertThat(args[6], equalTo(customer.getEmail()));
        assertThat(args[7], equalTo(customer.getPhoneNr()));
        assertThat(args[8], equalTo(customer.getFax()));
        assertThat(args[9], equalTo(customer.getDateOfBirth()));
        assertThat(args[10], equalTo(customer.getProductGroupId()));
        assertThat(args[11], equalTo("TEST MUTATOR"));
    }

    private int fetchCustomerStatus(long customerId) {
        return db.apply((template) -> {
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
