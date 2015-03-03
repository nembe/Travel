package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.dao.AnnotationDao;
import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.domain.AnnotationDefinition;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.WhitelistEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static nl.yellowbrick.data.domain.AnnotationType.TRANSPONDER_CARD;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CardBindingServiceTest {

    CardBindingService cardBindingService;

    TransponderCardDao transponderCardDao;
    AnnotationDao annotationDao;
    long mainAccountId = 1;

    @Before
    public void setUp() {
        transponderCardDao = mock(TransponderCardDao.class);
        annotationDao = mock(AnnotationDao.class);

        cardBindingService = new CardBindingService(transponderCardDao, annotationDao, mainAccountId);
    }

    @Test
    public void creates_active_card_for_supplied_account_id() {
        WhitelistEntry entry = new WhitelistEntry("1111", "AA-BB-CC");

        TransponderCard card = new TransponderCard();
        card.setCustomerId(mainAccountId);
        card.setLicenseplate(entry.getLicensePlate());
        card.setCardNumber(entry.getTravelcardNumber());
        card.setCountry("NL");
        card.setStatus(CardStatus.ACTIVE);

        AnnotationDefinition def = new AnnotationDefinition();

        when(transponderCardDao.createCard(card)).thenReturn(card);
        when(annotationDao.findDefinition(mainAccountId, "Travelcard nummer", TRANSPONDER_CARD)).thenReturn(Optional.of(def));

        assertThat(cardBindingService.createTransponderCard(entry), is(card));
    }

    @Test
    public void annotates_card_during_creation() {
        TransponderCard card = new TransponderCard();
        card.setId(123l);

        AnnotationDefinition def = new AnnotationDefinition();

        when(transponderCardDao.createCard(any())).thenReturn(card);
        when(annotationDao.findDefinition(mainAccountId, "Travelcard nummer", TRANSPONDER_CARD)).thenReturn(Optional.of(def));

        cardBindingService.createTransponderCard(new WhitelistEntry("1111", "AA-BB-CC"));

        verify(annotationDao).createAnnotationValue(def, 123l, "1111");
    }

    @Test
    public void creates_definition_if_needed() {
        TransponderCard card = new TransponderCard();
        card.setId(123l);

        AnnotationDefinition def = new AnnotationDefinition();
        def.setCustomerId(mainAccountId);
        def.setType(TRANSPONDER_CARD);
        def.setName("Travelcard nummer");
        def.setDefaultAnnotation(true);
        def.setFreeInput(true);

        when(transponderCardDao.createCard(any())).thenReturn(card);
        when(annotationDao.findDefinition(mainAccountId, "Travelcard nummer", TRANSPONDER_CARD)).thenReturn(Optional.empty());
        when(annotationDao.createAnnotationDefinition(any())).thenReturn(def);

        cardBindingService.createTransponderCard(new WhitelistEntry("1111", "AA-BB-CC"));

        verify(annotationDao).createAnnotationDefinition(def);
        verify(annotationDao).createAnnotationValue(def, 123l, "1111");
    }
}
