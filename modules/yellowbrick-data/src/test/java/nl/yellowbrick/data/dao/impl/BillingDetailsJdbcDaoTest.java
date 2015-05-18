package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.DirectDebitDetails;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BillingDetailsJdbcDaoTest extends BaseSpringTestCase {

    private static final long CUSTOMER_ID_WITH_DATA = 394744;
    private static final long CUSTOMER_ID_WITHOUT_DATA = 12345;

    @Autowired
    BillingDetailsJdbcDao dao;

    @Autowired
    DbHelper db;

    @Test
    public void finds_direct_debit_details_by_customer_id() {
        DirectDebitDetails details = dao.findDirectDebitDetailsForCustomer(CUSTOMER_ID_WITH_DATA).get();

        assertThat(details, equalTo(expectedDetails()));
        assertThat(dao.findDirectDebitDetailsForCustomer(CUSTOMER_ID_WITHOUT_DATA), equalTo(Optional.empty()));
    }

    @Test
    public void finds_direct_debit_details_by_sepa_number() {
        List<DirectDebitDetails> detailsList = dao.findDirectDebitDetailsBySepaNumber("NL39RABO0300065264");

        assertThat(detailsList, hasSize(1));
        assertThat(detailsList.get(0), equalTo(expectedDetails()));
    }

    @Test
    public void stores_verified_flag_as_y_or_n() {
        db.accept((t) -> t.update("UPDATE PAYMENT_DIRECT_DEBIT_DETAILS SET VERIFIED = 'N'"));
        assertThat(dao.findDirectDebitDetailsForCustomer(394744).get().isVerified(), is(false));

        db.accept((t) -> t.update("UPDATE PAYMENT_DIRECT_DEBIT_DETAILS SET VERIFIED = 'Y'"));
        assertThat(dao.findDirectDebitDetailsForCustomer(394744).get().isVerified(), is(true));
    }

    @Test
    public void checks_for_existence_of_credit_card_reference() {
        assertTrue(dao.existsCreditCardReferenceForCustomer(CUSTOMER_ID_WITH_DATA));
        assertFalse(dao.existsCreditCardReferenceForCustomer(CUSTOMER_ID_WITHOUT_DATA));
    }

    private DirectDebitDetails expectedDetails() {
        DirectDebitDetails details = new DirectDebitDetails();
        details.setId(1);
        details.setCustomerId(394744);
        details.setBic("RABONL2U");
        details.setSepaNumber("NL39 RABO 0300 0652 64");
        details.setVerified(true);

        return details;
    }
}
