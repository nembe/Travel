package nl.yellowbrick.admin.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SleeveOrderExportSchedulerTest {

    private static final String CRON_EXPRESSION = "0 0 6 1/1 * ?";

    SleeveOrderExportScheduler scheduler;
    SleeveOrderExportService exportService;
    TaskScheduler taskScheduler;

    @Before
    public void setUp() {
        exportService = mock(SleeveOrderExportService.class);
        taskScheduler = mock(TaskScheduler.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_exception_when_given_bad_cron_expression() {
        new SleeveOrderExportScheduler(exportService, taskScheduler, "not a cron expression");
    }

    @Test
    public void schedules_exports_based_on_supplied_cron() {
        scheduler = new SleeveOrderExportScheduler(exportService, taskScheduler, CRON_EXPRESSION);
        scheduler.scheduleExports();

        verify(taskScheduler).schedule(any(), eq(new CronTrigger(CRON_EXPRESSION)));
    }

    @Test
    public void determines_next_export_date() {
        scheduler = new SleeveOrderExportScheduler(exportService, taskScheduler, CRON_EXPRESSION);
        scheduler.scheduleExports();

        assertThat(scheduler.nextScheduledExport().toLocalTime(), is(LocalTime.of(6, 0)));
    }
}
