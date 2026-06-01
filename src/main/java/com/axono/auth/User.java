package com.axono.auth;

import java.time.LocalDateTime;

/**
 * Immutable representation of an authenticated user as persisted in the
 * {@code users} database table.
 *
 * <p>Never carries the plaintext password — only the BCrypt hash.
 */
public final class User {

    /** Primary key in the {@code users} table. */
    private final int id;

    /** Unique login identifier. */
    private final String username;

    /** BCrypt hash of the user's password. */
    private final String passwordHash;

    /** The user's first (given) name. */
    private final String firstName;

    /** The user's last (family) name. */
    private final String lastName;

    /** The user's current year of study (e.g. {@code "Year 2"}). */
    private final String yearOfStudy;

    /** Timestamp at which the row was created. */
    private final LocalDateTime createdAt;

    /**
     * Constructs an immutable {@code User}.
     *
     * @param userId   primary key.
     * @param login    unique login identifier.
     * @param pwHash   BCrypt hash.
     * @param first    user's first name.
     * @param last     user's last name.
     * @param year     the user's year of study.
     * @param created  row creation timestamp.
     */
    public User(final int userId, final String login, final String pwHash,
            final String first, final String last,
            final String year, final LocalDateTime created) {
        this.id = userId;
        this.username = login;
        this.passwordHash = pwHash;
        this.firstName = first;
        this.lastName = last;
        this.yearOfStudy = year;
        this.createdAt = created;
    }

    /** @return primary key. */
    public int getId() {
        return id;
    }

    /** @return unique login identifier. */
    public String getUsername() {
        return username;
    }

    /** @return BCrypt hash of the password. */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** @return the user's first name. */
    public String getFirstName() {
        return firstName;
    }

    /** @return the user's last name. */
    public String getLastName() {
        return lastName;
    }

    /** @return concatenation of first and last name. */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** @return the user's year of study. */
    public String getYearOfStudy() {
        return yearOfStudy;
    }

    /** @return row creation timestamp. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
