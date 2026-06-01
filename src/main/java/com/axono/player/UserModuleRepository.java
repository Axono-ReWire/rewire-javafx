package com.axono.player;

import com.axono.database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data access object for user module selections. Persists which modules a user
 * has selected for learning to the {@code user_modules} table.
 */
public final class UserModuleRepository {

    /** Private constructor to prevent instantiation. */
    private UserModuleRepository() {
    }

    /**
     * Saves the given list of module names for a user, replacing any existing
     * selections. Uses a transaction to ensure atomicity.
     *
     * @param final userId the user ID.
     * @param final modules the module names to save.
     * @throws SQLException if the database operation fails.
     */
    public static void saveUserModules(final int userId,
            final List<String> modules) throws SQLException {
        try (Connection conn = Database.open()) {
            conn.setAutoCommit(false);
            try {
                deleteUserModules(conn, userId);
                insertUserModules(conn, userId, modules);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    /**
     * Loads all module names for a user from the database.
     *
     * @param final userId the user ID.
     * @return a list of module names, or an empty list if no modules exist.
     * @throws SQLException if the database operation fails.
     */
    public static List<String> loadUserModules(final int userId)
            throws SQLException {
        String sql = "SELECT module_name FROM user_modules "
                + "WHERE user_id = ? ORDER BY module_name";
        List<String> result = new ArrayList<>();
        try (Connection conn = Database.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("module_name"));
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Deletes all module selections for a user.
     *
     * @param final conn the database connection.
     * @param final userId the user ID.
     * @throws SQLException if the operation fails.
     */
    private static void deleteUserModules(final Connection conn,
            final int userId) throws SQLException {
        String sql = "DELETE FROM user_modules WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Inserts module selections for a user.
     *
     * @param final conn the database connection.
     * @param final userId the user ID.
     * @param final modules the module names.
     * @throws SQLException if the operation fails.
     */
    private static void insertUserModules(final Connection conn,
            final int userId,
            final List<String> modules) throws SQLException {
        String sql = "INSERT INTO user_modules (user_id, module_name) "
                + "VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String module : modules) {
                stmt.setInt(1, userId);
                stmt.setString(2, module);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}
