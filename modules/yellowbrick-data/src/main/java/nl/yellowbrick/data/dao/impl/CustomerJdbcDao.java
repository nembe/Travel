package nl.yellowbrick.data.dao.impl;

import com.google.common.base.Joiner;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class CustomerJdbcDao implements CustomerDao, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(CustomerJdbcDao.class);
    private static final String PACKAGE = "WEBAPP";
    private static final String SAVE_PRIVATE_DATA = "CustomerSavePrivateData";
    private static final String SAVE_BUSINESS_DATA = "CustomerSaveBusinessData";

    private static final int ACTIVATION_FAILED_STATUS = 0;

    @Autowired
    private JdbcTemplate template;

    @Value("${mutator}")
    private String mutator;
    private SimpleJdbcCall saveCustomerCall;
    private SimpleJdbcCall saveBusinessCustomerCall;

    @Override
    public void afterPropertiesSet() throws Exception {
        compileJdbcCalls();
    }

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
                "INNER JOIN PRODUCT_GROUP pg ON pg.id = c.productgroup_id",
                "INNER JOIN TBLBILLINGAGENT ba ON c.billingagentidfk = ba.billingagentid",
                "INNER JOIN CUSTOMERSTATUS cs ON c.customerstatusidfk = cs.customerstatusid",
                "WHERE c.productgroup_id = 1 ",
                "AND c.customerstatusidfk < 2 ",
                "ORDER BY applicationdate"
        );

        return template.query(sql, beanRowMapper());
    }

    @Override
    public List<Customer> findAllByFuzzyNameAndDateOfBirth(String firstName, String lastName, Date dateOfBirth) {
        if(dateOfBirth == null) {
            return new ArrayList<>();
        }

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

        return template.query(query, beanRowMapper(),
                dayOfBirth,
                firstName.toLowerCase().trim(),
                lastName.toLowerCase().trim());
    }

    @Override
    public List<Customer> findAllByEmail(String email) {
        return template.query("SELECT * FROM CUSTOMER WHERE email = ?", beanRowMapper(), email);
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

    @Override
    public void savePrivateCustomer(Customer customer) {
        saveCustomerCall.execute(
                customer.getCustomerId(),
                customer.getGender(),
                customer.getInitials(),
                customer.getFirstName(),
                customer.getInfix(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNr(),
                customer.getFax(),
                customer.getDateOfBirth(),
                customer.getProductGroupId(),
                mutator
        );
    }

    @Override
    public void saveBusinessCustomer(Customer customer,
                                     String invoiceAttn, String invoiceEmail, boolean invoiceAnnotations) {
        saveBusinessCustomerCall.execute(
                customer.getCustomerId(),
                customer.getBusinessName(),
                customer.getBusinessTypeId(),
                customer.getGender(),
                customer.getInitials(),
                customer.getFirstName(),
                customer.getInfix(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNr(),
                customer.getFax(),
                customer.getDateOfBirth(),
                customer.getProductGroupId(),
                invoiceAttn,
                invoiceEmail,
                invoiceAnnotations? 'Y' : 'N',
                mutator
        );
    }

    private void compileJdbcCalls() {
        saveCustomerCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(SAVE_PRIVATE_DATA)
                .declareParameters(
                        new SqlParameter("CustomerId_in", Types.NUMERIC),
                        new SqlParameter("Gender_in", Types.CHAR),
                        new SqlParameter("Initials_in", Types.VARCHAR),
                        new SqlParameter("FirstName_in", Types.VARCHAR),
                        new SqlParameter("Infix_in", Types.VARCHAR),
                        new SqlParameter("LastName_in", Types.VARCHAR),
                        new SqlParameter("Email_in", Types.VARCHAR),
                        new SqlParameter("PhoneNr_in", Types.VARCHAR),
                        new SqlParameter("Fax_in", Types.VARCHAR),
                        new SqlParameter("DateOfBirth_in", Types.DATE),
                        new SqlParameter("ProductGroup_in", Types.NUMERIC),
                        new SqlParameter("Mutator_in", Types.VARCHAR)
                );

        saveBusinessCustomerCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(SAVE_BUSINESS_DATA)
                .declareParameters(
                        new SqlParameter("CustomerId_in", Types.NUMERIC),
                        new SqlParameter("BusinessName_in", Types.VARCHAR),
                        new SqlParameter("BusinessTypeId_in", Types.NUMERIC),
                        new SqlParameter("Gender_in", Types.CHAR),
                        new SqlParameter("Initials_in", Types.VARCHAR),
                        new SqlParameter("FirstName_in", Types.VARCHAR),
                        new SqlParameter("Infix_in", Types.VARCHAR),
                        new SqlParameter("LastName_in", Types.VARCHAR),
                        new SqlParameter("Email_in", Types.VARCHAR),
                        new SqlParameter("PhoneNr_in", Types.VARCHAR),
                        new SqlParameter("Fax_in", Types.VARCHAR),
                        new SqlParameter("DateOfBirth_in", Types.DATE),
                        new SqlParameter("ProductGroup_in", Types.NUMERIC),
                        new SqlParameter("InvoiceAttn_in", Types.VARCHAR),
                        new SqlParameter("InvoiceEmail_in", Types.VARCHAR),
                        new SqlParameter("InvoiceAnnotations_in", Types.CHAR),
                        new SqlParameter("Mutator_in", Types.VARCHAR)
                );

        saveCustomerCall.compile();
        saveBusinessCustomerCall.compile();
    }

    private String buildQuery(String... parts) {
        return Joiner.on(' ').join(parts);
    }

    private RowMapper<Customer> beanRowMapper() {
        BeanPropertyRowMapper<Customer> rowMapper = new BeanPropertyRowMapper<>(Customer.class);
        rowMapper.setPrimitivesDefaultedForNullValue(true);

        return rowMapper;
    }
}
