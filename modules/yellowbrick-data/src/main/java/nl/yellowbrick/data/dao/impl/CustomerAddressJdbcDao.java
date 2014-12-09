package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerAddressJdbcDao implements CustomerAddressDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public Optional<CustomerAddress> findByCustomerId(long customerId) {
        String sql = "SELECT * FROM CUSTOMERADDRESS " +
                "WHERE CUSTOMERIDFK = ? " +
                "AND ADDRESSTYPEIDFK = 1 OR ADDRESSTYPEIDFK = NULL " +
                "AND ROWNUM <= 1";

        return template.query(sql, rowMapper(), customerId).stream().findFirst();
    }

    @Override
    public void savePrivateCustomerAddress(CustomerAddress address) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    private RowMapper<CustomerAddress> rowMapper() {
        BeanPropertyRowMapper<CustomerAddress> rowMapper = new BeanPropertyRowMapper<>(CustomerAddress.class);
        rowMapper.setPrimitivesDefaultedForNullValue(true);
        rowMapper.setCheckFullyPopulated(false);

        return rowMapper;
    }
}
