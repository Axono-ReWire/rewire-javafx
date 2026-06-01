package com.axono.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Executes pending database migrations from {@code /migrations/} directory.
 * Migrations are identified by filenames matching pattern NNN_description.sql
 * (e.g., 001_add_column.sql, 002_create_table.sql).
 *
 * <p>Migration execution is tracked in the {@code schema_migrations} table
 * to ensure each migration runs exactly once. This allows safe, incremental
 * schema updates without deleting existing data.</p>
 */
public final class MigrationRunner {

    /** Classpath resource directory for migration files. */
    private static final String MIGRATIONS_DIR = "/migrations/";

    /** Utility class — not instantiable. */
    private MigrationRunner() {
    }

    /**
     * Discovers and executes all pending migrations from the migrations
     * directory. Each migration file is executed only once; subsequent calls
     * skip already-applied migrations.
     *
     * @throws SQLException if a migration fails or the database cannot be
     *                      accessed.
     * @throws IOException  if migration files cannot be read from the
     *                      classpath.
     */
    public static void runMigrations() throws SQLException, IOException {
        List<String> pendingMigrations = getPendingMigrations();
        if (pendingMigrations.isEmpty()) {
            System.out.println("No pending migrations.");
            return;
        }

        System.out.println("Found " + pendingMigrations.size()
                + " pending migration(s).");

        try (Connection conn = Database.open()) {
            for (String migrationName : pendingMigrations) {
                executeMigration(conn, migrationName);
            }
        }
    }

    /**
     * Returns only those migrations from the known list that have not yet
     * been applied (tracked in schema_migrations).
     *
     * @return list of pending migration filenames in sorted order; empty if
     *         all have been applied; never null.
     * @throws SQLException if the database cannot be accessed.
     * @throws IOException  if the index file cannot be read.
     */
    private static List<String> getPendingMigrations()
            throws SQLException, IOException {
        List<String> allMigrations = discoverMigrationFiles();
        List<String> appliedMigrations = getAppliedMigrations();
        List<String> pending = new ArrayList<>();

        for (String migration : allMigrations) {
            if (!appliedMigrations.contains(migration)) {
                pending.add(migration);
            }
        }

        return pending;
    }

    /**
     * Reads the migrations index file and returns all listed migration
     * filenames in sorted order. Blank lines and lines starting with
     * {@code #} are ignored. Adding a new migration requires only adding
     * one line to {@code migrations/index.txt} — no Java changes needed.
     *
     * @return sorted list of migration filenames (never null).
     * @throws IOException if the index file cannot be read.
     */
    private static List<String> discoverMigrationFiles() throws IOException {
        String index = Database.loadClasspathText(
                MIGRATIONS_DIR + "index.txt");
        List<String> found = new ArrayList<>();
        for (String line : index.split("\n")) {
            String name = line.trim();
            if (!name.isEmpty() && !name.startsWith("#")) {
                found.add(name);
            }
        }
        Collections.sort(found);
        return found;
    }

    /**
     * Queries the {@code schema_migrations} table and returns the set of
     * migration names that have already been applied.
     *
     * @return list of applied migration names (never null, empty if none
     *         applied).
     * @throws SQLException if the database cannot be accessed.
     */
    private static List<String> getAppliedMigrations() throws SQLException {
        List<String> applied = new ArrayList<>();
        String sql = "SELECT migration FROM schema_migrations "
                + "ORDER BY migration";

        try (Connection conn = Database.open();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                applied.add(rs.getString("migration"));
            }
        } catch (SQLException ex) {
            // schema_migrations table may not exist yet; treat as empty list
            if (ex.getMessage().contains("no such table")) {
                return Collections.emptyList();
            }
            throw ex;
        }

        return applied;
    }

    /**
     * Executes a single migration file and records its completion in the
     * {@code schema_migrations} table.
     *
     * <p>If the migration SQL fails because the change was already made
     * (e.g., "duplicate column name"), the migration is treated as
     * successfully applied and recorded without re-raising the error.
     * This guards against the case where the base schema already has the
     * column (fresh installs) while existing databases need the ALTER.</p>
     *
     * @param conn          the database connection to use.
     * @param migrationName the migration filename (e.g.,
     *                      "001_add_column.sql").
     * @throws SQLException if the migration fails for a non-idempotent
     *                      reason.
     * @throws IOException  if the migration file cannot be read.
     */
    private static void executeMigration(final Connection conn,
            final String migrationName) throws SQLException, IOException {
        System.out.println("Applying migration: " + migrationName);

        String migrationSql = loadMigrationFile(migrationName);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(migrationSql);
        } catch (SQLException ex) {
            // If the change was already in place (fresh install with correct
            // schema), treat this as a successful no-op and record it.
            String msg = ex.getMessage() == null ? "" : ex.getMessage()
                    .toLowerCase();
            boolean alreadyApplied = msg.contains("duplicate column name")
                    || msg.contains("already has a column");
            if (!alreadyApplied) {
                throw ex;
            }
            System.out.println("Migration already applied (column exists): "
                    + migrationName);
        }

        // Record the migration in schema_migrations (only if not already there)
        String insertSql = "INSERT OR IGNORE INTO schema_migrations "
                + "(migration) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, migrationName);
            pstmt.executeUpdate();
        }

        System.out.println("Migration complete: " + migrationName);
    }

    /**
     * Loads a migration file from the classpath and returns its SQL content
     * with comment lines stripped.
     *
     * @param migrationName the migration filename (e.g.,
     *                      "001_add_column.sql").
     * @return the SQL content of the migration file.
     * @throws IOException if the file cannot be read or is not found.
     */
    private static String loadMigrationFile(final String migrationName)
            throws IOException {
        String raw = Database.loadClasspathText(
                MIGRATIONS_DIR + migrationName);
        StringBuilder sb = new StringBuilder();
        for (String line : raw.split("\n")) {
            if (!line.trim().startsWith("--")) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
