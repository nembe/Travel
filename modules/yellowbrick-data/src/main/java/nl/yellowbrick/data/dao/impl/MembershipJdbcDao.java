package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.MembershipDao;
import nl.yellowbrick.data.domain.*;
import nl.yellowbrick.data.errors.ActivationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

@Component
public class MembershipJdbcDao implements MembershipDao, InitializingBean {

    private static final String PACKAGE = "WEBAPP";
    private static final String PROCEDURE = "CustomerValidateMembership";

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private Mutator mutator;

    private SimpleJdbcCall jdbcCall;
    private Logger log = LoggerFactory.getLogger(MembershipJdbcDao.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        compileJdbcCall();
    }

    @Override
    public void saveValidatedMembership(Membership membership) {
        RandomPinCode pinCode = new RandomPinCode();
        RandomPassword password = new RandomPassword();

        Customer cust = membership.getCustomer();
        PriceModel pm = membership.getPriceModel();

        Map<String, Object> results = jdbcCall.execute(new MapSqlParameterSource(new HashMap<String, Object>() {{
            put("CustomerId_in", cust.getCustomerId());
            put("CustomerNr_in", cust.getCustomerNr());
            put("NumberOfTCards_in", cust.getNumberOfTCards());
            put("NumberOfQCards_in", cust.getNumberOfQCards());
            put("IssuePhysicalCard_in", pm.isDefaultIssuePhysicalCard() ? 'Y' : 'N');
            put("RegistrationFee_in", pm.getRegistratiekosten());
            put("PinCode_in", pinCode.get());
            put("Password_in", password.get());
            put("Mutator_in", mutator.get());
        }}));

        int result = Integer.parseInt(results.get("Return_out").toString());

        if(result == -1) {
            log.info("Called {}.{} which returned {}", PACKAGE, PROCEDURE, result);
        } else {
            String errorMsg = String.format("Failed to save membership. Call %s.%s returned error code %d",
                    PACKAGE, PROCEDURE, result);
            throw new ActivationException(errorMsg);
        }
    }

    private void compileJdbcCall() {
        jdbcCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(PROCEDURE)
                .declareParameters(
                        new SqlParameter("CustomerId_in", Types.NUMERIC),
                        new SqlParameter("CustomerNr_in", Types.VARCHAR),
                        new SqlParameter("NumberOfTCards_in", Types.NUMERIC),
                        new SqlParameter("NumberOfQCards_in", Types.NUMERIC),
                        new SqlParameter("IssuePhysicalCard_in", Types.VARCHAR),
                        new SqlParameter("RegistrationFee_in", Types.NUMERIC),
                        new SqlParameter("PinCode_in", Types.VARCHAR),
                        new SqlParameter("Password_in", Types.VARCHAR),
                        new SqlParameter("Mutator_in", Types.VARCHAR),
                        new SqlOutParameter("Return_out", Types.INTEGER)
                );

        jdbcCall.compile();
    }
}
