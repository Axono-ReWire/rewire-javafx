package com.axono.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void hashReturnsNonNullHash() {
        String hash = PasswordHasher.hash("password");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void verifyCorrectPasswordReturnsTrue() {
        String hash = PasswordHasher.hash("secret123");
        assertTrue(PasswordHasher.verify("secret123", hash));
    }

    @Test
    void verifyWrongPasswordReturnsFalse() {
        String hash = PasswordHasher.hash("secret123");
        assertFalse(PasswordHasher.verify("wrong", hash));
    }

    @Test
    void hashSameInputProducesDifferentHashes() {
        String h1 = PasswordHasher.hash("same");
        String h2 = PasswordHasher.hash("same");
        assertNotEquals(h1, h2);
    }
}
