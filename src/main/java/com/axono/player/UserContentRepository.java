package com.axono.player;

import com.axono.content.LearningContent;
import com.axono.content.LearningContentParseException;
import com.axono.content.LearningContentParser;
import com.axono.database.Database;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the {@code user_content} table.
 *
 * <p>User-created learning content is stored with its full XML text (including
 * the {@code <mediaAssets>} block) in the database, alongside the
 * {@code base_directory} path that points to the directory on the local
 * filesystem where the associated media files live. At load time the XML is
 * re-parsed and a {@link com.axono.content.MediaAssetRegistry} is constructed
 * so that media {@code src} values resolve to the correct absolute paths.</p>
 */
public final class UserContentRepository {

    /** SQL to insert or replace a user-content record. */
    private static final String SQL_UPSERT =
            "INSERT OR REPLACE INTO user_content "
            + "(id, title, module_name, topic_name, xml_content, "
            + "base_directory, created_by, is_quiz, updated_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))";

    /** SQL to load all content for a given user. */
    private static final String SQL_LOAD_BY_USER =
            "SELECT id, title, module_name, topic_name, xml_content, "
            + "base_directory, is_quiz "
            + "FROM user_content WHERE created_by = ? "
            + "ORDER BY updated_at DESC";

    /** SQL to load all user-content (for merged browser view). */
    private static final String SQL_LOAD_ALL =
            "SELECT id, title, module_name, topic_name, xml_content, "
            + "base_directory, is_quiz "
            + "FROM user_content ORDER BY updated_at DESC";

    /** SQL to delete a record. */
    private static final String SQL_DELETE =
            "DELETE FROM user_content WHERE id = ? AND created_by = ?";

    // ── Parameter indices for prepared statements ────────────────────────────

    /** Parameter index for id in upsert. */
    private static final int PARAM_ID = 1;

    /** Parameter index for title in upsert. */
    private static final int PARAM_TITLE = 2;

    /** Parameter index for module_name in upsert. */
    private static final int PARAM_MODULE_NAME = 3;

    /** Parameter index for topic_name in upsert. */
    private static final int PARAM_TOPIC_NAME = 4;

    /** Parameter index for xml_content in upsert. */
    private static final int PARAM_XML_CONTENT = 5;

    /** Parameter index for base_directory in upsert. */
    private static final int PARAM_BASE_DIR = 6;

    /** Parameter index for created_by in upsert. */
    private static final int PARAM_CREATED_BY = 7;

    /** Parameter index for is_quiz in upsert. */
    private static final int PARAM_IS_QUIZ = 8;

    /** Private constructor — utility class. */
    private UserContentRepository() {
    }

    /**
     * Immutable holder for all fields needed to persist a user-content row.
     * Use the nested {@link Builder} to construct instances; this avoids a
     * parameter-count violation on the constructor.
     */
    public static final class UserContentRecord {

        /** Unique content identifier. */
        private final String recId;

        /** Display title. */
        private final String recTitle;

        /** Module folder name. */
        private final String recModuleName;

        /** Topic folder name. */
        private final String recTopicName;

        /** Full XML text including mediaAssets. */
        private final String recXmlContent;

        /** Absolute base directory for media files. */
        private final Path recBaseDir;

        /** Authenticated user id. */
        private final int recCreatedBy;

        /** Whether the content is a quiz. */
        private final boolean recIsQuiz;

        /**
         * Private — callers must use {@link Builder}.
         *
         * @param b the builder with populated fields.
         */
        private UserContentRecord(final Builder b) {
            this.recId = b.bId;
            this.recTitle = b.bTitle;
            this.recModuleName = b.bModuleName;
            this.recTopicName = b.bTopicName;
            this.recXmlContent = b.bXml;
            this.recBaseDir = b.bBaseDir;
            this.recCreatedBy = b.bCreatedBy;
            this.recIsQuiz = b.bIsQuiz;
        }

        /**
         * Fluent builder for {@link UserContentRecord}.
         *
         * <pre>{@code
         * UserContentRecord rec = new UserContentRecord.Builder()
         *     .id(uuid).title(title).moduleName(mod).topicName(topic)
         *     .xmlContent(xml).baseDir(dir).createdBy(uid).isQuiz(false)
         *     .build();
         * }</pre>
         */
        public static final class Builder {

            /** Builder field: unique content id. */
            private String bId;

            /** Builder field: display title. */
            private String bTitle;

            /** Builder field: module folder name. */
            private String bModuleName;

            /** Builder field: topic folder name. */
            private String bTopicName;

            /** Builder field: full XML text. */
            private String bXml;

            /** Builder field: base directory path. */
            private Path bBaseDir;

            /** Builder field: authenticated user id. */
            private int bCreatedBy;

            /** Builder field: whether the content is a quiz. */
            private boolean bIsQuiz;

            /**
             * Sets the unique content id (UUID).
             *
             * @param id the unique content identifier.
             * @return this builder.
             */
            public Builder id(final String id) {
                this.bId = id;
                return this;
            }

            /**
             * Sets the display title.
             *
             * @param title the display title.
             * @return this builder.
             */
            public Builder title(final String title) {
                this.bTitle = title;
                return this;
            }

