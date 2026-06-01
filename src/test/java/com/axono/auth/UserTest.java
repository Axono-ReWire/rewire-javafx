package com.axono.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {

    /** Standard test timestamp. */
    private LocalDateTime createdAt;

    /** Test user ID for identity testing. */
    private static final int TEST_USER_ID = 42;
    /** Base user ID for standard test construction. */
    private static final int BASE_USER_ID = 1;
    /** Test username constant. */
    private static final String TEST_USERNAME = "jdoe";
    /** Test password hash constant. */
    private static final String TEST_PASSWORD_HASH = "hash";
    /** Test first name constant. */
    private static final String TEST_FIRST_NAME = "John";
    /** Test last name constant. */
    private static final String TEST_LAST_NAME = "Doe";
    /** Test year of study constant. */
    private static final String TEST_YEAR = "Year 1";
    /** Year constant for test timestamp. */
    private static final int YEAR = 2024;
    /** Month constant for test timestamp. */
    private static final int MONTH = 1;
    /** Day constant for test timestamp. */
    private static final int DAY = 15;
    /** Hour constant for test timestamp. */
    private static final int HOUR = 10;
    /** Minute constant for test timestamp. */
    private static final int MINUTE = 30;

    @BeforeEach
    void setUp() {
        createdAt = LocalDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE);
    }

    @Test
    void getIdReturnsCorrectId() {
        User user = new User(TEST_USER_ID, TEST_USERNAME, TEST_PASSWORD_HASH,
                TEST_FIRST_NAME, TEST_LAST_NAME, TEST_YEAR, createdAt);
        assertEquals(TEST_USER_ID, user.getId());
    }

    @Test
    void getUsernameReturnsCorrectUsername() {
        User user = new User(BASE_USER_ID, TEST_USERNAME, TEST_PASSWORD_HASH,
                TEST_FIRST_NAME, TEST_LAST_NAME, TEST_YEAR, createdAt);
        assertEquals(TEST_USERNAME, user.getUsername());
    }

    @Test
    void getPasswordHashReturnsCorrectHash() {
        User user = new User(BASE_USER_ID, TEST_USERNAME, "myhash",
                TEST_FIRST_NAME, TEST_LAST_NAME, TEST_YEAR, createdAt);
        assertEquals("myhash", user.getPasswordHash());
    }

    @Test
    void getFullNameConcatenatesFirstAndLast() {
        User user = new User(BASE_USER_ID, TEST_USERNAME, TEST_PASSWORD_HASH,
                TEST_FIRST_NAME, TEST_LAST_NAME, TEST_YEAR, createdAt);
        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void getFullNameWithEmptyLastNameReturnsFirstWithSpace() {
        User user = new User(BASE_USER_ID, TEST_USERNAME, TEST_PASSWORD_HASH,
                TEST_FIRST_NAME, "", TEST_YEAR, createdAt);
        assertEquals("John ", user.getFullName());
    }

    @Test
    void getFullNameWithEmptyFirstNameReturnsSpaceAndLast() {
        User user = new User(BASE_USER_ID, TEST_USERNAME, TEST_PASSWORD_HASH,
                "", TEST_LAST_NAME, TEST_YEAR, createdAt);
        assertEquals(" Doe", user.getFullName());
    }

    @Test
    void getYearOfStudyReturnsCorrectValue() {
        User user = new User(BASE_USER_ID, TEST_USERNAME, TEST_PASSWORD_HASH,
                TEST_FIRST_NAME, TEST_LAST_NAME, "Year 2", createdAt);
        assertEquals("Year 2", user.getYearOfStudy());
    }

    @Test
    void getCreatedAtReturnsCorrectValue() {
        User user = new User(BASE_USER_ID, TEST_USERNAME, TEST_PASSWORD_HASH,
                TEST_FIRST_NAME, TEST_LAST_NAME, TEST_YEAR, createdAt);
        assertSame(createdAt, user.getCreatedAt());
    }
}
