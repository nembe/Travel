package nl.yellowbrick.admin.controller;

import com.google.common.collect.Lists;
import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.ProductGroup;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
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

import static org.hamcrest.Matchers.*;
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

        assertThat(html.select("tbody tr"), hasSize(1));
        assertThat(html.select("tbody tr td").text(), containsString(chosenProductGroup.getDescription()));
    }

    private Document doGet(String path) throws Exception {
        MvcResult res = mockMvc.perform(get(BASE + path)).andReturn();
        return Jsoup.parse(res.getResponse().getContentAsString());
    }

    private void stubCustomers() {
        when(customerDao.findAllPendingActivation()).thenReturn(Lists.newArrayList(
                customerWithProductGroup(productGroups.get(0)),
                customerWithProductGroup(productGroups.get(1))
        ));
    }

    private Customer customerWithProductGroup(ProductGroup productGroup) {
        Customer customer = new Customer();
        customer.setProductGroupId(productGroup.getId().intValue());
        customer.setProductGroup(productGroup.getDescription());

        return customer;
    }
}
