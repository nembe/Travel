package nl.yellowbrick.activation.task;

import nl.yellowbrick.activation.service.AdminNotificationService;
import nl.yellowbrick.activation.service.CardAssignmentService;
import nl.yellowbrick.activation.service.CardOrderValidationService;
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
import static nl.yellowbrick.data.domain.CardType.TRANSPONDER_CARD;
import static org.mockito.Mockito.*;

public class CardOrderValidationTaskTest {

    CardOrderValidationTask cardOrderValidationTask;

    CardOrderDao cardOrderDao;
    CardAssignmentService cardAssignmentService;
    CardOrderValidationService validationService;
    AdminNotificationService notificationService;

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
        validationService = mock(CardOrderValidationService.class);
        notificationService = mock(AdminNotificationService.class);

        cardOrderValidationTask = new CardOrderValidationTask(
                cardOrderDao, cardAssignmentService, validationService, notificationService);
    }

    @Test
    public void skips_orders_that_have_errors() {
        when(validationService.validate(any())).thenReturn(someError());

        // try to validate all kinds of orders
        cardOrderValidationTask.validatePendingNonPhysicalCardOrders();
        cardOrderValidationTask.validatePendingPhysicalCardOrders();

        verify(cardOrderDao, never()).validateCardOrder(any());
        verifyZeroInteractions(cardAssignmentService);
    }

    @Test
    public void validates_non_physical_card_orders() {
        when(validationService.validate(any())).thenReturn(emptyErrors());

        cardOrderValidationTask.validatePendingNonPhysicalCardOrders();

        verify(cardOrderDao, times(1)).validateCardOrder(any());
        verify(cardOrderDao).validateCardOrder(nonPhysicalOrder);
        verify(cardAssignmentService).assignTransponderCard(nonPhysicalOrder);
    }

    @Test
    public void validates_physical_card_orders() {
        when(validationService.validate(any())).thenReturn(emptyErrors());

        cardOrderValidationTask.validatePendingPhysicalCardOrders();

        verify(cardOrderDao, times(1)).validateCardOrder(any());
        verify(cardOrderDao).validateCardOrder(order);
        verify(cardAssignmentService).assignTransponderCard(order);
    }

    @Test
    public void notifies_admin_when_activation_fails_due_to_exhausted_cards() {
        when(validationService.validate(any())).thenReturn(emptyErrors());

        doThrow(new ExhaustedCardPoolException(new Customer()))
                .when(cardAssignmentService).assignTransponderCard(order);

        cardOrderValidationTask.validatePendingPhysicalCardOrders();

        // check that admin is notified
        verify(notificationService).notifyCardPoolExhausted(anyLong());
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
