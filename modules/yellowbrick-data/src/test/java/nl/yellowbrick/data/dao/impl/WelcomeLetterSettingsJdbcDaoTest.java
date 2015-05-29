package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class WelcomeLetterSettingsJdbcDaoTest extends BaseSpringTestCase {

    @Autowired WelcomeLetterSettingsJdbcDao dao;

    @Test
    public void reads_and_writes_latest_customer_id() {
        assertFalse(dao.latestExportedCustomer().isPresent());

        dao.updateLatestExportedCustomer(12345l);
        assertThat(dao.latestExportedCustomer().get(), is(12345l));

        dao.updateLatestExportedCustomer(6789l);
        assertThat(dao.latestExportedCustomer().get(), is(6789l));
    }
}
