package com.axono.results;

import com.axono.ui.UIConstants;
import com.axono.content.LearningContent;
import com.axono.content.QuestionData;
import com.axono.content.Slide;
import com.axono.player.QuizResult;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Post-quiz results page. Shows the score summary (score, percentage, title)
 * at the top, then an embedded {@link AnswerView} for a per-question breakdown.
 * Action buttons allow the user to retake the quiz or return to the browser.
 */
public final class ResultsPage extends BorderPane {

    /** The quiz attempt whose results are displayed. */
    private final QuizResult result;

    /** The learning content used to reconstruct per-question review data. */
    private final LearningContent content;

    /** Callback invoked when the user clicks "Back". */
    private final Runnable onBack;

    /** Callback invoked when the user clicks "Retake Quiz"; may be null. */
    private final Runnable onRetake;

    /**
     * Constructs a {@code ResultsPage} for the given quiz attempt.
     *
     * @param quizResult   the saved quiz attempt to display.
     * @param quizContent  the corresponding learning content (used to rebuild
     *                     questions for answer review); may be {@code null}
     *                     for legacy results where the file is no longer
     *                     available.
     * @param backCallback called when the user clicks the back button;
     *                     typically navigates to the browser or history.
     * @param retakeCallback called when the user clicks "Retake Quiz";
     *                       may be {@code null} to hide the button.
     */
    public ResultsPage(final QuizResult quizResult,
            final LearningContent quizContent,
            final Runnable backCallback,
            final Runnable retakeCallback) {
        this.result = quizResult;
        this.content = quizContent;
        this.onBack = backCallback;
        this.onRetake = retakeCallback;
        buildUI();
    }

    /** Builds the BorderPane layout: score banner top, answer review centre. */
    private void buildUI() {
        getStyleClass().add("bg-app");
        setTop(buildScoreBanner());
        setCenter(new AnswerView(buildQuestions()));
    }

    /**
     * Builds the horizontal score banner showing title, score chip,
     * percentage and action buttons.
     *
     * @return the banner {@link HBox}.
     */
    private HBox buildScoreBanner() {
        String quizTitle = content != null ? content.getTitle() : "Quiz";
        Label titleLabel = new Label(quizTitle);
        titleLabel.getStyleClass().add("text-dark");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        int score = result.getScore();
        int max = result.getMaxScore();
        Label scoreLabel = new Label(score + " / " + max);
        scoreLabel.getStyleClass().add("text-primary");
        scoreLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        int pct = max > 0
                ? (int) (score * UIConstants.PERCENT_MAX / max) : 0;
        Label pctLabel = new Label(pct + "%");
        pctLabel.getStyleClass().add("text-secondary");
        pctLabel.setStyle("-fx-font-size: 18px;");

        Button backBtn = outlineButton("← Back");
        backBtn.setOnAction(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });

        HBox buttons = new HBox(UIConstants.SPACING_3XL, backBtn);
        if (onRetake != null) {
            Button retakeBtn = outlineButton("Retake Quiz");
            retakeBtn.setOnAction(e -> onRetake.run());
            buttons.getChildren().add(retakeBtn);
        }
        buttons.setAlignment(Pos.CENTER);

        VBox scoreVBox = new VBox(UIConstants.SPACING_MD,
                titleLabel, scoreLabel, pctLabel, buttons);
        scoreVBox.setAlignment(Pos.CENTER);

        HBox banner = new HBox(scoreVBox);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(UIConstants.SPACING_4XL));
        banner.getStyleClass().add("panel-header");
        return banner;
    }

    /**
     * Reconstructs the list of {@link Question} objects from the quiz
     * content slides and the stored answer indices in the result.
     *
     * <p>The XML uses 1-based answer indices; {@link Question} uses 0-based.
     * The conversion is done here. Unanswered questions (index 0 in the JSON)
     * are represented with a selected index of {@code -1} so the
     * {@link AnswerView} highlights the correct answer as "unchosen".</p>
     *
     * @return the reconstructed question list; empty if content is null.
     */
    private List<Question> buildQuestions() {
        if (content == null) {
            return new ArrayList<>();
        }
        int[] userAnswers = parseAnswersJson(result.getAnswersJson());
        List<Slide> slides = content.getSlides();
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < slides.size(); i++) {
            QuestionData qd = slides.get(i).getQuestionData();
            if (qd == null) {
                continue;
            }
            // XML correctAnswer is 1-based; Question expects 0-based
            int correctIdx = qd.correctAnswerIndex() - 1;
            int userAnswer1Based = i < userAnswers.length ? userAnswers[i] : 0;
            // 0 means unanswered; use -1 so no option appears as selected
            int selectedIdx = userAnswer1Based > 0
                    ? userAnswer1Based - 1 : -1;
            questions.add(new Question(
                    qd.questionText(),
                    qd.answerOptions(),
                    correctIdx,
                    selectedIdx));
        }
        return questions;
    }

    /**
     * Parses a compact JSON integer array (e.g. {@code "[2,1,3,4]"}) into an
     * {@code int[]} without any JSON library dependency.
     *
     * @param json the JSON string; may be {@code null} or {@code "[]"}.
     * @return the parsed array; empty array when the input has no entries.
     */
    private static int[] parseAnswersJson(final String json) {
        String trimmed = json == null ? "[]" : json.trim();
        if ("[]".equals(trimmed) || trimmed.isEmpty()) {
            return new int[0];
        }
        String inner = trimmed.substring(1, trimmed.length() - 1);
        String[] parts = inner.split(",");
        int[] answers = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                answers[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException ex) {
                answers[i] = 0;
            }
        }
        return answers;
    }

    /**
     * Creates a styled outline {@link Button} with hover fill effects.
     *
     * @param text the button label.
     * @return a configured outline {@link Button}.
     */
    private static Button outlineButton(final String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-outline");
        return b;
    }
}
