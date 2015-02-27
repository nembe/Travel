package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.dao.AnnotationDao;
import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CardBindingService {

    private static final String CARD_COUNTRY = "NL";
    private static final String ANNOTATION_NAME = "Travelcard nummer";

    private final TransponderCardDao transponderCardDao;
    private final AnnotationDao annotationDao;
    private final Long mainAccountId;

    @Autowired
    public CardBindingService(TransponderCardDao transponderCardDao,
                              AnnotationDao annotationDao,
                              @Value("${tc.import.mainAccountId}") Long mainAccountId) {
        this.transponderCardDao = transponderCardDao;
        this.annotationDao = annotationDao;
        this.mainAccountId = mainAccountId;
    }

    public void cancelTransponderCard(WhitelistEntry entry) {
        transponderCardDao.cancelCard(entry.getTransponderCardId());
    }

    public void updateLicensePlate(WhitelistEntry entry) {
        transponderCardDao.updateLicensePlate(entry.getTransponderCardId(), entry.getLicensePlate());
    }

    public TransponderCard createTransponderCard(WhitelistEntry entry) {
        TransponderCard card = new TransponderCard();
        card.setCustomerId(mainAccountId);
        card.setCardNumber(entry.getTravelcardNumber());
        card.setLicenseplate(entry.getLicensePlate());
        card.setCountry(CARD_COUNTRY);
        card.setStatus(CardStatus.ACTIVE);

        card = transponderCardDao.createCard(card);
        annotateCardWithTravelcardNumber(card, entry.getTravelcardNumber());

        return card;
    }

    private void annotateCardWithTravelcardNumber(TransponderCard card, String tcNumber) {
        Annotation annotation = new Annotation();
        annotation.setCustomerId(mainAccountId);
        annotation.setType(AnnotationType.TRANSPONDER_CARD);
        annotation.setRecordId(card.getId());
        annotation.setValue(tcNumber);
        annotation.setName(ANNOTATION_NAME);
        annotation.setDefaultAnnotation(true);
        annotation.setFreeInput(false);

        annotationDao.createAnnotation(annotation);
    }
}
