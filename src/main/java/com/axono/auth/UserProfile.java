package com.axono.auth;

import java.util.ArrayList;
import java.util.List;

/**
 * Transient onboarding state — holds the data the user enters during the
 * sign-up wizard before it is persisted to the database via
 * {@link AuthService}.
 *
 * <p>For data about an already-authenticated user, prefer
 * {@link Session}.
 */
public final class UserProfile {

    /** The user's first (given) name. */
    private String firstName = "";

    /** The user's last (family) name. */
    private String lastName = "";

    /** The user's chosen unique username for login. */
    private String username = "";

    /** Plaintext password collected during signup (never persisted). */
    private String password = "";

    /** The user's current year of study (e.g. "Year 1"). */
    private String yearOfStudy = "";

    /** The list of module names the user selected during onboarding. */
    private List<String> subjects = new ArrayList<>();

    /** @return the user's first name. */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     *
     * @param v the new first name.
     */
    public void setFirstName(final String v) {
        firstName = v;
    }

    /** @return the user's last name. */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     *
     * @param v the new last name.
     */
    public void setLastName(final String v) {
        lastName = v;
    }

    /** @return the user's chosen username. */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's chosen username.
     *
     * @param v the new username.
     */
    public void setUsername(final String v) {
        username = v;
    }

    /** @return the plaintext password (only used between signup steps). */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the plaintext password. Cleared after signup completes.
     *
     * @param v the new plaintext password.
     */
    public void setPassword(final String v) {
        password = v;
    }

    /** @return the user's full name in "First Last" form. */
    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    /** @return the user's year of study. */
    public String getYearOfStudy() {
        return yearOfStudy;
    }

    /**
     * Sets the user's year of study.
     *
     * @param v the new year-of-study value.
     */
    public void setYearOfStudy(final String v) {
        yearOfStudy = v;
    }

    /** @return mutable list of selected subject (module) names. */
    public List<String> getSubjects() {
        return subjects;
    }

    /**
     * Sets the list of selected subjects.
     *
     * @param v the new list.
     */
    public void setSubjects(final List<String> v) {
        subjects = v;
    }
}
