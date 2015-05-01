package nl.yellowbrick.activation.task;

import nl.yellowbrick.activation.service.CardAssignmentService;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static nl.yellowbrick.data.domain.CardOrderStatus.INSERTED;
import static nl.yellowbrick.data.domain.CardType.TRANSPONDER_CARD;
import static org.mockito.Mockito.*;

public class CardOrderValidationTaskTest {

    CardOrderValidationTask cardOrderValidationTask;

    CardOrderDao cardOrderDao;
    CardAssignmentService cardAssignmentService;

    CardOrder order;
    CardOrder nonPhysicalOrder;

    @Before
    public void setUp() {
        order = new CardOrder();
        order.setCardType(TRANSPONDER_CARD);
        order.setExport(true);

        nonPhysicalOrder = new CardOrder();
        nonPhysicalOrder.setCardType(TRANSPONDER_CARD);
        nonPhysicalOrder.setExport(false);

        cardOrderDao = mock(CardOrderDao.class);

        when(cardOrderDao.findByStatusAndType(INSERTED, TRANSPONDER_CARD))
                .thenReturn(Arrays.asList(order, nonPhysicalOrder));

        cardAssignmentService = mock(CardAssignmentService.class);

        cardOrderValidationTask = new CardOrderValidationTask(cardOrderDao, cardAssignmentService);
    }

    @Test
    public void validates_non_physical_card_orders() {
        cardOrderValidationTask.validatePendingNonPhysicalCardOrders();

        verify(cardOrderDao, times(1)).validateCardOrder(any());
        verify(cardOrderDao).validateCardOrder(nonPhysicalOrder);
        verify(cardAssignmentService).assignTransponderCard(nonPhysicalOrder);
    }

    @Test
    public void validates_physical_card_orders() {
        cardOrderValidationTask.validatePendingPhysicalCardOrders();

        verify(cardOrderDao, times(1)).validateCardOrder(any());
        verify(cardOrderDao).validateCardOrder(order);
        verify(cardAssignmentService).assignTransponderCard(order);
    }
}
