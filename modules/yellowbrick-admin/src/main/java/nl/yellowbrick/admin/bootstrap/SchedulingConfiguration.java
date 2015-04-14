package nl.yellowbrick.admin.bootstrap;

import nl.yellowbrick.admin.service.CardOrderExportScheduler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties
@EnableScheduling
public class SchedulingConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "orderexport")
    public CardOrderExportScheduler.Config loadCardExportSchedule() {
        return new CardOrderExportScheduler.Config();
    }
}
