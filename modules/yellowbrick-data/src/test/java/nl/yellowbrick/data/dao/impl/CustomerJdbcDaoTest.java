package nl.yellowbrick.data.dao.impl;

import com.google.common.base.Function;
import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.database.Functions;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static nl.yellowbrick.data.database.Functions.CALL_RECORDERS;
import static nl.yellowbrick.data.database.Functions.FunctionCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;

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

        assertThat(customers.size(), equalTo(4));
    }

    @Test
    public void fills_in_customer_bean() {
        Customer c = customerDao.findAllPendingActivation().get(1);

        assertThat(c, equalTo(testCustomer()));
    }

    @Test
    public void marks_customer_as_pending_review() {
        Customer customer = customerDao.findAllPendingActivation().get(0);

        assertThat(customer.getStatus(), equalTo(CustomerStatus.REGISTERED));

        customerDao.markAsPendingHumanReview(customer);

        assertThat(customer.getStatus(), equalTo(CustomerStatus.ACTIVATION_FAILED));
        assertThat(fetchCustomerStatus(customer.getCustomerId()), equalTo(CustomerStatus.ACTIVATION_FAILED.code()));
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
    public void fetches_customers_by_fuzzy_match_on_name() {
        assertThat(customerDao.findAllByFuzzyName("Rinze", "Opstal"), not(empty()));
        assertThat(customerDao.findAllByFuzzyName("rInZe", "oPsTaL"), not(empty()));
        assertThat(customerDao.findAllByFuzzyName("Rinze ", "   Opstal"), not(empty()));
        assertThat(customerDao.findAllByFuzzyName("Something", "Other"), empty());
    }

    @Test
    public void fetches_customers_by_email() {
        assertThat(customerDao.findAllByEmail("bestaatniet@taxameter.nl"), hasSize(4));
        assertThat(customerDao.findAllByEmail("something@other.com"), empty());
    }

    @Test
    public void delegates_saving_private_customer_to_stored_procedure() throws Exception {
        CountDownLatch lock = new CountDownLatch(1);
        LinkedList<Functions.FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        Customer customer = testCustomer();
        customerDao.savePrivateCustomer(customer);

        lock.await(2, TimeUnit.SECONDS);

        FunctionCall call = calls.getFirst();
        Object[] args = call.arguments;
        String fnName = call.functionName;

        assertThat(fnName, Matchers.equalTo("customerSavePrivateData"));
        assertThat(call.getNumericArg(0).longValue(), equalTo(customer.getCustomerId()));
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

    @Test
    public void delegates_saving_business_customer_to_stored_procedure() throws Exception {
        CountDownLatch lock = new CountDownLatch(1);
        LinkedList<Functions.FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        Customer customer = testCustomer();
        customer.setBusinessName("test business");
        customer.setBusinessTypeId(1);
        customer.setInvoiceAttn("attn");
        customer.setInvoiceEmail("test@test.com");
        customer.setExtraInvoiceAnnotations(true);

        customerDao.saveBusinessCustomer(customer);

        lock.await(2, TimeUnit.SECONDS);

        FunctionCall call = calls.getFirst();
        Object[] args = call.arguments;
        String fnName = call.functionName;

        assertThat(fnName, Matchers.equalTo("customerSaveBusinessData"));
        assertThat(call.getNumericArg(0).longValue(), equalTo(customer.getCustomerId()));
        assertThat(args[1], equalTo(customer.getBusinessName()));
        assertThat(call.getNumericArg(2).longValue(), equalTo(customer.getBusinessTypeId()));
        assertThat(args[3], equalTo(customer.getGender()));
        assertThat(args[4], equalTo(customer.getInitials()));
        assertThat(args[5], equalTo(customer.getFirstName()));
        assertThat(args[6], equalTo(customer.getInfix()));
        assertThat(args[7], equalTo(customer.getLastName()));
        assertThat(args[8], equalTo(customer.getEmail()));
        assertThat(args[9], equalTo(customer.getPhoneNr()));
        assertThat(args[10], equalTo(customer.getFax()));
        assertThat(args[11], equalTo(customer.getDateOfBirth()));
        assertThat(args[12], equalTo(customer.getProductGroupId()));
        assertThat(args[13], equalTo(customer.getInvoiceAttn()));
        assertThat(args[14], equalTo(customer.getInvoiceEmail()));
        assertThat(args[15], equalTo("1"));
        assertThat(args[16], equalTo("TEST MUTATOR"));
    }

    @Test
    public void retrieves_business_identifiers() {
        BusinessIdentifier businessRegistrationNumber = new BusinessIdentifier();
        businessRegistrationNumber.setId(35481);
        businessRegistrationNumber.setLabel("businessRegistrationNumber");
        businessRegistrationNumber.setRequired(true);
        businessRegistrationNumber.setValue("14090089");

        BusinessIdentifier vatNumber = new BusinessIdentifier();
        vatNumber.setId(35482);
        vatNumber.setLabel("vatNumber");
        vatNumber.setRequired(false);

        assertThat(customerDao.getBusinessIdentifiers(398734),
                containsInAnyOrder(businessRegistrationNumber, vatNumber));
    }

    @Test
    public void updates_business_identifier_value() {
        Supplier<BusinessIdentifier> biSupplier = () -> customerDao.getBusinessIdentifiers(398734).get(0);

        BusinessIdentifier businessIdentifier = biSupplier.get();
        businessIdentifier.setValue("booyakasha");

        customerDao.updateBusinessIdentifier(businessIdentifier);

        assertThat(biSupplier.get().getValue(), equalTo("booyakasha"));
    }

    @Test
    public void finds_customer_by_id() {
        Customer customer = customerDao.findById(4776).get();

        assertThat(customer, equalTo(testCustomer()));
    }

    @Test
    public void finds_customer_by_phone_number() {
        List<Customer> customers = customerDao.findAllByMobile("+31641017015");

        assertThat(customers, hasSize(1));
        assertThat(customers.get(0), equalTo(testCustomer()));
    }

    @Test
    public void finds_by_business_identifier() {
        assertThat(customerDao.findAllByBusinessIdentifier("some label", "some value"), empty());
        assertThat(customerDao.findAllByBusinessIdentifier("businessRegistrationNumber", "14090089"), hasSize(1));
    }

    private int fetchCustomerStatus(long customerId) {
        return db.apply((template) -> {
            return template.queryForObject("SELECT customerstatusidfk FROM CUSTOMER WHERE customerid = ?",
                    Integer.class,
                    customerId);
        });
    }

    private Customer testCustomer() {
        Function<LocalDateTime, Timestamp> toTs = (localDateTime) -> {
            return Timestamp.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        };

        Customer cust = new Customer();
        cust.setAccountHolderName("M.C.  Slomp");
        cust.setAccountNr("539161179");
        cust.setAccountType("");
        cust.setActionCode(null);
        cust.setAgentName("automatische incasso per week");
        cust.setApplicationDate(toTs.apply(LocalDateTime.of(2007, 4, 8, 18, 26, 22)));
        cust.setBillingAgentId(602l);
        cust.setBusinessName("");
        cust.setBusinessTypeId(0l);
        cust.setBusiness("N");
        cust.setCreditLimit(5000l);
        cust.setCustomerId(4776);
        cust.setCustomerNr("203126");
        cust.setStatus(CustomerStatus.REGISTERED);
        cust.setDateOfBirth(null);
        cust.setEmail("bestaatniet@taxameter.nl");
        cust.setExitDate(null);
        cust.setFax("");
        cust.setFirstCardLicensePlate("39-LB-40");
        cust.setFirstCardMobile("+31495430798");
        cust.setFirstName("Mathijn");
        cust.setGender("M");
        cust.setInfix("");
        cust.setInitials("M.C.");
        cust.setLastName("Slomp");
        cust.setMemberDate(toTs.apply(LocalDateTime.of(2007, 4, 11, 8, 33, 37)));
        cust.setNumberOfQCards(0);
        cust.setNumberOfRTPCards(0);
        cust.setNumberOfTCards(1);
        cust.setParkadammerTotal(0);
        cust.setPhoneNr("0614992123");
        cust.setPincode("6858");
        cust.setProductGroup("YELLOWBRICK");
        cust.setProductGroupId(1);
        cust.setInvoiceAttn("bar");
        cust.setInvoiceEmail("foo");
        cust.setExtraInvoiceAnnotations(true);

        return cust;
    }
}
