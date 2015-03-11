package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.BaseMvcTestCase;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Date;

import static nl.yellowbrick.admin.matchers.HtmlMatchers.isCheckbox;
import static nl.yellowbrick.admin.matchers.HtmlMatchers.isField;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class ProductGroupControllerTest extends BaseMvcTestCase {

    @Autowired @InjectMocks ProductGroupController controller;
    @Autowired @Spy ProductGroupDao productGroupDao;

    @Test
    public void returns_404_if_product_group_not_found_on_get() throws Exception {
        getProductGroup("something").andExpect(status().is(404));
    }

    @Test
    public void renders_prefilled_product_group_form() throws Exception {
        Document html = parseHtml(mockMvc.perform(get("/productgroups/travelcard")).andReturn());

        assertThat(html.select(".field input"), hasItems(
                isField("description", "TRAVELCARD"),
                isField("startDate", "2012-09-03"),
                isField("endDate", "2015-09-03"),
                isField("maxAnnotations", "8"),
                isCheckbox("internalCardProvisioning", true)
        ));
    }

    @Test
    public void returns_404_if_product_group_update_fails() throws Exception {
        // it's only likely to fail if someone messes with the form parameters or tries to post an update
        // using an outdated description of the product group

        postProductGroupUpdate("something", 123).andExpect(status().is(404));
    }

    @Test
    public void updates_product_group() throws Exception {
        postProductGroupUpdate("travelcard", 3).andExpect(redirectedUrl("/productgroups/updated"));

        verify(productGroupDao).update(eq(updatedProductGroup()));
    }

    private ResultActions getProductGroup(String group) throws Exception {
        return mockMvc.perform(get("/productgroups/" + group));
    }

    private ResultActions postProductGroupUpdate(String group, int id) throws Exception  {
        return mockMvc.perform(post("/productgroups/" + group)
                .param("id", String.valueOf(id))
                .param("description", "updated")
                .param("startDate", "2015-01-01")
                .param("endDate", "2020-01-01")
                .param("maxAnnotations", "5")
                .param("internalCardProvisioning", "true")
                .param("save", "Save"));
    }

    private ProductGroup updatedProductGroup() {
        ProductGroup pg = new ProductGroup();
        pg.setId(3l);
        pg.setDescription("updated");
        pg.setStartDate(Date.valueOf("2015-01-01"));
        pg.setEndDate(Date.valueOf("2020-01-01"));
        pg.setMaxAnnotations(5);
        pg.setInternalCardProvisioning(true);

        return pg;
    }
}
