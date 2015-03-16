package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.domain.ProductGroup;
import nl.yellowbrick.data.domain.ProductSubgroup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.sql.Timestamp;
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

    @Test
    public void returns_empty_list_if_cant_find_subgroups() {
        assertThat(dao.findSubgroupsForProductGroup(123l), empty());
    }

    @Test
    public void fetches_subgroups_for_given_product_group() {
        List<ProductSubgroup> subgroups = dao.findSubgroupsForProductGroup(1l);

        assertThat(subgroups, hasSize(2));
        assertThat(subgroups.get(1), equalTo(testSubgroup()));
    }

    @Test
    public void updates_product_subgroup_properties_and_sets_mutator() {
        ProductSubgroup sb = testSubgroup();
        sb.setDescription("updated");
        sb.setBusiness(false);
        sb.setDefaultIssuePhysicalCard(true);
        sb.setTheme("updated");

        dao.update(sb);

        ProductSubgroup updatedSg = dao.findSubgroupsForProductGroup(1l)
                .stream()
                .filter(sg -> sg.getId().equals(sb.getId()))
                .findFirst()
                .get();

        assertThat(updatedSg.getDescription(), is("updated"));
        assertThat(updatedSg.isBusiness(), is(false));
        assertThat(updatedSg.isDefaultIssuePhysicalCard(), is(true));
        assertThat(updatedSg.getMutator(), is("TEST MUTATOR"));
        assertThat(updatedSg.getMutationDate(), notNullValue());
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

    private ProductSubgroup testSubgroup() {
        ProductSubgroup sg = new ProductSubgroup();
        sg.setId(1l);
        sg.setProductGroupId(1l);
        sg.setDescription("Zakelijk");
        sg.setBusiness(true);
        sg.setDefaultIssuePhysicalCard(false);
        sg.setTheme("default");
        sg.setMutator("SYSTEM");
        sg.setMutationDate(Timestamp.valueOf("2012-06-01 00:46:39"));

        return sg;
    }
}
