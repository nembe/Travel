package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.WhitelistEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CardBindingService {

    private static final String CARD_COUNTRY = "NL";

    private final TransponderCardDao transponderCardDao;
    private final Long mainAccountId;

    @Autowired
    public CardBindingService(TransponderCardDao transponderCardDao, 
                              @Value("${tc.import.mainAccountId}") Long mainAccountId) {
        this.transponderCardDao = transponderCardDao;
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

        return transponderCardDao.createCard(card);
    }
}
