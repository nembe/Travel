package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.dao.AnnotationDao;
import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.domain.*;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        when(transponderCardDao.createCard(card)).thenReturn(card);

        assertThat(cardBindingService.createTransponderCard(entry), is(card));
    }

    @Test
    public void annotates_card_during_creation() {
        TransponderCard card = new TransponderCard();
        card.setId(123l);

        when(transponderCardDao.createCard(any())).thenReturn(card);

        cardBindingService.createTransponderCard(new WhitelistEntry("1111", "AA-BB-CC"));

        Annotation annotation = new Annotation();
        annotation.setCustomerId(mainAccountId);
        annotation.setType(AnnotationType.TRANSPONDER_CARD);
        annotation.setRecordId(123l);
        annotation.setValue("1111");
        annotation.setName("Travelcard nummer");
        annotation.setDefaultAnnotation(true);
        annotation.setFreeInput(false);

        verify(annotationDao).createAnnotation(annotation);
    }
}
