package nl.yellowbrick.activation.task;

import nl.yellowbrick.activation.service.AdminNotificationService;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class CardPoolAssessmentTaskTest {

    CardPoolAssessmentTask cardPoolAssessment;

    ProductGroupDao productGroupDao;
    CardOrderDao cardOrderDao;
    AdminNotificationService notificationService;

    @Before
    public void setUp() {
        productGroupDao = mock(ProductGroupDao.class);
        cardOrderDao = mock(CardOrderDao.class);
        notificationService = mock(AdminNotificationService.class);

        cardPoolAssessment = new CardPoolAssessmentTask(productGroupDao, cardOrderDao, notificationService);

        // return a single product group for test purposes
        when(productGroupDao.all()).thenReturn(Arrays.asList(productGroup()));
    }

    @Test
    public void no_op_if_rate_is_below_availability() {
        stubAvailable(11);
        stubIssued(10 * 5); // rate is averaged over 5 weeks

        cardPoolAssessment.assessTransponderCardPoolSize();

        verifyZeroInteractions(notificationService);
    }

    @Test
    public void notifies_admin_if_rate_is_eql_to_or_above_availability() {
        stubAvailable(10);
        stubIssued(10 * 5);

        cardPoolAssessment.assessTransponderCardPoolSize();

        verify(notificationService).notifyCardPoolExhausting(productGroup().getId(), 10);
    }

    private void stubIssued(int issued) {
        when(cardOrderDao.transponderCardsIssuedForProductGroup(anyLong(), any())).thenReturn(issued);
    }

    private void stubAvailable(int available) {
        when(cardOrderDao.transponderCardsAvailableForProductGroup(anyLong())).thenReturn(available);
    }

    private static ProductGroup productGroup() {
        ProductGroup pg = new ProductGroup();
        pg.setId(1l);

        return pg;
    }
}
