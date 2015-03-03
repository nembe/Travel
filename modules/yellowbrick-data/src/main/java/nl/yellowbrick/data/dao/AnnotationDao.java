package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.AnnotationDefinition;
import nl.yellowbrick.data.domain.AnnotationType;

import java.util.Optional;

public interface AnnotationDao {

    AnnotationDefinition createAnnotationDefinition(AnnotationDefinition annotation);

    Optional<AnnotationDefinition> findDefinition(long customerId, String name, AnnotationType type);

    void createAnnotationValue(AnnotationDefinition definition, long recordId, String value);
}
