package nl.yellowbrick.admin.service;

import com.google.common.base.Joiner;
import nl.yellowbrick.data.dao.SpecialRateTemplateDao;
import nl.yellowbrick.data.dao.SubscriptionDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.SpecialRateTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

import static nl.yellowbrick.data.domain.SpecialRateTemplate.TRANSACTION_TYPE.STREET_PARKING;
import static nl.yellowbrick.data.domain.SpecialRateTemplate.TRANSACTION_TYPE.SUBSCRIPTION;

@Component
public class RateTranslationService {

    @Autowired
    private SpecialRateTemplateDao rateTemplateDao;

    @Autowired
    private SubscriptionDao subscriptionDao;

    @Autowired
    private MessageSource messageSource;

    public String describeRateForCustomer(Customer customer, Locale locale) {
        boolean hasSubscription = subscriptionDao.findForCustomer(customer.getCustomerId()).isPresent();

        Optional<SpecialRateTemplate> template = rateTemplateDao.findForProductGroup(
                customer.getProductGroupId(),
                hasSubscription ? SUBSCRIPTION : STREET_PARKING
        );

        if(!template.isPresent())
            return messageSource.getMessage("specialrate.none", null, locale);

        String i18nKey = Joiner.on(".").join(
                "specialrate",
                hasSubscription ? "subscription" : "transaction",
                template.get().getSpecialRateBase().equalsIgnoreCase("EUROCENT") ? "cents" : "share"
        );

        return messageSource.getMessage(
                i18nKey,
                new Object[] { template.get().getBalanceTotal(), template.get().getSpecialRateNumber() },
                locale);
    }
}
