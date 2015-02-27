package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.AnnotationDao;
import nl.yellowbrick.data.domain.Annotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AnnotationJdbcDao implements AnnotationDao {

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private Mutator mutator;

    @Override
    public void createAnnotation(Annotation annotation) {
        long definitionId = createDefinitionReturningId(annotation);

        createAnnotationValue(definitionId, annotation.getRecordId(), annotation.getValue());
    }

    private long createDefinitionReturningId(Annotation annotation) {
        Long id = template.queryForObject("select annotation_definition_seq.nextval from dual", Long.class);
        String insertSql = "insert into annotation_definition " +
                "(id, customer_id, annotation_type, name, mutator, mutation_date, is_default, free_input) " +
                "values (?, ?, ?, ?, ?, SYSDATE, ?, ?)";

        template.update(insertSql, id,
                annotation.getCustomerId(),
                annotation.getType().getCode(),
                annotation.getName(),
                mutator.get(),
                annotation.isDefaultAnnotation() ? "1" : "0",
                annotation.isFreeInput() ? "1" : "0");

        return id;
    }

    public void createAnnotationValue(long definitionId, long recordId, String value) {
        String sql = "insert into annotation_value(id, definition_id, record_id, annotation_value) " +
                "values (annotation_value_seq.nextval, ?, ?, ?)";

        template.update(sql, definitionId, recordId, value);
    }
}
