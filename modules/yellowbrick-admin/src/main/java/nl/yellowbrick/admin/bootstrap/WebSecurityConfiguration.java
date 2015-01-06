package nl.yellowbrick.admin.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.Collection;

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
    public void configureGlobal(AuthenticationManagerBuilder auth, LdapContextSource ldapContextSource) throws Exception {
        auth.ldapAuthentication()
                .userSearchBase("OU=BP Users,OU=Waysis - BP,DC=waysis,DC=local")
                .userSearchFilter("(sAMAccountName={0})")
                .contextSource(ldapContextSource)
                .ldapAuthoritiesPopulator(authoritiesPopulator());
    }

    private LdapAuthoritiesPopulator authoritiesPopulator() {
        return new LdapAuthoritiesPopulator() {
            @Override
            public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
                return AuthorityUtils.createAuthorityList("ROLE_USER");
            }
        };
    }
}
