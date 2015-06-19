package nl.yellowbrick.activation.bootstrap;

import nl.yellowbrick.activation.task.AccountActivationTask;
import nl.yellowbrick.activation.task.CardOrderValidationTask;
import nl.yellowbrick.activation.task.CardPoolAssessmentTask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = { "nl.yellowbrick" })
@EnableTransactionManagement
@EnableAutoConfiguration
@EnableScheduling
public class Application {

    private static final Object[] CONTEXT_SOURCES = new Object[] {
            Application.class,
            AccountActivationTask.class,
            CardOrderValidationTask.class,
            CardPoolAssessmentTask.class
    };

    public static void main(String[] args) {
        SpringApplication.run(CONTEXT_SOURCES, args);
    }
}
