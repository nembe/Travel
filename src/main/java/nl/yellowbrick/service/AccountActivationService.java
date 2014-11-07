package nl.yellowbrick.service;

import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.dao.MembershipDao;
import nl.yellowbrick.dao.PriceModelDao;
import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.domain.Membership;
import nl.yellowbrick.domain.PriceModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private EmailNotificationService emailNotificationService;

    private Log log = LogFactory.getLog(AccountActivationService.class);

    public void activateCustomerAccount(Customer customer) {
        log.info("Starting acceptance for customer id: " + customer.getCustomerId());

        Optional<PriceModel> maybePriceModel = priceModelDao.findForCustomer(customer);

        if(!maybePriceModel.isPresent()) {
            log.error("Activation failed due to lack of price model");
            return;
        }

        PriceModel priceModel = maybePriceModel.get();

        customerDao.assignNextCustomerNr(customer);

        // TODO implement
        // cardOrder.saveSpecialTarif(customerId);

        // TODO how do these cards get assigned now?
        if(customer.getNumberOfTCards() > 0) {
            Membership membership = new Membership(customer, priceModel);
            membershipDao.saveValidatedMembership(membership);

            log.info("Saved validated membership for customer id " + customer.getCustomerId());

            // TODO implement
            // cardOrder.validateCardOrdersOfCustomer(customerId);

            emailNotificationService.notifyAccountAccepted(customer);
        } else {
            log.error(String.format("Customer id %s expected to have transponder cards at this point",
                    customer.getCustomerId()));
        }
    }
}
