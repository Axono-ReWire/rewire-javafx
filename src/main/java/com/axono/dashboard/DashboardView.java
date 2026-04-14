package com.axono.dashboard;

import com.axono.model.UserProfile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.axono.ui.UITheme;

/**
 * The main dashboard view displayed after onboarding is complete. Shows a
 * welcome banner with the user's name, a progress overview of their selected
 * subjects, and recommended next topics to explore.
 */
public final class DashboardView extends ScrollPane {

    private static final String BG_COLOR_STYLE = "-fx-background-color: ";

    private final UserProfile profile;

    /**
     * Constructs the {@code DashboardView} for the given user profile.
     *
     * @param profile the {@link UserProfile} containing the user's name and
     *                subjects.
     */
    public DashboardView(UserProfile profile) {
        this.profile = profile;
        buildUI();
    }

    /**
     * Builds the full scrollable dashboard layout, assembling the banner,
     * progress section, and recommended topics section.
     */
    private void buildUI() {
        VBox content = new VBox(40);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(60, 20, 60, 20));
        content.setMaxWidth(800);
        content.setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
        content.getChildren().addAll(
                buildBanner(),
                buildProgressSection(),
                buildRecommendedTopics());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
    }

    /**
     * Builds and returns the top welcome banner with the user's name
     * and Profile / Logout action buttons.
     *
     * @return a {@link VBox} containing the welcome label and action buttons.
     */
    private VBox buildBanner() {
        Label welcome = new Label("Welcome, " + profile.getName() + "!");
        welcome.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.TEXT_DARK + ";");

        HBox buttons = new HBox(10, outlineButton("Profile"), outlineButton("Logout"));
        buttons.setAlignment(Pos.CENTER);

        VBox banner = new VBox(20, welcome, buttons);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    /**
     * Builds and returns the progress section, rendering a progress bar row
     * for each module in the user's selected subjects list.
     *
     * @return a {@link VBox} containing the section heading and progress rows.
     */
    private VBox buildProgressSection() {
        Label pageTitle = sectionLabel("Your Learning Dashboard", 28);
        Label subTitle = sectionLabel("Your Progress", 22);

        VBox entries = new VBox(10);
        var subjects = profile.getSubjects();
        if (subjects.isEmpty()) {
            entries.getChildren().add(bodyLabel("No modules selected."));
        } else {
            for (String subject : subjects) {
                Label name = bodyLabel(subject);
                name.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(name, Priority.ALWAYS);

                StackPane bar = progressBar(0);

                Label pct = new Label("0% Complete");
                pct.setStyle("-fx-text-fill: " + UITheme.SECONDARY + "; -fx-font-size: 13px;");

                HBox row = new HBox(12, name, bar, pct);
                row.setAlignment(Pos.CENTER_LEFT);
                entries.getChildren().add(row);
            }
        }

        VBox section = new VBox(16, pageTitle, subTitle, cardWrap(entries));
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(700);
        return section;
    }

    /**
     * Builds and returns the recommended topics section containing a placeholder
     * topic card for Analogue Electronics.
     *
     * @return a {@link VBox} containing the recommended topics heading and card.
     */
    private VBox buildRecommendedTopics() {
        Label title = sectionLabel("Recommended Topics", 22);

        Label icon = new Label("📘");
        icon.setStyle("-fx-font-size: 26px;");
        Label topic = new Label("Electronics");
        topic.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.SECONDARY + ";");

        HBox header = new HBox(8, icon, topic);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox moduleList = new VBox(12,
                buildModuleCard("Continue: Analogue Electronics", "Resume Lesson"),
                buildModuleCard("Explore Other Topics", "Browse All"));

        VBox section = new VBox(20, title, cardWrap(new VBox(20, header, moduleList)));
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(700);
        return section;
    }

    /**
     * Builds a module action card with a title label and an action button.
     *
     * @param title   the card's title text.
     * @param btnText the label for the card's action button.
     * @return a {@link VBox} card containing the title and button.
     */
    private VBox buildModuleCard(String title, String btnText) {
        Label label = bodyLabel(title);
        label.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.TEXT_DARK + ";");
        return cardWrap(new VBox(10, label, outlineButton(btnText)));
    }

    /**
     * Wraps the given {@link VBox} in a styled card with a white background,
     * border, and rounded corners.
     *
     * @param content the {@link VBox} to style as a card.
     * @return the same {@link VBox} with card styling applied.
     */
    private VBox cardWrap(VBox content) {
        content.setPadding(new Insets(20));
        content.setMaxWidth(700);
        content.setStyle(
                BG_COLOR_STYLE + UITheme.WHITE + ";" +
                        "-fx-border-color: " + UITheme.BORDER + ";" +
                        "-fx-border-radius: 6px; -fx-background-radius: 6px;");
        return content;
    }

    /**
     * Creates a horizontal progress bar {@link StackPane} filled to the given
     * percentage.
     *
     * @param percent the completion percentage (0–100).
     * @return a {@link StackPane} rendering the progress bar track and fill.
     */
    private StackPane progressBar(double percent) {
        StackPane track = new StackPane();
        track.setPrefSize(160, 8);
        track.setStyle(BG_COLOR_STYLE + "#E5E7EB; -fx-background-radius: 4px;");

        double w = Math.max(4, 160 * percent / 100.0);
        StackPane fill = new StackPane();
        fill.setPrefSize(w, 8);
        fill.setStyle(BG_COLOR_STYLE + UITheme.PRIMARY + "; -fx-background-radius: 4px;");
        fill.setTranslateX(-(160 - w) / 2);

        track.getChildren().add(fill);
        return track;
    }

    /**
     * Creates a section heading {@link Label} with the given text and font size.
     *
     * @param text the heading text.
     * @param size the font size in pixels.
     * @return a styled section heading {@link Label}.
     */
    private Label sectionLabel(String text, int size) {
        Label l = new Label(text);
        l.setStyle(String.format(
                "-fx-font-size: %dpx; -fx-font-weight: bold; -fx-text-fill: %s;", size, UITheme.TEXT_DARK));
        return l;
    }

    /**
     * Creates a standard body text {@link Label} with the given text.
     *
     * @param text the label text.
     * @return a styled body {@link Label}.
     */
    private Label bodyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-text-fill: " + UITheme.TEXT_DARK + ";");
        return l;
    }

    /**
     * Creates a styled outline {@link Button} with hover fill effects.
     *
     * @param text the button label.
     * @return a configured outline {@link Button}.
     */
    private Button outlineButton(String text) {
        String base = "-fx-background-color: transparent;" +
                "-fx-border-color: " + UITheme.PRIMARY + "; -fx-border-width: 2px;" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                "-fx-text-fill: " + UITheme.PRIMARY + ";" +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
        String hover = BG_COLOR_STYLE + UITheme.PRIMARY + ";" +
                "-fx-border-color: " + UITheme.PRIMARY + "; -fx-border-width: 2px;" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
        Button b = new Button(text);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }
}
