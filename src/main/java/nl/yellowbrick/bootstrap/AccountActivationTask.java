package nl.yellowbrick.bootstrap;

import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.domain.Customer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@EnableScheduling
public class AccountActivationTask {

    @Autowired
    private CustomerDao customerDao;

    private Log log = LogFactory.getLog(AccountActivationTask.class);

    @Scheduled(fixedDelayString = "${activation.delay}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void validateAndActivateAccounts()  {
        log.debug("starting validateAndActivateAccounts");

        List<Customer> customers = customerDao.findAllPendingActivation();
        log.info(String.format("processing %d customers", customers.size()));

        customers.forEach((cust) -> {
            // TODO pipe to validation and activation
        });
    }
}
