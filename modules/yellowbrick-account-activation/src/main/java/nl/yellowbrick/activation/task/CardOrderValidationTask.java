package nl.yellowbrick.activation.task;

import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.stream.Collectors;

public class CardOrderValidationTask {

    private static final Logger log = LoggerFactory.getLogger(CardOrderValidationTask.class);

    private final CardOrderDao cardOrderDao;

    @Autowired
    public CardOrderValidationTask(CardOrderDao cardOrderDao) {
        this.cardOrderDao = cardOrderDao;
    }

    @Scheduled(fixedDelayString = "${tasks.vehicle-profile-validation-delay}")
    public void validatePendingNonPhysicalCardOrders() {
        log.debug("starting validatePendingNonPhysicalCardOrders");

        List<CardOrder> orders = insertedOrders();
        List<CardOrder> nonPhysicalOrders = orders.stream()
                .filter((order) -> !order.isExport())
                .collect(Collectors.toList());

        log.info("validating {} non physical card orders out of a total of {} card orders",
                nonPhysicalOrders.size(), orders.size());

        nonPhysicalOrders.forEach(cardOrderDao::validateCardOrder);
    }

    @Scheduled(cron = "${tasks.transpondercard-validation-cron}")
    public void validatePendingPhysicalCardOrders() {
        log.debug("starting validatePendingNonPhysicalCardOrders");

        List<CardOrder> orders = insertedOrders();
        List<CardOrder> physicalOrders = orders.stream()
                .filter(CardOrder::isExport)
                .collect(Collectors.toList());

        log.info("validating {} physical card orders out of a total of {} card orders",
                physicalOrders.size(), orders.size());

        physicalOrders.forEach(cardOrderDao::validateCardOrder);
    }

    private List<CardOrder> insertedOrders() {
        return cardOrderDao.findByStatusAndType(CardOrderStatus.INSERTED, CardType.TRANSPONDER_CARD);
    }
}
