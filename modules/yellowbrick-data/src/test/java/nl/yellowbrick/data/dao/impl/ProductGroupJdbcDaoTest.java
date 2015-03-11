package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.domain.ProductGroup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProductGroupJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    ProductGroupJdbcDao dao;

    @Test
    public void fetches_all_records() {
        List<ProductGroup> all = dao.all();

        assertThat(all.size(), is(3));
        assertThat(all.get(1), equalTo(testProductGroup()));
    }

    @Test
    public void fetches_record_by_case_insensitive_description() {
        assertThat(dao.findByDescription("bla bla bla"), is(Optional.empty()));
        assertThat(dao.findByDescription("ABN"), is(Optional.of(testProductGroup())));
        assertThat(dao.findByDescription("aBn"), is(Optional.of(testProductGroup())));
    }

    @Test
    public void updates_product_group_properties_and_sets_mutator() {
        ProductGroup pg = testProductGroup();
        pg.setDescription("TEST");
        pg.setStartDate(Date.valueOf("2015-01-01"));
        pg.setEndDate(Date.valueOf("2020-01-01"));
        pg.setInternalCardProvisioning(!pg.isInternalCardProvisioning());
        pg.setMaxAnnotations(5);

        dao.update(pg);

        ProductGroup updatedPg = dao.findByDescription("TEST").get();

        assertThat(updatedPg.getDescription(), is(pg.getDescription()));
        assertThat(updatedPg.getStartDate(), is(pg.getStartDate()));
        assertThat(updatedPg.getEndDate(), is(pg.getEndDate()));
        assertThat(updatedPg.isInternalCardProvisioning(), is(pg.isInternalCardProvisioning()));
        assertThat(updatedPg.getMaxAnnotations(), is(updatedPg.getMaxAnnotations()));
        assertThat(updatedPg.getMutator(), is("TEST MUTATOR"));
        assertThat(updatedPg.getMutationDate(), notNullValue());
    }

    private ProductGroup testProductGroup() {
        ProductGroup pg = new ProductGroup();
        pg.setId(2l);
        pg.setDescription("ABN");
        pg.setInternalCardProvisioning(false);
        pg.setMutator("YBBEHEER:max@prod");
        pg.setMutationDate(null);
        pg.setMaxAnnotations(8);
        pg.setStartDate(Date.valueOf("2012-09-02"));
        pg.setEndDate(Date.valueOf("2012-09-02"));

        return pg;
    }
}
