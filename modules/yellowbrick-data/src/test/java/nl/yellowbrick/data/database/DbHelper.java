package nl.yellowbrick.data.database;

import com.google.common.base.Function;
import nl.yellowbrick.data.functions.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class DbHelper {

    @Autowired
    private JdbcTemplate template;

    public void truncateTable(String tableName) {
        Functions.unchecked(() -> {
            template.execute("DELETE FROM " + tableName);
        });
    }

    public <T> T apply(Function<JdbcTemplate, T> dbAction) {
        return dbAction.apply(template);
    }

    public void accept(Consumer<JdbcTemplate> dbAction) {
        dbAction.accept(template);
    }
}
