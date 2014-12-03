package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.dao.ConfigSection;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.Config;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ConfigJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    ConfigJdbcDao configDao;

    @Autowired
    DbHelper db;

    @Test
    public void returns_empty_list_if_section_missing() {
        db.truncateTable("TBLCONFIG");

        assertThat(configDao.findAllBySection(ConfigSection.ACTIVATION), empty());
    }

    @Test
    public void returns_entries_for_section() {
        configDao.findAllBySection(ConfigSection.ACTIVATION).forEach((config) -> {
            assertThat(config.getSection(), equalTo("ACT_CUST"));
            assertThat(config.getField(), not(isEmptyString()));
        });
    }

    @Test
    public void returns_empty_for_unmatched_field() {
        Optional<Config> field = configDao.findSectionField(ConfigSection.ACTIVATION, "I'm not really a field");

        assertThat(field, equalTo(Optional.empty()));
    }

    @Test
    public void returns_matched_field() {
        Optional<Config> field = configDao.findSectionField(ConfigSection.ACTIVATION, "HTTP_LINK");

        Config expectedField = new Config();
        expectedField.setSection("ACT_CUST");
        expectedField.setField("HTTP_LINK");
        expectedField.setValue("http://localhost:8084/MyYellowbrick/auth/password/reset/");
        expectedField.setDescription("link om password opnieuw in te stellen");
        expectedField.setTitle("ACTIVATION_CUSTOMER");

        assertThat(field.get(), equalTo(expectedField));
    }

    @Test
    public void is_case_insensitive() {
        Config fieldUpper = configDao.findSectionField(ConfigSection.ACTIVATION, "HTTP_LINK").get();
        Config fieldLower = configDao.findSectionField(ConfigSection.ACTIVATION, "http_link").get();

        assertThat(fieldUpper, equalTo(fieldLower));
        assertNotNull(fieldLower);
    }
}
