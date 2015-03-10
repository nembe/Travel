package nl.yellowbrick.data.dao.impl;


import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductGroupJdbcDao implements ProductGroupDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public List<ProductGroup> all() {
        return template.query("select * from product_group", rowMapper());
    }

    @Override
    public Optional<ProductGroup> findByDescription(String description) {
        String sql = "select * from product_group where lower(description) = ?";

        return template.query(sql, rowMapper(), description.toLowerCase()).stream().findFirst();
    }

    private RowMapper<ProductGroup> rowMapper() {
        return (rs, rowNum) -> {
            ProductGroup productGroup = new ProductGroup();
            productGroup.setId(rs.getLong(("id")));
            productGroup.setDescription(rs.getString("description"));
            productGroup.setInternalCardProvisioning(rs.getString("internal_card_prov").equals("1"));
            productGroup.setMutator(rs.getString("mutator"));
            productGroup.setMutationDate(rs.getTimestamp("mutation_date"));
            productGroup.setMaxAnnotations(rs.getInt("annotations_max"));
            productGroup.setStartDate(rs.getDate("start_date"));
            productGroup.setEndDate(rs.getDate("end_date"));

            return productGroup;
        };
    }
}
