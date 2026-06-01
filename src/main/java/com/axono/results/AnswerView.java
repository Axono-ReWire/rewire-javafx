package com.axono.results;

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
        private final List<Boolean> correctPane = new ArrayList<>();

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

                VBox content = new VBox(UIConstants.SPACING_8XL);
                content.setAlignment(Pos.TOP_CENTER);
                content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
                content.getStyleClass().add("bg-app");

                content.setPadding(new Insets(
                                UIConstants.PADDING_CONTENT_V,
                                UIConstants.PADDING_CONTENT_H,
                                UIConstants.PADDING_CONTENT_V,
                                UIConstants.PADDING_CONTENT_H));

                content.getChildren().addAll(buildBanner(),
                                buildTitledPanes(),
                                createActionButton("Return to top",
                                                () -> this.setVvalue(0)));

                HBox wrapper = new HBox(content);
                wrapper.setAlignment(Pos.TOP_CENTER);
                wrapper.getStyleClass().add("bg-app");
                HBox.setHgrow(content, Priority.ALWAYS);

                setContent(wrapper);
                setFitToWidth(true);

                setBorder(Border.EMPTY);
                getStyleClass().add("bg-app");
        }

        /*
         * Banner method to display the headings, expansion and collapse
         * buttons.
         */
        private VBox buildBanner() {

                Label header = createLabel("Answer Review",
                                UIConstants.FONT_BANNER,
                                true,
                                "text-dark");

                Label subHeader = createLabel(
                                "Review your answers after"
                                                + " completion of the quiz",
                                UIConstants.FONT_SECTION,
                                false,
                                "text-dark");
                Label helpLabel = createLabel(
                                "Use the buttons below to control viewing",
                                UIConstants.FONT_SECTION,
                                false,
                                "text-muted");
                Label motivationLabel = createLabel(
                                "Remember it is never ever a failiure, "
                                                + "and always a lesson!",
                                UIConstants.FONT_SECTION,
                                false, "text-muted");

                Button expandAll = createActionButton("Expand All",
                                () -> panes.forEach(p -> p.setExpanded(true)));
                Button collapseAll = createActionButton("Collapse All",
                                () -> panes.forEach(p -> p
                                                .setExpanded(false)));
                Button expandIncorrect = createActionButton(
                                "Expand Incorrect Only", () -> {
                                        for (int i = 0; i < panes
                                                        .size(); i++) {
                                                boolean isCorrect = correctPane
                                                                .get(i);
                                                panes.get(i).setExpanded(
                                                                !isCorrect);
                                        }
                                });

                HBox buttons = new HBox(UIConstants.SPACING_3XL,
                                expandAll,
                                collapseAll);
                buttons.setAlignment(Pos.TOP_CENTER);

                HBox secondLineButton = new HBox(UIConstants.SPACING_3XL,
                                expandIncorrect);
                secondLineButton.setAlignment(Pos.TOP_CENTER);

                VBox banner = new VBox(UIConstants.SPACING_XL,
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

                VBox titlePanes = new VBox(UIConstants.SPACING_XL);

                for (int i = 0; i < questions.size(); i++) {
                        TitledPane pane = createQuestionPane(questions
                                        .get(i), i);
                        panes.add(pane);
                        titlePanes.getChildren()
                                        .addAll(pane, createSeparator());
                }

                titlePanes.setAlignment(Pos.TOP_CENTER);

                return titlePanes;
        }

        private static Label createLogo(final boolean correct) {
                Label headerLogo = new Label();
                headerLogo.setStyle(
                        "-fx-font-size:" + UIConstants.FONT_BODY_LG);
                if (correct) {
                        headerLogo.setText("✓");
                        headerLogo.getStyleClass().add("text-primary");
                } else {
                        headerLogo.setText("❌");
                        headerLogo.getStyleClass().add("text-error");
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
                if (choseCorrectAndCorrect) {
                        answerLogo.setText("✓");
                        answerLogo.getStyleClass().add("text-secondary");
                        answerLogo.setStyle("-fx-font-size:"
                                        + UIConstants.FONT_LABEL + ";");
                } else if (choseIncorrect) {
                        answerLogo.setText("❌");
                        answerLogo.getStyleClass().add("text-error");
                        answerLogo.setStyle("-fx-font-size:"
                                        + UIConstants.FONT_LABEL + ";");
                }
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label answerLabel = createLabel(answer,
                                UIConstants.FONT_CAPTION,
                                true,
                                "text-dark");
                HBox box = new HBox(UIConstants.SPACING_LG,
                                answerLabel,
                                spacer,
                                answerLogo);

                if (unchosenCorrect) {
                        Label correctLabel = createLabel("⭐ Correct Answer ⭐",
                                        UIConstants.FONT_SMALL,
                                        true,
                                        "text-secondary");
                        box.getChildren().add(correctLabel);
                }

                return createCard(box);
        }

        private TitledPane createQuestionPane(final Question ques,
                        final int quesNo) {

                boolean isCorrect = ques.isCorrect();

                correctPane.add(isCorrect);

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
                                UIConstants.FONT_LABEL, true, "text-dark");

                Label quesLogo = createLogo(isCorrect);
                Label titleLabel = createLabel(ques,
                                UIConstants.FONT_LABEL,
                                true,
                                "text-dark");

                return new HBox(UIConstants.SPACING_LG,
                                quesLogo,
                                quesNumber,
                                titleLabel);
        }

        private TitledPane buildPane(final HBox header,
                        final VBox content) {

                TitledPane pane = new TitledPane(null, content);
                pane.setGraphic(header);
                pane.setExpanded(false);
                pane.getStyleClass().add("answer-pane");
                pane.setAnimated(true);

                pane.setTooltip(createTooltip("click to expand or collapse"));
                pane.getTooltip()
                                .setShowDelay(Duration.millis(TOOLTIP_SHOW_MS));
                pane.getTooltip()
                                .setHideDelay(Duration.millis(TOOLTIP_HIDE_MS));

                return pane;
        }

        private VBox buildAnswerContent(final Question ques) {
                VBox contentBox = new VBox(UIConstants.SPACING_LG);
                List<String> answers = ques.getAnswers();
                for (int i = 0; i < answers.size(); i++) {
                        String answer = answers.get(i);

                        // sort this out next
                        boolean correctAndSelected = ques
                                        .choseCorrectAndCorrect(i);
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

        private static void colourAnswerBox(
                        final HBox box,
                        final boolean choseCorrectAndCorrect,
                        final boolean choseIncorrect,
                        final boolean unchosenCorrect) {
                String cssClass;
                if (choseCorrectAndCorrect) {
                        cssClass = "answer-correct";
                } else if (choseIncorrect) {
                        cssClass = "answer-incorrect";
                } else if (unchosenCorrect) {
                        cssClass = "answer-show-correct";
                } else {
                        cssClass = "answer-neutral";
                }
                box.getStyleClass().add(cssClass);
        }

        private static Label createLabel(
                        final String text,
                        final int size,
                        final boolean bold,
                        final String cssClass) {
                Label label = new Label(text);
                String weight = bold ? "bold" : "normal";
                label.setStyle(String.format("-fx-font-size: %dpx;"
                                + "-fx-font-weight: %s;", size, weight));
                if (cssClass != null && !cssClass.isEmpty()) {
                        label.getStyleClass().add(cssClass);
                }
                return label;
        }

        private static Separator createSeparator() {
                Separator separator = new Separator();
                separator.setStyle("-fx-background-color: primary;");
                separator.setOpacity(SEPARATOR_OPACITY);
                return separator;
        }

        private static Tooltip createTooltip(final String text) {
                Tooltip tooltip = new Tooltip(text);
                tooltip.setStyle("-fx-font-size:" + UIConstants.FONT_SMALL
                        + ";");
                return tooltip;
        }

        private static Button createActionButton(final String text,
                        final Runnable action) {
                Button b = new Button(text);
                b.getStyleClass().add("btn-outline");
                b.setOnAction(e -> action.run());
                return b;
        }

        private static HBox createCard(final HBox content) {
                content.setPadding(new Insets(UIConstants.SPACING_XL));
                content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
                return content;
        }
}
