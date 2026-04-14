package com.axono.database;

import java.sql.SQLException;
import java.util.Map;

/**
 * Service class providing higher-level user data operations built on top of
 * {@link DatabaseHelper}. Encapsulates queries specific to the {@code user}
 * and {@code university} database tables.
 */
public final class UserService {

    private DatabaseHelper db; // The database helper used for raw record retrieval.

    /**
     * Constructs a {@code UserService} backed by the given {@link DatabaseHelper}.
     *
     * @param db the database helper instance to use for queries.
     */
    public UserService(DatabaseHelper db) {
        this.db = db;
    }

    /**
     * Retrieves the full name of a user by concatenating their
     * {@code first_name} and {@code last_name} database columns.
     *
     * @param userId the integer primary key of the user.
     * @return the full name as {@code "firstName lastName"}, or {@code null}
     *         if no user with the given ID exists.
     * @throws SQLException if the database query fails.
     */
    public String getUserFullName(int userId) throws SQLException {
        Map<String, Object> user = db.getById("user", userId);

        if (user == null) {
            return null;
        }

        String firstName = (String) user.get("first_name");
        String lastName = (String) user.get("last_name");

        return firstName + " " + lastName;
    }

    /**
     * Retrieves the university record associated with the given user.
     *
     * @param userId the integer primary key of the user.
     * @return a {@link Map} of the university row's columns to values,
     *         or {@code null} if the user or their university cannot be found.
     * @throws SQLException if the database query fails.
     */
    public Map<String, Object> getUserUniversity(int userId) throws SQLException {
        Map<String, Object> user = db.getById("user", userId);

        if (user == null) {
            return null;
        }

        int universityId = (int) user.get("university_id");

        return db.getById("university", universityId);
    }
}
