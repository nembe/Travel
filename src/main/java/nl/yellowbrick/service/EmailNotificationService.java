package nl.yellowbrick.service;

import nl.yellowbrick.dao.ConfigDao;
import nl.yellowbrick.dao.ConfigSection;
import nl.yellowbrick.domain.Config;
import nl.yellowbrick.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailNotificationService {

    @Autowired
    private ConfigDao configDao;

    public void notifyAccountAccepted(Customer customer) {
        // TODO implement

        String from = configDao.findSectionField(ConfigSection.TAXAMETER, "reply_address").get().getValue();

        final List<Config> activationCfg = configDao.findAllBySection(ConfigSection.ACTIVATION);
        String expireTokenFormula = Config.findByField(activationCfg, "expire_token_formule_new_customer").getValue();
        String httpLink = Config.findByField(activationCfg, "http_link").getValue();

        //                RegistrationSQLHelper regiSql = new RegistrationSQLHelper();
//                CustomerRegistration reg = regiSql.getCustomerRegistration(customerId);
//                String customerLocale = (reg != null && (reg.getLocale() != null)) ? reg.getLocale() : BeheerderDatabaseFunctions.getConfigValue("YB", "DEFAULT_LOCALE");
//
//                String token = sqlSystemUser.createAndStoreUserToken(customerId, expireTokenFormule);
//                String subject = messageSql.getMessage("emailSubjectNewCustomer", customerLocale);
//                String body = messageSql.getGroupSpecificMessage("emailBodyNewCustomer", productGroupID, customerLocale);
//
//                body = StringFunctions.replace(body, "%GENDERTITLE%", messageSql.getMessage("gendertitle." + gender, customerLocale).toLowerCase());
//                body = StringFunctions.replace(body, "%LASTNAME%", StringFunctions.makeLastname( infix, lastname ) );
//                body = StringFunctions.replace(body, "%CUSTOMERNR%", customerNr );
//                body = StringFunctions.replace(body, "%LINK%",  http_link + token );
//                body = StringFunctions.replace(body, "%LINK1%", http_link + token );
//
//
//                logger.info("(CUSTOMER VALIDATION STEP 2) Generated customer acceptance mail : " + body );
//
//                nl.pecoma.mail.MailSender.sendMailSMTP( from, email, subject, body );
//                logger.info("(CUSTOMER VALIDATION STEP 2) Completed validation for customer id: " + customerId );
//                returnURL = "customerValidationOverview.jsp?groupid=" + rs.getProductGroupID();

    }
}
