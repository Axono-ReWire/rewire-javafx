package com.axono.answer;

import com.axono.questionmodel.Question;
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
 * Scrollable JavaFX view that renders the post-quiz answer review:
 * a banner with expand/collapse controls and one titled pane per
 * question, each colour-coded for correct, incorrect, and missed
 * answers.
 */
public final class AnswerView extends ScrollPane {

        /** Header colour used when the user got a question wrong. */
        private static final String HEADER_INCORRECT = "#ec5b5b";

        /** Text-fill colour for an incorrectly selected answer. */
        private static final String ANSWER_INCORRECT = "#ee1e1edd";

        /** Border colour applied to correctly answered rows. */
        private static final String CORRECT_BORDER = "#f4f8f4";

        /** Background colour for correctly answered rows. */
        private static final String CORRECT_BACKGROUND = "#b7f1aadd";

        /** Border colour for incorrectly answered rows. */
        private static final String INCORRECT_BORDER = "#f4f8f4";

        /** Background colour for incorrectly answered rows. */
        private static final String INCORRECT_BACKGROUND = "#eecfcfdd";

        /** Background highlighting the correct option a user missed. */
        private static final String SHOW_CORRECT_BACKGROUND = "#b7f1aadd";

        /** Border colour for neutral (non-selected, non-correct) rows. */
        private static final String OTHER_BORDER = "#e7e6e6";

        /** Background colour for neutral rows. */
        private static final String OTHER_BACKGROUND = "#fcf9f9";

        /** Reusable CSS prefix for background-colour declarations. */
        private static final String BG_COLOR = "-fx-background-color: ";

        /** Reusable CSS prefix for text-fill declarations. */
        private static final String TEXT_FILL = "-fx-text-fill: ";

        /** Fixed width applied to the per-answer status icon, in px. */
        private static final int ANSWER_LOGO_WIDTH = 30;

        /** Tooltip show delay, in milliseconds. */
        private static final int TOOLTIP_SHOW_MS = 400;

        /** Tooltip hide delay, in milliseconds. */
        private static final int TOOLTIP_HIDE_MS = 200;

        /** Opacity applied to inter-question separators. */
        private static final double SEPARATOR_OPACITY = 0.5;

        /** Titled panes rendered, one per question, in display order. */
        private final List<TitledPane> panes = new ArrayList<>();

        /** Questions being rendered. */
        private final List<Question> questions;

        /**
         * Parallel list flagging whether each pane represents a correctly
         * answered question; used by the "Expand Incorrect Only" action.
         */
        private final List<Boolean> panesCorrect = new ArrayList<>();

        /**
         * Constructs an AnswerView for the given list of questions and
         * immediately builds the UI tree.
         *
         * @param questionList the questions to render
         */
        public AnswerView(final List<Question> questionList) {
                this.questions = questionList;
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

                content.getChildren().addAll(buildBanner(),
                                buildTitledPanes(),
                                createActionButton("Return to top",
                                                () -> this.setVvalue(0)));

                HBox wrapper = new HBox(content);
                wrapper.setAlignment(Pos.TOP_CENTER);
                wrapper.setStyle(BG_COLOR + UITheme.BG + ";");
                HBox.setHgrow(content, Priority.ALWAYS);

                setContent(wrapper);
                setFitToWidth(true);

                setBorder(Border.EMPTY);
                setStyle(BG_COLOR + UITheme.BG + ";");
        }

        /*
         * Banner method to display the headings, expansion and collapse
         * buttons.
         */
        private VBox buildBanner() {

                Label header = createLabel("Answer Review",
                                UIConstants.FONT_BANNER,
                                true,
                                UITheme.TEXT_DARK);

                Label subHeader = createLabel(
                                "Review your answers after completion of the quiz",
                                UIConstants.FONT_NAV,
                                false,
                                UITheme.TEXT_DARK);
                Label helpLabel = createLabel(
                                "Use the buttons below to control viewing",
                                UIConstants.FONT_NAV,
                                false,
                                UITheme.TEXT_MUTED);
                Label motivationLabel = createLabel(
                                "Remember it is never ever a failiure, "
                                                + "and always a lesson!",
                                UIConstants.FONT_NAV,
                                false, UITheme.TEXT_MUTED);

                Button expandAll = createActionButton("Expand All",
                                () -> panes.forEach(p -> p.setExpanded(true)));
                Button collapseAll = createActionButton("Collapse All",
                                () -> panes.forEach(p -> p.setExpanded(false)));
                Button expandIncorrect = createActionButton(
                                "Expand Incorrect Only",
                                () -> {
                                        for (int i = 0; i < panes.size(); i++) {
                                                boolean isCorrect = panesCorrect.get(i);
                                                panes.get(i).setExpanded(!isCorrect);
                                        }
                                });

                HBox buttons = new HBox(UIConstants.PADDING_MD,
                                expandAll,
                                collapseAll);
                buttons.setAlignment(Pos.TOP_CENTER);

                HBox secondLineButton = new HBox(UIConstants.PADDING_MD,
                                expandIncorrect);
                secondLineButton.setAlignment(Pos.TOP_CENTER);

                VBox banner = new VBox(UIConstants.SPACING_LG,
                                header,
                                subHeader,
                                helpLabel,
                                buttons,
                                secondLineButton,
                                motivationLabel);
                banner.setAlignment(Pos.TOP_CENTER);
                return banner;
        }

