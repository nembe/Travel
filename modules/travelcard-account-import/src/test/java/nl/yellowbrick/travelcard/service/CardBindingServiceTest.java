package nl.yellowbrick.travelcard.service;

import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.WhitelistEntry;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CardBindingServiceTest {

    CardBindingService cardBindingService;

    TransponderCardDao transponderCardDao;
    long mainAccountId = 1;

    @Before
    public void setUp() {
        transponderCardDao = mock(TransponderCardDao.class);
        cardBindingService = new CardBindingService(transponderCardDao, mainAccountId);
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
}
