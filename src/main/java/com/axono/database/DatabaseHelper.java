package com.axono.database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:database/rewire.db";

    private Connection connection;

    public DatabaseHelper() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
    }

    public Map<String, Object> getById(String tableName, int recordId) throws SQLException {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, recordId);

        ResultSet rs = stmt.executeQuery();

        if (!rs.next()) {
            return null;
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Map<String, Object> row = new HashMap<>();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object value = rs.getObject(i);
            row.put(columnName, value);
        }

        return row;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}