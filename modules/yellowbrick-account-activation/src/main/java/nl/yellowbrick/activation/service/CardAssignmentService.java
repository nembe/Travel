package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

@Component
public class CardAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(CardAssignmentService.class);

    @Autowired
    private CardOrderDao cardOrderDao;

    @Autowired
    private CustomerDao customerDao;

    public boolean canAssignTransponderCards(Customer customer, int amount) {
        return cardOrderDao.cardsAvailableForProductGroup(customer.getProductGroupId()) >= amount;
    }

    public void assignTransponderCard(CardOrder order) {
        if(!order.getCardType().equals(CardType.TRANSPONDER_CARD)) {
            log.error("assignTransponderCard called with card type {} for order id {}",
                    order.getCardType().name(), order.getId());
            throw new IllegalArgumentException("Expected card type " + CardType.TRANSPONDER_CARD.name());
        }

        Customer customer = customerDao.findById(order.getCustomerId()).orElseThrow(IllegalStateException::new);

        log.info("Assigning {} transponder cards", order.getAmount());

        boolean updateMobileWithCard = hasInitialCardData(customer);
        List<String> cardNumbers = cardOrderDao.nextTransponderCardNumbers(
                customer.getProductGroupId(),
                order.getAmount(),
                Optional.empty());

        if (cardNumbers.size() < order.getAmount()) {
            throw new ExhaustedCardPoolException(customer);
        }

        for(String cardNumber: cardNumbers) {
            log.info("Assigning card number {} to customer ID {}", cardNumber, customer.getCustomerId());
            cardOrderDao.processTransponderCard(cardNumber, customer, order, updateMobileWithCard);

            // set the updateMobileWithCard flag only the first time around
            updateMobileWithCard = false;
        }
    }

    private boolean hasInitialCardData(Customer customer) {
        return !isNullOrEmpty(customer.getFirstCardMobile()) || !isNullOrEmpty(customer.getFirstCardLicensePlate());
    }
}
