package nl.yellowbrick.activation.task;

import nl.yellowbrick.activation.service.AdminNotificationService;
import nl.yellowbrick.activation.service.CardAssignmentService;
import nl.yellowbrick.activation.service.CardOrderValidationService;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.stream.Collectors;

public class CardOrderValidationTask {

    private static final Logger log = LoggerFactory.getLogger(CardOrderValidationTask.class);

    private final CardOrderDao cardOrderDao;
    private final CardAssignmentService cardAssignmentService;
    private final CardOrderValidationService validationService;
    private final AdminNotificationService notificationService;

    @Autowired
    public CardOrderValidationTask(CardOrderDao cardOrderDao,
                                   CardAssignmentService cardAssignmentService,
                                   CardOrderValidationService validationService,
                                   AdminNotificationService notificationService
    ) {
        this.cardOrderDao = cardOrderDao;
        this.cardAssignmentService = cardAssignmentService;
        this.validationService = validationService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${tasks.vehicle-profile-validation-delay}")
    public void validatePendingNonPhysicalCardOrders() {
        log.debug("starting validatePendingNonPhysicalCardOrders");

        List<CardOrder> orders = insertedOrders();
        List<CardOrder> nonPhysicalOrders = orders.stream()
                .filter(order -> !order.isExport())
                .filter(this::passesValidation)
                .collect(Collectors.toList());

        log.info("processing {} non physical card orders out of a total of {} card orders",
                nonPhysicalOrders.size(), orders.size());

        nonPhysicalOrders.forEach(this::validateAndProcessOrder);
    }

    @Scheduled(cron = "${tasks.transpondercard-validation-cron}")
    public void validatePendingPhysicalCardOrders() {
        log.debug("starting validatePendingNonPhysicalCardOrders");

        List<CardOrder> orders = insertedOrders();
        List<CardOrder> physicalOrders = orders.stream()
                .filter(CardOrder::isExport)
                .filter(this::passesValidation)
                .collect(Collectors.toList());

        log.info("processing {} physical card orders out of a total of {} card orders",
                physicalOrders.size(), orders.size());

        physicalOrders.forEach(this::validateAndProcessOrder);
    }

    private boolean passesValidation(CardOrder order) {
        Errors errors = validationService.validate(order);

        if(errors.hasErrors()) {
            log.info("Order id {} failed validation", order.getId());
            return false;
        }
        return true;
    }

    private List<CardOrder> insertedOrders() {
        return cardOrderDao.findByStatusAndType(CardOrderStatus.INSERTED, CardType.TRANSPONDER_CARD);
    }

    private void validateAndProcessOrder(CardOrder order) {
        try {
            cardAssignmentService.assignTransponderCard(order);
            cardOrderDao.validateCardOrder(order);
        } catch(ExhaustedCardPoolException e) {
            log.error("Failed card order validation", e);
            notificationService.notifyCardPoolExhausted(e.getProductGroupId());
        }
    }
}
