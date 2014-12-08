package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MembershipDao;
import nl.yellowbrick.data.dao.PriceModelDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.Membership;
import nl.yellowbrick.data.domain.PriceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AccountActivationService {

    @Autowired
    private PriceModelDao priceModelDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private MembershipDao membershipDao;

    @Autowired
    private CardOrderDao cardOrderDao;

    @Autowired
    private EmailNotificationService emailNotificationService;

    private Logger log = LoggerFactory.getLogger(AccountActivationService.class);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void activateCustomerAccount(Customer customer) {
        log.info("Starting acceptance for customer ID: " + customer.getCustomerId());

        Optional<PriceModel> maybePriceModel = priceModelDao.findForCustomer(customer);

        if(!maybePriceModel.isPresent()) {
            log.error("Activation failed due to lack of price model");
            return;
        }

        customerDao.assignNextCustomerNr(customer);
        cardOrderDao.saveSpecialTarifIfApplicable(customer);

        // TODO how do these cards get assigned now?
        if(customer.getNumberOfTCards() > 0) {
            Membership membership = new Membership(customer, maybePriceModel.get());
            membershipDao.saveValidatedMembership(membership);

            log.info("Saved validated membership for customer ID " + customer.getCustomerId());

            cardOrderDao.validateCardOrders(customer);
            emailNotificationService.notifyAccountAccepted(customer);

            log.info("Finished activation of customer ID " + customer.getCustomerId());
        } else {
            log.error(String.format("Customer ID %s expected to have transponder cards at this point",
                    customer.getCustomerId()));
        }
    }
}
