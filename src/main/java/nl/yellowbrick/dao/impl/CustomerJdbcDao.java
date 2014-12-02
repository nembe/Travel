package nl.yellowbrick.dao.impl;

import com.google.common.base.Joiner;
import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class CustomerJdbcDao implements CustomerDao {

    private static final int ACTIVATION_FAILED_STATUS = 0;

    @Autowired
    private JdbcTemplate template;

    private Logger log = LoggerFactory.getLogger(CustomerJdbcDao.class);

    @Override
    public List<Customer> findAllPendingActivation() {
        String sql = buildQuery(
                "SELECT c.*,",
                "c.productgroup_id AS product_group_id,",
                "c.billingagentidfk AS billing_agent_id,",
                "c.invoice_annotations AS extra_invoice_annotations,",
                "ba.agentnaam AS agentname,",
                "cs.label AS status,",
                "pg.description product_group,",
                "0 as parkadammertotal",
                "FROM CUSTOMER c",
                "INNER JOIN CUSTOMERADDRESS ca ON c.customerid = ca.customeridfk",
                "INNER JOIN PRODUCT_GROUP pg ON pg.id = c.productgroup_id",
                "INNER JOIN TBLBILLINGAGENT ba ON c.billingagentidfk = ba.billingagentid",
                "INNER JOIN CUSTOMERSTATUS cs ON c.customerstatusidfk = cs.customerstatusid",
                "WHERE (ca.addresstypeidfk = 1 OR ca.addresstypeidfk IS NULL)",
                "AND c.productgroup_id = 1 ",
                "AND c.customerstatusidfk = 1 ",
                "ORDER BY applicationdate"
        );

        BeanPropertyRowMapper<Customer> rowMapper = new BeanPropertyRowMapper<>(Customer.class);
        rowMapper.setPrimitivesDefaultedForNullValue(true);

        return template.query(sql, rowMapper);
    }

    @Override
    public List<Customer> findAllByFuzzyNameAndDateOfBirth(String firstName, String lastName, Date dateOfBirth) {
        Date dayOfBirth = Date.from(Instant
                        .ofEpochMilli(dateOfBirth.getTime())
                        .atOffset(ZoneOffset.ofHours(0))
                        .truncatedTo(ChronoUnit.DAYS)
                        .toInstant()
        );

        BeanPropertyRowMapper<Customer> rowMapper = new BeanPropertyRowMapper<>(Customer.class);
        rowMapper.setPrimitivesDefaultedForNullValue(true);

        String query = buildQuery(
                "SELECT * FROM CUSTOMER",
                "WHERE TRUNC(dateofbirth) = ?",
                "AND TRIM(LOWER(firstname)) = ?",
                "AND TRIM(LOWER(lastname)) = ?"
        );

        return template.query(query, rowMapper,
                dayOfBirth,
                firstName.toLowerCase().trim(),
                lastName.toLowerCase().trim());
    }

    @Override
    public void markAsPendingHumanReview(Customer customer) {
        customer.setCustomerStatusIdfk(ACTIVATION_FAILED_STATUS);

        template.update("UPDATE CUSTOMER SET customerstatusidfk = ? WHERE customerid = ?",
                customer.getCustomerStatusIdfk(),
                customer.getCustomerId());
    }

    @Override
    public void assignNextCustomerNr(Customer customer) {
        Long nr = template.queryForObject("SELECT CUSTOMERNUMBER_SEQ.NEXTVAL FROM DUAL", Long.class);
        customer.setCustomerNr(nr.toString());
    }

    @Override
    public Optional<String> getRegistrationLocale(Customer customer) {
        String sql = "SELECT locale FROM CUSTOMER_REGISTRATION cr WHERE CUSTOMERIDFK = ?";

        try {
            return Optional.ofNullable(
                    template.queryForObject(sql, String.class, customer.getCustomerId())
            );
        } catch(DataAccessException e) {
            log.warn("Failed to retrieve locale for customer ID: " + customer.getCustomerId(), e);
            return Optional.empty();
        }
    }

    private String buildQuery(String... parts) {
        return Joiner.on(' ').join(parts);
    }
}
