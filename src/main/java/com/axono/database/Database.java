package com.axono.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    // Path to your database file
    private static final String URL = "jdbc:sqlite:database/rewire.db";
    // Example alternatives:
    // "jdbc:sqlite:data/app.db"
    // "jdbc:sqlite:/absolute/path/to/app.db"

    /**
     * Returns a new database connection.
     *
     * @return a valid JDBC Connection
     * @throws RuntimeException if connection fails
     */
    public static Connection getConnection() {
        try {
            // Force-load SQLite driver
            Class.forName("org.sqlite.JDBC");

            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }
}