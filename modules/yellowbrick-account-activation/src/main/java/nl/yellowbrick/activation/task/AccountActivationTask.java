package nl.yellowbrick.activation.task;

import com.google.common.base.Strings;
import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.activation.service.AccountValidationService;
import nl.yellowbrick.activation.service.AdminNotificationService;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.MarketingActionDao;
import nl.yellowbrick.data.dao.PriceModelDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import nl.yellowbrick.data.domain.MarketingAction;
import nl.yellowbrick.data.domain.PriceModel;
import nl.yellowbrick.data.errors.ActivationException;
import nl.yellowbrick.data.errors.ExhaustedCardPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountActivationTask {

    private static final Logger log = LoggerFactory.getLogger(AccountActivationTask.class);

    private final CustomerDao customerDao;
    private final PriceModelDao priceModelDao;
    private final MarketingActionDao marketingActionDao;
    private final AccountActivationService accountActivationService;
    private final AccountValidationService accountValidationService;
    private final AdminNotificationService notificationService;

    @Autowired
    public AccountActivationTask(CustomerDao customerDao, PriceModelDao priceModelDao,
                                 MarketingActionDao marketingActionDao,
                                 AccountActivationService activationService,
                                 AccountValidationService accountValidationService,
                                 AdminNotificationService notificationService) {
        this.customerDao = customerDao;
        this.priceModelDao = priceModelDao;
        this.marketingActionDao = marketingActionDao;
        this.accountActivationService = activationService;
        this.accountValidationService = accountValidationService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${tasks.customer-activation-delay}")
    public void validateAndActivateAccounts()  {
        log.debug("starting validateAndActivateAccounts");

        List<Customer> customers = newlyRegisteredCustomers();
        log.info("processing {} customers", customers.size());

        customers.forEach(this::validateAndActivateAccount);
    }

    private List<Customer> newlyRegisteredCustomers() {
        return customerDao.findAllPendingActivation().stream()
                .filter((customer) -> customer.getStatus() == CustomerStatus.REGISTERED)
                .collect(Collectors.toList());
    }

    private void validateAndActivateAccount(Customer customer) {
        try {
            Errors errors = accountValidationService.validate(customer);

            if(errors.hasErrors()) {
                log.info("validation failed for customer ID: " + customer.getCustomerId());
                customerDao.markAsPendingHumanReview(customer);
            } else {

                Optional<PriceModel> priceModel = priceModelDao.findForCustomer(customer.getCustomerId());

                if(!priceModel.isPresent()) {
                    log.error("Activation failed due to lack of price model for customer ID: " + customer.getCustomerId());
                    customerDao.markAsPendingHumanReview(customer);
                    return;
                }

                if(!Strings.isNullOrEmpty(customer.getActionCode())) {
                    Optional<MarketingAction> marketingAction = marketingActionDao.findByActionCode(customer.getActionCode());

                    if(!marketingAction.isPresent() || !marketingAction.get().isCurrentlyValid()) {
                        log.error("unacceptable action code " + customer.getActionCode());
                        customerDao.markAsPendingHumanReview(customer);
                    } else {
                        log.info("applying action code {} to customer ID {}",
                                customer.getActionCode(),
                                customer.getCustomerId());
                        priceModel.get().setRegistratiekosten(marketingAction.get().getRegistrationCost());
                    }
                }

                log.info("validation succeeded for customer ID: " + customer.getCustomerId());
                accountActivationService.activateCustomerAccount(customer, priceModel.get());
            }
        } catch(ExhaustedCardPoolException e) {
            log.error("Failed customer activation", e);
            notificationService.notifyCardPoolExhausted(customer.getProductGroupId());
        } catch(ActivationException e) {
            log.error(e.getMessage(), e);
        }
    }
}
