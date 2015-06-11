package nl.yellowbrick.data.bootstrap;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfiguration {

    @Autowired
    private DataSourceProperties properties;

    @ConditionalOnWebApplication
    @ConditionalOnProperty("jndiDatasource")
    @Bean
    public DataSource jndiDataSource(@Value("${jndiDatasource}") String jndiName) {
        return new JndiDataSourceLookup().getDataSource(jndiName);
    }

    @ConditionalOnMissingBean
    @Bean
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setUser(properties.username);
        ds.setPassword(properties.password);
        ds.setJdbcUrl(properties.url);
        ds.setDriverClass(properties.driverClassName);

        return ds;
    }
}
