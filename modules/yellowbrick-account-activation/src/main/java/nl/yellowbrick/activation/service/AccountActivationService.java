package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MembershipDao;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.Membership;
import nl.yellowbrick.data.domain.PriceModel;
import nl.yellowbrick.data.errors.ActivationException;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static nl.yellowbrick.data.domain.CardType.QPARK_CARD;
import static nl.yellowbrick.data.domain.CardType.TRANSPONDER_CARD;

@Component
public class AccountActivationService {

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

    private Logger log = LoggerFactory.getLogger(AccountActivationService.class);

    public void activateCustomerAccount(Customer customer, PriceModel priceModel) {
        log.info("Starting acceptance for customer ID: " + customer.getCustomerId());

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
}
