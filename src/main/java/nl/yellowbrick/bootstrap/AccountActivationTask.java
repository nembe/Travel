package nl.yellowbrick.bootstrap;

import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.service.AccountActivationService;
import nl.yellowbrick.validation.CustomerMembershipValidator;
import nl.yellowbrick.validation.GeneralCustomerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.DataBinder;

import java.util.List;

@EnableScheduling
public class AccountActivationTask {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private GeneralCustomerValidator generalCustomerValidator;

    @Autowired
    private CustomerMembershipValidator customerMembershipValidator;

    @Autowired
    private AccountActivationService accountActivationService;

    private Logger log = LoggerFactory.getLogger(AccountActivationTask.class);

    @Scheduled(fixedDelayString = "${activation.delay}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void validateAndActivateAccounts()  {
        log.debug("starting validateAndActivateAccounts");

        List<Customer> customers = customerDao.findAllPendingActivation();
        log.info(String.format("processing %d customers", customers.size()));

        customers.forEach((customer) -> {
            DataBinder binder = new DataBinder(customer);

            binder.addValidators(generalCustomerValidator, customerMembershipValidator);
            binder.validate();

            if(binder.getBindingResult().hasErrors()) {
                log.info("validation failed for customer ID: " + customer.getCustomerId());
                customerDao.markAsPendingHumanReview(customer);
            } else {
                log.info("validation succeeded for customer ID: " + customer.getCustomerId());
                accountActivationService.activateCustomerAccount(customer);
            }
        });
    }
}
