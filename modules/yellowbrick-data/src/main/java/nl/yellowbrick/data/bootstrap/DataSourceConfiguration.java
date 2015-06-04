package nl.yellowbrick.data.bootstrap;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

import static com.google.common.base.Strings.isNullOrEmpty;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfiguration {

    private static final String DEFAULT_JNDI_NAME = "jdbc/pooledDS";

    @Autowired
    private DataSourceProperties properties;

    @ConditionalOnProperty("datasource.url")
    @Bean
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setUser(properties.username);
        ds.setPassword(properties.password);
        ds.setJdbcUrl(properties.url);
        ds.setDriverClass(properties.driverClassName);

        return ds;
    }

    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    @Bean
    public DataSource jndiDataSource() {
        String name = isNullOrEmpty(properties.jdniName)
                ? DEFAULT_JNDI_NAME
                : properties.jdniName;

        return new JndiDataSourceLookup().getDataSource(name);
    }
}
