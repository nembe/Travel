package nl.yellowbrick.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

@Configuration
public class MvcTestConfiguration {

    // use in-memory authentication instead of hitting ldap during tests
    @Autowired
    public void configureSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("testUser").password("testPassword").roles("USER");
    }
}
