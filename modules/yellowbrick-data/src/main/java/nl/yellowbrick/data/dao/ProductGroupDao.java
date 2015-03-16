package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.ProductGroup;
import nl.yellowbrick.data.domain.ProductSubgroup;

import java.util.List;
import java.util.Optional;

public interface ProductGroupDao {

    List<ProductGroup> all();

    Optional<ProductGroup> findByDescription(String description);

    Optional<ProductGroup> update(ProductGroup productGroup);

    List<ProductSubgroup> findSubgroupsForProductGroup(long productGroupId);

    void update(ProductSubgroup productSubgroup);
}
