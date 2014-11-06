package nl.yellowbrick.dao.impl;

import nl.yellowbrick.dao.ConfigDao;
import nl.yellowbrick.dao.ConfigSection;
import nl.yellowbrick.domain.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ConfigJdbcDao implements ConfigDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public List<Config> findAllBySection(ConfigSection section) {
        return template.query("SELECT * FROM TBLCONFIG WHERE SECTION = ?", rowMapper(), section.getKey());
    }

    @Override
    public Optional<Config> findSectionField(ConfigSection section, String field) {
        String sql = "SELECT * FROM TBLCONFIG WHERE SECTION = ? AND FIELD = ?";

        List<Config> configs = template.query(sql, rowMapper(), section.getKey(), field);

        switch(configs.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(configs.get(0));
            default:
                throw new IllegalStateException("Expected to get no more than 1 Config but got " + configs.size());
        }
    }

    private RowMapper<Config> rowMapper() {
        BeanPropertyRowMapper<Config> rowMapper = new BeanPropertyRowMapper<>(Config.class);
        rowMapper.setPrimitivesDefaultedForNullValue(true);

        return rowMapper;
    }
}
