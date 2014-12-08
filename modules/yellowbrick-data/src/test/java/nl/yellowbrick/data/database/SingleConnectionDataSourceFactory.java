package nl.yellowbrick.data.database;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.DataSourceFactory;

import javax.sql.DataSource;
import java.sql.Driver;

// avoids issues with transaction isolation in tests
public class SingleConnectionDataSourceFactory implements DataSourceFactory {

    private final SingleConnectionDataSource dataSource = new SingleConnectionDataSource();

    public SingleConnectionDataSourceFactory() {
        dataSource.setSuppressClose(true);
    }

    @Override
    public ConnectionProperties getConnectionProperties() {
        return new ConnectionProperties() {
            @Override
            public void setDriverClass(Class<? extends Driver> driverClass) {
                dataSource.setDriverClassName(driverClass.getName());
            }

            @Override
            public void setUrl(String url) {
                dataSource.setUrl(url);
            }

            @Override
            public void setUsername(String username) {
                dataSource.setUsername(username);
            }

            @Override
            public void setPassword(String password) {
                dataSource.setPassword(password);
            }
        };
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
