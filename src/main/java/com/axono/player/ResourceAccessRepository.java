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
 * DAO for tracking user resource access (learning content opened/viewed).
 * Used to show "Recently Accessed" resources in the dashboard.
 */
public final class ResourceAccessRepository {

    /** Index of the module_name parameter in the trackAccess SQL statement. */
    private static final int PARAM_MODULE_NAME = 3;

    /** Utility class — not instantiable. */
    private ResourceAccessRepository() {
    }

    /**
     * Records or updates access time for a resource. If the user has
     * previously accessed this resource, updates the accessed_at timestamp.
     * Otherwise, creates a new access record.
     *
     * @param userId         the user ID.
     * @param presentationId the presentation ID.
     * @param moduleName     the module name.
     * @throws SQLException  if the operation fails.
     */
    public static void trackAccess(final int userId,
            final String presentationId,
            final String moduleName) throws SQLException {
        String sql = "INSERT INTO resource_access "
                + "(user_id, presentation_id, module_name) "
                + "VALUES (?, ?, ?) "
                + "ON CONFLICT(user_id, presentation_id) "
                + "DO UPDATE SET accessed_at = datetime('now')";
        try (Connection conn = Database.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, presentationId);
            stmt.setString(PARAM_MODULE_NAME, moduleName);
            stmt.executeUpdate();
        }
    }

    /**
     * Loads recently accessed resources for a user, ordered by access time.
     *
     * @param userId the user ID.
     * @param limit  the maximum number of entries to return.
     * @return list of {@link RecentResource} tuples, most recent first.
     *         Empty list if no accesses recorded.
     * @throws SQLException if the operation fails.
     */
    public static List<RecentResource> getRecentResources(
            final int userId, final int limit) throws SQLException {
        String sql = "SELECT module_name, presentation_id, accessed_at "
                + "FROM resource_access "
                + "WHERE user_id = ? "
                + "ORDER BY accessed_at DESC "
                + "LIMIT ?";
        List<RecentResource> result = new ArrayList<>();
        try (Connection conn = Database.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new RecentResource(
                            rs.getString("module_name"),
                            rs.getString("presentation_id"),
                            rs.getString("accessed_at")));
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Simple DTO for recent resource data.
     */
    public static final class RecentResource {

        /** The module name of the resource. */
        private final String moduleName;

        /** The presentation ID of the resource. */
        private final String presentationId;

        /** ISO 8601 timestamp of when the resource was last accessed. */
        private final String accessedAt;

        /**
         * Constructs a RecentResource DTO.
         *
         * @param module       the module name.
         * @param presentation the presentation ID.
         * @param timestamp    the access timestamp.
         */
        public RecentResource(final String module,
                final String presentation,
                final String timestamp) {
            this.moduleName = module;
            this.presentationId = presentation;
            this.accessedAt = timestamp;
        }

        /**
         * Returns the module name.
         *
         * @return the module name.
         */
        public String getModuleName() {
            return moduleName;
        }

        /**
         * Returns the presentation ID.
         *
         * @return the presentation ID.
         */
        public String getPresentationId() {
            return presentationId;
        }

        /**
         * Returns the access timestamp.
         *
         * @return the ISO 8601 timestamp.
         */
        public String getAccessedAt() {
            return accessedAt;
        }
    }
}
