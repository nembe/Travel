package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MembershipDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.Membership;
import nl.yellowbrick.data.domain.PriceModel;
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
    private EmailNotificationService emailNotificationService;

    private Logger log = LoggerFactory.getLogger(AccountActivationService.class);

    public void activateCustomerAccount(Customer customer, PriceModel priceModel) {
        log.info("Starting acceptance for customer ID: " + customer.getCustomerId());

        customerDao.assignNextCustomerNr(customer);
        cardOrderDao.saveSpecialTarifIfApplicable(customer);

        if(customer.getNumberOfTCards() > 0) {
            Membership membership = new Membership(customer, priceModel);
            membershipDao.saveValidatedMembership(membership);

            log.info("Saved validated membership for customer ID " + customer.getCustomerId());

            cardOrderDao.validateCardOrders(customer, TRANSPONDER_CARD, QPARK_CARD);
            cardAssignmentService.assignAllOrderedByCustomer(customer);
            emailNotificationService.notifyAccountAccepted(customer);

            log.info("Finished activation of customer ID " + customer.getCustomerId());
        } else {
            log.error("Customer ID {} expected to have transponder cards at this point", customer.getCustomerId());
        }
    }
}
