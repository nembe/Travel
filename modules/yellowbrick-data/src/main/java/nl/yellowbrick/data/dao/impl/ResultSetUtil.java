package nl.yellowbrick.data.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetUtil {

    public static Long getLongOrNull(ResultSet rs, String columnName) throws SQLException {
        Long l = rs.getLong(columnName);
        return rs.wasNull() ? null : l;
    }
}
