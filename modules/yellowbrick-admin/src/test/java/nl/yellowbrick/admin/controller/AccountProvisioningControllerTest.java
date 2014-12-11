package nl.yellowbrick.admin.controller;

import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.database.DbHelper;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class AccountProvisioningControllerTest extends BaseSpringTestCase {

    // id of customer pending manual validation
    private static final long CUSTOMER_ID = 394744;

    // under test
    @Autowired WebApplicationContext wac;
    @Autowired @InjectMocks AccountProvisioningController controller;

    // spy on collaborators
    @Autowired @Spy CustomerDao customerDao;
    @Autowired @Spy CustomerAddressDao addressDao;
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

        mockMvc.perform(get("/provisioning/" + CUSTOMER_ID)).andExpect(status().is(409));
    }

    @Test
    public void shows_account_registration_validation_errors() throws Exception {
        // make use of the age restriction to check validations
        db.accept((t) -> t.update("UPDATE CUSTOMER SET DATEOFBIRTH = NULL WHERE CUSTOMERID = ?", CUSTOMER_ID));

        MvcResult res = mockMvc.perform(get("/provisioning/" + CUSTOMER_ID)).andReturn();

        assertThat(res.getResponse().getStatus(), equalTo(200));
        assertThat(res.getResponse().getContentAsString(), containsString("must be specified"));
    }

    @Test
    public void saves_changes_to_customer_and_address() throws Exception {
        // TODO get rid of these mocks
        doNothing().when(customerDao).savePrivateCustomer(any());
        doNothing().when(addressDao).savePrivateCustomerAddress(anyLong(), any());

        postAccountProvisioningForm();

        verify(customerDao).savePrivateCustomer(argThat(isUpdatedCustomer()));
        verify(addressDao).savePrivateCustomerAddress(eq(CUSTOMER_ID), argThat(isUpdatedAddress()));
    }

    @Test
    public void activates_customer_account() throws Exception {
        // TODO get rid of these mocks
        doNothing().when(customerDao).savePrivateCustomer(any());
        doNothing().when(addressDao).savePrivateCustomerAddress(anyLong(), any());

        postAccountProvisioningForm();

        verify(accountActivationService).activateCustomerAccount(any());
    }

    private MvcResult postAccountProvisioningForm() throws Exception {
        return mockMvc.perform(post("/provisioning/" + CUSTOMER_ID)
                .param("email", "some.other.email@test.com")
                .param("street", "middle of nowhere")
                .param("numberOfPPlusCards", "2")
                .param("dateOfBirth", "07-09-1985")
                .param("validate", "Submit") // TODO do we need this here ?
                // TODO change and test remaining fields
        ).andReturn();
    }

    private Matcher<Customer> isUpdatedCustomer() {
        return new ArgumentMatcher<Customer>() {
            @Override
            public boolean matches(Object o) {
                Customer cust = (Customer) o;

                return cust.getCustomerId() == CUSTOMER_ID
                        && cust.getEmail().equals("some.other.email@test.com")
                        && cust.getNumberOfQCards() == 2
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
