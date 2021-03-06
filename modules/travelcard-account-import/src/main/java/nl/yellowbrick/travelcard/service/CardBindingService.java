package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.dao.AnnotationDao;
import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.domain.AnnotationDefinition;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.WhitelistEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static nl.yellowbrick.data.domain.AnnotationType.TRANSPONDER_CARD;

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

    public TransponderCard assignActiveTransponderCard(WhitelistEntry entry) {
        String cardNr = entry.getTravelcardNumber();
        Optional<TransponderCard> maybeCard = transponderCardDao.findByCardNumber(cardNr);

        return maybeCard.isPresent() ? assignExistingCard(maybeCard.get(), entry) : assignNewCard(entry);
    }

    private TransponderCard assignNewCard(WhitelistEntry entry) {
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

    private TransponderCard assignExistingCard(TransponderCard card, WhitelistEntry entry) {
        if(card.getCustomerId() > 0 && card.getCustomerId() != mainAccountId) {
            String err = String.format("Travelcard number %s matches card belonging to customer id %s",
                    entry.getTravelcardNumber(),
                    card.getCustomerId());
            throw new IllegalStateException(err);
        }

        transponderCardDao.activateCard(card.getId(), mainAccountId);
        transponderCardDao.updateLicensePlate(card.getId(), entry.getLicensePlate());

        return transponderCardDao.findByCardNumber(card.getCardNumber()).get();
    }

    private void annotateCardWithTravelcardNumber(TransponderCard card, String tcNumber) {
        AnnotationDefinition annotationDef = annotationDao
                .findDefinition(mainAccountId, ANNOTATION_NAME, TRANSPONDER_CARD)
                .orElseGet(this::createDefaultAnnotationDefinition);

        annotationDao.createAnnotationValue(annotationDef, card.getId(), tcNumber);
    }

    private AnnotationDefinition createDefaultAnnotationDefinition() {
        AnnotationDefinition annotation = new AnnotationDefinition();
        annotation.setCustomerId(mainAccountId);
        annotation.setType(TRANSPONDER_CARD);
        annotation.setName(ANNOTATION_NAME);
        annotation.setDefaultAnnotation(true);
        annotation.setFreeInput(true);

        return annotationDao.createAnnotationDefinition(annotation);
    }
}