        private VBox buildTitledPanes() {

                VBox titlePanes = new VBox(UIConstants.PADDING_SM);

                for (int i = 0; i < questions.size(); i++) {
                        TitledPane pane = createQuestionPane(questions.get(i), i);
                        panes.add(pane);
                        titlePanes.getChildren()
                                        .addAll(pane, createSeparator());
                }

                titlePanes.setAlignment(Pos.TOP_CENTER);

                return titlePanes;
        }

        private Label createLogo(final boolean correct) {
                Label headerLogo = new Label();

                if (correct) {
                        headerLogo.setText("✓");
                        headerLogo.setStyle(TEXT_FILL
                                        + UITheme.PRIMARY + ";"
                                        + "-fx-font-size:"
                                        + UIConstants.FONT_LG);
                } else {
                        headerLogo.setText("❌");
                        headerLogo.setStyle(TEXT_FILL
                                        + HEADER_INCORRECT + ";"
                                        + "-fx-font-size:"
                                        + UIConstants.FONT_LG);
                }

                return headerLogo;
        }

        private HBox createAnswerBox(
                        final String answer,
                        final boolean choseCorrectAndCorrect,
                        final boolean choseIncorrect,
                        final boolean unchosenCorrect) {

                Label answerLogo = new Label();
                answerLogo.setMinWidth(ANSWER_LOGO_WIDTH);
                answerLogo.setPrefWidth(ANSWER_LOGO_WIDTH);
                // define a method
                if (choseCorrectAndCorrect) {
                        answerLogo.setText("✓");
                        answerLogo.setStyle(TEXT_FILL
                                        + UITheme.SECONDARY + ";"
                                        + "-fx-font-size:"
                                        + UIConstants.FONT_MD + ";");
                } else if (choseIncorrect) {
                        answerLogo.setText("❌");
                        answerLogo.setStyle(TEXT_FILL
                                        + ANSWER_INCORRECT + ";"
                                        + "-fx-font-size:"
                                        + UIConstants.FONT_MD + ";");
                }
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label answerLabel = createLabel(answer,
                                UIConstants.FONT_SM,
                                true,
                                UITheme.TEXT_MUTED);
                HBox box = new HBox(UIConstants.SPACING_MD,
                                answerLabel,
                                spacer,
                                answerLogo);

                if (unchosenCorrect) {
                        Label correctLabel = createLabel("⭐ Correct Answer ⭐",
                                        UIConstants.FONT_XS,
                                        true,
                                        UITheme.SECONDARY);
                        box.getChildren().add(correctLabel);
                }

                return createCard(box);
        }

        private TitledPane createQuestionPane(final Question ques,
                        final int quesNo) {

                boolean isCorrect = ques.isCorrect();

                panesCorrect.add(isCorrect);

                HBox header = buildQuestionHeader(isCorrect,
                                quesNo,
                                ques.getQuess());

                VBox content = buildAnswerContent(ques);
                return buildPane(header, content);
        }

        private HBox buildQuestionHeader(final Boolean isCorrect,
                        final int quesNo,
                        final String ques) {

                Label quesNumber = createLabel(" Q" + (quesNo + 1) + "   -",
                                UIConstants.FONT_MD, true, UITheme.TEXT_DARK);

                Label quesLogo = createLogo(isCorrect);
                Label titleLabel = createLabel(ques,
                                UIConstants.FONT_MD,
                                true,
                                UITheme.TEXT_MUTED);

                return new HBox(UIConstants.SPACING_MD,
                                quesLogo,
                                quesNumber,
                                titleLabel);
        }

