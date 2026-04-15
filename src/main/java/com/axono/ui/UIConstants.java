package com.axono.ui;

/**
 * Application-wide numeric layout constants for Axono ReWire.
 *
 * <p>
 * Centralises every value that would otherwise be a magic number
 * scattered across view classes. Import wherever a layout measurement
 * is needed:
 * 
 * <pre>
 * import com.axono.ui.UIConstants;
 * </pre>
 *
 * <p>
 * Constants are grouped by concern. Spacing and padding each follow
 * a named scale (XS → 3XL) so that any two places using the same
 * measurement share one definition. The consolidated replacements for
 * the previous per-view constants are noted in the Javadoc of each entry.
 */
public final class UIConstants {

    /**
     * Private constructor — this class is a constants holder
     * and must never be instantiated.
     */
    private UIConstants() {
    }

    // ── Window dimensions ────────────────────────────────────────────────────

    /** Width of the application windows in pixels. */
    public static final int WINDOW_WIDTH = 960;

    /** Height of the application windows in pixels. */
    public static final int WINDOW_HEIGHT = 800;

    // ── Fixed dimensions (widths, heights, sizes) ────────────────────────────

    /** Maximum width of the central scrollable content column. */
    public static final int CONTENT_MAX_WIDTH = 800;

    /** Maximum width of section cards and sub-sections. */
    public static final int SECTION_MAX_WIDTH = 700;

    /** Maximum width of the subject content scroll area. */
    public static final int SUBJECT_MAX_WIDTH = 620;

    /** Maximum width of the onboarding summary card. */
    public static final int SUMMARY_CARD_MAX_WIDTH = 520;

    /** Maximum width of the sign-up form card. */
    public static final int FORM_MAX_WIDTH = 500;

    /** Maximum width of the welcome card. */
    public static final int WELCOME_CARD_MAX_WIDTH = 460;

    /** Preferred width of styled text input fields. */
    public static final int FIELD_PREF_WIDTH = 400;

    /**
     * Preferred height of styled text input fields.
     * Also the preferred height of Back/Next wizard buttons.
     */
    public static final int FIELD_PREF_HEIGHT = 36;

    /** Preferred width of the Back/Next navigation buttons. */
    public static final int WIZARD_BTN_WIDTH = 130;

    /** Preferred height of the Back/Next navigation buttons. */
    public static final int WIZARD_BTN_HEIGHT = 38;

    /** Minimum width of the key-label column in each summary row. */
    public static final int SUMMARY_KEY_MIN_WIDTH = 130;

    /** Preferred width and height for subject icon images in pixels. */
    public static final int SUBJECT_ICON_SIZE = 50;

    /** Width of the progress bar track in pixels. */
    public static final int PROGRESS_BAR_WIDTH = 160;

    /** Height of the progress bar track and fill in pixels. */
    public static final int PROGRESS_BAR_HEIGHT = 8;

    /** Minimum fill width to keep the progress bar visually present. */
    public static final int PROGRESS_MIN_FILL = 4;

    /** Divisor used to convert a raw percentage integer to a 0–1 fraction. */
    public static final double PERCENT_DIVISOR = 100.0;

    // ── Spacing scale ────────────────────────────────────────────────────────

    /** 6 px — checkbox↔label gaps, step-indicator gaps. */
    public static final int SPACING_XS = 6;

    /**
     * 8 px — nav-item gaps, icon↔label gaps, step-indicator circles,
     * module item children, score card rows, and field gap spacers.
     */
    public static final int SPACING_SM = 8;

    /**
     * 10 px — button HBox gaps, entry/module/logo rows,
     * onboarding content VBox, and summary row bottom margins.
     */
    public static final int SPACING_MD = 10;

    /**
     * 12 px — compact banners, module card lists, progress rows,
     * results summary card rows, and separator top/bottom margins.
     */
    public static final int SPACING_LG = 12;

    /**
     * 14 px — spacing between elements inside the welcome card.
     * Kept separate as it does not share a value with any other spacing use.
     */
    public static final int WELCOME_CARD_SPACING = 14;

    /** 16 px — section heading↔card gaps and summary key↔value row spacing. */
    public static final int SPACING_XL = 16;

    /** 20 px — banner VBox spacing, large section↔card gaps, subject card lists. */
    public static final int SPACING_2XL = 20;

    /** 40 px — top-level section spacing in scroll-view content VBoxes. */
    public static final int SPACING_3XL = 40;

    // ── Padding scale ────────────────────────────────────────────────────────

    /** 4 px — top margin applied above each form field label via VBox.setMargin. */
    public static final int LABEL_MARGIN_TOP = 4;

    /** 14 px — top/bottom padding of the onboarding content pane. */
    public static final int ONBOARDING_CONTENT_PADDING_V = 14;

    /** 12 px — top/bottom padding of the nav bar and Back/Next button bar. */
    public static final int PADDING_SM = 12;

    /**
     * 20 px — left/right padding of the scroll-view content VBox,
     * and internal padding of card containers.
     */
    public static final int PADDING_MD = 20;

    /**
     * 24 px — left/right padding of the nav bar, onboarding content pane,
     * and Back/Next button bar.
     */
    public static final int PADDING_LG = 24;

    /** 30 px — padding around full-screen StackPane views (Welcome, Summary). */
    public static final int VIEW_PADDING = 30;

    /** 60 px — top/bottom padding of scrollable content VBoxes. */
    public static final int CONTENT_PADDING_V = 60;

    // ── Form / validation ────────────────────────────────────────────────────

    /** VBox spacing between rows in the sign-up form (7 px, unique value). */
    public static final int FORM_ROW_SPACING = 7;

    /** Minimum age in years a user must be in order to register. */
    public static final int MIN_AGE_YEARS = 13;

    // ── Font sizes ───────────────────────────────────────────────────────────

    /**
     * 12 px — hint text and tiny metadata labels.
     * This is the absolute minimum; no text should render smaller.
     */
    public static final int FONT_XS = 12;

    /** 13 px — muted sub-labels and summary key labels. */
    public static final int FONT_SM = 13;

    /** 14 px — body text, button labels, and form labels. */
    public static final int FONT_BODY = 14;

    /** 15 px — card row labels and secondary descriptions. */
    public static final int FONT_MD = 15;

    /** 16 px — topic buttons and module names in lists. */
    public static final int FONT_LG = 16;

    /** 18 px — navigation bar logo and view subtitle labels. */
    public static final int FONT_NAV = 18;

    /** 20 px — card headings and welcome/section title labels. */
    public static final int FONT_SECTION = 20;

    /** 22 px — sub-section headings in the dashboard. */
    public static final int FONT_SUBSECTION = 22;

    /** 24 px — card-level heading labels (e.g. "Score", "Summary"). */
    public static final int FONT_CARD_HEADING = 24;

    /** 28 px — page-title labels (e.g. "Results Breakdown"). */
    public static final int FONT_PAGE_TITLE = 28;

    /** 32 px — main view banner headings. */
    public static final int FONT_BANNER = 32;

    /** 40 px — large emoji / icon labels in onboarding views. */
    public static final int FONT_ICON = 40;

}