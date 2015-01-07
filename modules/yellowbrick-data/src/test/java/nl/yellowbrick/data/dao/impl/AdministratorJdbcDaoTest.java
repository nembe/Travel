package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.domain.Administrator;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AdministratorJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    AdministratorJdbcDao administratorDao;

    @Test
    public void returns_administrator_or_empty() throws Exception {
        assertThat(administratorDao.findByUsername("john@doe.com"), equalTo(Optional.empty()));

        Administrator administrator = new Administrator();

        administrator.setId(99);
        administrator.setUsername("ruisalgado");
        administrator.setPassword("$2a$10$MhiNXVFrkfl4kmbgiYyO6uisavxSrExbXeWO0yvcj/CjkQ8hc6SBe");

        Administrator actualAdmin = administratorDao.findByUsername("ruisalgado").get();
        assertThat(actualAdmin, equalTo(administrator));
    }
}
