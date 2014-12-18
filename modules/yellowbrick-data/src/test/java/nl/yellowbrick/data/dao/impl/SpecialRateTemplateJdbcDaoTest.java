package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.domain.SpecialRateTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static nl.yellowbrick.data.domain.SpecialRateTemplate.TRANSACTION_TYPE.STREET_PARKING;
import static nl.yellowbrick.data.domain.SpecialRateTemplate.TRANSACTION_TYPE.SUBSCRIPTION;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SpecialRateTemplateJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    SpecialRateTemplateJdbcDao dao;

    @Test
    public void returns_template_or_empty() {
        assertThat(dao.findForProductGroup(12345, STREET_PARKING), equalTo(Optional.empty()));

        SpecialRateTemplate templateA = new SpecialRateTemplate();
        templateA.setId(21);
        templateA.setProductGroupId(9);
        templateA.setBalanceTotal(999999);
        templateA.setSpecialRateNumber(0);
        templateA.setSpecialRateBase("EUROCENT");

        SpecialRateTemplate templateB = new SpecialRateTemplate();
        templateB.setId(22);
        templateB.setProductGroupId(9);
        templateB.setBalanceTotal(5);
        templateB.setSpecialRateNumber(0);
        templateB.setSpecialRateBase("EUROCENT");

        assertThat(dao.findForProductGroup(9, STREET_PARKING), equalTo(Optional.of(templateA)));
        assertThat(dao.findForProductGroup(9, SUBSCRIPTION), equalTo(Optional.of(templateB)));
    }
}
