package com.axono.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserProfileTest {

    private UserProfile userProfile;

    @BeforeEach
    void setup() {
        userProfile = new UserProfile();
    }

    @Test
    void sampleTest() {
        assertEquals(4, 2 + 2);
    }

    @Test
    void testDefaultValues() {
        assertEquals("", userProfile.getName());
        assertEquals("", userProfile.getYearOfStudy());
        assertEquals("", userProfile.getInstitution());
        assertNotNull(userProfile.getSubjects());
        assertTrue(userProfile.getSubjects().isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        userProfile.setName("Joe");
        userProfile.setYearOfStudy("Year 1");
        userProfile.setInstitution("University of York");

        List<String> modules = List.of("Analogue Electronics", "Engineering Mathematics");
        userProfile.setSubjects(modules);

        assertEquals("Joe", userProfile.getName());
        assertEquals("Year 1", userProfile.getYearOfStudy());
        assertEquals("University of York", userProfile.getInstitution());
        assertEquals(2, userProfile.getSubjects().size());
        assertTrue(userProfile.getSubjects().contains("Analogue Electronics"));
        assertTrue(userProfile.getSubjects().contains("Engineering Mathematics"));

    }

}
