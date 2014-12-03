package nl.yellowbrick.data;

import nl.yellowbrick.data.database.SingleConnectionDataSourceFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = { "nl.yellowbrick" })
@EnableAutoConfiguration
public class TestConfiguration {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setDataSourceFactory(new SingleConnectionDataSourceFactory())
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("functions.sql")
                .build();
    }
}
