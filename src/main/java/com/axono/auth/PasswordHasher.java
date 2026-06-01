package com.axono.auth;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Thin wrapper around jBCrypt providing salted password hashing and
 * verification.
 *
 * <p>BCrypt automatically incorporates a per-hash salt, so callers do not
 * need to manage salts separately.
 */
public final class PasswordHasher {

    /** BCrypt work factor (cost). 12 is the OWASP 2024 default. */
    private static final int WORK_FACTOR = 12;

    /** Utility class — not instantiable. */
    private PasswordHasher() {
    }

    /**
     * Hashes the given plaintext password with a fresh BCrypt salt.
     *
     * @param plaintext the password to hash.
     * @return the BCrypt hash (includes salt and cost).
     */
    public static String hash(final String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     *
     * @param plaintext the candidate password.
     * @param hash      the stored BCrypt hash.
     * @return {@code true} when the password matches the hash.
     */
    public static boolean verify(final String plaintext, final String hash) {
        return BCrypt.checkpw(plaintext, hash);
    }
}
