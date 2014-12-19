package nl.yellowbrick.admin.service;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.Customer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.web.WebAppConfiguration;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@WebAppConfiguration
public class RateTranslationServiceTest extends BaseSpringTestCase {

    @Autowired
    RateTranslationService rateTranslationService;

    @Autowired
    MessageSource messageSource;

    @Autowired
    DbHelper db;

    @Test
    public void returns_default_text_when_missing_template() {
        String expectedDescription = messageSource.getMessage("specialrate.none", null, ENGLISH);

        Customer customer = new Customer();
        customer.setCustomerId(12345);

        String actualDescription = rateTranslationService.describeRateForCustomer(customer, ENGLISH);

        assertThat(actualDescription, is(expectedDescription));
    }

    @Test
    public void tweaks_text_depending_on_having_subscription_and_on_rate_base() {
        Customer customerWithSub = new Customer();
        customerWithSub.setCustomerId(394744);
        customerWithSub.setProductGroupId(9);

        Customer customerNoSub = new Customer();
        customerNoSub.setCustomerId(4776);
        customerNoSub.setProductGroupId(9);

        // no subscription, eurocent
        String expectedDescription = messageSource.getMessage("specialrate.transaction.cents", new Object[]{ 999999, 0 }, ENGLISH);
        String actualDescription = rateTranslationService.describeRateForCustomer(customerNoSub, ENGLISH);
        assertThat(actualDescription, equalTo(expectedDescription));

        // with subscription, eurocent
        expectedDescription = messageSource.getMessage("specialrate.subscription.cents", new Object[]{ 5, 0 }, ENGLISH);
        actualDescription = rateTranslationService.describeRateForCustomer(customerWithSub, ENGLISH);
        assertThat(actualDescription, equalTo(expectedDescription));

        // no subscription, no eurocent
        db.accept((t) -> t.update("UPDATE SPECIALRATE_TEMPLATE SET SPECIALRATE_BASE = ''"));
        expectedDescription = messageSource.getMessage("specialrate.transaction.share", new Object[]{ 999999, 0 }, ENGLISH);
        actualDescription = rateTranslationService.describeRateForCustomer(customerNoSub, ENGLISH);
        assertThat(actualDescription, equalTo(expectedDescription));

        // with subscription, no eurocent
        expectedDescription = messageSource.getMessage("specialrate.subscription.share", new Object[]{ 5, 0 }, ENGLISH);
        actualDescription = rateTranslationService.describeRateForCustomer(customerWithSub, ENGLISH);
        assertThat(actualDescription, equalTo(expectedDescription));
    }


}
