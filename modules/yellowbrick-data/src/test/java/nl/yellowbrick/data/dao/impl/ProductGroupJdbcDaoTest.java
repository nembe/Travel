package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.domain.ProductGroup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ProductGroupJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    ProductGroupJdbcDao dao;

    @Test
    public void fetches_all_records() {
        List<ProductGroup> all = dao.all();

        ProductGroup pg = new ProductGroup();
        pg.setId(2l);
        pg.setDescription("ABN");
        pg.setInternalCardProvisioning(false);
        pg.setMutator("YBBEHEER:max@prod");
        pg.setMutationDate(null);
        pg.setMaxAnnotations(8);
        pg.setStartDate(Date.valueOf("2012-09-02"));
        pg.setEndDate(Date.valueOf("2012-09-02"));

        assertThat(all.size(), is(2));
        assertThat(all.get(1), equalTo(pg));
    }
}
