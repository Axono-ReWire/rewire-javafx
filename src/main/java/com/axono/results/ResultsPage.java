package com.axono.results;

import com.axono.ui.UITheme;

import javafx.scene.layout.*;
import javafx.scene.control.*;

import javafx.geometry.Pos;
import javafx.geometry.Insets;

public class ResultsPage extends ScrollPane {

    private static final String BG_COLOUR = " -fx-background-color: ";

    public ResultsPage() {

        buildUI();

    }

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

    private VBox buildBanner() {

        Label bannerHeader = createLabel("Results", 32, true, UITheme.TEXT_DARK);
        Label bannerLabel1 = createLabel("Congratulations on completing the quiz!", 18, false,
                UITheme.TEXT_DARK);
        Label bannerLabel2 = createLabel("Your results for ..... are shown below:", 14, true, UITheme.TERTIARY);

        HBox buttons = new HBox(12,
                outlineButton("Save Results"),
                outlineButton("Retake quiz"),
                outlineButton("Select another quiz"));
        buttons.setAlignment(Pos.CENTER);

        VBox banner = new VBox(12, bannerHeader, bannerLabel1, buttons, bannerLabel2);
        banner.setAlignment(Pos.CENTER);
        return banner;

    }

    private VBox buildScoreSection() {

        Label bannerHeader = createLabel("Results Breakdown", 28, true, UITheme.TEXT_DARK);
        Label scoreHeader = createLabel("Score", 24, true, UITheme.TEXT_DARK);
        Label scoreLabel = createLabel("Your Score", 20, true, UITheme.SECONDARY);

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

    private VBox buildSummarySection() {

        Label summaryHeader = createLabel("Summary", 24, true, UITheme.TEXT_DARK);
        Label summaryLabel = createLabel("Your Summary", 20, true, UITheme.SECONDARY);

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

    // HELPER METHODS HERE
    // --------------------------------------------------------------------------------------------------------

    // helper for build buttons
    private Button outlineButton(String text) {
        String base = "-fx-background-color: transparent;" +
                "-fx-border-color: " + UITheme.PRIMARY + "; -fx-border-width: 2px;" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                "-fx-text-fill: " + UITheme.PRIMARY + ";" +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
        String hover = BG_COLOUR + UITheme.PRIMARY + ";" +
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

    private Label createLabel(String text, int size, boolean bold, String color) {
        Label header = new Label(text);
        String weight = bold ? "bold" : "normal";
        header.setStyle(
                String.format("-fx-font-size: %dpx; -fx-font-weight: %s; -fx-text-fill: %s", size, weight, color));
        return header;

    }

    private Label createIcon(String text) {
        Label logo = new Label(text);
        logo.setStyle("-fx-font-size: 24px;");
        return logo;
    }

    private HBox createLogoRow(Label logo, Label text) {
        HBox row = new HBox(10, logo, text);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;

    }

    private VBox createSection(String icon, String text) {
        Label logo = createIcon(icon);
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill:" + UITheme.TEXT_DARK + ";");
        HBox row = createLogoRow(logo, label);
        return createCard(new VBox(10, row));

    }

    private VBox createCard(VBox content) {
        content.setPadding(new Insets(20));
        content.setMaxWidth(700);
        content.setStyle(UITheme.CARD_STYLE);
        return content;

    }

}