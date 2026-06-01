package com.axono.dashboard;

import com.axono.auth.Session;
import com.axono.auth.User;
import com.axono.ui.UIConstants;
import com.axono.content.LearningContent;
import com.axono.content.LearningContentLoader;
import com.axono.auth.UserProfile;
import com.axono.player.QuizResult;
import com.axono.player.QuizResultRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Main dashboard view shown after login. Displays three data-driven sections:
 * "Your Progress" (selected modules with real quiz-completion percentages),
 * "Continue Learning" (modules with incomplete quizzes), and "Next Steps"
 * (unselected modules available to explore).
 */
public final class DashboardView extends ScrollPane {

    /** Maximum recent attempts shown on the dashboard. */
    private static final int RECENT_LIMIT = 5;

    /** Maximum suggestions shown in "Next Steps". */
    private static final int NEXT_STEPS_LIMIT = 3;

    /** User profile containing name and selected subjects. */
    private final UserProfile profile;

    /** Callback to open the full quiz history view. */
    private final Runnable onViewAllResults;

    /** Callback invoked when the user clicks a module card's action button. */
    private final Consumer<String> onModuleDetail;

    /** Callback invoked when the user clicks Logout. */
    private final Runnable onLogout;

    /** Callback invoked when the user clicks Profile. */
    private final Runnable onProfile;

    /** Repository for loading quiz attempt history. */
    private final QuizResultRepository quizRepo = new QuizResultRepository();

    /**
     * Constructs the {@code DashboardView}.
     *
     * @param userProfile     the profile of the authenticated user.
     * @param onViewAll       callback for the "View all" button on attempts.
     * @param onModuleAction  callback invoked with a module name when Resume
     *                        or Explore is clicked; may be null.
     * @param logoutAction    callback for the Logout button; may be null.
     * @param profileAction   callback for the Profile button; may be null.
     */
    public DashboardView(final UserProfile userProfile,
            final Runnable onViewAll,
            final Consumer<String> onModuleAction,
            final Runnable logoutAction,
            final Runnable profileAction) {
        this.profile = userProfile;
        this.onViewAllResults = onViewAll == null ? () -> { } : onViewAll;
        this.onModuleDetail = onModuleAction == null
                ? m -> { } : onModuleAction;
        this.onLogout = logoutAction == null ? () -> { } : logoutAction;
        this.onProfile = profileAction == null ? () -> { } : profileAction;
        buildUI();
    }

    /**
     * Backward-compatible constructor without module/logout/profile callbacks.
     *
     * @param userProfile the profile of the authenticated user.
     * @param onViewAll   callback for the "View all" button.
     */
    public DashboardView(final UserProfile userProfile,
            final Runnable onViewAll) {
        this(userProfile, onViewAll, null, null, null);
    }

