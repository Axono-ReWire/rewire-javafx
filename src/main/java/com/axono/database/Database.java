package com.axono.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Static utility for opening short-lived JDBC connections to the SQLite
 * database. Callers are expected to wrap returned connections in
 * try-with-resources so that connections are reliably closed.
 */
public final class Database {

    /** JDBC URL pointing to the local SQLite database file. */
    private static final String URL = "jdbc:sqlite:database/rewire.db";

    /** Utility class — not instantiable. */
    private Database() {
    }

    /**
     * Opens a new JDBC {@link Connection} to the local SQLite database.
     * The caller owns the connection and must close it.
     *
     * @return a new {@link Connection}; never {@code null}.
     * @throws SQLException if the connection cannot be established.
     */
    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Reads a classpath resource as a UTF-8 string.
     *
     * @param resourcePath absolute classpath path, e.g. {@code "/schema.sql"}.
     * @return the full content of the resource.
     * @throws IOException if the resource is missing or cannot be read.
     */
    static String loadClasspathText(final String resourcePath)
            throws IOException {
        InputStream in = Database.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException(
                    "Missing classpath resource: " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
