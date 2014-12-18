package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.DirectDebitDetails;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DirectDebitDetailsJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    DirectDebitDetailsJdbcDao dao;

    @Autowired
    DbHelper db;

    @Test
    public void returns_empty_for_no_match() {
        assertThat(dao.findForCustomer(12345), equalTo(Optional.empty()));
    }

    @Test
    public void returns_details_otherwise() {
        DirectDebitDetails expectation = new DirectDebitDetails();
        expectation.setId(1);
        expectation.setBic("RABONL2U");
        expectation.setSepaNumber("NL39 RABO 0300 0652 64");
        expectation.setVerified(true);

        DirectDebitDetails details = dao.findForCustomer(394744).get();

        assertThat(details, equalTo(expectation));
    }

    @Test
    public void stores_verified_flag_as_y_or_n() {
        db.accept((t) -> t.update("UPDATE PAYMENT_DIRECT_DEBIT_DETAILS SET VERIFIED = 'N'"));
        assertThat(dao.findForCustomer(394744).get().isVerified(), is(false));

        db.accept((t) -> t.update("UPDATE PAYMENT_DIRECT_DEBIT_DETAILS SET VERIFIED = 'Y'"));
        assertThat(dao.findForCustomer(394744).get().isVerified(), is(true));
    }
}
