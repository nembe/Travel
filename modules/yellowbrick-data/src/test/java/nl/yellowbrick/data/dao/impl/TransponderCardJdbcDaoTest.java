package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.Instant;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TransponderCardJdbcDaoTest extends BaseSpringTestCase {

    public static final long CARD_ID = 222005;

    @Autowired
    TransponderCardJdbcDao dao;

    @Autowired
    DbHelper db;

    @Test
    public void fetches_card_by_id() {
        TransponderCard card = new TransponderCard();
        card.setId(CARD_ID);
        card.setCardNumber("278577");
        card.setCustomerId(398744);
        card.setStatus(CardStatus.ACTIVE);
        card.setLicenseplate("39-LB-40");
        card.setCountry("NL");
        card.setMutationDate(Timestamp.from(Instant.parse("2014-12-14T19:04:22.000Z")));
        card.setMutator("ADMIN:ron");

        TransponderCard actualCard = dao.findById(CARD_ID).get();

        assertThat(actualCard, equalTo(card));
    }

    @Test
    public void creates_card_setting_id_and_mutator() {
        TransponderCard card = new TransponderCard();
        card.setCardNumber("cardnr");
        card.setCustomerId(1234);
        card.setStatus(CardStatus.ACTIVE);
        card.setLicenseplate("AA-BB-CC");
        card.setCountry("NL");

        TransponderCard created = dao.createCard(card);

        assertThat(created.getId(), notNullValue());
        assertThat(created.getMutator(), is("TEST MUTATOR"));
        assertThat(created.getMutationDate(), notNullValue());

        card.setId(created.getId());
        card.setMutator(created.getMutator());
        card.setMutationDate(created.getMutationDate());

        assertEquals(created, card);
    }

    @Test
    public void updates_license_plate_by_card_id() {
        dao.updateLicensePlate(CARD_ID, "bla bla");

        TransponderCard card = dao.findById(CARD_ID).get();

        assertThat(card.getLicenseplate(), is("bla bla"));
    }

    @Test
    public void sets_card_as_cancelled() {
        dao.cancelCard(CARD_ID);

        TransponderCard card = dao.findById(CARD_ID).get();

        assertThat(card.getStatus(), is(CardStatus.INACTIVE));
    }
}
