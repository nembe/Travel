package nl.yellowbrick.data.dao.impl;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

public class StreamingStatementCreator implements PreparedStatementCreator {

    private static final int FETCH_SIZE = Short.MAX_VALUE;

    private final String sql;
    private final Object[] args;

    public StreamingStatementCreator(String sql, Object... args) {
        this.sql = sql;
        this.args = args;
    }

     @Override
    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(sql, TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
        statement.setFetchSize(FETCH_SIZE);

        if(args != null)
            new ArgumentPreparedStatementSetter(args).setValues(statement);

        return statement;
    }
}
