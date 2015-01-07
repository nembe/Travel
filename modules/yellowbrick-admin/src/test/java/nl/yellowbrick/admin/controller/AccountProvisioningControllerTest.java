package nl.yellowbrick.admin.controller;

import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.DirectDebitDetailsDao;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.AddressType;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class AccountProvisioningControllerTest extends BaseSpringTestCase {

    // ids of customers pending manual validation
    private static final long PRIVATE_CUSTOMER_ID = 394744;
    private static final long BUSINESS_CUSTOMER_ID = 398734;

    // under test
    @Autowired WebApplicationContext wac;
    @Autowired @InjectMocks AccountProvisioningController controller;

    // spy on collaborators
    @Autowired @Spy CustomerDao customerDao;
    @Autowired @Spy CustomerAddressDao addressDao;
    @Autowired @Spy DirectDebitDetailsDao directDebitDetailsDao;
    @Autowired @Mock AccountActivationService accountActivationService;

    // test helpers
    @Autowired DbHelper db;
    MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void returns_404_if_customer_id_not_found() throws Exception {
        mockMvc.perform(get("/provisioning/12345")).andExpect(status().is(404));
    }

    @Test
    public void returns_409_if_customer_address_not_found() throws Exception {
        db.truncateTable("CUSTOMERADDRESS");

        mockMvc.perform(get("/provisioning/" + PRIVATE_CUSTOMER_ID)).andExpect(status().is(409));
    }

    @Test
    public void shows_account_registration_validation_errors() throws Exception {
        // make use of the age restriction to check validations
        db.accept((t) -> t.update("UPDATE CUSTOMER SET DATEOFBIRTH = NULL WHERE CUSTOMERID = ?", PRIVATE_CUSTOMER_ID));

        MvcResult res = mockMvc.perform(get("/provisioning/" + PRIVATE_CUSTOMER_ID)).andReturn();

        assertThat(res.getResponse().getStatus(), equalTo(200));
        assertThat(res.getResponse().getContentAsString(), containsString("must be specified"));
    }

    @Test
    public void shows_iban_if_direct_debit() throws Exception {
        MvcResult res = mockMvc.perform(get("/provisioning/" + PRIVATE_CUSTOMER_ID)).andReturn();

        assertThat(res.getResponse().getContentAsString(), containsString("NL39 RABO 0300 0652 64"));
    }

    @Test
    public void shows_credit_card_type() throws Exception {
        db.accept((t) -> {
            // change billing agent to match visa
            t.update("UPDATE CUSTOMER SET BILLINGAGENTIDFK = ? WHERE CUSTOMERID = ?", 601, PRIVATE_CUSTOMER_ID);
        });

        MvcResult res = mockMvc.perform(get("/provisioning/" + PRIVATE_CUSTOMER_ID)).andReturn();

        assertThat(res.getResponse().getContentAsString(), containsString("Visa"));
    }

    @Test
    public void shows_transponder_card_association_data() throws Exception {
        MvcResult res = mockMvc.perform(get("/provisioning/" + PRIVATE_CUSTOMER_ID)).andReturn();

        assertThat(res.getResponse().getContentAsString(), containsString("0616545500"));
        assertThat(res.getResponse().getContentAsString(), containsString("27-HZZ-9"));
    }

    @Test
    public void saves_changes_to_customer_and_address() throws Exception {
        postPersonalAccountProvisioningForm();

        verify(customerDao).savePrivateCustomer(argThat(isUpdatedPrivateCustomer()));
        verify(addressDao).savePrivateCustomerAddress(eq(PRIVATE_CUSTOMER_ID), argThat(isUpdatedAddress()));
    }

    @Test
    public void activates_private_customer_account() throws Exception {
        postPersonalAccountProvisioningForm();

        verify(accountActivationService).activateCustomerAccount(argThat(isUpdatedPrivateCustomer()), any());
    }

    @Test
    public void shows_different_form_for_business_accounts() throws Exception {
        MvcResult res = mockMvc.perform(get("/provisioning/" + BUSINESS_CUSTOMER_ID)).andReturn();

        assertThat(res.getModelAndView().getViewName(), is("provisioning/validate_business"));
    }

    @Test
    public void saves_business_customer_data() throws Exception {
        postBusinessAccountProvisioningForm();

        Matcher<CustomerAddress> isMainAddress = new ArgumentMatcher<CustomerAddress>() {
            @Override
            public boolean matches(Object o) {
                CustomerAddress address = (CustomerAddress) o;

                return address.getAddress().equals("North Orange")
                        && address.getHouseNr().equals("1209")
                        && address.getCity().equals("Delaware");
            }
        };

        Matcher<CustomerAddress> isBillingAddress = new ArgumentMatcher<CustomerAddress>() {
            @Override
            public boolean matches(Object o) {
                CustomerAddress address = (CustomerAddress) o;

                return address.getAddress().equals("Evergreen Terrace")
                        && address.getHouseNr().equals("742")
                        && address.getCity().equals("Springfield");
            }
        };

        Matcher<BusinessIdentifier> isUpdatedBusinessIdentifier = new ArgumentMatcher<BusinessIdentifier>() {
            @Override
            public boolean matches(Object o) {
                BusinessIdentifier bi = (BusinessIdentifier) o;

                return bi.getValue().equals("12345678") && bi.getId() == 123;
            }
        };


        // saves customer data
        verify(customerDao).saveBusinessCustomer(argThat(isUpdatedBusinessCustomer()));
        // saves main address
        verify(addressDao).saveBusinessCustomerAddress(eq(BUSINESS_CUSTOMER_ID), argThat(isMainAddress),
                eq(AddressType.MAIN));
        // saves billing address
        verify(addressDao).saveBusinessCustomerAddress(eq(BUSINESS_CUSTOMER_ID), argThat(isBillingAddress),
                eq(AddressType.BILLING));
        // updates business identifiers
        verify(customerDao).updateBusinessIdentifier(argThat(isUpdatedBusinessIdentifier));
    }

    @Test
    public void activates_business_customer_account() throws Exception {
        postBusinessAccountProvisioningForm();

        verify(accountActivationService).activateCustomerAccount(argThat(isUpdatedBusinessCustomer()), any());
    }

    @Test
    public void shows_subscription_status() throws Exception {
        mockMvc.perform(get("/provisioning/" + PRIVATE_CUSTOMER_ID)).andExpect(content().string(containsString(
                "Subscription active"
        )));

        mockMvc.perform(get("/provisioning/" + BUSINESS_CUSTOMER_ID)).andExpect(content().string(containsString(
                "Subscription inactive"
        )));
    }

    private MvcResult postPersonalAccountProvisioningForm() throws Exception {
        return mockMvc.perform(post("/provisioning/" + PRIVATE_CUSTOMER_ID)
                .param("email", "some.other.email@test.com")
                .param("street", "middle of nowhere")
                .param("numberOfPPlusCards", "2")
                .param("dateOfBirth", "07-09-1985")
                .param("validatePersonalAccount", "Submit")
        ).andReturn();
    }

    private MvcResult postBusinessAccountProvisioningForm() throws Exception  {
        return mockMvc.perform(post("/provisioning/" + BUSINESS_CUSTOMER_ID)
                .param("businessName", "ACME inc")
                .param("email", "ceo@business.com")
                .param("street", "North Orange")
                .param("houseNr", "1209")
                .param("city", "Delaware")
                .param("billingAddressStreet", "Evergreen Terrace")
                .param("billingAddressHouseNr", "742")
                .param("billingAddressCity", "Springfield")
                .param("numberOfPPlusCards", "1")
                .param("dateOfBirth", "07-09-1985")
                .param("businessIdentifiers[0].id", "123")
                .param("businessIdentifiers[0].value", "12345678")
                .param("validateBusinessAccount", "Submit")
        ).andReturn();
    }

    private Matcher<Customer> isUpdatedPrivateCustomer() {
        return new ArgumentMatcher<Customer>() {
            @Override
            public boolean matches(Object o) {
                Customer cust = (Customer) o;

                return cust.getCustomerId() == PRIVATE_CUSTOMER_ID
                        && cust.getEmail().equals("some.other.email@test.com")
                        && cust.getNumberOfQCards() == 2
                        && cust.getDateOfBirth().equals(Date.valueOf("1985-09-07"));

            }
        };
    }

    private Matcher<Customer> isUpdatedBusinessCustomer() {
        return new ArgumentMatcher<Customer>() {
            @Override
            public boolean matches(Object o) {
                Customer cust = (Customer) o;

                return cust.getCustomerId() == BUSINESS_CUSTOMER_ID
                        && cust.getBusinessName().equals("ACME inc")
                        && cust.getEmail().equals("ceo@business.com")
                        && cust.getNumberOfQCards() == 1
                        && cust.getDateOfBirth().equals(Date.valueOf("1985-09-07"));

            }
        };
    }

    private Matcher<CustomerAddress> isUpdatedAddress() {
        return new ArgumentMatcher<CustomerAddress>() {
            @Override
            public boolean matches(Object o) {
                CustomerAddress address = (CustomerAddress) o;

                return address.getAddress().equals("middle of nowhere");
            }
        };
    }
}
