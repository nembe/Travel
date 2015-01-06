package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.dao.AdministratorDao;
import nl.yellowbrick.data.domain.Administrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdministratorJdbcDao implements AdministratorDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public Optional<Administrator> findByUsername(String username) {
        String sql = "SELECT a.*, a.usernaam AS username FROM tbladministrator a where usernaam = ?";
        RowMapper<Administrator> rowMapper = new BeanPropertyRowMapper<>(Administrator.class);

        return template.query(sql, rowMapper, username).stream().findFirst();
    }
}