            /**
             * Sets the module folder name.
             *
             * @param m the module folder name.
             * @return this builder.
             */
            public Builder moduleName(final String m) {
                this.bModuleName = m;
                return this;
            }

            /**
             * Sets the topic folder name.
             *
             * @param t the topic folder name.
             * @return this builder.
             */
            public Builder topicName(final String t) {
                this.bTopicName = t;
                return this;
            }

            /**
             * Sets the full XML text including the mediaAssets block.
             *
             * @param xml the XML content.
             * @return this builder.
             */
            public Builder xmlContent(final String xml) {
                this.bXml = xml;
                return this;
            }

            /**
             * Sets the absolute base directory for media files.
             *
             * @param dir the base directory path.
             * @return this builder.
             */
            public Builder baseDir(final Path dir) {
                this.bBaseDir = dir;
                return this;
            }

            /**
             * Sets the id of the authenticated user.
             *
             * @param uid the user id.
             * @return this builder.
             */
            public Builder createdBy(final int uid) {
                this.bCreatedBy = uid;
                return this;
            }

            /**
             * Sets whether the content is a quiz.
             *
             * @param quiz {@code true} if content is a quiz.
             * @return this builder.
             */
            public Builder isQuiz(final boolean quiz) {
                this.bIsQuiz = quiz;
                return this;
            }

            /**
             * Builds and returns the {@link UserContentRecord}.
             *
             * @return a new immutable record.
             */
            public UserContentRecord build() {
                return new UserContentRecord(this);
            }
        }
    }

    /**
     * Persists a user-content record to the database. If a record with the
     * same {@code id} already exists it is replaced (upsert behaviour).
     *
     * @param rec the record to persist.
     * @throws SQLException if the insert fails.
     */
    public static void save(final UserContentRecord rec) throws SQLException {
        try (Connection conn = Database.open();
             PreparedStatement ps = conn.prepareStatement(SQL_UPSERT)) {
            ps.setString(PARAM_ID, rec.recId);
            ps.setString(PARAM_TITLE, rec.recTitle);
            ps.setString(PARAM_MODULE_NAME, rec.recModuleName);
            ps.setString(PARAM_TOPIC_NAME, rec.recTopicName);
            ps.setString(PARAM_XML_CONTENT, rec.recXmlContent);
            ps.setString(PARAM_BASE_DIR,
                    rec.recBaseDir.toAbsolutePath().toString());
            ps.setInt(PARAM_CREATED_BY, rec.recCreatedBy);
            ps.setInt(PARAM_IS_QUIZ, rec.recIsQuiz ? 1 : 0);
            ps.executeUpdate();
        }
    }

    /**
     * Loads all content created by a given user, re-parsing each XML string
     * and constructing the {@link com.axono.content.MediaAssetRegistry}.
     *
     * @param userId the authenticated user's id.
     * @return an unmodifiable list of parsed content; never {@code null}.
     * @throws SQLException if the database cannot be read.
     */
    public static List<LearningContent> loadByUser(final int userId)
            throws SQLException {
        try (Connection conn = Database.open();
             PreparedStatement ps = conn.prepareStatement(SQL_LOAD_BY_USER)) {
            ps.setInt(1, userId);
            return parseResultSet(ps.executeQuery());
        }
    }

    /**
     * Loads all user-created content from the database (all users). Used by
     * {@link LearningContentLoader} to merge user content into the browser.
     *
     * @return a mutable list of parsed content; never {@code null}.
     * @throws SQLException if the database cannot be read.
     */
    public static List<LearningContent> loadAll() throws SQLException {
        try (Connection conn = Database.open();
             PreparedStatement ps = conn.prepareStatement(SQL_LOAD_ALL)) {
            return parseResultSet(ps.executeQuery());
        }
    }

    /**
     * Deletes the content record with the given id, only when it belongs to
     * the specified user (prevents cross-user deletion).
     *
     * @param id       the content id.
     * @param userId   the authenticated user's id.
     * @throws SQLException if the database operation fails.
     */
    public static void delete(final String id, final int userId)
            throws SQLException {
        try (Connection conn = Database.open();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Parses each row in a {@link ResultSet} into a {@link LearningContent},
     * re-constructing the XML and registry from the stored columns.
     *
     * @param rs the result set (caller owns the connection).
     * @return a mutable list of successfully parsed content.
     * @throws SQLException if reading a column fails.
     */
    private static List<LearningContent> parseResultSet(final ResultSet rs)
            throws SQLException {
        List<LearningContent> result = new ArrayList<>();
        while (rs.next()) {
            String id = rs.getString("id");
            String moduleName = rs.getString("module_name");
            String topicName = rs.getString("topic_name");
            String xmlContent = rs.getString("xml_content");
            String baseDirStr = rs.getString("base_directory");

            Path baseDir = null;
            try {
                baseDir = Paths.get(baseDirStr);
            } catch (Exception ignored) {
                // Non-parseable path — content will have no registry
            }

            try {
                LearningContent content = LearningContentParser.parseFromString(
                        xmlContent, id, moduleName, topicName, baseDir);
                result.add(content);
            } catch (LearningContentParseException ex) {
                System.err.println("Skipping malformed user content id="
                        + id + ": " + ex.getMessage());
            }
        }
        return result;
    }
}
