package nl.yellowbrick.admin.service;

import com.google.common.collect.Maps;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class CardOrderExportScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardOrderExportScheduler.class);

    private final ProductGroupDao productGroupDao;
    private final CardOrderExportService cardOrderExportService;
    private final TaskScheduler taskScheduler;
    private final Map<ProductGroup, CronTrigger> schedule;

    @Autowired
    public CardOrderExportScheduler(ProductGroupDao productGroupDao,
                                    CardOrderExportService exportService,
                                    TaskScheduler taskScheduler,
                                    Config config) {
        this.productGroupDao = productGroupDao;
        this.cardOrderExportService = exportService;
        this.taskScheduler = taskScheduler;
        this.schedule = readSchedule(config);
    }

    @PostConstruct
    public void scheduleExports() {
        schedule.forEach((productGroup, cron) -> {
            LOGGER.info("scheduling card export for product group {} with expression {}",
                    productGroup.getDescription(), cron.toString());
            taskScheduler.schedule(() -> cardOrderExportService.exportForProductGroup(productGroup), cron);
        });
    }

    private Map<ProductGroup, CronTrigger> readSchedule(Config config) {
        Map<ProductGroup, CronTrigger> schedule = Maps.newHashMap();

        config.getSchedule().forEach((productGroupId, cronExpression) -> {
            schedule.put(
                productGroupDao.findById(Long.valueOf(productGroupId)).orElseThrow(IllegalArgumentException::new),
                new CronTrigger(cronExpression)
            );
        });

        return schedule;
    }

    public static class Config {

        private final Map<Integer, String> schedule = Maps.newHashMap();

        public Map<Integer, String> getSchedule() {
            return this.schedule;
        }

        public void put(int productGroupId, String cronExpression) {
            this.schedule.put(productGroupId, cronExpression);
        }
    }
}