        private TitledPane buildPane(final HBox header,
                        final VBox content) {

                TitledPane pane = new TitledPane(null, content);
                pane.setGraphic(header);
                pane.setExpanded(false);
                pane.setStyle(UITheme.CARD_STYLE);
                pane.setAnimated(true);

                pane.setTooltip(createTooltip("click to expand or collapse"));
                pane.getTooltip()
                                .setShowDelay(Duration.millis(TOOLTIP_SHOW_MS));
                pane.getTooltip()
                                .setHideDelay(Duration.millis(TOOLTIP_HIDE_MS));

                return pane;
        }

        private VBox buildAnswerContent(final Question ques) {
                VBox contentBox = new VBox(UIConstants.SPACING_MD);
                List<String> answers = ques.getAnswers();
                for (int i = 0; i < answers.size(); i++) {
                        String answer = answers.get(i);

                        // sort this out next
                        boolean correctAndSelected = ques.choseCorrectAndCorrect(i);
                        boolean incorrectSelection = ques.choseIncorrect(i);
                        boolean unchosenCorrect = ques.unchosenCorrect(i);

                        HBox box = createAnswerBox(answer,
                                        correctAndSelected,
                                        incorrectSelection,
                                        unchosenCorrect);

                        colourAnswerBox(box,
                                        correctAndSelected,
                                        incorrectSelection,
                                        unchosenCorrect);
                        contentBox.getChildren().add(box);
                }

                return contentBox;
        }

        private void colourAnswerBox(
                        final HBox box,
                        final boolean choseCorrectAndCorrect,
                        final boolean choseIncorrect,
                        final boolean unchosenCorrect) {

                String highlightBorder;
                String shadeBackground;

                if (choseCorrectAndCorrect) {
                        highlightBorder = CORRECT_BORDER;
                        shadeBackground = CORRECT_BACKGROUND;
                } else if (choseIncorrect) {
                        highlightBorder = INCORRECT_BORDER;
                        shadeBackground = INCORRECT_BACKGROUND;
                } else if (unchosenCorrect) {
                        highlightBorder = CORRECT_BORDER;
                        shadeBackground = SHOW_CORRECT_BACKGROUND;
                } else {
                        highlightBorder = OTHER_BORDER;
                        shadeBackground = OTHER_BACKGROUND;
                }

                box.setStyle("-fx-background-color:"
                                + shadeBackground + ";"
                                + "-fx-border-color: "
                                + highlightBorder + ";"
                                + "-fx-border-width: 4px;"
                                + "-fx-border-radius: 10px;"
                                + "-fx-padding: 10px 10px;");
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
                                                + TEXT_FILL + "%s;",
                                                size, weight, colour));

                return label;
        }

        private Separator createSeparator() {
                Separator separator = new Separator();
                separator.setStyle(BG_COLOR + UITheme.TERTIARY);
                separator.setOpacity(SEPARATOR_OPACITY);
                return separator;
        }

        private Tooltip createTooltip(final String text) {
                Tooltip tooltip = new Tooltip(text);
                tooltip.setStyle(String.format(
                                "-fx-font-size:" + UIConstants.FONT_XS + ";"
                                                + TEXT_FILL + UITheme.SECONDARY + ";"
                                                + "-fx-background-color: " + UITheme.WHITE + ";"));
                return tooltip;
        }

        private Button createActionButton(final String text,
                        final Runnable action) {
                String both = "-fx-border-color: " + UITheme.PRIMARY
                                + "; -fx-border-width: 2px;"
                                + "-fx-border-radius: 4px;"
                                + "-fx-background-radius: 4px;"
                                + "-fx-font-weight: bold; -fx-font-size: 12px;"
                                + "-fx-cursor: hand;";
                String base = "-fx-background-color:" + UITheme.WHITE + ";"
                                + "-fx-text-fill: " + UITheme.PRIMARY + ";" + both;

                String hover = BG_COLOR + UITheme.PRIMARY + ";"
                                + "-fx-border-color: " + UITheme.TERTIARY + ";"
                                + "-fx-text-fill: " + UITheme.WHITE + ";"
                                + both;
                Button b = new Button(text);
                b.setStyle(base);
                b.setOnMouseEntered(e -> b.setStyle(hover));
                b.setOnMouseExited(e -> b.setStyle(base));
                b.setOnAction(e -> action.run());
                return b;
        }

        private HBox createCard(final HBox content) {
                content.setPadding(new Insets(UIConstants.PADDING_SM));
                content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
                content.setStyle(
                                UITheme.CARD_STYLE + "-fx-padding: 10px 10px;");
                return content;
        }
}