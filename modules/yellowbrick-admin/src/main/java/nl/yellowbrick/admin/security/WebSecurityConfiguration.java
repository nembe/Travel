package nl.yellowbrick.admin.security;

import nl.yellowbrick.data.dao.AdministratorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    @ConditionalOnProperty(prefix = "ldap", value = "enabled")
    public LdapContextSource ldapContextSource() {
        return new LdapContextSource();
    }

    @Autowired(required = false)
    public void configureLdapAuth(AuthenticationManagerBuilder auth, LdapContextSource ldapContextSource) throws Exception {
        auth.ldapAuthentication()
                .userSearchBase("OU=BP Users,OU=Waysis - BP,DC=waysis,DC=local")
                .userSearchFilter("(sAMAccountName={0})")
                .contextSource(ldapContextSource)
                .ldapAuthoritiesPopulator(new StaticLdapAuthoritiesPopulator());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(AdministratorDao administratorDao) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        daoAuthenticationProvider.setUserDetailsService(new AdminUserDetailsService(administratorDao));

        return daoAuthenticationProvider;
    }

    @Autowired
    public void configureDatabaseAuth(AuthenticationManagerBuilder auth,
                                      DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider);
    }
}
