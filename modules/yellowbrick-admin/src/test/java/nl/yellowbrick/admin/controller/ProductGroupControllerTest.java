package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.BaseMvcTestCase;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import static nl.yellowbrick.admin.matchers.HtmlMatchers.isCheckbox;
import static nl.yellowbrick.admin.matchers.HtmlMatchers.isField;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class ProductGroupControllerTest extends BaseMvcTestCase {

    @Autowired @InjectMocks ProductGroupController controller;

    @Test
    public void returns_404_if_product_group_not_found() throws Exception {
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

    private ResultActions getProductGroup(String group) throws Exception {
        return mockMvc.perform(get("/productgroups/" + group));
    }
}
