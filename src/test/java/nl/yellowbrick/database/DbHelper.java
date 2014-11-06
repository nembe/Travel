package nl.yellowbrick.database;

import com.google.common.base.Function;
import nl.yellowbrick.functions.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DbHelper {

    @Autowired
    private JdbcTemplate template;

    public void truncateTable(String tableName) {
        Functions.unchecked(() -> {
            template.execute("DELETE FROM " + tableName);
        });
    }

    public <T> T withTemplate(Function<JdbcTemplate, T> dbAction) {
        return dbAction.apply(template);
    }
}
