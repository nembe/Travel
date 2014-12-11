package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.PriceModel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PriceModelJdbcDaoTest extends BaseSpringTestCase {

    private static final long CUSTOMER_ID = 4776;

    @Autowired
    PriceModelJdbcDao priceModelDao;
    @Autowired DbHelper db;

    @Test
    public void returns_empty_if_cant_find_subgroup() {
        db.truncateTable("PRODUCT_SUBGROUP");

        assertThat(priceModelDao.findForCustomer(CUSTOMER_ID), equalTo(Optional.empty()));
    }

    @Test
    public void returns_empty_if_cant_find_price_model() {
        db.truncateTable("PRICEMODEL");

        assertThat(priceModelDao.findForCustomer(CUSTOMER_ID), equalTo(Optional.empty()));
    }

    @Test
    public void returns_price_model_if_available() {
        PriceModel model = priceModelDao.findForCustomer(CUSTOMER_ID).get();
        
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

}
