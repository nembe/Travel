package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MembershipDao;
import nl.yellowbrick.data.domain.*;
import nl.yellowbrick.data.errors.ActivationException;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import static nl.yellowbrick.data.domain.CardType.QPARK_CARD;
import static nl.yellowbrick.data.domain.CardType.TRANSPONDER_CARD;

@Component
public class AccountActivationService {

    private static final long LOCK_TIMEOUT_SECONDS = 5;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private MembershipDao membershipDao;

    @Autowired
    private CardOrderDao cardOrderDao;

    @Autowired
    private CardAssignmentService cardAssignmentService;

    @Autowired
    private CustomerNotificationService emailNotificationService;

    @Autowired
    private Function<String, Lock> lockSupplier;

    private Logger log = LoggerFactory.getLogger(AccountActivationService.class);

    public void activateCustomerAccount(Customer customer, PriceModel priceModel) {
        log.info("Acquiring lock for acceptance of customer ID: " + customer.getCustomerId());
        Lock lock = lockSupplier.apply(getClass().getName());

        try {
            if(lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.info("Starting acceptance for customer ID: " + customer.getCustomerId());
                // customer data may be stale at this point. Make sure to refresh it
                doActivation(reloadCustomerInstance(customer), priceModel);
            } else {
                throw new ActivationException("Couldn't acquire lock: timed out");
            }
        } catch(InterruptedException e) {
            throw new ActivationException("Couldn't acquire lock: interrupted", e);
        } finally {
            lock.unlock();
        }
    }

    private void doActivation(Customer customer, PriceModel priceModel) {
        if(customer.getStatus().compareTo(CustomerStatus.REGISTERED) > 0) {
            throw new ActivationException(String.format(
                    "Customer ID %s already active with status %s",
                    customer.getCustomerId(),
                    customer.getStatus().name()));
        }

        if(customer.getNumberOfTCards() < 1) {
            throw new ActivationException(String.format(
                    "Customer ID %s expected to have transponder cards at this point",
                    customer.getCustomerId()));
        }

        if(!cardAssignmentService.canAssignTransponderCards(customer, customer.getNumberOfTCards())) {
            throw new ExhaustedCardPoolException(customer);
        }

        customerDao.assignNextCustomerNr(customer);
        cardOrderDao.saveSpecialTarifIfApplicable(customer);

        if(customer.getNumberOfTCards() > 0) {
            Membership membership = new Membership(customer, priceModel);
            membershipDao.saveValidatedMembership(membership);

            log.info("Saved validated membership for customer ID " + customer.getCustomerId());

            cardOrderDao.findForCustomer(customer, CardOrderStatus.INSERTED, QPARK_CARD).forEach(cardOrderDao::validateCardOrder);
            cardOrderDao.findForCustomer(customer, CardOrderStatus.INSERTED, TRANSPONDER_CARD).forEach(order -> {
                cardAssignmentService.assignTransponderCard(order);
                cardOrderDao.validateCardOrder(order);
            });

            emailNotificationService.notifyAccountAccepted(customer);

            log.info("Finished activation of customer ID " + customer.getCustomerId());
        } else {
            log.error("Customer ID {} expected to have transponder cards at this point", customer.getCustomerId());
        }
    }

    private Customer reloadCustomerInstance(Customer customer) {
        long customerId = customer.getCustomerId();

        return customerDao
                .findById(customerId)
                .orElseThrow(() -> new ActivationException("Couldn't retrieve data for customer ID " + customerId));
    }
}
