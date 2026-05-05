package com.axono.answer;

import com.axono.ui.UITheme;
import com.axono.ui.UIConstants;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Border;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Answer view which shows the answer chosen, and the correct answer
 * following a quiz attempt
 */

public class AnswerView extends ScrollPane {
    //
    private static final String BG_COLOR = "-fx-background-color: ";
    private final List<TitledPane> panes = new ArrayList<>();

    public AnswerView() {
        buildUI();

    }

    private void buildUI() {

        VBox content = new VBox(UIConstants.SPACING_3XL);
        content.setAlignment(Pos.TOP_CENTER);
        content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        content.setStyle(BG_COLOR + UITheme.BG + ";");

        content.setPadding(new Insets(
                UIConstants.CONTENT_PADDING_V,
                UIConstants.PADDING_MD,
                UIConstants.CONTENT_PADDING_V,
                UIConstants.PADDING_MD));

        content.getChildren().addAll(buildBanner(), buildTitledPanes());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(BG_COLOR + UITheme.BG + ";");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);

        setBorder(Border.EMPTY);
        setStyle(BG_COLOR + UITheme.BG + ";");

    }

    private VBox buildBanner() {

        Label header = createLabel("Answer Review",
                UIConstants.FONT_BANNER,
                true,
                UITheme.TEXT_DARK);

        Label subHeader = createLabel("Review your answers",
                UIConstants.FONT_NAV,
                true,
                UITheme.TERTIARY);
        Label instructHeader = createLabel("Click on each question to review",
                UIConstants.FONT_SM,
                true,
                UITheme.TERTIARY);

        Button expandButton = createSmallButton("Expand all",
                () -> panes.forEach(p -> p.setExpanded(true)));
        Button collapseButton = createSmallButton("Collapse all",
                () -> panes.forEach(p -> p.setExpanded(false)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(UIConstants.PADDING_MD,
                expandButton,
                spacer,
                collapseButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox banner = new VBox(UIConstants.SPACING_2XL,
                header,
                subHeader,
                instructHeader,
                buttons);
        banner.setAlignment(Pos.TOP_CENTER);
        return banner;
    }

    private VBox buildTitledPanes() {

        VBox titlePanes = new VBox(UIConstants.PADDING_SM);
        panes.clear();
        for (String title : getTitles()) {
            TitledPane pane = createTitledPane(title);

            panes.add(pane);

            titlePanes.getChildren().addAll(pane, createSeparator());
        }

        titlePanes.setAlignment(Pos.TOP_CENTER);

        return titlePanes;
    }

    private TitledPane createTitledPane(String title) {

        Label titleLabel = createLabel(title,
                UIConstants.FONT_NAV,
                true, UITheme.PRIMARY);

        int correctIndex = getCorrect(title);
        int selectedIndex = getSelected(title);
        VBox contentBox = new VBox(UIConstants.SPACING_MD);

        for (int i = 0; i < 4; i++) {
            VBox answerBox = createAnswerBox();
            boolean isCorrect = (i == correctIndex);
            boolean isSelected = (i == selectedIndex);

            showCorrectIncorrect(answerBox, isCorrect, isSelected);

            Label answerLabel = createLabel("Answer",
                    UIConstants.FONT_MD,
                    false,
                    UITheme.TEXT_DARK);

            answerBox.getChildren().add(answerLabel);
            contentBox.getChildren().add(answerBox);

        }

        VBox feedbackBox = createFeedbackBox();
        contentBox.getChildren().add(feedbackBox);

        TitledPane pane = new TitledPane(null, contentBox);
        pane.setGraphic(titleLabel);
        pane.setExpanded(false);
        pane.setStyle(UITheme.CARD_STYLE);
        pane.setAnimated(true);

        pane.setTooltip(createTooltip("Click to expand/collapse",
                UITheme.PRIMARY));
        pane.getTooltip().setShowDelay(Duration.millis(400));
        pane.getTooltip().setHideDelay(Duration.millis(200));

        return pane;
    }

    // needs a refactor
    private List<String> getTitles() {
        return List.of(
                "Question 1:",
                "Question 2:",
                "Question 3:",
                "Question 4:",
                "Question 5:",
                "Question 6:");

    }

    private Label createLabel(
            final String text,
            final int size,
            final boolean bold,
            final String colour) {
        Label label = new Label(text);
        String weight = bold ? "bold" : "normal";
        label.setStyle(
                String.format("-fx-font-size: %dpx;"
                        + "-fx-font-weight: %s;"
                        + "-fx-text-fill: %s;",
                        size, weight, colour));

        return label;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle(BG_COLOR + UITheme.SECONDARY);
        separator.setOpacity(0.5);
        return separator;

    }

    private Tooltip createTooltip(String text, String colour) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setStyle(String.format("-fx-font-size: %s;", colour,
                UITheme.WHITE));
        return tooltip;
    }

    private Button createSmallButton(final String text, Runnable action) {
        String both = "-fx-border-color: " + UITheme.PRIMARY
                + "; -fx-border-width: 2px;"
                + "-fx-border-radius: 4px;"
                + "-fx-background-radius: 6px;"
                + "-fx-text-fill: " + UITheme.TERTIARY + ";"
                + "-fx-font-weight: bold; -fx-font-size: 14px;"
                + "-fx-cursor: hand;";
        String base = "-fx-background-color: transparent;" +
                "-fx-text-fill: " + UITheme.PRIMARY + ";" + both;

        String hover = BG_COLOR + UITheme.PRIMARY + ";"
                + "-fx-border-color: " + UITheme.TERTIARY + ";"
                + both;
        Button b = new Button(text);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        b.setOnAction(e -> action.run());
        return b;
    }

    private VBox createAnswerBox() {
        VBox box = new VBox(UIConstants.SPACING_MD);
        return createCard(box);
    }

    private VBox createCard(VBox content) {
        content.setPadding(new Insets(UIConstants.PADDING_MD));
        content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        content.setStyle(UITheme.CARD_STYLE);
        return content;

    }

    private VBox createFeedbackBox() {
        VBox feedback = new VBox(UIConstants.SPACING_MD);
        feedback.setPadding(new Insets(UIConstants.PADDING_MD));
        feedback.setStyle(UITheme.CARD_STYLE);
        Label title = createLabel("Feedback:", UIConstants.FONT_SM, true, UITheme.PRIMARY);
        feedback.getChildren().addAll(title);
        return feedback;

    }

    // test seems good
    private int getCorrect(String questionTitle) {
        switch (questionTitle) {
            case "Question 1:":
                return 3;
            case "Question 2:":
                return 3;
            case "Question 3:":
                return 3;
            case "Question 4:":
                return 2;
            case "Question 5:":
                return 1;
            case "Question 6:":
                return 0;
            default:
                return 0;
        }
    }

    private int getSelected(String questionTitle) {
        switch (questionTitle) {
            case "Question 1:":
                return 0;
            case "Question 2:":
                return 1;
            case "Question 3:":
                return 2;
            case "Question 4:":
                return 3;
            case "Question 5:":
                return 1;
            case "Question 6:":
                return 0;
            default:
                return 0;
        }
    }

    // working in linr
    private void showCorrectIncorrect(VBox box,
            boolean isCorrect,
            boolean isSelected) {

        String showColor;

        if (isSelected && isCorrect) {
            showColor = "#048a04";

        } else if (isSelected && !isCorrect) {
            showColor = "#A80909";

        } else if (!isSelected && isCorrect) {
            showColor = "#048A04";

        } else {
            showColor = "#DBD6D6";

        }

        box.setStyle(
                UITheme.CARD_STYLE + "-fx-border-color: " + showColor + ";"
                        + "-fx-border-width: 2px;" +
                        "-fx-border-radius: 4px;");

    }
}
