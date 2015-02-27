package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.domain.Annotation;
import nl.yellowbrick.data.domain.AnnotationType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

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
    public void inserts_new_annotation_definition_and_value() {
        Annotation ann = sampleAnnotation();
        dao.createAnnotation(ann);

        Map<String, Object> def = fetchLatestAnnotationDefinition();
        assertThat(def.get("id"), notNullValue());
        assertThat(def.get("customer_id").toString(), is(String.valueOf(ann.getCustomerId())));
        assertThat(def.get("annotation_type"), is(AnnotationType.TRANSPONDER_CARD.getCode()));
        assertThat(def.get("name"), is(ann.getName()));
        assertThat(def.get("mutator"), is("TEST MUTATOR"));
        assertThat(def.get("mutation_date"), notNullValue());
        assertThat(def.get("is_default"), is("1"));
        assertThat(def.get("free_input"), is("1"));

        Map<String, Object> val = fetchLatestAnnotationValue();
        assertThat(val.get("id"), notNullValue());
        assertThat(val.get("definition_id"), equalTo(def.get("id")));
        assertThat(val.get("record_id").toString(), is(String.valueOf(ann.getRecordId())));
        assertThat(val.get("annotation_value"), is(ann.getValue()));
    }

    private Annotation sampleAnnotation() {
        Annotation annotation = new Annotation();

        annotation.setCustomerId(1l);
        annotation.setType(AnnotationType.TRANSPONDER_CARD);
        annotation.setRecordId(2l);
        annotation.setValue("1111");
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
