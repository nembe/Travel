package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.exparity.hamcrest.date.DateMatchers.isToday;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TransponderCardJdbcDaoTest extends BaseSpringTestCase {

    public static final long CARD_ID = 222005;
    public static final String CARD_NUMBER = "278577";
    public static final long CUSTOMER_ID = 4776;

    @Autowired
    TransponderCardJdbcDao dao;

    @Autowired
    DbHelper db;

    @Test
    public void fetches_card_by_id() {
        TransponderCard actualCard = dao.findById(CARD_ID).get();

        assertThat(actualCard.getMutationDate(), equalTo(testCard().getMutationDate()));
        assertThat(actualCard, equalTo(testCard()));
    }

    @Test
    public void fetches_card_by_card_number() {
        TransponderCard actualCard = dao.findByCardNumber(CARD_NUMBER).get();

        assertThat(actualCard, equalTo(testCard()));
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
        assertThat(card.getMutator(), is("TEST MUTATOR"));
        assertThat(card.getMutationDate(), isToday());
    }

    @Test
    public void sets_card_as_cancelled() {
        dao.cancelCard(CARD_ID);

        TransponderCard card = dao.findById(CARD_ID).get();

        assertThat(card.getStatus(), is(CardStatus.INACTIVE));
        assertThat(card.getMutator(), is("TEST MUTATOR"));
        assertThat(card.getMutationDate(), isToday());
    }

    @Test
    public void sets_card_as_active() {
        db.accept(t -> t.update("update transpondercard set cardstatusidfk = ?", CardStatus.INACTIVE.code()));

        assertThat(dao.findById(CARD_ID).get().getStatus(), is(CardStatus.INACTIVE));

        dao.activateCard(CARD_ID, CUSTOMER_ID);
        TransponderCard card = dao.findById(CARD_ID).get();

        assertThat(card.getStatus(), is(CardStatus.ACTIVE));
        assertThat(card.getCustomerId(), is(CUSTOMER_ID));
        assertThat(card.getMutator(), is("TEST MUTATOR"));
        assertThat(card.getMutationDate(), isToday());
    }

    private TransponderCard testCard() {
        TransponderCard card = new TransponderCard();
        card.setId(CARD_ID);
        card.setCardNumber(CARD_NUMBER);
        card.setCustomerId(398744);
        card.setStatus(CardStatus.ACTIVE);
        card.setLicenseplate("39-LB-40");
        card.setCountry("NL");
        card.setMutationDate(Timestamp.valueOf(LocalDateTime.of(2014, 12, 14, 19, 4, 22)));
        card.setMutator("ADMIN:ron");

        return card;
    }
}
