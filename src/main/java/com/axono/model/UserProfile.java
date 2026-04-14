package com.axono.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model representing a user's profile within the app.
 * Stores personal details and selected study modules collected during
 * onboarding.
 */
public final class UserProfile {

    /** The user's display name (first name collected during sign-up). */
    private String name = "";

    /** The user's current year of study (e.g. "Year 1", "Year 2"). */
    private String yearOfStudy = "";

    /** The user's institution or last name as stored during sign-up. */
    private String institution = "";

    /** The list of module names the user selected during onboarding. */

    private List<String> subjects = new ArrayList<>();

    /**
     * Returns the user's name.
     *
     * @return the name string, never {@code null}.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param v the new name value.
     */
    public void setName(String v) {
        name = v;
    }

    /**
     * Returns the user's year of study.
     *
     * @return the year-of-study string, never {@code null}.
     */
    public String getYearOfStudy() {
        return yearOfStudy;
    }

    /**
     * Sets the user's year of study.
     *
     * @param v the new year-of-study value.
     */
    public void setYearOfStudy(String v) {
        yearOfStudy = v;
    }

    /**
     * Returns the user's educational institution.
     *
     * @return the institution string, never {@code null}.
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * Sets the user's educational institution.
     *
     * @param v the new institution value.
     */
    public void setInstitution(String v) {
        institution = v;
    }

    /**
     * Returns the list of module names the user has selected.
     *
     * @return a mutable {@link List} of module name strings.
     */
    public List<String> getSubjects() {
        return subjects;
    }

    /**
     * Sets the list of subjects/modules the user has selected.
     *
     * @param v the new list of subjects.
     */
    public void setSubjects(List<String> v) {
        subjects = v;
    }
}
