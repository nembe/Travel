package nl.yellowbrick.activation.service;

import com.google.common.base.Joiner;
import nl.yellowbrick.data.dao.*;
import nl.yellowbrick.data.domain.Config;
import nl.yellowbrick.data.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class EmailNotificationService {

    @Autowired
    private ConfigDao configDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private SystemUserDao systemUserDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private JavaMailSender mailSender;

    private Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    public void notifyAccountAccepted(Customer customer) {
        // get all the required data for the email template
        List<Config> activationCfg = configDao.findAllBySection(ConfigSection.ACTIVATION);
        Optional<Config> expireTokenFormula = Config.findByField(activationCfg, "expire_token_formule_new_customer");
        Optional<Config> httpLink = Config.findByField(activationCfg, "http_link");
        Optional<Config> from = configDao.findSectionField(ConfigSection.TAXAMETER, "reply_address");
        String customerLocale = customerDao.getRegistrationLocale(customer).orElse(defaultLocale());
        Optional<String> subject = messageDao.getMessage("emailSubjectNewCustomer", customerLocale);
        Optional<String> genderTitle = messageDao.getMessage("gendertitle." + customer.getGender(), customerLocale);
        Optional<String> body = messageDao.getGroupSpecificMessage("emailBodyNewCustomer", customer.getProductGroupId(), customerLocale);

        // ensure all data is available
        Map<String, Optional<?>> namedConfigs = new HashMap<>();
        namedConfigs.put("expireTokenFormula", expireTokenFormula);
        namedConfigs.put("httpLink", httpLink);
        namedConfigs.put("from", from);
        namedConfigs.put("subject", subject);
        namedConfigs.put("genderTitle", genderTitle);
        namedConfigs.put("body", body);

        long nrMissingConfigs = namedConfigs.entrySet().stream()
                .filter((entry) -> !entry.getValue().isPresent())
                .map((entry) -> {
                    log.error("missing property: " + entry.getKey());
                    return entry;
                }).count();

        if(nrMissingConfigs > 0)
            return;

        String token = createUserToken(customer, expireTokenFormula.get().getValue());

        String messageBody = body.get().replace("%GENDERTITLE%", genderTitle.get().toLowerCase())
                .replace("%FIRSTNAME%", customer.getFirstName())
                .replace("%LASTNAME%", lastNameWithInfix(customer))
                .replace("%CUSTOMERNR%", customer.getCustomerNr())
                .replace("%LINK%", httpLink.get().getValue() + token)
                .replace("%LINK1%", httpLink.get().getValue() + token);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "ISO-8859-1");

            messageHelper.setText(messageBody, true);
            messageHelper.setTo(customer.getEmail());
            messageHelper.setFrom(from.get().getValue());
            messageHelper.setSubject(subject.get());

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to compose notifyAccountAccepted email", e);
        }
    }

    private String createUserToken(Customer customer, String expireTokenFormula) {
        int hoursOffset = Integer.parseInt(expireTokenFormula);
        LocalDateTime expireAt = LocalDateTime.now(ZoneId.of("Europe/Amsterdam")).plusHours(hoursOffset);

        return systemUserDao.createAndStoreUserToken(customer, expireAt);
    }

    private String lastNameWithInfix(Customer customer) {
        return Joiner.on(" ").skipNulls().join(customer.getInfix(), customer.getLastName());
    }

    private String defaultLocale() {
        Optional<Config> defaultLocale = configDao.findSectionField(ConfigSection.YB, "DEFAULT_LOCALE");

        if(defaultLocale.isPresent())
            return defaultLocale.get().getValue();

        log.error("Expected DEFAULT_LOCALE to be available but it wasn't. Defaulting to nl_NL");
        return "nl_NL";
    }
}