    /** Assembles the scrollable dashboard content. */
    private void buildUI() {
        VBox content = new VBox(UIConstants.SPACING_8XL);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H,
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H));
        content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        content.getStyleClass().add("bg-app");

        content.getChildren().addAll(
                buildBanner(),
                buildProgressSection(),
                buildContinueLearningSection(),
                buildRecentAttemptsSection(),
                buildNextStepsSection()
        );

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getStyleClass().add("bg-app");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        getStyleClass().add("bg-app");
    }

    // ── Sections ────────────────────────────────────────────────────────────

    /**
     * Builds the welcome banner with Profile and Logout buttons.
     *
     * @return the banner VBox.
     */
    private VBox buildBanner() {
        Label welcome = new Label("Welcome, " + profile.getFirstName() + "!");
        welcome.getStyleClass().add("text-dark");
        welcome.setStyle("-fx-font-size: " + UIConstants.FONT_BANNER + "px;"
                + "-fx-font-weight: bold;");

        Button profileBtn = outlineButton("Profile");
        profileBtn.setOnAction(e -> onProfile.run());

        Button logoutBtn = outlineButton("Logout");
        logoutBtn.setOnAction(e -> onLogout.run());

        HBox buttons = new HBox(UIConstants.SPACING_LG, profileBtn, logoutBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox banner = new VBox(UIConstants.SPACING_3XL, welcome, buttons);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    /**
     * Builds "Your Progress": one row per selected module with a real progress
     * bar based on quiz completion.
     *
     * @return the section VBox.
     */
    private VBox buildProgressSection() {
        Label sectionTitle = sectionLabel("Your Progress",
                UIConstants.SPACING_3XL);

        List<String> subjects = profile.getSubjects();
        Map<String, Double> progressMap = buildProgressMap(subjects);

        VBox entries = new VBox(UIConstants.SPACING_LG);
        if (subjects.isEmpty()) {
            entries.getChildren().add(bodyLabel(
                    "No modules selected. Use \"Next Steps\" below to add "
                            + "modules to your learning path."));
        } else {
            for (String subject : subjects) {
                double pct = progressMap.getOrDefault(subject, 0.0);
                entries.getChildren().add(buildProgressRow(subject, pct));
            }
        }

        VBox section = new VBox(UIConstants.SPACING_2XL,
                sectionTitle, cardWrap(entries));
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
        return section;
    }

    /**
     * Builds "Continue Learning": modules the user has started but not
     * completed. Only shows modules that are in the user's selected subjects.
     *
     * @return the section VBox.
     */
    private VBox buildContinueLearningSection() {
        Label sectionTitle = sectionLabel("Continue Learning",
                UIConstants.SPACING_3XL);

        List<String> subjects = profile.getSubjects();
        Map<String, Double> progressMap = buildProgressMap(subjects);

        // Collect modules with incomplete quizzes
        List<String> incomplete = new ArrayList<>();
        for (String subject : subjects) {
            double pct = progressMap.getOrDefault(subject, 0.0);
            if (pct > 0.0
                    && pct < UIConstants.PERCENT_MAX) {
                incomplete.add(subject);
            }
        }

        // Load recently accessed modules
        List<String> recent = new ArrayList<>();
        User user = Session.get();
        if (user != null) {
            try {
                List<com.axono.player.ResourceAccessRepository.RecentResource>
                        resources = com.axono.player
                        .ResourceAccessRepository.getRecentResources(
                        user.getId(), RECENT_LIMIT);
                Set<String> recentModules = new LinkedHashSet<>();
                for (var res : resources) {
                    if (subjects.contains(res.getModuleName())) {
                        recentModules.add(res.getModuleName());
                    }
                }
                recent.addAll(recentModules);
            } catch (SQLException ex) {
                System.err.println("Could not load recent resources: "
                        + ex.getMessage());
            }
        }

        // Combine lists: incomplete first, then recent (deduplicated)
        Set<String> displayed = new LinkedHashSet<>(incomplete);
        for (String mod : recent) {
            if (!displayed.contains(mod)) {
                displayed.add(mod);
            }
        }

        VBox card;
        if (displayed.isEmpty()) {
            String msg = subjects.isEmpty()
                    ? "Select some modules to start learning."
                    : "No in-progress modules yet. Start a quiz or open "
                            + "a resource to see it here.";
            card = cardWrap(new VBox(UIConstants.SPACING_LG, bodyLabel(msg)));
        } else {
            VBox rows = new VBox(UIConstants.SPACING_LG);
            for (String module : displayed) {
                double pct = progressMap.getOrDefault(module, 0.0);
                rows.getChildren().add(
                        buildContinueRow(module, pct));
            }
            card = cardWrap(rows);
        }

        VBox section = new VBox(UIConstants.SPACING_2XL, sectionTitle, card);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
        return section;
    }

    /**
     * Builds the recent quiz attempts section.
     *
     * @return the section VBox.
     */
    private VBox buildRecentAttemptsSection() {
        Label title = sectionLabel("Recent Quiz Attempts",
                UIConstants.FONT_SUBHEADER);
        User user = Session.get();
        VBox card = (user == null)
                ? cardWrap(new VBox(UIConstants.SPACING_LG,
                        bodyLabel("Sign in to see your quiz history.")))
                : buildAttemptsCard(user);

        VBox section = new VBox(UIConstants.SPACING_2XL, title, card);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
        return section;
    }

    /**
     * Builds "Next Steps": unselected modules suggested for exploration.
     * Shows up to {@link #NEXT_STEPS_LIMIT} suggestions.
     *
     * @return the section VBox.
     */
    private VBox buildNextStepsSection() {
        Label sectionTitle = sectionLabel("Next Steps",
                UIConstants.SPACING_3XL);

        Set<String> selectedSet = new LinkedHashSet<>(profile.getSubjects());
        List<LearningContent> all = LearningContentLoader.loadAll();
        Set<String> unselectedModules = new LinkedHashSet<>();
        for (LearningContent lc : all) {
            String mod = lc.getModule();
            if (!mod.isEmpty() && !selectedSet.contains(mod)) {
                unselectedModules.add(mod);
            }
        }

        VBox card;
        if (unselectedModules.isEmpty()) {
            card = cardWrap(new VBox(UIConstants.SPACING_LG,
                    bodyLabel("You have selected all available modules!")));
        } else {
            VBox rows = new VBox(UIConstants.SPACING_LG);
            int count = 0;
            for (String module : unselectedModules) {
                if (count >= NEXT_STEPS_LIMIT) {
                    break;
                }
                rows.getChildren().add(buildNextStepRow(module));
                count++;
            }
            card = cardWrap(rows);
        }

        VBox section = new VBox(UIConstants.SPACING_2XL, sectionTitle, card);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
        return section;
    }

    // ── Row builders ────────────────────────────────────────────────────────

    /**
     * Builds one row for "Your Progress": module name + progress bar + pct.
     *
     * @param module the module name.
     * @param pct    the completion percentage (0–100).
     * @return the row HBox.
     */
    private HBox buildProgressRow(final String module, final double pct) {
        Label name = bodyLabel(module);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);

        StackPane bar = progressBar(pct);

        int rounded = (int) Math.round(pct);
        Label pctLabel = new Label(rounded + "% Complete");
        pctLabel.getStyleClass().add("text-secondary");
        pctLabel.setStyle("-fx-font-size: " + UIConstants.FONT_CAPTION + "px;");

        HBox row = new HBox(UIConstants.SPACING_XL, name, bar, pctLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Builds one "Continue Learning" row with progress bar and Resume button.
     *
     * @param module the module name.
     * @param pct    the current completion percentage.
     * @return the row HBox.
     */
    private HBox buildContinueRow(final String module, final double pct) {
        Label name = bodyLabel(module);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);

        StackPane bar = progressBar(pct);
        int rounded = (int) Math.round(pct);
        Label pctLabel = new Label(rounded + "%");
        pctLabel.getStyleClass().add("text-secondary");
        pctLabel.setStyle("-fx-font-size: " + UIConstants.FONT_CAPTION + "px;");

        Button resume = smallPrimaryButton("Resume");
        resume.setOnAction(e -> onModuleDetail.accept(module));

        HBox row = new HBox(UIConstants.SPACING_XL,
                name, bar, pctLabel, resume);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Builds one "Next Steps" row with module name and Explore button.
     *
     * @param module the module name.
     * @return the row HBox.
     */
    private HBox buildNextStepRow(final String module) {
        Label name = bodyLabel(module);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);

        Button explore = smallPrimaryButton("Explore");
        explore.setOnAction(e -> onModuleDetail.accept(module));

        HBox row = new HBox(UIConstants.SPACING_XL, name, explore);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── Recent attempts helpers ─────────────────────────────────────────────

    /**
     * Loads and renders recent attempts for the given user.
     *
     * @param user the authenticated user.
     * @return a card VBox containing the attempts list.
     */
    private VBox buildAttemptsCard(final User user) {
        List<QuizResult> attempts;
        try {
            attempts = quizRepo.findByUser(user.getId());
        } catch (SQLException ex) {
            return cardWrap(new VBox(UIConstants.SPACING_LG,
                    bodyLabel("Could not load history: " + ex.getMessage())));
        }
        if (attempts.isEmpty()) {
            return cardWrap(new VBox(UIConstants.SPACING_LG,
                    bodyLabel("No quiz attempts yet. "
                            + "Finish a quiz to see your score here.")));
        }
        Map<String, String> titles = buildTitleIndex();
        VBox rows = new VBox(UIConstants.SPACING_MD);
        int shown = 0;
        for (QuizResult a : attempts) {
            if (shown >= RECENT_LIMIT) {
                break;
            }
            rows.getChildren().add(buildAttemptRow(a, titles));
            shown++;
        }
        Button viewAll = new Button("View all");
        viewAll.getStyleClass().add("btn-outline-sm");
        viewAll.setOnAction(e -> onViewAllResults.run());
        return cardWrap(new VBox(UIConstants.SPACING_LG, rows, viewAll));
    }

    private HBox buildAttemptRow(final QuizResult attempt,
            final Map<String, String> titles) {
        String title = titles.getOrDefault(
                attempt.getPresentationId(), attempt.getPresentationId());
        Label nameLabel = new Label(title);
        nameLabel.getStyleClass().add("text-dark");
        nameLabel.setStyle("-fx-font-size: " + UIConstants.FONT_BODY + "px;");
        nameLabel.setWrapText(true);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label score = new Label(
                attempt.getScore() + " / " + attempt.getMaxScore());
        score.getStyleClass().add("score-chip");
        score.setStyle("-fx-font-size: " + UIConstants.FONT_SMALL + "px;");

        HBox row = new HBox(UIConstants.SPACING_LG, nameLabel, score);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Map<String, String> buildTitleIndex() {
        Map<String, String> map = new HashMap<>();
        for (LearningContent lc : LearningContentLoader.loadAll()) {
            map.put(lc.getId(), lc.getTitle());
        }
        return map;
    }

    // ── Progress calculation ────────────────────────────────────────────────

    /**
     * Builds a map of module name → completion percentage for the given
     * subjects. Percentage = (distinct quizzes completed / total quizzes
     * in module) × 100. Returns 0 for modules with no quizzes.
     *
     * @param subjects the list of module names to compute progress for.
     * @return a map of module name to percentage (0–100).
     */
    private Map<String, Double> buildProgressMap(
            final List<String> subjects) {
        Map<String, Double> result = new HashMap<>();
        User user = Session.get();
        if (user == null || subjects.isEmpty()) {
            return result;
        }

        List<LearningContent> all = LearningContentLoader.loadAll();
        Map<String, List<String>> quizzesByModule = new HashMap<>();
        for (LearningContent lc : all) {
            if (lc.isQuiz()) {
                String mod = lc.getModule();
                quizzesByModule
                        .computeIfAbsent(mod, k -> new ArrayList<>())
                        .add(lc.getId());
            }
        }

        Set<String> completedIds = loadCompletedIds(user.getId());

        for (String subject : subjects) {
            List<String> quizIds = quizzesByModule.getOrDefault(
                    subject, new ArrayList<>());
            if (quizIds.isEmpty()) {
                result.put(subject, 0.0);
                continue;
            }
            long done = 0;
            for (String qid : quizIds) {
                if (completedIds.contains(qid)) {
                    done++;
                }
            }
            result.put(subject,
                    (done * UIConstants.PERCENT_MAX) / quizIds.size());
        }
        return result;
    }

    /**
     * Returns the set of quiz {@code presentation_id} values the user has
     * already submitted at least once.
     *
     * @param userId the user ID.
     * @return a set of completed presentation IDs, or empty if an error occurs.
     */
    private Set<String> loadCompletedIds(final int userId) {
        Set<String> ids = new LinkedHashSet<>();
        try {
            for (QuizResult r : quizRepo.findByUser(userId)) {
                ids.add(r.getPresentationId());
            }
        } catch (SQLException ex) {
            System.err.println(
                    "Failed to load quiz results: " + ex.getMessage());
        }
        return ids;
    }

    // ── Widget builders ─────────────────────────────────────────────────────

    /**
     * Wraps content in a card-styled VBox.
     *
     * @param content the VBox to wrap.
     * @return the styled card VBox.
     */
    private static VBox cardWrap(final VBox content) {
        content.setPadding(new Insets(UIConstants.PADDING_CONTENT_H));
        content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        content.getStyleClass().add("card");
        return content;
    }

    /**
     * Renders a horizontal progress bar filled to the given percentage.
     *
     * <p>The fill {@link StackPane} has both its preferred size and max size
     * fixed to {@code w} pixels wide so that the parent {@link StackPane}
     * layout does not stretch it to the full track width. It is then shifted
     * left with {@code translateX} so that its left edge aligns with the
     * track's left edge regardless of width.
     *
     * @param percent completion percentage 0–100. Zero produces an empty track.
     * @return the StackPane progress bar.
     */
    private StackPane progressBar(final double percent) {
        StackPane track = new StackPane();
        track.setPrefSize(UIConstants.PROGRESS_TRACK_WIDTH,
                UIConstants.PROGRESS_BAR_HEIGHT);
        track.setMaxWidth(UIConstants.PROGRESS_TRACK_WIDTH);
        track.getStyleClass().add("progress-track");

        if (percent <= 0) {
            return track;
        }

        double w = Math.max(UIConstants.SPACING_XS,
                UIConstants.PROGRESS_TRACK_WIDTH
                        * percent / UIConstants.PERCENT_MAX);
        StackPane fill = new StackPane();
        // setMaxSize is critical: without it StackPane stretches the fill
        // to the full track width, making every bar appear 100% filled.
        fill.setPrefSize(w, UIConstants.PROGRESS_BAR_HEIGHT);
        fill.setMaxSize(w, UIConstants.PROGRESS_BAR_HEIGHT);
        fill.getStyleClass().add("progress-fill");
        fill.setTranslateX(-(UIConstants.PROGRESS_TRACK_WIDTH - w) / 2);
        track.getChildren().add(fill);
        return track;
    }

    /**
     * Creates a section heading label.
     *
     * @param text the text.
     * @param size the font size in px.
     * @return the label.
     */
    private static Label sectionLabel(final String text, final int size) {
        Label l = new Label(text);
        l.getStyleClass().add("text-dark");
        l.setStyle("-fx-font-size: " + size + "px; -fx-font-weight: bold;");
        return l;
    }

    /**
     * Creates a standard body-text label.
     *
     * @param text the text.
     * @return the label.
     */
    private static Label bodyLabel(final String text) {
        Label l = new Label(text);
        l.getStyleClass().add("text-dark");
        l.setStyle("-fx-font-size: " + UIConstants.FONT_BODY + "px;");
        return l;
    }

    /**
     * Creates a small outline-to-fill action button.
     *
     * @param text the label.
     * @return the button.
     */
    private static Button outlineButton(final String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-outline");
        return b;
    }

    /**
     * Creates a small filled primary action button.
     *
     * @param text the label.
     * @return the button.
     */
    private Button smallPrimaryButton(final String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-primary");
        return b;
    }
}
