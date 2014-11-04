package nl.yellowbrick.dao.impl;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import nl.yellowbrick.BaseSpringTestCase;
import nl.yellowbrick.dao.CustomerDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStreamReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class CustomerJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    CustomerDao customerDao;

    @Autowired
    JdbcTemplate template;

    @Test
    public void returnsEmptyCollectionIfNoData() {
        assertThat(customerDao.findAllPendingActivation().size(), equalTo(0));
    }

    @Test
    public void returnsCustomersIfDataIsInPlace() throws Exception {
        insertCustomersAndAddresses();

        assertThat(customerDao.findAllPendingActivation().size(), equalTo(2));
    }

    private void insertCustomersAndAddresses() throws Exception {
        String sql = CharStreams.toString(new InputStreamReader(
                this.getClass().getClassLoader().getResourceAsStream("samples/customers_and_addresses.sql"),
                Charsets.UTF_8
        ));

        template.execute(sql);
    }
}