package nl.yellowbrick.activation.task;

import nl.yellowbrick.activation.service.AdminNotificationService;
import nl.yellowbrick.activation.service.CardAssignmentService;
import nl.yellowbrick.activation.service.OrderValidationService;
import nl.yellowbrick.activation.validation.UnboundErrors;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;

import java.util.Arrays;

import static nl.yellowbrick.data.domain.CardOrderStatus.INSERTED;
import static nl.yellowbrick.data.domain.CardType.SLEEVE;
import static nl.yellowbrick.data.domain.CardType.TRANSPONDER_CARD;
import static org.mockito.Mockito.*;

public class OrderValidationTaskTest {

    OrderValidationTask orderValidationTask;

    CardOrderDao cardOrderDao;
    CardAssignmentService cardAssignmentService;
    OrderValidationService validationService;
    AdminNotificationService notificationService;

    CardOrder order;
    CardOrder nonPhysicalOrder;
    CardOrder sleeveOrder;

    @Before
    public void setUp() {
        order = new CardOrder();
        order.setCardType(TRANSPONDER_CARD);
        order.setExport(true);

        nonPhysicalOrder = new CardOrder();
        nonPhysicalOrder.setCardType(TRANSPONDER_CARD);
        nonPhysicalOrder.setExport(false);

        sleeveOrder = new CardOrder();
        sleeveOrder.setCardType(SLEEVE);

        cardOrderDao = mock(CardOrderDao.class);

        when(cardOrderDao.findByStatusAndType(INSERTED, TRANSPONDER_CARD))
                .thenReturn(Arrays.asList(order, nonPhysicalOrder));

        when(cardOrderDao.findByStatusAndType(INSERTED, SLEEVE))
                .thenReturn(Arrays.asList(sleeveOrder));

        cardAssignmentService = mock(CardAssignmentService.class);
        validationService = mock(OrderValidationService.class);
        notificationService = mock(AdminNotificationService.class);

        orderValidationTask = new OrderValidationTask(
                cardOrderDao, cardAssignmentService, validationService, notificationService);
    }

    @Test
    public void skips_orders_that_have_errors() {
        when(validationService.validate(any())).thenReturn(someError());

        // try to validate all kinds of orders
        orderValidationTask.validatePendingNonPhysicalCardOrders();
        orderValidationTask.validatePendingPhysicalCardOrders();

        verify(cardOrderDao, never()).validateCardOrder(any());
        verifyZeroInteractions(cardAssignmentService);
    }

    @Test
    public void validates_non_physical_card_orders() {
        when(validationService.validate(any())).thenReturn(emptyErrors());

        orderValidationTask.validatePendingNonPhysicalCardOrders();

        verify(cardOrderDao, times(1)).validateCardOrder(any());
        verify(cardOrderDao).validateCardOrder(nonPhysicalOrder);
        verify(cardAssignmentService).assignTransponderCard(nonPhysicalOrder);
    }

    @Test
    public void validates_physical_card_orders() {
        when(validationService.validate(any())).thenReturn(emptyErrors());

        orderValidationTask.validatePendingPhysicalCardOrders();

        verify(cardOrderDao, times(1)).validateCardOrder(any());
        verify(cardOrderDao).validateCardOrder(order);
        verify(cardAssignmentService).assignTransponderCard(order);
    }

    @Test
    public void notifies_admin_when_activation_fails_due_to_exhausted_cards() {
        when(validationService.validate(any())).thenReturn(emptyErrors());

        doThrow(new ExhaustedCardPoolException(new Customer()))
                .when(cardAssignmentService).assignTransponderCard(order);

        orderValidationTask.validatePendingPhysicalCardOrders();

        // check that admin is notified
        verify(notificationService).notifyCardPoolExhausted(anyLong());
    }

    @Test
    public void validates_sleeve_orders() {
        when(validationService.validate(any())).thenReturn(emptyErrors());

        orderValidationTask.validatePendingSleeveOrders();

        verify(cardOrderDao, times(1)).validateCardOrder(any());
        verify(cardOrderDao).validateCardOrder(sleeveOrder);

        verifyZeroInteractions(cardAssignmentService);
    }

    private Errors emptyErrors() {
        return new UnboundErrors(new Object());
    }

    private Errors someError() {
        Errors errors = emptyErrors();
        errors.reject("something");

        return errors;
    }
}
