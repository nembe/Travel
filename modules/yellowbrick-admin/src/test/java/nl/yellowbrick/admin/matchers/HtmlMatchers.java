package nl.yellowbrick.admin.matchers;

import org.hamcrest.Matcher;
import org.hamcrest.core.CombinableMatcher;
import org.jsoup.nodes.Element;
import org.mockito.ArgumentMatcher;

public class HtmlMatchers {

    public static Matcher<Element> hasAttr(String attrName, String attrValue) {
        return new ArgumentMatcher<Element>() {
            @Override
            public boolean matches(Object o) {
                Element element = (Element) o;

                return element.attr(attrName).equals(attrValue);
            }
        };
    }

    public static Matcher<Element> isField(String fieldName, String fieldValue) {
        return new CombinableMatcher<>(hasAttr("name", fieldName)).and(hasAttr("value", fieldValue));
    }
}
