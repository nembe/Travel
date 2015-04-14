package nl.yellowbrick.admin.service;

import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Optional;
import java.util.function.Supplier;

import static nl.yellowbrick.admin.service.CardOrderExportScheduler.Config;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class CardOrderExportSchedulerTest {

    private static final String CRON_EXPRESSION = "0 0 6 1/1 * ?";

    Supplier<CardOrderExportScheduler> scheduler;

    ProductGroupDao productGroupDao;
    CardOrderExportService exportService;
    TaskScheduler taskScheduler;
    Config config;

    @Before
    public void setUp() {
        productGroupDao = mock(ProductGroupDao.class);
        exportService = mock(CardOrderExportService.class);
        taskScheduler = mock(TaskScheduler.class);
        config = new Config();

        scheduler = () -> new CardOrderExportScheduler(productGroupDao, exportService, taskScheduler, config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_exception_when_given_bad_product_group() {
        when(productGroupDao.findById(123)).thenReturn(Optional.empty());
        config.put(123, CRON_EXPRESSION);

        scheduler.get();
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_exception_when_given_bad_cron_expression() {
        when(productGroupDao.findById(123)).thenReturn(Optional.of(new ProductGroup()));
        config.put(123, "not really a cron expression");

        scheduler.get();
    }

    @Test
    public void schedules_exports_per_product_group() {
        when(productGroupDao
                .findById(anyLong()))
                .thenAnswer(invocation -> maybeProductGroup(Integer.valueOf(invocation.getArguments()[0].toString())));

        config.put(1, CRON_EXPRESSION);
        config.put(2, CRON_EXPRESSION);

        scheduler.get().scheduleExports();

        verify(taskScheduler, times(2)).schedule(any(), any(CronTrigger.class));
    }

    private Optional<ProductGroup> maybeProductGroup(Integer id) {
        ProductGroup pg = new ProductGroup();
        pg.setId(id.longValue());

        return Optional.of(pg);
    }

}
