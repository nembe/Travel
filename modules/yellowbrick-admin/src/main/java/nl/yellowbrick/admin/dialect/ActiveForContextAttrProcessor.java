package nl.yellowbrick.admin.dialect;

import org.springframework.web.servlet.support.RequestContext;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;

import java.util.HashMap;
import java.util.Map;

public class ActiveForContextAttrProcessor extends AbstractAttributeModifierAttrProcessor {

    public ActiveForContextAttrProcessor() {
        super("activeforctx");
    }

    public int getPrecedence() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected Map<String, String> getModifiedAttributeValues(Arguments arguments, Element element, String attributeName) {
        final String contextPath = element.getAttributeValue(attributeName);
        final Map<String, String> values = new HashMap<>();

        RequestContext requestContext = (RequestContext) arguments.getContext().getVariables().get("springRequestContext");
        if (requestContext.getRequestUri().startsWith(contextPath)) {
            values.put("class", "active");
        }

        return values;
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
