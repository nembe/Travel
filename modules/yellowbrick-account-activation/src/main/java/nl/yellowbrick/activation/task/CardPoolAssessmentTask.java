package nl.yellowbrick.activation.task;

import nl.yellowbrick.activation.service.AdminNotificationService;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.ProductGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;

public class CardPoolAssessmentTask {

    private static final Logger log = LoggerFactory.getLogger(CardPoolAssessmentTask.class);

    // number of weeks that will be looked back into
    // when determining the rate of card issuance
    private static final int CARD_ISSUANCE_PERIOD_WEEKS = 5;

    private final ProductGroupDao productGroupDao;
    private final CardOrderDao cardOrderDao;
    private final AdminNotificationService notificationService;

    @Autowired
    public CardPoolAssessmentTask(ProductGroupDao productGroupDao,
                                  CardOrderDao cardOrderDao,
                                  AdminNotificationService notificationService) {
        this.productGroupDao = productGroupDao;
        this.cardOrderDao = cardOrderDao;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${tasks.card-pool-assessment-cron}")
    public void assessTransponderCardPoolSize() {
        LocalDate since = LocalDate.now().minusWeeks(CARD_ISSUANCE_PERIOD_WEEKS);

        productGroupDao.all().forEach(pg -> {
            int cardsAvailable = cardOrderDao.transponderCardsAvailableForProductGroup(pg.getId());
            int cardsIssued = cardOrderDao.transponderCardsIssuedForProductGroup(pg.getId(), since);
            int cardsIssuedPerWeek = cardsIssued > 0
                    ? cardsIssued / CARD_ISSUANCE_PERIOD_WEEKS
                    : 0;

            log.info("Product group {} determined to be issuing cards at a rate of {} per week over the last {} weeks",
                    pg.getDescription(),
                    cardsIssuedPerWeek,
                    CARD_ISSUANCE_PERIOD_WEEKS);

            if(cardsIssuedPerWeek >= cardsAvailable) {
                log.warn("Notifying admin that at {} cards available {}'s card pool is about to expire");
                notificationService.notifyCardPoolExhausting(pg.getId(), cardsAvailable);
            }
        });
    }
}
