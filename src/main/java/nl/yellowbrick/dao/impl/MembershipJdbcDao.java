package nl.yellowbrick.dao.impl;

import nl.yellowbrick.dao.MembershipDao;
import nl.yellowbrick.domain.Membership;
import nl.yellowbrick.domain.RandomPassword;
import nl.yellowbrick.domain.RandomPinCode;
import nl.yellowbrick.errors.ActivationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    // TODO check what these values should actually be (maybe make it configurable)
    private static final int DEFAULT_MEMBERSHIP_FEE = 0;
    private static final int DEFAULT_REGISTRATION_FEE = 0;

    private static final String PACKAGE = "WEBAPP";
    private static final String PROCEDURE = "CustomerValidateMembership";

    @Autowired
    private JdbcTemplate template;

    @Value("${mutator}")
    private String mutator;

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

        Map<String, Object> results = jdbcCall.execute(new MapSqlParameterSource(new HashMap<String, Object>() {{
            put("CustomerId_in", membership.getCustomer().getCustomerId());
            put("CustomerNr_in", membership.getCustomer().getCustomerNr());
            put("ParkadammerTotal_in", membership.getCustomer().getParkadammerTotal());
            put("NumberOfTCards_in", membership.getCustomer().getNumberOfTCards());
            put("NumberOfQCards_in", membership.getCustomer().getNumberOfQCards());
            put("CreditLimit_in", membership.getCustomer().getCreditLimit());
            put("SubscriptionFee_in", DEFAULT_MEMBERSHIP_FEE);
            put("RegistrationFee_in", DEFAULT_REGISTRATION_FEE);
            put("InitialTCardFee_in", membership.getPriceModel().getInitTranspCardCost());
            put("AdditionalTCardFee_in", membership.getPriceModel().getTranspCardCost());
            put("InitialRTPCardFee_in", membership.getPriceModel().getInitRtpCardCost());
            put("AdditionalRTPCardFee_in", membership.getPriceModel().getRtpCardCost());
            put("PinCode_in", pinCode.get());
            put("Password_in", password.get());
            put("Mutator_in", mutator);
        }}));

        log.info(String.format("Called %s.%s which returned %s", PACKAGE, PROCEDURE, results.get("Return_out")));
    }

    private void compileJdbcCall() {
        jdbcCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(PROCEDURE)
                .declareParameters(
                        new SqlParameter("CustomerId_in", Types.NUMERIC),
                        new SqlParameter("CustomerNr_in", Types.VARCHAR),
                        new SqlParameter("ParkadammerTotal_in", Types.NUMERIC),
                        new SqlParameter("NumberOfTCards_in", Types.NUMERIC),
                        new SqlParameter("NumberOfQCards_in", Types.NUMERIC),
                        new SqlParameter("CreditLimit_in", Types.NUMERIC),
                        new SqlParameter("SubscriptionFee_in", Types.NUMERIC),
                        new SqlParameter("RegistrationFee_in", Types.NUMERIC),
                        new SqlParameter("InitialTCardFee_in", Types.NUMERIC),
                        new SqlParameter("AdditionalTCardFee_in", Types.NUMERIC),
                        new SqlParameter("InitialRTPCardFee_in", Types.NUMERIC),
                        new SqlParameter("AdditionalRTPCardFee_in", Types.NUMERIC),
                        new SqlParameter("PinCode_in", Types.VARCHAR),
                        new SqlParameter("Password_in", Types.VARCHAR),
                        new SqlParameter("Mutator_in", Types.VARCHAR),
                        new SqlOutParameter("Return_out", Types.INTEGER)
                );

        jdbcCall.compile();
    }
}
