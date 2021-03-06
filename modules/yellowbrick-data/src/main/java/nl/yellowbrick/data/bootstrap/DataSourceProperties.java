package nl.yellowbrick.data.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datasource")
public class DataSourceProperties {

    protected String driverClassName;
    protected String url;
    protected String username;
    protected String password;
    protected String jdniName;

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJdniName() {
        return jdniName;
    }

    public void setJdniName(String jdniName) {
        this.jdniName = jdniName;
    }
}
