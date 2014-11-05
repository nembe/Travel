package nl.yellowbrick.dao.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import nl.yellowbrick.dao.CustomerDao;
import nl.yellowbrick.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerJdbcDao implements CustomerDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public List<Customer> findAllPendingActivation() {
        String sql = Joiner.on(' ').join(ImmutableList.of(
                "SELECT c.*, ba.agentnaam AS agentname, cs.label AS custstatus, pg.description cgroup, 0 as parkammertotal",
                "FROM CUSTOMER c",
                "INNER JOIN CUSTOMERADDRESS ca ON c.customerid = ca.customeridfk",
                "INNER JOIN PRODUCT_GROUP pg ON pg.id = c.productgroup_id",
                "INNER JOIN TBLBILLINGAGENT ba ON c.billingagentidfk = ba.billingagentid",
                "INNER JOIN CUSTOMERSTATUS cs ON c.customerstatusidfk = cs.customerstatusid",
                "WHERE (ca.ADDRESSTYPEIDFK = 1 OR ca.ADDRESSTYPEIDFK IS NULL)",
                "AND c.PRODUCTGROUP_ID = 1 ",
                "AND c.CUSTOMERSTATUSIDFK = 1 ",
                "ORDER BY APPLICATIONDATE"
        ));

        BeanPropertyRowMapper<Customer> rowMapper = new BeanPropertyRowMapper<>(Customer.class);
        rowMapper.setPrimitivesDefaultedForNullValue(true);

        return template.query(sql, rowMapper);
    }
}
