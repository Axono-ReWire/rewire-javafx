package com.axono.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionTest {

    /** Test user for session operations. */
    private User testUser;

    @BeforeEach
    void setUp() {
        Session.clear();
        testUser = new User(1, "jdoe", "hash", "John", "Doe",
                "Year 1", LocalDateTime.now());
    }

    @Test
    void isAuthenticatedInitiallyReturnsFalse() {
        assertFalse(Session.isAuthenticated());
    }

    @Test
    void setAndGetReturnsUser() {
        Session.set(testUser);
        assertSame(testUser, Session.get());
    }

    @Test
    void isAuthenticatedAfterSetReturnsTrue() {
        Session.set(testUser);
        assertTrue(Session.isAuthenticated());
    }

    @Test
    void clearRemovesUser() {
        Session.set(testUser);
        Session.clear();
        assertNull(Session.get());
    }

    @Test
    void isAuthenticatedAfterClearReturnsFalse() {
        Session.set(testUser);
        Session.clear();
        assertFalse(Session.isAuthenticated());
    }
}
