package nl.yellowbrick.dao.impl;

import nl.yellowbrick.BaseSpringTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class MessageJdbcDaoTest extends BaseSpringTestCase {

    @Autowired MessageJdbcDao messageDao;

    @Test
    public void returns_empty_when_no_match() {
        // wrong key
        assertThat(messageDao.getMessage("NO_MATCH_KEY", "nl_NL"), equalTo(Optional.empty()));
        // wrong locale
        assertThat(messageDao.getMessage("emailSubjectNewCustomer", "no_LO"), equalTo(Optional.empty()));
    }

    @Test
    public void returns_message_when_match() {
        String message = messageDao.getMessage("emailSubjectNewCustomer", "nl_NL").get();
        assertThat(message, equalTo("Welkom bij Yellowbrick!"));
    }

    @Test
    public void returns_empty_when_no_matching_group() {
        // wrong key
        assertThat(messageDao.getGroupSpecificMessage("NO_MATCH_KEY", 8, "nl_NL"), equalTo(Optional.empty()));
        // wrong locale
        assertThat(messageDao.getGroupSpecificMessage("emailBodyNewCustomer", 8, "no_LO"), equalTo(Optional.empty()));
        // wrong group
        assertThat(messageDao.getGroupSpecificMessage("emailBodyNewCustomer", 0, "nl_NL"), equalTo(Optional.empty()));
    }

    @Test
    public void returns_grouped_message_when_match() {
        String message = messageDao.getGroupSpecificMessage("emailBodyNewCustomer", 8, "nl_NL").get();
        assertThat(message, startsWith("Geachte"));
    }
}