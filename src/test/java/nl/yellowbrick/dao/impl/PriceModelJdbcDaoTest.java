package nl.yellowbrick.dao.impl;

import nl.yellowbrick.BaseSpringTestCase;
import nl.yellowbrick.database.DbHelper;
import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.domain.PriceModel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PriceModelJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    PriceModelJdbcDao priceModelDao;

    @Autowired
    DbHelper db;

    @Test
    public void returnsEmptyIfCantFindSubgroup() {
        db.truncateTable("PRODUCT_SUBGROUP");

        assertThat(priceModelDao.findForCustomer(customer()), equalTo(Optional.empty()));
    }

    @Test
    public void returnsEmptyIfCantFindPriceModel() {
        db.truncateTable("PRICEMODEL");

        assertThat(priceModelDao.findForCustomer(customer()), equalTo(Optional.empty()));
    }

    @Test
    public void returnsPriceModelIfAvailable() {
        PriceModel model = priceModelDao.findForCustomer(customer()).get();
        
        assertThat(model.getId(), equalTo(81l));
        assertThat(model.getDescription(), equalTo("Yellowbrick particulier hoesje"));
        assertThat(model.getSubscriptionCostEuroCents(), equalTo(75));
        assertThat(model.getTransactionCostMaximumEuroCents(), equalTo(32));
        assertThat(model.getTransactionCostMinimumEuroCents(), equalTo(32));
        assertThat(model.getTransactionCostPercentage(), equalTo(0));
        assertThat(model.getKortingenGeldigheidsduur(), equalTo(182));
        assertThat(model.getRegistratiekosten(), equalTo(1000));
        assertThat(model.getSleevePrice(), equalTo(100));
        assertThat(model.getMaxAmountCards(), equalTo(10));
        assertThat(model.getInitRtpCardCost(), equalTo(0));
        assertThat(model.getRtpCardCost(), equalTo(500));
        assertThat(model.getInitTranspCardCost(), equalTo(0));
        assertThat(model.getTranspCardCost(), equalTo(500));
        assertThat(model.getQparkPassCost(), equalTo(500));
    }

    private Customer customer() {
        Customer cust = new Customer();
        cust.setCustomerId(4776);
        cust.setBusiness("N");

        return cust;
    }
}