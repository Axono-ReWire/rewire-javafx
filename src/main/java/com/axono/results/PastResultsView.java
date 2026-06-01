package com.axono.results;

import com.axono.auth.Session;
import com.axono.auth.User;
import com.axono.ui.UIConstants;
import com.axono.player.QuizResult;
import com.axono.player.QuizResultRepository;
import com.axono.content.LearningContent;
import com.axono.content.LearningContentLoader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Full history view that lists every quiz attempt for the currently
 * authenticated user, newest first. Reads directly from the
 * {@code quiz_results} table via {@link QuizResultRepository} and looks up
 * presentation titles through {@link LearningContentLoader} so the user sees
 * meaningful labels rather than raw file paths.
 */
public final class PastResultsView extends ScrollPane {

    /** Repository used to query attempts. */
    private final QuizResultRepository repo = new QuizResultRepository();

    /**
     * Optional callback invoked when the user clicks "Details" on an attempt.
     * Receives the {@link QuizResult} and the corresponding
     * {@link LearningContent} (or {@code null} when the file can no longer
     * be located on disk).
     */
    private final BiConsumer<QuizResult, LearningContent> onViewDetails;

    /**
     * Constructs the view without a details callback.
     * "Details" buttons will not appear.
     */
    public PastResultsView() {
        this(null);
    }

    /**
     * Constructs the view with a details callback.
     *
     * @param detailsCallback invoked when the user clicks "Details" on a row;
     *                        may be {@code null}.
     */
    public PastResultsView(
            final BiConsumer<QuizResult, LearningContent> detailsCallback) {
        this.onViewDetails = detailsCallback;
        buildUI();
    }

