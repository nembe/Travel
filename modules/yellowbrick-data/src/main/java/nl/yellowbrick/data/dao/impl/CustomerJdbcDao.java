package nl.yellowbrick.data.dao.impl;

import com.google.common.base.Joiner;
import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private Mutator mutator;

    private SimpleJdbcCall saveCustomerCall;
    private SimpleJdbcCall saveBusinessCustomerCall;

    @Override
    public void afterPropertiesSet() throws Exception {
        compileJdbcCalls();
    }

    @Override
    public Optional<Customer> findById(long id) {
        String sql = buildQuery(
                "SELECT c.*,",
                "c.productgroup_id AS product_group_id,",
                "c.billingagentidfk AS billing_agent_id,",
                "c.invoice_annotations AS extra_invoice_annotations,",
                "c.phonenr_tcard AS first_card_mobile,",
                "c.license_plate_tcard AS first_card_license_plate,",
                "ba.agentnaam AS agentname,",
                "cs.label AS status,",
                "pg.description product_group,",
                "0 as parkadammertotal",
                "FROM CUSTOMER c",
                "INNER JOIN PRODUCT_GROUP pg ON pg.id = c.productgroup_id",
                "INNER JOIN TBLBILLINGAGENT ba ON c.billingagentidfk = ba.billingagentid",
                "INNER JOIN CUSTOMERSTATUS cs ON c.customerstatusidfk = cs.customerstatusid",
                "WHERE c.customerid = ?"
        );

        return template.query(sql, customerRowMapper(), id).stream().findFirst();
    }

    @Override
    public List<Customer> findAllPendingActivation() {
        String sql = buildQuery(
                "SELECT c.*,",
                "c.productgroup_id AS product_group_id,",
                "c.billingagentidfk AS billing_agent_id,",
                "c.invoice_annotations AS extra_invoice_annotations,",
                "c.phonenr_tcard AS first_card_mobile,",
                "c.license_plate_tcard AS first_card_license_plate,",
                "ba.agentnaam AS agentname,",
                "cs.label AS status,",
                "pg.description product_group,",
                "0 as parkadammertotal",
                "FROM CUSTOMER c",
                "INNER JOIN PRODUCT_GROUP pg ON pg.id = c.productgroup_id",
                "INNER JOIN TBLBILLINGAGENT ba ON c.billingagentidfk = ba.billingagentid",
                "INNER JOIN CUSTOMERSTATUS cs ON c.customerstatusidfk = cs.customerstatusid",
                "AND c.customerstatusidfk < 2 ",
                "ORDER BY applicationdate"
        );

        return template.query(sql, customerRowMapper());
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

        return template.query(query, customerRowMapper(),
                dayOfBirth,
                firstName.toLowerCase().trim(),
                lastName.toLowerCase().trim());
    }

    @Override
    public List<Customer> findAllByEmail(String email) {
        return template.query("SELECT * FROM CUSTOMER WHERE email = ?", customerRowMapper(), email);
    }

    @Override
    public void markAsPendingHumanReview(Customer customer) {
        customer.setStatus(CustomerStatus.ACTIVATION_FAILED);

        template.update("UPDATE CUSTOMER SET customerstatusidfk = ? WHERE customerid = ?",
                customer.getStatus().code(),
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
                mutator.get()
        );
    }

    @Override
    public void saveBusinessCustomer(Customer customer) {
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
                customer.getInvoiceAttn(),
                customer.getInvoiceEmail(),
                customer.isExtraInvoiceAnnotations()? '1' : '0',
                mutator.get()
        );
    }

    @Override
    public List<BusinessIdentifier> getBusinessIdentifiers(long customerId) {
        String sql = "SELECT c.id, c.value, f.label, f.required " +
                "FROM IDENTIFICATION_FIELD f " +
                "LEFT OUTER JOIN CUSTOMER_IDENTIFICATION c ON c.fieldidfk = f.id AND CUSTOMERIDFK = ? " +
                "WHERE NOT REGEXP_LIKE(f.LABEL, '.*_\\d+$')";

        return template.query(sql, businessIdentifierRowMapper(), customerId);
    }

    @Override
    public void updateBusinessIdentifier(BusinessIdentifier bi) {
        String sql = "UPDATE CUSTOMER_IDENTIFICATION " +
                "SET VALUE = ?, MUTATOR = ?, MUTATION_DATE = CURRENT_DATE " +
                "WHERE ID = ?";

        template.update(sql, bi.getValue(), mutator.get(), bi.getId());
    }

    private void compileJdbcCalls() {
        saveCustomerCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(SAVE_PRIVATE_DATA)
                .declareParameters(
                        new SqlParameter("CustomerId_in", Types.NUMERIC),
                        new SqlParameter("Gender_in", Types.VARCHAR),
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
                        new SqlParameter("Gender_in", Types.VARCHAR),
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
                        new SqlParameter("InvoiceAnnotations_in", Types.VARCHAR),
                        new SqlParameter("Mutator_in", Types.VARCHAR)
                );

        saveCustomerCall.compile();
        saveBusinessCustomerCall.compile();
    }

    private String buildQuery(String... parts) {
        return Joiner.on(' ').join(parts);
    }

    private RowMapper<Customer> customerRowMapper() {
        CustomerRowMapper rowMapper = new CustomerRowMapper();
        rowMapper.setPrimitivesDefaultedForNullValue(true);

        return rowMapper;
    }

    private RowMapper<BusinessIdentifier> businessIdentifierRowMapper() {
        BeanPropertyRowMapper<BusinessIdentifier> rowMapper = new BeanPropertyRowMapper<>(BusinessIdentifier.class);
        rowMapper.setPrimitivesDefaultedForNullValue(true);

        return rowMapper;
    }

    private class CustomerRowMapper extends BeanPropertyRowMapper<Customer> {

        private CustomerRowMapper() {
            super(Customer.class);
        }

        @Override
        protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
            if(pd.getName().equals("status"))
                return CustomerStatus.byCode(rs.getInt("customerstatusidfk"));
            return super.getColumnValue(rs, index, pd);
        }
    }
}
