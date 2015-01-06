package nl.yellowbrick.admin.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import java.net.URL;

@ComponentScan(basePackages = { "nl.yellowbrick" })
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        setSslEnvironment();
        SpringApplication.run(Application.class, args);
    }

    public static void setSslEnvironment() {
        URL trustStoreUrl = Application.class.getClassLoader().getResource("truststore.jks");

        System.setProperty("javax.net.ssl.trustStore", trustStoreUrl.getPath());
        System.setProperty("javax.net.ssl.trustStoreType", "jks");
    }
}
