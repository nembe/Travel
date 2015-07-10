package nl.yellowbrick.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class SleeveOrderExportScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SleeveOrderExportScheduler.class);

    private final SleeveOrderExportService sleeveOrderExportService;
    private final TaskScheduler taskScheduler;
    private final CronTrigger cron;

    @Autowired
    public SleeveOrderExportScheduler(SleeveOrderExportService exportService,
                                      TaskScheduler taskScheduler,
                                      @Value("${sleeveexport.schedule}") String cronExpression) {
        this.sleeveOrderExportService = exportService;
        this.taskScheduler = taskScheduler;
        this.cron = new CronTrigger(cronExpression);
    }

    @PostConstruct
    public void scheduleExports() {
        LOGGER.info("scheduling sleeve order export with expression {}", cron.toString());
        taskScheduler.schedule(sleeveOrderExportService::export, cron);
    }

    public LocalDateTime nextScheduledExport() {
        Date nextExecutionTime = cron.nextExecutionTime(new SimpleTriggerContext());
        Instant instant = Instant.ofEpochMilli(nextExecutionTime.getTime());

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
