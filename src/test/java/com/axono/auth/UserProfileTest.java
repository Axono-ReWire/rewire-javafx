package com.axono.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserProfileTest {

    /** User profile for testing. */
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = new UserProfile();
    }

    @Test
    void defaultConstructorHasEmptyStringFields() {
        assertEquals("", profile.getFirstName());
        assertEquals("", profile.getLastName());
        assertEquals("", profile.getUsername());
        assertEquals("", profile.getPassword());
        assertEquals("", profile.getYearOfStudy());
    }

    @Test
    void defaultSubjectsIsEmpty() {
        assertTrue(profile.getSubjects().isEmpty());
    }

    @Test
    void setAndGetFirstNameRoundTrip() {
        profile.setFirstName("Alice");
        assertEquals("Alice", profile.getFirstName());
    }

    @Test
    void setAndGetLastNameRoundTrip() {
        profile.setLastName("Smith");
        assertEquals("Smith", profile.getLastName());
    }

    @Test
    void setAndGetUsernameRoundTrip() {
        profile.setUsername("asmith");
        assertEquals("asmith", profile.getUsername());
    }

    @Test
    void setAndGetPasswordRoundTrip() {
        profile.setPassword("secret");
        assertEquals("secret", profile.getPassword());
    }

    @Test
    void setAndGetYearOfStudyRoundTrip() {
        profile.setYearOfStudy("Year 2");
        assertEquals("Year 2", profile.getYearOfStudy());
    }

    @Test
    void getFullNameWithBothNamesConcatenates() {
        profile.setFirstName("Alice");
        profile.setLastName("Smith");
        assertEquals("Alice Smith", profile.getFullName());
    }

    @Test
    void getFullNameWithEmptyNamesReturnsEmpty() {
        assertEquals("", profile.getFullName());
    }

    @Test
    void getFullNameWithOnlyFirstNameTrimmed() {
        profile.setFirstName("Alice");
        assertEquals("Alice", profile.getFullName());
    }

    @Test
    void setAndGetSubjectsRoundTrip() {
        List<String> subjects = List.of("Maths", "Physics");
        profile.setSubjects(subjects);
        assertNotNull(profile.getSubjects());
        assertEquals(2, profile.getSubjects().size());
        assertTrue(profile.getSubjects().contains("Maths"));
    }
}
