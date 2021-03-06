package nl.yellowbrick.data.dao.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import nl.yellowbrick.data.dao.MessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MessageJdbcDao implements MessageDao {

    @Autowired
    private JdbcTemplate template;

    private Logger log = LoggerFactory.getLogger(MessageJdbcDao.class);

    @Override
    public Optional<String> getMessage(String key, String locale) {
        String sql = "SELECT text FROM MESSAGE WHERE key = ? AND locale = ?";

        try {
            return Optional.ofNullable(
                    template.queryForObject(sql, String.class, key, locale)
            );
        } catch(DataAccessException e) {
            log.error(String.format("did not find message for key %s locale %s", key, locale), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getGroupSpecificMessage(String key, int groupId, String locale) {
        String mainKey = key + ".Group" + groupId;
        String altKey = key + ".Group0";
        String sql = Joiner.on(' ').join(ImmutableList.of(
                "SELECT * FROM",
                "( SELECT text FROM MESSAGE",
                "WHERE locale = ?",
                "AND key = ?",
                "OR key = ?",
                "ORDER BY key DESC",
                ") WHERE ROWNUM <= 1"
        ));

        try {
            return Optional.ofNullable(
                    template.queryForObject(sql, String.class, locale, mainKey, altKey)
            );
        } catch(DataAccessException e) {
            log.error(String.format("did not find message for group %d key %s locale %s", groupId, key, locale), e);
            return Optional.empty();
        }
    }
}
