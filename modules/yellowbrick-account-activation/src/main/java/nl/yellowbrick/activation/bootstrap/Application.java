package nl.yellowbrick.activation.bootstrap;

import nl.yellowbrick.activation.task.AccountActivationTask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = { "nl.yellowbrick" })
@EnableTransactionManagement
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(new Object[] { Application.class, AccountActivationTask.class }, args);
    }
}
