package com.axono.ui;

/**
 * Application-wide numeric UI constants — window sizes,
 * spacing scale, font sizes, and component dimensions.
 *
 * <p>CSS colour tokens are defined in {@code styles.css}.
 * This class covers only numeric layout values.</p>
 */
public final class UIConstants {

    // ── Window / scene ────────────────────────────────────────────
    /** Primary window width in pixels. */
    public static final int WINDOW_WIDTH  = 960;
    /** Primary window height in pixels. */
    public static final int WINDOW_HEIGHT = 800;

    // ── Content max-widths ────────────────────────────────────────
    /** Max width for full-page content columns. */
    public static final int CONTENT_MAX_WIDTH  = 800;
    /** Max width for narrower section/card columns. */
    public static final int SECTION_MAX_WIDTH  = 700;
    /** Max width for login / sign-up form cards. */
    public static final int FORM_MAX_WIDTH     = 500;
    /** Max width for the onboarding welcome card. */
    public static final int WELCOME_CARD_WIDTH = 460;
    /** Max width for the onboarding summary card. */
    public static final int SUMMARY_CARD_WIDTH = 520;
    /** Max width for the onboarding subject selection card. */
    public static final int SUBJECT_CARD_WIDTH = 620;

    // ── Component / popup dimensions ──────────────────────────────
    /** Width of the profile popup dialog. */
    public static final int PROFILE_POPUP_WIDTH  = 500;
    /** Height of the profile popup dialog. */
    public static final int PROFILE_POPUP_HEIGHT = 380;
    /** Preferred width for the sign-up illustration. */
    public static final int SIGNUP_IMG_WIDTH     = 400;
    /** Preferred width for onboarding/form navigation buttons. */
    public static final int NAV_BTN_WIDTH  = 130;
    /** Preferred height for onboarding/form navigation buttons. */
    public static final int NAV_BTN_HEIGHT = 38;
    /** Preferred height for text-input fields. */
    public static final int FIELD_HEIGHT   = 36;
    /** Height used as the thumbnail / viewport size offset. */
    public static final int THUMBNAIL_HEIGHT = 260;
    /** Preferred width of the dashboard progress-bar track. */
    public static final int PROGRESS_TRACK_WIDTH = 160;
    /** Preferred height of progress bars. */
    public static final int PROGRESS_BAR_HEIGHT  = 8;
    /** Multiplier to convert a 0–1 fraction to a percentage. */
    public static final double PERCENT_MAX = 100.0;

    // ── Spacing scale (gaps, VBox/HBox spacing, small paddings) ──
    /** Extra-small spacing: 4 px. */
    public static final int SPACING_XS  = 4;
    /** Small spacing: 6 px. */
    public static final int SPACING_SM  = 6;
    /** Medium spacing: 8 px. */
    public static final int SPACING_MD  = 8;
    /** Large spacing: 10 px. */
    public static final int SPACING_LG  = 10;
    /** Extra-large spacing: 12 px. */
    public static final int SPACING_XL  = 12;
    /** 2x extra-large spacing: 16 px. */
    public static final int SPACING_2XL = 16;
    /** Standard section gap: 20 px. */
    public static final int SPACING_3XL = 20;
    /** Navigation/header gap: 24 px. */
    public static final int SPACING_4XL = 24;
    /** Card inner gap: 28 px. */
    public static final int SPACING_5XL = 28;
    /** Large section gap: 30 px. */
    public static final int SPACING_6XL = 30;
    /** Banner/heading gap: 32 px. */
    public static final int SPACING_7XL = 32;
    /** Section separation: 40 px. */
    public static final int SPACING_8XL = 40;
    /** Content vertical padding: 60 px. */
    public static final int SPACING_9XL = 60;

    // ── Padding aliases (match spacing scale) ─────────────────────
    /** Standard padding inside nav bars (vertical). */
    public static final int PADDING_NAV_V     = SPACING_XL;
    /** Standard padding inside nav bars (horizontal). */
    public static final int PADDING_NAV_H     = SPACING_4XL;
    /** Standard horizontal page/content padding. */
    public static final int PADDING_CONTENT_H = SPACING_3XL;
    /** Standard vertical page/content padding. */
    public static final int PADDING_CONTENT_V = SPACING_9XL;
    /** Padding inside card containers. */
    public static final int PADDING_CARD      = SPACING_3XL;
    /** Minimum age for signup. */
    public static final int MIN_AGE = 7;

    // ── Font sizes ────────────────────────────────────────────────
    /** Banner / page-title font size. */
    public static final int FONT_BANNER    = 32;
    /** Sub-header font size. */
    public static final int FONT_SUBHEADER = 22;
    /** Section-heading font size. */
    public static final int FONT_SECTION   = 18;
    /** Content-row label font size. */
    public static final int FONT_LABEL     = 15;
    /** Standard body-text font size. */
    public static final int FONT_BODY      = 14;
    /** Small caption / muted-label font size. */
    public static final int FONT_CAPTION   = 13;
    /** Extra-small label font size. */
    public static final int FONT_SMALL     = 12;
    /** Tiny uppercase label font size. */
    public static final int FONT_TINY      = 10;
    /** Large body font size. */
    public static final int FONT_BODY_LG   = 16;

    /** Utility class — not instantiable. */
    private UIConstants() { }
}
