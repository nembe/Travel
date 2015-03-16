package nl.yellowbrick.admin.controller;

import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.DirectDebitDetailsDao;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.AddressType;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.hamcrest.Matcher;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.sql.Date;

import static nl.yellowbrick.admin.matchers.HtmlMatchers.hasAttr;
import static nl.yellowbrick.admin.matchers.HtmlMatchers.isField;
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
public class AccountProvisioningFormControllerTest extends BaseMvcTestCase {

    private static final String BASE = "/provisioning/accounts/";

    // ids of customers pending manual validation
    private static final long PRIVATE_CUSTOMER_ID = 394744;
    private static final long BUSINESS_CUSTOMER_ID = 398734;

    // under test
    @Autowired @InjectMocks AccountProvisioningFormController controller;

    // spy on collaborators
    @Autowired @Spy CustomerDao customerDao;
    @Autowired @Spy CustomerAddressDao addressDao;
    @Autowired @Spy DirectDebitDetailsDao directDebitDetailsDao;
    @Autowired @Mock AccountActivationService accountActivationService;

    // test helpers
    @Autowired DbHelper db;

    @Test
    public void returns_404_if_customer_id_not_found() throws Exception {
        mockMvc.perform(get(BASE + "12345")).andExpect(status().is(404));
    }

    @Test
    public void returns_409_if_customer_address_not_found() throws Exception {
        db.truncateTable("CUSTOMERADDRESS");

        mockMvc.perform(get(BASE + PRIVATE_CUSTOMER_ID)).andExpect(status().is(409));
    }

    @Test
    public void shows_account_registration_validation_errors() throws Exception {
        // make use of the age restriction to check validations
        db.accept((t) -> t.update("UPDATE CUSTOMER SET DATEOFBIRTH = NULL WHERE CUSTOMERID = ?", PRIVATE_CUSTOMER_ID));

        MvcResult res = mockMvc.perform(get(BASE + PRIVATE_CUSTOMER_ID)).andReturn();

        assertThat(res.getResponse().getStatus(), equalTo(200));
        assertThat(res.getResponse().getContentAsString(), containsString("must be specified"));
    }

    @Test
    public void shows_iban_if_direct_debit() throws Exception {
        MvcResult res = mockMvc.perform(get(BASE + PRIVATE_CUSTOMER_ID)).andReturn();

        assertThat(res.getResponse().getContentAsString(), containsString("NL39 RABO 0300 0652 64"));
    }

    @Test
    public void shows_credit_card_type() throws Exception {
        db.accept((t) -> {
            // change billing agent to match visa
            t.update("UPDATE CUSTOMER SET BILLINGAGENTIDFK = ? WHERE CUSTOMERID = ?", 601, PRIVATE_CUSTOMER_ID);
        });

        MvcResult res = mockMvc.perform(get(BASE + PRIVATE_CUSTOMER_ID)).andReturn();

        assertThat(res.getResponse().getContentAsString(), containsString("Visa"));
    }

    @Test
    public void shows_transponder_card_association_data() throws Exception {
        MvcResult res = mockMvc.perform(get(BASE + PRIVATE_CUSTOMER_ID)).andReturn();

        assertThat(res.getResponse().getContentAsString(), containsString("0616545500"));
        assertThat(res.getResponse().getContentAsString(), containsString("27-HZZ-9"));
    }

    @Test
    public void saves_changes_to_customer_and_address() throws Exception {
        mockMvc.perform(postPersonalAccountProvisioningForm()).andReturn();

        verify(customerDao).savePrivateCustomer(argThat(isUpdatedPrivateCustomer()));
        verify(addressDao).savePrivateCustomerAddress(eq(PRIVATE_CUSTOMER_ID), argThat(isUpdatedAddress()));
    }

    @Test
    public void activates_private_customer_account() throws Exception {
        mockMvc.perform(postPersonalAccountProvisioningForm()).andReturn();

        verify(accountActivationService).activateCustomerAccount(argThat(isUpdatedPrivateCustomer()), any());
    }

    @Test
    public void shows_different_form_for_business_accounts() throws Exception {
        MvcResult res = mockMvc.perform(get(BASE + BUSINESS_CUSTOMER_ID)).andReturn();

        assertThat(res.getModelAndView().getViewName(), is("provisioning/accounts/validate_business"));
    }

    @Test
    public void loads_private_customer_data() throws Exception {
        Document html = parseHtml(mockMvc.perform(get(BASE + PRIVATE_CUSTOMER_ID)).andReturn());

        assertThat(html.select(".field input"), hasItems(
                isField("initials", "W.J."),
                isField("firstName", "Wietse"),
                isField("lastName", "Scheltinga"),
                isField("dateOfBirth", "31-12-1981"),
                isField("email", "bestaatniet@taxameter.nl"),
                isField("phoneNr", "0616545500"),
                isField("street", "Davisstraat"),
                isField("houseNr", "42"),
                isField("supplement", "I"),
                isField("postalCode", "1057 TL"),
                isField("city", "Amsterdam"),
                isField("country", "NL")
        ));
    }

