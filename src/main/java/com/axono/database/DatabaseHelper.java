package com.axono.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Low-level SQLite database helper for the Axono ReWire application.
 * Opens a JDBC connection to the local SQLite database and provides
 * generic record retrieval by primary key.
 */
public final class DatabaseHelper {

    /** JDBC connection URL pointing to the local SQLite database file. */
    private static final String DB_URL = "jdbc:sqlite:database/rewire.db";

    /** The active JDBC {@link Connection} to the database. */
    private Connection connection;

    /**
     * Opens a new connection to the SQLite database.
     *
     * @throws SQLException if the database connection cannot be established.
     */
    public DatabaseHelper() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
    }

    /**
     * Retrieves a single row from the specified table by its {@code id} column.
     * Returns {@code null} if no matching record exists.
     *
     * @param tableName the name of the table to query.
     * @param recordId  the integer primary key of the record to retrieve.
     * @return a {@link Map} of column names to values, or {@code null} if not
     *         found.
     * @throws SQLException if the query fails.
     */
    public Map<String, Object> getById(String tableName, int recordId) throws SQLException {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, recordId);

        ResultSet rs = stmt.executeQuery();

        if (!rs.next()) {
            return new HashMap<>();
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

    /**
     * Closes the underlying JDBC connection, releasing all database resources.
     *
     * @throws SQLException if closing the connection fails.
     */

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