    /** Top-level layout: banner + history list inside a scroll pane. */
    private void buildUI() {
        VBox content = new VBox(UIConstants.SPACING_3XL);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H,
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H));
        content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        content.getStyleClass().add("bg-app");
        content.getChildren().addAll(buildBanner(), buildHistoryCard());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getStyleClass().add("bg-app");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        getStyleClass().add("bg-app");
    }

    /**
     * Builds the page banner with title and subtitle.
     *
     * @return the banner VBox.
     */
    private VBox buildBanner() {
        Label title = new Label("Your Quiz History");
        title.getStyleClass().add("text-dark");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label(
                "Every quiz you've completed, newest first.");
        subtitle.getStyleClass().add("text-muted");
        subtitle.setStyle("-fx-font-size: 14px;");

        VBox banner = new VBox(UIConstants.SPACING_LG, title, subtitle);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    /**
     * Builds the card containing the history rows, or an empty-state
     * message if the user is not signed in or has no attempts.
     *
     * @return the history card.
     */
    private VBox buildHistoryCard() {
        User user = Session.get();
        if (user == null) {
            return messageCard("Not signed in.",
                    "Log in to see your quiz history.");
        }
        List<QuizResult> attempts;
        try {
            attempts = repo.findByUser(user.getId());
        } catch (SQLException ex) {
            return messageCard("Database error",
                    ex.getMessage());
        }
        if (attempts.isEmpty()) {
            return messageCard("No attempts yet",
                    "Finish a quiz to see your history here.");
        }
        return buildAttemptsCard(attempts);
    }

    /**
     * Builds a card containing one row per attempt.
     *
     * @param attempts the attempts in display order.
     * @return the styled card.
     */
    private VBox buildAttemptsCard(final List<QuizResult> attempts) {
        Map<String, String> titles = buildTitleIndex();
        Map<String, LearningContent> contentMap = buildContentIndex();
        VBox rows = new VBox(UIConstants.SPACING_MD);
        for (QuizResult attempt : attempts) {
            String title = titles.getOrDefault(attempt.getPresentationId(),
                    fallbackTitle(attempt.getPresentationId()));
            LearningContent lc = contentMap.get(attempt.getPresentationId());
            rows.getChildren().add(buildAttemptRow(attempt, title, lc));
        }
        VBox card = new VBox(UIConstants.SPACING_LG, rows);
        card.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 28px 40px;");
        return card;
    }

    /**
     * Builds a single attempt row: title + score chip + timestamp +
     * optional "Details" button when a details callback is configured.
     *
     * @param attempt the quiz attempt.
     * @param title   the resolved presentation title.
     * @param lc      the corresponding {@link LearningContent}, or
     *                {@code null} when the file is no longer found.
     * @return the row node.
     */
    private HBox buildAttemptRow(final QuizResult attempt,
            final String title,
            final LearningContent lc) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("text-dark");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label when = new Label(attempt.getCompletedAt());
        when.getStyleClass().add("text-muted");
        when.setStyle("-fx-font-size: 12px;");
        VBox text = new VBox(2, titleLabel, when);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label scoreChip = new Label(attempt.getScore()
                + " / " + attempt.getMaxScore());
        scoreChip.getStyleClass().add("score-chip");
        scoreChip.setStyle("-fx-font-size: 13px;");

        HBox row = new HBox(UIConstants.SPACING_LG, text, scoreChip);
        if (onViewDetails != null) {
            row.getChildren().add(buildDetailsButton(attempt, lc));
        }
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(UIConstants.SPACING_MD,
                UIConstants.SPACING_LG,
                UIConstants.SPACING_MD,
                UIConstants.SPACING_LG));
        row.getStyleClass().add("content-row");
        return row;
    }

    /**
     * Builds the "Details →" button for an attempt row.
     *
     * @param attempt the quiz attempt.
     * @param lc      the resolved learning content; may be {@code null}.
     * @return the configured button.
     */
    private javafx.scene.control.Button buildDetailsButton(
            final QuizResult attempt, final LearningContent lc) {
        javafx.scene.control.Button btn =
                new javafx.scene.control.Button("Details →");
        btn.getStyleClass().add("btn-outline-sm");
        btn.setOnAction(e -> onViewDetails.accept(attempt, lc));
        return btn;
    }

    /**
     * Loads every presentation once and indexes them by id so the
     * history rows can show titles instead of paths.
     *
     * @return a map of presentation id to title.
     */
    private Map<String, String> buildTitleIndex() {
        Map<String, String> map = new HashMap<>();
        for (LearningContent p : LearningContentLoader.loadAll()) {
            map.put(p.getId(), p.getTitle());
        }
        return map;
    }

    /**
     * Loads every presentation once and indexes them by id so the
     * "Details" callback can receive the full {@link LearningContent}.
     *
     * @return a map of presentation id to {@link LearningContent}.
     */
    private Map<String, LearningContent> buildContentIndex() {
        Map<String, LearningContent> map = new HashMap<>();
        for (LearningContent p : LearningContentLoader.loadAll()) {
            map.put(p.getId(), p);
        }
        return map;
    }

    /**
     * Derives a friendly fallback title from a presentation id when the
     * referenced XML can no longer be located on disk.
     *
     * @param presentationId the raw id (typically a file path).
     * @return a display string.
     */
    private static String fallbackTitle(final String presentationId) {
        if (presentationId == null || presentationId.isEmpty()) {
            return "(unknown presentation)";
        }
        int slash = Math.max(presentationId.lastIndexOf('/'),
                presentationId.lastIndexOf('\\'));
        String tail = slash >= 0
                ? presentationId.substring(slash + 1) : presentationId;
        int dot = tail.lastIndexOf('.');
        return dot > 0 ? tail.substring(0, dot) : tail;
    }

    /**
     * Builds a simple message card used for empty / not-signed-in /
     * error states.
     *
     * @param heading the card heading.
     * @param body    the card body text.
     * @return the styled card.
     */
    private VBox messageCard(final String heading, final String body) {
        Label headingLabel = new Label(heading);
        headingLabel.getStyleClass().add("text-dark");
        headingLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("text-muted");
        bodyLabel.setStyle("-fx-font-size: 14px;");
        bodyLabel.setWrapText(true);

        VBox card = new VBox(UIConstants.SPACING_LG,
                headingLabel, bodyLabel);
        card.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 28px 40px;");
        return card;
    }
}
