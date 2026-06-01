package com.axono.auth;

import com.axono.database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service layer for user authentication. Handles signup (with duplicate
 * detection and password hashing) and login (with hash verification).
 *
 * <p>Stateless and safe to instantiate fresh per use.
 */
public final class AuthService {

    /** Minimum length for a valid plaintext password. */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /** INSERT column index for first_name. */
    private static final int COL_FIRST_NAME = 3;

    /** INSERT column index for last_name. */
    private static final int COL_LAST_NAME = 4;

    /** INSERT column index for year_of_study. */
    private static final int COL_YEAR_OF_STUDY = 5;

    /** INSERT column index for created_at. */
    private static final int COL_CREATED_AT = 6;

    /** SELECT fragment listing all user columns in a fixed order. */
    private static final String USER_COLUMNS =
            "id, username, password_hash, first_name, "
            + "last_name, year_of_study, created_at";

    /**
     * Creates a new user with a hashed password.
     *
     * @param username     unique username; must be non-empty.
     * @param plaintextPw  password to hash; must be ≥ 6 chars.
     * @param firstName    user's first name.
     * @param lastName     user's last name.
     * @param yearOfStudy  user's year of study.
     * @return the inserted {@link User} (with database-assigned id).
     * @throws AuthException if any field is invalid or the username
     *                       is already taken.
     * @throws SQLException  if the database call fails.
     */
    public User signup(final String username, final String plaintextPw,
            final String firstName, final String lastName,
            final String yearOfStudy)
            throws AuthException, SQLException {
        validateSignupInput(username, plaintextPw, firstName, lastName);
        if (findByUsername(username) != null) {
            throw new AuthException("Username \"" + username
                    + "\" is already taken");
        }
        return insertUser(username, plaintextPw, firstName, lastName,
                yearOfStudy);
    }

    /**
     * Attempts to authenticate a user by username and plaintext password.
     *
     * @param username    candidate username.
     * @param plaintextPw candidate password.
     * @return the matching {@link User} or {@link Optional#empty()} when
     *         no user matches or the password is wrong.
     * @throws SQLException if the database call fails.
     */
    public Optional<User> login(final String username,
            final String plaintextPw) throws SQLException {
        User user = findByUsername(username);
        if (user == null) {
            return Optional.empty();
        }
        if (!PasswordHasher.verify(plaintextPw, user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    /**
     * Validates non-empty fields and password strength.
     *
     * @param username    candidate username; must be non-empty.
     * @param plaintextPw candidate password; must meet minimum length.
     * @param firstName   user's first name; must be non-empty.
     * @param lastName    user's last name; must be non-empty.
     * @throws AuthException with a user-facing message on any violation.
     */
    private void validateSignupInput(final String username,
            final String plaintextPw, final String firstName,
            final String lastName) throws AuthException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthException("Username is required");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new AuthException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new AuthException("Last name is required");
        }
        if (plaintextPw == null
                || plaintextPw.length() < MIN_PASSWORD_LENGTH) {
            throw new AuthException("Password must be at least "
                    + MIN_PASSWORD_LENGTH + " characters");
        }
    }

    /**
     * Inserts a new user row and returns the populated {@link User}.
     *
     * @param username    unique username.
     * @param plaintextPw plaintext password to hash before storing.
     * @param firstName   user's first name.
     * @param lastName    user's last name.
     * @param yearOfStudy user's year of study.
     * @return the newly inserted {@link User} with its database-assigned id.
     * @throws SQLException if the insert fails.
     */
    private User insertUser(final String username, final String plaintextPw,
            final String firstName, final String lastName,
            final String yearOfStudy) throws SQLException {
        String hash = PasswordHasher.hash(plaintextPw);
        LocalDateTime now = LocalDateTime.now();
        String sql = "INSERT INTO users (username, password_hash, "
                + "first_name, last_name, year_of_study, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.open();
                PreparedStatement stmt = conn.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.setString(COL_FIRST_NAME, firstName);
            stmt.setString(COL_LAST_NAME, lastName);
            stmt.setString(COL_YEAR_OF_STUDY, yearOfStudy);
            stmt.setString(COL_CREATED_AT, now.toString());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;
                return new User(id, username, hash, firstName, lastName,
                        yearOfStudy, now);
            }
        }
    }

    /**
     * Looks up a user by username.
     *
     * @param username the username to search for.
     * @return the {@link User} or {@code null} if no such row.
     * @throws SQLException if the query fails.
     */
    private User findByUsername(final String username) throws SQLException {
        String sql = "SELECT " + USER_COLUMNS
                + " FROM users WHERE username = ?";
        try (Connection conn = Database.open();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    /**
     * Maps the current row of a {@link ResultSet} to a {@link User}.
     *
     * @param rs the result set positioned at the row to map.
     * @return the mapped {@link User}.
     * @throws SQLException if a column value cannot be read.
     */
    private User mapUser(final ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("year_of_study"),
                LocalDateTime.parse(rs.getString("created_at")));
    }
}
