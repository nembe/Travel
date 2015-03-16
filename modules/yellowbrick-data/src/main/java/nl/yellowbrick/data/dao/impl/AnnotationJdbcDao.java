package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.AnnotationDao;
import nl.yellowbrick.data.domain.AnnotationDefinition;
import nl.yellowbrick.data.domain.AnnotationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AnnotationJdbcDao implements AnnotationDao {

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private Mutator mutator;

    @Override
    public AnnotationDefinition createAnnotationDefinition(AnnotationDefinition def) {
        String insertSql = "insert into annotation_definition " +
                "(id, customer_id, annotation_type, name, mutator, mutation_date, is_default, free_input) " +
                "values (annotation_definition_seq.nextval, ?, ?, ?, ?, SYSDATE, ?, ?)";

        template.update(insertSql,
                def.getCustomerId(),
                def.getType().getCode(),
                def.getName(),
                mutator.get(),
                def.isDefaultAnnotation() ? "1" : "0",
                def.isFreeInput() ? "1" : "0");

        return findDefinition(def.getCustomerId(), def.getName(), def.getType()).get();
    }

    @Override
    public Optional<AnnotationDefinition> findDefinition(long customerId, String name, AnnotationType type) {
        String sql = "select * from annotation_definition " +
                "where customer_id = ? " +
                "and lower(name) = ? " +
                "and annotation_type = ?";

        return template.query(sql, rowMapper(), customerId, name.toLowerCase(), type.getCode()).stream().findFirst();
    }

    @Override
    public void createAnnotationValue(AnnotationDefinition definition, long recordId, String value) {
        String sql = "insert into annotation_value(id, definition_id, record_id, annotation_value) " +
                "values (annotation_value_seq.nextval, ?, ?, ?)";

        template.update(sql, definition.getId(), recordId, value);
    }

    private RowMapper<AnnotationDefinition> rowMapper() {
        return (rs, rowNum) -> {
            AnnotationDefinition def = new AnnotationDefinition();
            def.setId(rs.getLong("id"));
            def.setPosition(rs.getInt("position"));
            def.setCustomerId(rs.getLong("customer_id"));
            def.setType(AnnotationType.byCode(rs.getString("annotation_type")));
            def.setName(rs.getString("name"));
            def.setDefaultAnnotation(rs.getString("is_default").equals("1"));
            def.setFreeInput(rs.getString("free_input").equals("1"));

            return def;
        };
    }
}
