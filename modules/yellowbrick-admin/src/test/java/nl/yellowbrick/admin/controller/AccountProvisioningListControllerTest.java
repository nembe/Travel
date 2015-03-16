package nl.yellowbrick.admin.controller;

import com.google.common.collect.Lists;
import nl.yellowbrick.admin.builders.CustomerBuilder;
import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.CustomerStatus;
import nl.yellowbrick.data.domain.ProductGroup;
import org.hamcrest.Matcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebAppConfiguration
public class AccountProvisioningListControllerTest extends BaseSpringTestCase {

    private static final String BASE = "/provisioning/accounts";

    @Autowired WebApplicationContext wac;
    @Autowired @InjectMocks AccountProvisioningListController controller;

    @Autowired @Mock CustomerDao customerDao;
    @Autowired ProductGroupDao productGroupDao;

    // test helpers
    MockMvc mockMvc;
    List<ProductGroup> productGroups;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        productGroups = productGroupDao.all();

        stubCustomers();
    }

    @Test
    public void filters_by_first_product_group_by_default() throws Exception {
        ProductGroup defaultProductGroup = productGroups.get(0);

        Document html = doGet("");

        assertThat(html.select("tbody tr"), hasSize(1));
        assertThat(html.select("tbody tr td").text(), containsString(defaultProductGroup.getDescription()));
    }

    @Test
    public void filters_by_chosen_product_group() throws Exception {
        ProductGroup chosenProductGroup = productGroups.get(1);

        Document html = doGet("?productGroup=" + chosenProductGroup.getId());

        assertThat(html, allOf(
                rowCount(2),
                rowsWithProductGroup(chosenProductGroup, 2)));
    }

    @Test
    public void filters_by_customer_status() throws Exception {
        String q = "?productGroup=" + productGroups.get(1).getId();

        assertThat(doGet(q), allOf(
                rowCount(2),
                rowsWithStatus(CustomerStatus.ACTIVATION_FAILED, 1),
                rowsWithStatus(CustomerStatus.REGISTERED, 1)));

        assertThat(doGet(q + "&statusFilter=0"), allOf(
                rowCount(2),
                rowsWithStatus(CustomerStatus.ACTIVATION_FAILED, 1),
                rowsWithStatus(CustomerStatus.REGISTERED, 1)));

        assertThat(doGet(q + "&statusFilter=1"), allOf(
                rowCount(1),
                rowsWithStatus(CustomerStatus.ACTIVATION_FAILED, 1),
                rowsWithStatus(CustomerStatus.REGISTERED, 0)));

        assertThat(doGet(q + "&statusFilter=2"), allOf(
                rowCount(1),
                rowsWithStatus(CustomerStatus.ACTIVATION_FAILED, 0),
                rowsWithStatus(CustomerStatus.REGISTERED, 1)));
    }

    private Document doGet(String path) throws Exception {
        MvcResult res = mockMvc.perform(get(BASE + path)).andReturn();
        return Jsoup.parse(res.getResponse().getContentAsString());
    }

    private void stubCustomers() {
        when(customerDao.findAllPendingActivation()).thenReturn(Lists.newArrayList(
                new CustomerBuilder(productGroups.get(0)).withStatus(CustomerStatus.ACTIVATION_FAILED).build(),
                new CustomerBuilder(productGroups.get(1)).withStatus(CustomerStatus.ACTIVATION_FAILED).build(),
                new CustomerBuilder(productGroups.get(1)).withStatus(CustomerStatus.REGISTERED).build()
        ));
    }

    private Matcher<Document> rowsWithProductGroup(ProductGroup productGroup, int rowCount) {
        return new ArgumentMatcher<Document>() {
            @Override
            public boolean matches(Object o) {
                Document html = (Document) o;

                return html.select("tbody tr td").stream()
                        .filter(element -> element.text().contains(productGroup.getDescription()))
                        .count() == rowCount;
            }
        };
    }

    private Matcher<Document> rowCount(int rowCount) {
        return new ArgumentMatcher<Document>() {
            @Override
            public boolean matches(Object o) {
                Document html = (Document) o;

                return html.select("tbody tr").size() == rowCount;
            }
        };
    }

    private Matcher<Document> rowsWithStatus(CustomerStatus status, int rowCount) {
        return new ArgumentMatcher<Document>() {
            @Override
            public boolean matches(Object o) {
                Document html = (Document) o;

                return html.select("tbody tr td").stream()
                        .filter(element -> element.text().contains(status.name()))
                        .count() == rowCount;
            }
        };
    }
}
