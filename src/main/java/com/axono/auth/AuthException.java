package com.axono.auth;

/**
 * Thrown by {@link AuthService} for expected, user-facing authentication
 * failures (duplicate username, validation errors). The message is safe
 * to display to the user.
 */
public final class AuthException extends Exception {

    /** Serialisation version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code AuthException} with the given user-facing
     * message.
     *
     * @param message human-readable description of the failure.
     */
    public AuthException(final String message) {
        super(message);
    }
}
