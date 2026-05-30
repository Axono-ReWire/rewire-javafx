package com.axono.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link UserProfile} data model class.
 * Verifies that user details and selected study modules are properly
 * initialized, stored, and modified.
 */
class UserProfileTest {

    /** Expected result of basic arithmetic operation (2 + 2). */
    private static final int ARITHMETIC_RESULT = 4;

    /** The target UserProfile instance under test. */
    private UserProfile userProfile;

    /**
     * Sets up an unpopulated {@link UserProfile} instance
     * before each test execution to ensure test isolation.
     */
    @BeforeEach
    void setup() {
        userProfile = new UserProfile();
    }

    /**
     * A basic sanity check to ensure the JUnit 5 test runner
     * is configured and executing properly.
     */
    @Test
    void sampleTest() {
        assertEquals(ARITHMETIC_RESULT, 2 + 2);
    }

    /**
     * Verifies that a newly instantiated {@link UserProfile} correctly
     * implements its default states as specified by the onboarding fields:
     * empty strings for text attributes and an initialized, empty list for
     * subjects.
     */
    @Test
    void testDefaultValues() {
        // Assert that text fields default to empty strings ("") and are never
        // null
        assertEquals("", userProfile.getName());
        assertEquals("", userProfile.getYearOfStudy());
        assertEquals("", userProfile.getInstitution());

        // Assert that the subjects list is initialized as an empty collection
        assertNotNull(userProfile.getSubjects());
        assertTrue(userProfile.getSubjects().isEmpty());
    }

    /**
     * Verifies that mutations applied via setters are accurately preserved
     * and readable via their matching getter methods. Tests personal details
     * and the list of selected onboarding modules.
     */
    @Test
    void testGettersAndSetters() {
        userProfile.setName("Joe");
        userProfile.setYearOfStudy("Year 1");
        userProfile.setInstitution("University of York");

        List<String> modules = List.of("Analogue Electronics",
                "Engineering Mathematics");
        userProfile.setSubjects(modules);

        assertEquals("Joe", userProfile.getName());
        assertEquals("Year 1", userProfile.getYearOfStudy());
        assertEquals("University of York", userProfile.getInstitution());

        assertEquals(2, userProfile.getSubjects().size());
        assertTrue(userProfile.getSubjects().contains("Analogue Electronics"));
        assertTrue(
                userProfile.getSubjects()
                        .contains("Engineering Mathematics"));
    }

}

