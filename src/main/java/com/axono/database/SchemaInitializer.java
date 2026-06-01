package com.axono.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Reads {@code /schema.sql} from the classpath and executes each statement
 * against the SQLite database. All statements are written as
 * {@code CREATE TABLE IF NOT EXISTS}, so calling {@link #init()} on every
 * app launch is safe and idempotent.
 */
public final class SchemaInitializer {

    /** Classpath resource path for the schema file. */
    private static final String SCHEMA_RESOURCE = "/schema.sql";

    /** Utility class — not instantiable. */
    private SchemaInitializer() {
    }

    /**
     * Loads and executes every statement in {@code schema.sql}, then runs
     * any pending database migrations from the {@code migrations/} directory.
     *
     * @throws SQLException if the database cannot be opened or any
     *                      statement fails to execute.
     * @throws IOException  if the schema resource cannot be read.
     */
    public static void init() throws SQLException, IOException {
        String script = loadSchema();
        try (Connection conn = Database.open();
             Statement stmt = conn.createStatement()) {
            for (String raw : script.split(";")) {
                String sql = raw.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
        // Run pending migrations after base schema is created
        MigrationRunner.runMigrations();
    }

    /**
     * Reads the schema script from the classpath.
     *
     * @return the full SQL script as a single string.
     * @throws IOException if the resource is missing or unreadable.
     */
    private static String loadSchema() throws IOException {
        return Database.loadClasspathText(SCHEMA_RESOURCE);
    }
}
