package nl.yellowbrick.dao.impl;

import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class CustomerJdbcDao implements CustomerDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public List<Customer> findAllPendingActivation() {
        return template.query("SELECT c.FIRSTNAME, c.LASTNAME, c.EMAIL " +
                "FROM CUSTOMER c, CUSTOMERADDRESS ca, PRODUCT_GROUP p " +
                "WHERE C.CUSTOMERID = ca.CUSTOMERIDFK " +
                "AND p.id = c.productgroup_id " +
                "AND (ca.ADDRESSTYPEIDFK = 1 OR ca.ADDRESSTYPEIDFK IS NULL) " +
                "AND c.PRODUCTGROUP_ID = 1 " +
                "AND c.CUSTOMERSTATUSIDFK = 1 " +
                "ORDER BY APPLICATIONDATE", this::mapCustomer);
    }

    private Customer mapCustomer(ResultSet rs, int rowNum) throws SQLException {
        return new Customer(rs.getString("FIRSTNAME"), rs.getString("LASTNAME"), rs.getString("EMAIL"));
    }
}
