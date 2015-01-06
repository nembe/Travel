package nl.yellowbrick.admin.controller;

import com.google.common.net.HttpHeaders;
import nl.yellowbrick.admin.BaseMvcTestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebAppConfiguration
public class AuthenticationControllerTest extends BaseMvcTestCase {

    @Autowired WebApplicationContext wac;
    @Autowired FilterChainProxy springSecurityFilterChain;
    @Autowired AuthenticationController controller;

    MockMvc mockMvc;
    MockHttpSession mockHttpSession;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(springSecurityFilterChain) // make sure security filter is in place for this test
                .build();

        // have some random session in place
        mockHttpSession = new MockHttpSession(wac.getServletContext(), UUID.randomUUID().toString());
    }

    @Test
    public void adds_csrf_token() throws Exception {
        MvcResult getLogin = mockMvc.perform(get("/login")).andReturn();

        assertThat(extractCsrfToken(getLogin), not(isEmptyOrNullString()));
    }

    @Test
    public void authenticates_user_and_redirects_to_home() throws Exception {
        String csrf = extractCsrfToken(mockMvc.perform(get("/login").session(mockHttpSession)).andReturn());

        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .session(mockHttpSession)
                .param("_csrf", csrf)
                .param("username", "testUser")
                .param("password", "testPassword")).andReturn();

        assertThat(result.getResponse().getStatus(), is(302));
        assertThat(result.getResponse().getHeader(HttpHeaders.LOCATION), is("/"));
    }

    @Test
    public void sets_error_param_on_failed_authentication() throws Exception {
        String csrf = extractCsrfToken(mockMvc.perform(get("/login").session(mockHttpSession)).andReturn());

        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .session(mockHttpSession)
                .param("_csrf", csrf)
                .param("username", "testUser")
                .param("password", "wrongPassword")).andReturn();

        assertThat(result.getResponse().getStatus(), is(302));
        assertThat(result.getResponse().getHeader(HttpHeaders.LOCATION), is("/login?error"));
    }

    @Test
    public void displays_authentication_error() throws Exception {
        mockMvc.perform(get("/login?error").session(mockHttpSession))
                .andExpect(content().string(containsString("class=\"global-errors\"")));
    }

    private String extractCsrfToken(MvcResult mvcResult) throws Exception {
        Matcher csrfMatcher = Pattern.compile(".*input type=\"hidden\" name=\"_csrf\" value=\"(.*?)\".*", Pattern.DOTALL)
                .matcher(mvcResult.getResponse().getContentAsString());

        if(!csrfMatcher.find())
            throw new IllegalStateException("csrf token not found");

        return csrfMatcher.group(1);
    }
}