    @Test
    public void loads_business_customer_data() throws Exception {
        Document html = parseHtml(mockMvc.perform(get(BASE + BUSINESS_CUSTOMER_ID)).andReturn());

        assertThat(html.select(".field input"), hasItems(
                isField("businessName", "kabisa"),
                isField("businessIdentifiers[0].label", "businessRegistrationNumber"),
                isField("businessIdentifiers[0].value", "14090089"),
                isField("businessIdentifiers[1].label", "vatNumber"),
                isField("businessIdentifiers[1].value", ""),
                isField("initials", "M.R."),
                isField("firstName", "Rui"),
                isField("lastName", "Salgado"),
                isField("dateOfBirth", "07-09-1985"),
                isField("email", "rui.salgado@kabisa.nl"),
                isField("phoneNr", "+31495430798"),
                isField("street", "Marconilaan"),
                isField("houseNr", "8"),
                isField("supplement", ""),
                isField("postalCode", "6003 DD"),
                isField("city", "Weert"),
                isField("country", "NL"),
                isField("billingAddressPoBox", ""),
                isField("billingAddressStreet", "Kleine Gartmanplantsoen"),
                isField("billingAddressHouseNr", "10"),
                isField("billingAddressSupplement", ""),
                isField("billingAddressPostalCode", "1017 RR"),
                isField("billingAddressCity", "Amsterdam"),
                isField("billingAddressCountry", "NL")
        ));
    }

    @Test
    public void saves_business_customer_data() throws Exception {
        mockMvc.perform(postBusinessAccountProvisioningForm()).andReturn();

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
        mockMvc.perform(postBusinessAccountProvisioningForm()).andReturn();

        verify(accountActivationService).activateCustomerAccount(argThat(isUpdatedBusinessCustomer()), any());
    }

    @Test
    public void shows_subscription_status() throws Exception {
        mockMvc.perform(get(BASE + PRIVATE_CUSTOMER_ID)).andExpect(content().string(containsString(
                "Subscription active"
        )));

        mockMvc.perform(get(BASE + BUSINESS_CUSTOMER_ID)).andExpect(content().string(containsString(
                "Subscription inactive"
        )));
    }

    @Test
    public void deletes_billing_address_when_set_to_same_as_business_address() throws Exception {
        CustomerAddress billingAddress = addressDao.findByCustomerId(BUSINESS_CUSTOMER_ID, AddressType.BILLING).get();

        mockMvc.perform(
                // set billing addr = main addr
                postBusinessAccountProvisioningForm().param("billingAddressSameAsMailingAddress", "true")
        ).andReturn();

        verify(addressDao).deleteAddress(eq(billingAddress));
    }

    @Test
    public void validates_binding_errors_on_private_customer_form() throws Exception {
        MvcResult res = mockMvc.perform(
                postPersonalAccountProvisioningForm().param("numberOfTransponderCards", "totally not a number")
        ).andReturn();

        Document html = parseHtml(res);

        assertThat(html.select("input[name=numberOfTransponderCards]").first(), allOf(
                hasAttr("class", "field-error"),
                hasAttr("value", "totally not a number")
        ));

        verifyZeroInteractions(accountActivationService);
    }

    @Test
    public void validates_binding_errors_on_business_customer_form() throws Exception {
        MvcResult res = mockMvc.perform(
                postBusinessAccountProvisioningForm().param("numberOfTransponderCards", "totally not a number")
        ).andReturn();

        Document html = parseHtml(res);

        assertThat(html.select("input[name=numberOfTransponderCards]").first(), allOf(
                hasAttr("class", "field-error"),
                hasAttr("value", "totally not a number")
        ));

        verifyZeroInteractions(accountActivationService);
    }

    private MockHttpServletRequestBuilder postPersonalAccountProvisioningForm() throws Exception {
        return post("/provisioning/accounts/" + PRIVATE_CUSTOMER_ID)
                .param("email", "some.other.email@test.com")
                .param("street", "middle of nowhere")
                .param("numberOfPPlusCards", "2")
                .param("dateOfBirth", "07-09-1985")
                .param("validatePersonalAccount", "Submit");
    }

    private MockHttpServletRequestBuilder postBusinessAccountProvisioningForm() throws Exception  {
        return post("/provisioning/accounts/" + BUSINESS_CUSTOMER_ID)
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
                .param("validateBusinessAccount", "Submit");
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
