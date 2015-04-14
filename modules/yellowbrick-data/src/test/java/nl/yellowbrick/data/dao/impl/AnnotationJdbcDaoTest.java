package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.AnnotationDefinition;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static nl.yellowbrick.data.domain.AnnotationType.TRANSPONDER_CARD;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class AnnotationJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    AnnotationJdbcDao dao;

    @Autowired
    DbHelper db;

    @Test
    public void inserts_new_annotation_definition() {
        AnnotationDefinition ann = sampleAnnotation();
        dao.createAnnotationDefinition(ann);

        Map<String, Object> def = fetchLatestAnnotationDefinition();
        assertThat(def.get("id"), notNullValue());
        assertThat(def.get("customer_id").toString(), is(String.valueOf(ann.getCustomerId())));
        assertThat(def.get("annotation_type"), is(TRANSPONDER_CARD.getCode()));
        assertThat(def.get("name"), is(ann.getName()));
        assertThat(def.get("mutator"), is("TEST MUTATOR"));
        assertThat(def.get("mutation_date"), notNullValue());
        assertThat(def.get("is_default"), is("1"));
        assertThat(def.get("free_input"), is("1"));
    }

    @Test
    public void finds_definition_by_name_and_type_and_customer_id() {
        AnnotationDefinition definition = sampleAnnotation();
        definition.setId(1l);
        definition.setCustomerId(4776);

        AnnotationDefinition actualDefinition = dao.findDefinition(4776, "Travelcard nummer", TRANSPONDER_CARD).get();

        assertThat(actualDefinition, equalTo(definition));
    }

    @Test
    public void inserts_new_annotation_value() {
        AnnotationDefinition definition = sampleAnnotation();
        definition.setId(1l);

        dao.createAnnotationValue(definition, 123l, "test");

        Map<String, Object> val = fetchLatestAnnotationValue();
        assertThat(val.get("id"), notNullValue());
        assertThat(val.get("definition_id").toString(), equalTo(definition.getId().toString()));
        assertThat(val.get("record_id").toString(), is("123"));
        assertThat(val.get("annotation_value"), is("test"));
    }

    private AnnotationDefinition sampleAnnotation() {
        AnnotationDefinition annotation = new AnnotationDefinition();

        annotation.setCustomerId(1l);
        annotation.setType(TRANSPONDER_CARD);
        annotation.setName("Travelcard nummer");
        annotation.setDefaultAnnotation(true);
        annotation.setFreeInput(true);

        return annotation;
    }

    private Map<String, Object> fetchLatestAnnotationDefinition() {
        return db.fetchLatestRecord("annotation_definition", "id");
    }

    private Map<String, Object> fetchLatestAnnotationValue() {
        return db.fetchLatestRecord("annotation_value", "id");
    }
}
