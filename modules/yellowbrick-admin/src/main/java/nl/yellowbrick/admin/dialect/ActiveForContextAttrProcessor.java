package nl.yellowbrick.admin.dialect;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerMapping;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

import java.util.HashMap;
import java.util.Map;

public class ActiveForContextAttrProcessor extends AbstractAttributeModifierAttrProcessor {

    public ActiveForContextAttrProcessor() {
        super("activeforctx");
    }

    public int getPrecedence() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected Map<String, String> getModifiedAttributeValues(Arguments arguments, Element element, String attributeName) {
        final Map<String, String> values = new HashMap<>();
        final String expression = element.getAttributeValue(attributeName);

        // determine if it's an expression or string literal
        final String pathToMatch = expression.startsWith("$") || expression.startsWith("#")
                ? parseExpression(expression, arguments)
                : expression;

        final String actualPath = arguments.getContext().getVariables()
                .get(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                .toString();

        if (actualPath.startsWith(pathToMatch)) {
            values.put("class", "active");
        }

        return values;
    }

    private String parseExpression(String expression, Arguments arguments) {
        Configuration configuration = arguments.getConfiguration();
        IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);

        return (String) parser.parseExpression(configuration, arguments, expression).execute(configuration, arguments);
    }

    @Override
    protected ModificationType getModificationType(Arguments arguments, Element element, String s, String s2) {
        return ModificationType.APPEND_WITH_SPACE;
    }

    @Override
    protected boolean removeAttributeIfEmpty(Arguments arguments, Element element, String s, String s2) {
        return true;
    }

    @Override
    protected boolean recomputeProcessorsAfterExecution(Arguments arguments, Element element, String s) {
        return false;
    }
}
