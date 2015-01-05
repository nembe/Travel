package nl.yellowbrick.admin.bootstrap;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

import java.net.URL;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        URL trustStoreUrl = getClass().getClassLoader().getResource("truststore.jks");

        System.setProperty("javax.net.ssl.trustStore", trustStoreUrl.getPath());
        System.setProperty("javax.net.ssl.trustStoreType", "jks");

        return application.sources(Application.class);
    }

}
