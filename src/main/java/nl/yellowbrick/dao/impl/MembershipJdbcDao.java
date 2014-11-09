package nl.yellowbrick.dao.impl;

import nl.yellowbrick.dao.MembershipDao;
import nl.yellowbrick.domain.Membership;
import nl.yellowbrick.domain.RandomPassword;
import nl.yellowbrick.domain.RandomPinCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

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
    private Log log = LogFactory.getLog(MembershipJdbcDao.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        compileJdbcCall();
    }

    public void saveValidatedMembership(Membership membership) {
        long newCreditLimit = membership.getCustomer().getCreditLimit() * 100;

        RandomPinCode pinCode = new RandomPinCode();
        RandomPassword password = new RandomPassword();

        try {

        Connection connection = template.getDataSource().getConnection();

        final String sql = "{CALL WEBAPP.CustomerValidateMembership(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
		boolean retval = false;
		CallableStatement cs = null;
		int exitCode = -999;
			cs = connection.prepareCall(sql);
			cs.setLong(1, membership.getCustomer().getCustomerId());
			cs.setString(2, membership.getCustomer().getCustomerNr());
			cs.setInt(3, membership.getCustomer().getParkadammerTotal());
			cs.setInt(4, membership.getCustomer().getNumberOfTCards());
			cs.setInt(5, membership.getCustomer().getNumberOfQCards());
			cs.setLong(6, newCreditLimit);
			cs.setLong(7, DEFAULT_MEMBERSHIP_FEE);
			cs.setLong(8, DEFAULT_REGISTRATION_FEE);
			cs.setLong(9, membership.getPriceModel().getInitTranspCardCost());
			cs.setLong(10, membership.getPriceModel().getTranspCardCost());
			cs.setLong(11, membership.getPriceModel().getInitRtpCardCost());
			cs.setLong(12, membership.getPriceModel().getRtpCardCost());
			cs.setString(13, pinCode.get());
			cs.setString(14, password.get());
			cs.setString(15, mutator);
			cs.registerOutParameter(16, Types.INTEGER);
			cs.executeUpdate();
            exitCode = cs.getInt(16);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
    }

    private void compileJdbcCall() {
        jdbcCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(PROCEDURE)
                .withReturnValue()
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
