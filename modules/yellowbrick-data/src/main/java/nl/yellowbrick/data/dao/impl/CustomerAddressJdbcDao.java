package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.Optional;

@Component
public class CustomerAddressJdbcDao implements CustomerAddressDao, InitializingBean {

    private static final String PACKAGE = "WEBAPP";
    private static final String PROCEDURE = "CustomerSaveAddress";
    private static final int ADDRESS_TYPE_ID = 1;

    @Autowired
    private JdbcTemplate template;

    @Value("${mutator}")
    private String mutator;

    private SimpleJdbcCall saveAddressCall;

    @Override
    public void afterPropertiesSet() throws Exception {
        compileJdbcCall();
    }

    @Override
    public Optional<CustomerAddress> findByCustomerId(long customerId) {
        String sql = "SELECT * FROM CUSTOMERADDRESS " +
                "WHERE CUSTOMERIDFK = ? " +
                "AND ADDRESSTYPEIDFK = 1 OR ADDRESSTYPEIDFK = NULL " +
                "AND ROWNUM <= 1";

        return template.query(sql, rowMapper(), customerId).stream().findFirst();
    }

    @Override
    public void savePrivateCustomerAddress(long customerId, CustomerAddress address) {
        saveAddressCall.execute(
                address.getCustomerAddressId(),
                customerId,
                ADDRESS_TYPE_ID,
                address.getAddress(),
                address.getHouseNr(),
                address.getSupplement(),
                address.getPoBox(),
                address.getZipCode(),
                address.getCity(),
                address.getCountryCode(),
                address.getExtraInfo(),
                mutator
        );
    }

    private void compileJdbcCall() {
        saveAddressCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(PROCEDURE)
                .declareParameters(
                        new SqlParameter("AddressId_in", Types.NUMERIC),
                        new SqlParameter("CustomerId_in", Types.NUMERIC),
                        new SqlParameter("AddressTypeId_in", Types.NUMERIC),
                        new SqlParameter("Address_in", Types.VARCHAR),
                        new SqlParameter("HouseNr_in", Types.VARCHAR),
                        new SqlParameter("Supplement_in", Types.VARCHAR),
                        new SqlParameter("POBoxNr_in", Types.VARCHAR),
                        new SqlParameter("ZipCode_in", Types.VARCHAR),
                        new SqlParameter("City_in", Types.VARCHAR),
                        new SqlParameter("CountryCode_in", Types.VARCHAR),
                        new SqlParameter("ExtraInfo_in", Types.VARCHAR),
                        new SqlParameter("Mutator_in", Types.VARCHAR)
                );

        saveAddressCall.compile();
    }

    private RowMapper<CustomerAddress> rowMapper() {
        BeanPropertyRowMapper<CustomerAddress> rowMapper = new BeanPropertyRowMapper<>(CustomerAddress.class);
        rowMapper.setPrimitivesDefaultedForNullValue(true);
        rowMapper.setCheckFullyPopulated(false);

        return rowMapper;
    }
}
