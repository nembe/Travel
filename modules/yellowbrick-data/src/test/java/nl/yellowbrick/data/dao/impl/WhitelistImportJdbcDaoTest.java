package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.WhitelistEntry;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WhitelistImportJdbcDaoTest extends BaseSpringTestCase {

    private static String TC_NUMBER = "tc111111111";

    @Autowired WhitelistImportJdbcDao dao;
    @Autowired DbHelper db;

    @Test
    public void retrieves_entries_by_travelcard_number() {
        WhitelistEntry expectedEntry = new WhitelistEntry(TC_NUMBER, "AA-BB-CC", 1l);
        WhitelistEntry actualEntry = dao.findByTravelcardNumber(TC_NUMBER).get();

        assertThat(actualEntry, is(expectedEntry));
    }

    @Test
    public void creates_new_entries() {
        WhitelistEntry entry = new WhitelistEntry("tc222222222", "DD-EE-FF");
        dao.createEntry(entry);

        WhitelistEntry createdEntry = dao.findByTravelcardNumber(entry.getTravelcardNumber()).get();

        assertThat(entry, is(createdEntry));
    }

    @Test
    public void updates_existing_entries() {
        WhitelistEntry entry = dao.findByTravelcardNumber(TC_NUMBER).get();

        entry.setLicensePlate("test");
        entry.setTransponderCardId(123l);
        entry.setObsolete(true);
        dao.updateEntry(entry);

        WhitelistEntry updatedEntry = dao.findByTravelcardNumber(entry.getTravelcardNumber()).get();

        assertThat(updatedEntry.getLicensePlate(), is("test"));
        assertThat(updatedEntry.getTransponderCardId(), is(123l));
        assertThat(updatedEntry.isObsolete(), is(true));
    }

    @Test
    public void marks_all_as_obsolete() {
        dao.markAllAsObsolete();

        final WhitelistEntry entry = dao.findByTravelcardNumber(TC_NUMBER).get();

        assertThat(entry.isObsolete(), is(true));
    }

    @Test
    public void deletes_all_obsolete() {
        dao.deleteAllObsolete();

        assertTrue(dao.findByTravelcardNumber(TC_NUMBER).isPresent());

        dao.markAllAsObsolete();
        dao.deleteAllObsolete();

        assertFalse(dao.findByTravelcardNumber(TC_NUMBER).isPresent());
    }
}
