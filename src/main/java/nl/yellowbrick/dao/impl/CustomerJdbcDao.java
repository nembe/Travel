package nl.yellowbrick.dao.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
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
        String sql = Joiner.on(' ').join(ImmutableList.of(
                "SELECT c.*",
                "FROM CUSTOMER c",
                "INNER JOIN CUSTOMERADDRESS ca ON c.customerid = ca.customeridfk",
                "INNER JOIN PRODUCT_GROUP p ON p.id = c.productgroup_id",
                "WHERE (ca.ADDRESSTYPEIDFK = 1 OR ca.ADDRESSTYPEIDFK IS NULL)",
                "AND c.PRODUCTGROUP_ID = 1 ",
                "AND c.CUSTOMERSTATUSIDFK = 1 ",
                "ORDER BY APPLICATIONDATE"
        ));

        return template.query(sql, this::mapCustomer);
    }

    private Customer mapCustomer(ResultSet rs, int rowNum) throws SQLException {
        Customer customer = new Customer();
        customer.setFirstName(rs.getString("firstname"));
        customer.setLastName(rs.getString("lastname"));
        customer.setEmail(rs.getString("email"));

        return customer;
    }
}
