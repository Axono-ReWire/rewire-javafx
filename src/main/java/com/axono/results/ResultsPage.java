package com.axono.results;

import com.axono.ui.UITheme;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Border;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/**
 * The quiz results page view, presenting the user's score, percentage,
 * and a summary of their quiz performance including time taken and feedback.
 */
public final class ResultsPage extends ScrollPane {

    /** Reusable JavaFX CSS prefix for setting background colour. */
    private static final String BG_COLOUR = " -fx-background-color: ";

    /**
     * Constructs the {@code ResultsPage} and builds the scrollable layout.
     */
    public ResultsPage() {
        buildUI();
    }

    /**
     * Builds the full scrollable results layout including the banner,
     * score section, and summary section.
     */
    private void buildUI() {

        VBox content = new VBox(40);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(60, 20, 60, 20));
        content.setMaxWidth(800);
        content.setStyle(BG_COLOUR + UITheme.BG + ";");
        content.getChildren().addAll(
                buildBanner(), buildScoreSection(), buildSummarySection());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(BG_COLOUR + UITheme.BG + ";");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        setStyle(BG_COLOUR + UITheme.BG + ";");
    }

    /**
     * Builds and returns the results banner containing the page title,
     * congratulatory message, action buttons, and quiz name label.
     *
     * @return a {@link VBox} containing the banner elements.
     */
    private VBox buildBanner() {

        Label bannerHeader = createLabel(
                "Results", 32, true, UITheme.TEXT_DARK);
        Label bannerLabel1 = createLabel(
                "Congratulations on completing the quiz!", 18, false,
                UITheme.TEXT_DARK);
        Label bannerLabel2 = createLabel(
                "Your results for ..... are shown below:",
                14, true, UITheme.TERTIARY);

        HBox buttons = new HBox(12,
                outlineButton("Save Results"),
                outlineButton("Retake quiz"),
                outlineButton("Select another quiz"));
        buttons.setAlignment(Pos.CENTER);

        VBox banner = new VBox(
                12, bannerHeader, bannerLabel1, buttons, bannerLabel2);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    /**
     * Builds and returns the score breakdown section containing the score
     * header, a score card with percentage and results rows.
     *
     * @return a {@link VBox} containing the score section elements.
     */
    private VBox buildScoreSection() {

        Label bannerHeader = createLabel("Results Breakdown",
                28, true, UITheme.TEXT_DARK);
        Label scoreHeader = createLabel("Score",
                24, true, UITheme.TEXT_DARK);
        Label scoreLabel = createLabel("Your Score",
                20, true, UITheme.SECONDARY);

        Label scoreLogo = createIcon("🏆");
        HBox scoreRow = createLogoRow(scoreLogo, scoreLabel);

        VBox scoreBox = createCard(new VBox(8, scoreRow,
                createSection("%",
                        "Percentage:"),
                createSection("📝", "Results:")));

        VBox scoreSection = new VBox(20, bannerHeader, scoreHeader, scoreBox);
        scoreSection.setAlignment(Pos.CENTER_LEFT);
        scoreSection.setMaxWidth(700);
        return scoreSection;
    }

    /**
     * Builds and returns the summary section containing time taken,
     * feedback, and a question review row.
     *
     * @return a {@link VBox} containing the summary section elements.
     */
    private VBox buildSummarySection() {

        Label summaryHeader = createLabel("Summary",
                24, true, UITheme.TEXT_DARK);
        Label summaryLabel = createLabel("Your Summary",
                20, true, UITheme.SECONDARY);

        Label summaryLogo = createIcon("📈");
        HBox summaryRow = createLogoRow(summaryLogo, summaryLabel);

        VBox summaryBox = createCard(new VBox(12,
                summaryRow,
                createSection("⏰", "Time taken:"),
                createSection("💬", "Feedback:"),
                createSection("❓", "Question review:")));

        VBox summarySection = new VBox(20, summaryHeader, summaryBox);
        summarySection.setAlignment(Pos.CENTER_LEFT);
        summarySection.setMaxWidth(700);
        return summarySection;
    }

    /**
     * Creates a styled outline {@link Button} with hover fill effects.
     *
     * @param text the button label.
     * @return a configured outline {@link Button}.
     */
    private Button outlineButton(final String text) {
        String base = "-fx-background-color: transparent;"
                + "-fx-border-color: " + UITheme.PRIMARY
                + "; -fx-border-width: 2px;"
                + "-fx-border-radius: 4px; -fx-background-radius: 4px;"
                + "-fx-text-fill: " + UITheme.PRIMARY + ";"
                + "-fx-font-weight: bold; -fx-font-size: 14px;"
                + "-fx-cursor: hand;";
        String hover = BG_COLOUR + UITheme.PRIMARY + ";"
                + "-fx-border-color: " + UITheme.PRIMARY
                + "; -fx-border-width: 2px;"
                + "-fx-border-radius: 4px; -fx-background-radius: 4px;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-font-size: 14px;"
                + "-fx-cursor: hand;";
        Button b = new Button(text);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    /**
     * Creates a {@link Label} with configurable font size, weight, and colour.
     *
     * @param text  the label text.
     * @param size  the font size in pixels.
     * @param bold  {@code true} to apply bold font weight.
     * @param color the hex colour string for the text.
     * @return a configured {@link Label}.
     */
    private Label createLabel(
            final String text,
            final int size,
            final boolean bold,
            final String color) {
        Label header = new Label(text);
        String weight = bold ? "bold" : "normal";
        header.setStyle(
                String.format("-fx-font-size: %dpx;"
                        + "-fx-font-weight: %s; -fx-text-fill: %s",
                        size, weight, color));
        return header;
    }

    /**
     * Creates a large emoji / icon {@link Label} at a fixed font size.
     *
     * @param text the emoji or icon character(s) to display.
     * @return a styled icon {@link Label}.
     */
    private Label createIcon(final String text) {
        Label logo = new Label(text);
        logo.setStyle("-fx-font-size: 24px;");
        return logo;
    }

    /**
     * Creates a horizontal {@link HBox} row containing an icon label
     * followed by a text label.
     *
     * @param logo the icon {@link Label} to place on the left.
     * @param text the text {@link Label} to place on the right.
     * @return an {@link HBox} row with the icon and text aligned left.
     */
    private HBox createLogoRow(final Label logo, final Label text) {
        HBox row = new HBox(10, logo, text);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Creates a card row consisting of an icon and a bold label,
     * wrapped in a styled card container.
     *
     * @param icon the emoji or icon string for the row.
     * @param text the row's descriptive label text.
     * @return a {@link VBox} card containing the icon-label row.
     */
    private VBox createSection(final String icon, final String text) {
        Label logo = createIcon(icon);
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;"
                + "-fx-text-fill:" + UITheme.TEXT_DARK + ";");
        HBox row = createLogoRow(logo, label);
        return createCard(new VBox(10, row));
    }

    /**
     * Wraps the given {@link VBox} in a styled card with standard padding,
     * max width, and the application card style.
     *
     * @param content the {@link VBox} to wrap as a card.
     * @return the same {@link VBox} with card styling applied.
     */
    private VBox createCard(final VBox content) {
        content.setPadding(new Insets(20));
        content.setMaxWidth(700);
        content.setStyle(UITheme.CARD_STYLE);
        return content;
    }

}
