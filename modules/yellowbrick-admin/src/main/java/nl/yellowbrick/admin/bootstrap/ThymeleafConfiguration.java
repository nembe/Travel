package nl.yellowbrick.admin.bootstrap;

import nl.yellowbrick.admin.dialect.BrickwallDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.IDialect;

@Configuration
public class ThymeleafConfiguration {

    @Bean
    public IDialect brickwallDialect() {
        return new BrickwallDialect();
    }
}
