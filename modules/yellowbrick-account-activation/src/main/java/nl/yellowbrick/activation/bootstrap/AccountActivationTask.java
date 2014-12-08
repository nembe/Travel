package nl.yellowbrick.activation.bootstrap;

import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.errors.ActivationException;
import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.activation.validation.AccountRegistrationValidator;
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

    private static final Logger log = LoggerFactory.getLogger(AccountActivationTask.class);

    private final CustomerDao customerDao;
    private final AccountActivationService accountActivationService;
    private final AccountRegistrationValidator[] accountRegistrationValidators;

    @Autowired
    public AccountActivationTask(CustomerDao customerDao, AccountActivationService activationService,
                                 AccountRegistrationValidator... validators) {
        this.customerDao = customerDao;
        this.accountActivationService = activationService;
        this.accountRegistrationValidators = validators;
    }

    @Scheduled(fixedDelayString = "${activation.delay}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void validateAndActivateAccounts()  {
        log.debug("starting validateAndActivateAccounts");

        List<Customer> customers = customerDao.findAllPendingActivation();
        log.info(String.format("processing %d customers", customers.size()));

        customers.forEach(this::validateAndActivateAccount);
    }

    private void validateAndActivateAccount(Customer customer) {
        try {
            DataBinder binder = new DataBinder(customer);

            binder.addValidators(accountRegistrationValidators);
            binder.validate();

            if(binder.getBindingResult().hasErrors()) {
                log.info("validation failed for customer ID: " + customer.getCustomerId());
                customerDao.markAsPendingHumanReview(customer);
            } else {
                log.info("validation succeeded for customer ID: " + customer.getCustomerId());
                accountActivationService.activateCustomerAccount(customer);
            }
        } catch(ActivationException e) {
            log.error(e.getMessage(), e);
        }
    }
}
