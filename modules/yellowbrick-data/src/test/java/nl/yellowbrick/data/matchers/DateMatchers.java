package nl.yellowbrick.data.matchers;

import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;

import java.util.Date;

public class DateMatchers {

    public static Matcher<Date> after(Date date) {
        return new ArgumentMatcher<Date>() {
            @Override
            public boolean matches(Object o) {
                Date otherDate = (Date) o;

                return otherDate.after(date);
            }
        };
    }
}
