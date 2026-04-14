package com.axono.ui;

/**
 * Centralised UI theming constants for the Axono ReWire application.
 * All colour hex strings and reusable inline CSS snippets are defined
 * here to ensure a consistent visual style across all views.
 *
 * <p>
 * This class is a non-instantiable utility class.
 * </p>
 */
public final class UITheme {

    /**
     * Private constructor – prevents instantiation of this utility class.
     */
    private UITheme() {
    }

    public static final String PRIMARY = "#59BE8B"; // Primary brand colour hex code for Axono ReWire.
    public static final String SECONDARY = "#207282"; // Secondary brand colour hex code for Axono ReWire.
    public static final String TERTIARY = "#399386"; // Tertiary brand colour hex code for Axono ReWire.
    public static final String SECONDARY_OPTION = "#cccccc"; // Alternative colour for secondary elements.
    public static final String BG = "#F4F6F9"; // Background colour hex code for the application.
    public static final String WHITE = "#FFFFFF"; // White colour hex code.
    public static final String TEXT_DARK = "#212529"; // Dark text colour hex code.
    public static final String TEXT_MUTED = "#6C757D"; // Muted text colour hex code.
    public static final String BORDER = "#DEE2E6"; // Border colour hex code.

    /**
     * Reusable inline CSS style for card containers.
     * Applies a white background, rounded border, and standard padding.
     */
    public static final String CARD_STYLE = "-fx-background-color: white;" +
            "-fx-border-color: #DEE2E6;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 28px 40px;";
}
