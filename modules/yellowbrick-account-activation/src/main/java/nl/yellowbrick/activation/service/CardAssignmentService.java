package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.errors.ActivationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CardAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(CardAssignmentService.class);

    @Autowired
    private CardOrderDao cardOrderDao;

    public void assignToCustomer(Customer customer) {
        log.info("Assigning transpoder cards to customer ID " + customer.getCustomerId());

        List<CardOrder> transponderCardOrders = cardOrderDao.findForCustomer(
                customer,
                CardOrderStatus.ACCEPTED,
                CardType.TRANSPONDER_CARD);

        // keep track of last number used from pool
        String lastUsedNumber = null;

        for(CardOrder cardOrder: transponderCardOrders) {
            List<String> cardNumbers = cardOrderDao.nextTransponderCardNumbers(
                    customer.getProductGroupId(),
                    cardOrder.getAmount(),
                    Optional.ofNullable(lastUsedNumber));

            if (cardNumbers.size() < cardOrder.getAmount()) {
                log.error("not enough cards to assign to customer " + customer.getCustomerId());
                throw new ActivationException("not enough cards in the pool");
            }

            for(String cardNumber: cardNumbers) {
                // set the updateMobileWithCard flag only the first time around
                boolean updateMobileWithCard = lastUsedNumber == null;
                lastUsedNumber = cardNumber;

                log.info("Assigning card number {} to customer ID {}", lastUsedNumber, customer.getCustomerId());
                cardOrderDao.processTransponderCard(lastUsedNumber, customer, updateMobileWithCard);
            }
        }
    }
}
