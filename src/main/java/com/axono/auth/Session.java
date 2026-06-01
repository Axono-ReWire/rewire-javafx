package com.axono.auth;

/**
 * Process-wide singleton holding the currently authenticated {@link User}.
 * Set by {@link AuthService} on a successful signup or login, cleared on
 * logout, and read by any controller that needs the active user.
 *
 * <p>This implementation assumes a single-user JavaFX session; the desktop
 * app is not multi-threaded across users.
 */
public final class Session {

    /** The currently authenticated user, or {@code null} if none. */
    private static User currentUser;

    /** Utility class — not instantiable. */
    private Session() {
    }

    /**
     * Records the given user as the active session.
     *
     * @param user the user to mark as authenticated; must not be null.
     */
    public static void set(final User user) {
        currentUser = user;
    }

    /**
     * Returns the active user, or {@code null} if no one is logged in.
     *
     * @return the authenticated {@link User}, or {@code null}.
     */
    public static User get() {
        return currentUser;
    }

    /** Clears the active user (logout). */
    public static void clear() {
        currentUser = null;
    }

    /**
     * Returns {@code true} when there is an active authenticated user.
     *
     * @return whether a user is currently logged in.
     */
    public static boolean isAuthenticated() {
        return currentUser != null;
    }
}
