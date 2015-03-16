package nl.yellowbrick.data.dao.impl;


import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import nl.yellowbrick.data.domain.ProductSubgroup;
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

    @Autowired
    private Mutator mutator;

    @Override
    public List<ProductGroup> all() {
        return template.query("select * from product_group", rowMapper());
    }

    @Override
    public Optional<ProductGroup> findByDescription(String description) {
        String sql = "select * from product_group where lower(description) = ?";

        return template.query(sql, rowMapper(), description.toLowerCase()).stream().findFirst();
    }

    @Override
    public Optional<ProductGroup> update(ProductGroup productGroup) {
        String sql = "update product_group set " +
                "description = ?, " +
                "start_date = ?, " +
                "end_date = ?, " +
                "annotations_max = ?, " +
                "internal_card_prov = ?, " +
                "mutator = ?, " +
                "mutation_date = sysdate " +
                "where id = ?";

        template.update(sql, productGroup.getDescription(),
                productGroup.getStartDate(),
                productGroup.getEndDate(),
                productGroup.getMaxAnnotations(),
                productGroup.isInternalCardProvisioning() ? "1" : "0",
                mutator.get(),
                productGroup.getId());

        return findByDescription(productGroup.getDescription());
    }

    @Override
    public void update(ProductSubgroup productSubgroup) {
        String sql = "update product_subgroup set " +
                "description = ?, " +
                "business = ?, " +
                "default_issue_physical_card = ?, " +
                "theme = ?, " +
                "mutator = ?, " +
                "mutation_date = sysdate " +
                "where id = ?";

        template.update(sql, productSubgroup.getDescription(),
                productSubgroup.isBusiness() ? "Y" : "N",
                productSubgroup.isDefaultIssuePhysicalCard() ? "Y" : "N",
                productSubgroup.getTheme(),
                mutator.get(),
                productSubgroup.getId());
    }

    @Override
    public List<ProductSubgroup> findSubgroupsForProductGroup(long productGroupId) {
        return template.query("select * from product_subgroup where product_group_id = ?",
                subgroupRowMapper(), productGroupId);
    }

    private RowMapper<ProductSubgroup> subgroupRowMapper() {
        return (rs, rowNum) -> {
            ProductSubgroup subgroup = new ProductSubgroup();
            subgroup.setId(rs.getLong(("id")));
            subgroup.setProductGroupId(rs.getLong("product_group_id"));
            subgroup.setDescription(rs.getString("description"));
            subgroup.setBusiness(rs.getString("business").equals("Y"));
            subgroup.setDefaultIssuePhysicalCard(rs.getString("default_issue_physical_card").equals("Y"));
            subgroup.setTheme(rs.getString("theme"));
            subgroup.setProductGroupId(rs.getLong("product_group_id"));
            subgroup.setMutator(rs.getString("mutator"));
            subgroup.setMutationDate(rs.getTimestamp("mutation_date"));

            return subgroup;
        };
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
