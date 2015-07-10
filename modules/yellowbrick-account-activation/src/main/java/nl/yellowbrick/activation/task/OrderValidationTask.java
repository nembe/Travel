package nl.yellowbrick.activation.task;

import nl.yellowbrick.activation.service.AdminNotificationService;
import nl.yellowbrick.activation.service.CardAssignmentService;
import nl.yellowbrick.activation.service.OrderValidationService;
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

public class OrderValidationTask {

    private static final Logger log = LoggerFactory.getLogger(OrderValidationTask.class);

    private final CardOrderDao cardOrderDao;
    private final CardAssignmentService cardAssignmentService;
    private final OrderValidationService validationService;
    private final AdminNotificationService notificationService;

    @Autowired
    public OrderValidationTask(CardOrderDao cardOrderDao,
                               CardAssignmentService cardAssignmentService,
                               OrderValidationService validationService,
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

        List<CardOrder> orders = insertedOrders(CardType.TRANSPONDER_CARD);
        List<CardOrder> nonPhysicalOrders = orders.stream()
                .filter(order -> !order.isExport())
                .filter(this::passesValidation)
                .collect(Collectors.toList());

        log.info("processing {} non physical card orders out of a total of {} card orders",
                nonPhysicalOrders.size(), orders.size());

        nonPhysicalOrders.forEach(this::validateAndProcessTransponderCardOrder);
    }

    @Scheduled(cron = "${tasks.transpondercard-validation-cron}")
    public void validatePendingPhysicalCardOrders() {
        log.debug("starting validatePendingNonPhysicalCardOrders");

        List<CardOrder> orders = insertedOrders(CardType.TRANSPONDER_CARD);
        List<CardOrder> physicalOrders = orders.stream()
                .filter(CardOrder::isExport)
                .filter(this::passesValidation)
                .collect(Collectors.toList());

        log.info("processing {} physical card orders out of a total of {} card orders",
                physicalOrders.size(), orders.size());

        physicalOrders.forEach(this::validateAndProcessTransponderCardOrder);
    }

    @Scheduled(cron = "${tasks.sleeve-validation-cron}")
    public void validatePendingSleeveOrders() {
        log.debug("starting validatePendingSleeveOrders");

        List<CardOrder> sleeveOrders = insertedOrders(CardType.SLEEVE);
        List<CardOrder> validSleeveOrders = sleeveOrders.stream()
                .filter(this::passesValidation)
                .collect(Collectors.toList());

        log.info("processing {} sleeve orders out of a total of {} sleeve orders",
                validSleeveOrders.size(), sleeveOrders.size());

        sleeveOrders.forEach(cardOrderDao::validateCardOrder);
    }

    @Scheduled(cron = "${tasks.qparkcard-validation-cron}")
    public void validatePendingQParkCardOrders() {
        log.debug("starting validatePendingQParkCardOrders");

        List<CardOrder> orders = insertedOrders(CardType.QPARK_CARD);
        List<CardOrder> validOrders = orders.stream()
                .filter(this::passesValidation)
                .collect(Collectors.toList());

        log.info("processing {} qpark card orders out of a total of {} orders",
                validOrders.size(), orders.size());

        orders.forEach(cardOrderDao::validateCardOrder);
    }

    private boolean passesValidation(CardOrder order) {
        Errors errors = validationService.validate(order);

        if(errors.hasErrors()) {
            log.info("Order id {} failed validation", order.getId());
            return false;
        }
        return true;
    }

    private List<CardOrder> insertedOrders(CardType cardType) {
        return cardOrderDao.findByStatusAndType(CardOrderStatus.INSERTED, cardType);
    }

        private void validateAndProcessTransponderCardOrder(CardOrder order) {
        try {
            cardAssignmentService.assignTransponderCard(order);
            cardOrderDao.validateCardOrder(order);
        } catch(ExhaustedCardPoolException e) {
            log.error("Failed card order validation", e);
            notificationService.notifyCardPoolExhausted(e.getProductGroupId());
        }
    }
}
