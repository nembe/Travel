package nl.yellowbrick.admin.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll()
                .and()
            .authorizeRequests()
                .anyRequest()
                .authenticated();
    }

    @Bean
    @ConfigurationProperties(prefix = "ldap")
    public LdapContextSource ldapContextSource() {
        return new LdapContextSource();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, LdapContextSource ldapContextSource) throws Exception {
        auth.ldapAuthentication()
                .userSearchBase("OU=BP Users,OU=Waysis - BP,DC=waysis,DC=local")
                .userSearchFilter("(sAMAccountName={0})")
                .contextSource(ldapContextSource);
    }
}
