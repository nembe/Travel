package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.domain.AddressType;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.Optional;

@Component
public class CustomerAddressJdbcDao implements CustomerAddressDao, InitializingBean {

    private static final String PACKAGE = "WEBAPP";
    private static final String SAVE_ADDRESS = "CustomerSaveAddress";
    private static final String DELETE_ADDRESS = "CustomerDeleteAddress";

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private Mutator mutator;

    private SimpleJdbcCall saveAddressCall;
    private SimpleJdbcCall deleteAddressCall;

    private Logger log = LoggerFactory.getLogger(CustomerAddressJdbcDao.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        compileJdbcCalls();
    }

    @Override
    public Optional<CustomerAddress> findByCustomerId(long customerId, AddressType addressType) {
        String sql = "SELECT * FROM CUSTOMERADDRESS " +
                "WHERE CUSTOMERIDFK = ? " +
                "AND ADDRESSTYPEIDFK = ? " +
                "AND ROWNUM <= 1";

        return template.query(sql.toString(), rowMapper(), customerId, addressType.code()).stream().findFirst();
    }

    @Override
    public void savePrivateCustomerAddress(long customerId, CustomerAddress address) {
        saveAddress(customerId, address, AddressType.MAIN);
    }

    @Override
    public void saveBusinessCustomerAddress(long customerId, CustomerAddress address, AddressType addressType) {
        saveAddress(customerId, address, addressType);
    }

    private void saveAddress(long customerId, CustomerAddress address, AddressType addressType) {
        saveAddressCall.execute(
                address.getCustomerAddressId(),
                customerId,
                addressType.code(),
                address.getAddress(),
                address.getHouseNr(),
                address.getSupplement(),
                address.getPoBox(),
                address.getZipCode(),
                address.getCity(),
                address.getCountryCode(),
                address.getExtraInfo(),
                mutator.get()
        );
    }

    @Override
    public void deleteAddress(CustomerAddress address) {
        deleteAddressCall.execute(address.getCustomerAddressId(), mutator.get());
    }

    private void compileJdbcCalls() {
        saveAddressCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(SAVE_ADDRESS)
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

        deleteAddressCall = new SimpleJdbcCall(template)
                .withCatalogName(PACKAGE)
                .withProcedureName(DELETE_ADDRESS)
                .declareParameters(
                        new SqlParameter("AddressId_in", Types.NUMERIC),
                        new SqlParameter("Mutator_in", Types.VARCHAR)
                );

        saveAddressCall.compile();
        deleteAddressCall.compile();
    }

    private RowMapper<CustomerAddress> rowMapper() {
        BeanPropertyRowMapper<CustomerAddress> rowMapper = new BeanPropertyRowMapper<>(CustomerAddress.class);
        rowMapper.setPrimitivesDefaultedForNullValue(true);
        rowMapper.setCheckFullyPopulated(false);

        return rowMapper;
    }
}
