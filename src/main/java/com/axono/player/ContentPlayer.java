package com.axono.player;

import com.axono.auth.Session;
import com.axono.auth.User;
import com.axono.ui.UIConstants;
import com.axono.player.module.AudioModule;
import com.axono.player.module.ImageSetModule;
import com.axono.player.module.MathModule;
import com.axono.player.module.MediaModule;
import com.axono.player.module.QuizModule;
import com.axono.player.module.TextModule;
import com.axono.player.module.VideoModule;
import com.axono.content.AudioItem;
import com.axono.content.ImageItem;
import com.axono.content.MathItem;
import com.axono.content.MediaAssetRegistry;
import com.axono.content.MediaItem;
import com.axono.content.LearningContent;
import com.axono.content.QuestionData;
import com.axono.content.Slide;
import com.axono.content.TextItem;
import com.axono.content.VideoItem;
import java.util.function.Consumer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Main content playback view. Wraps a {@link LearningContent} and walks
 * the user through its slides, composing per-slide {@link MediaModule}
 * instances and ensuring outgoing modules' {@code onExit()} hooks fire
 * before the next slide is built — that is the contract that guarantees
 * audio and video actually stop when navigation happens.
 *
 * <p>For {@link com.axono.content.Quiz} sources, the Next button on the
 * final slide is replaced by a Finish button that computes the score and
 * writes a {@link QuizResult} row via {@link QuizResultRepository}.
 */
public final class ContentPlayer extends BorderPane {

    /** Source learning content being played. */
    private final LearningContent learningContent;

    /** Callback invoked when the user clicks the close button. */
    private final Runnable onClose;

    /**
     * Optional callback invoked after a quiz attempt is successfully saved.
     * Receives the persisted {@link QuizResult}. When {@code null} a plain
     * completion message is shown in the slide canvas instead.
     */
    private final Consumer<QuizResult> onQuizFinished;

    /** Repository for persisting quiz attempts. */
    private final QuizResultRepository quizRepo = new QuizResultRepository();

    /** Modules currently mounted on the active slide. */
    private final List<MediaModule> activeModules = new ArrayList<>();

    /** Per-slide selected answer (1-based) for quiz mode; 0 when blank. */
    private final int[] quizAnswers;

    /** Index of the slide currently shown. */
    private int currentSlide;

    /** Title label, updated on each slide change. */
    private Label counterLabel;

    /** Slide canvas hosting the active modules' views. */
    private ScrollPane slideCanvas;

    /** "Previous" button. */
    private Button prevBtn;

    /** "Next" or "Finish" button depending on slide + quiz state. */
    private Button nextBtn;

    /** Cached footer node so it can be shown/hidden in updateFooter(). */
    private HBox footer;

    /**
     * Constructs a {@code ContentPlayer} without a quiz-finished callback.
     * On quiz completion a plain score message is shown in the canvas.
     *
     * @param sourceContent the learning content to play.
     * @param closeHandler  callback invoked when the user closes the player.
     */
    public ContentPlayer(final LearningContent sourceContent,
            final Runnable closeHandler) {
        this(sourceContent, closeHandler, null);
    }

    /**
     * Constructs a {@code ContentPlayer} with an optional quiz-finished
     * callback that is invoked (instead of the plain completion message)
     * once a quiz attempt has been persisted.
     *
     * @param sourceContent      the learning content to play.
     * @param closeHandler       callback invoked when the user closes the
     *                           player; typically restores the browser.
     * @param quizFinishedHandler called with the saved {@link QuizResult}
     *                            after a quiz completes; may be {@code null}.
     */
    public ContentPlayer(final LearningContent sourceContent,
            final Runnable closeHandler,
            final Consumer<QuizResult> quizFinishedHandler) {
        this.learningContent = sourceContent;
        this.onClose = closeHandler;
        this.onQuizFinished = quizFinishedHandler;
        this.quizAnswers = new int[sourceContent.getSlides().size()];
        buildShell();
        showSlide(0);

        // Track resource access for "Recently Accessed" dashboard feature
        trackResourceAccess();
    }

    /**
     * Records that the current user has accessed this resource.
     * Used to show recently opened resources in the dashboard.
     */
    private void trackResourceAccess() {
        if (!Session.isAuthenticated()) {
            return;
        }
        try {
            User user = Session.get();
            if (user != null) {
                ResourceAccessRepository.trackAccess(
                        user.getId(),
                        learningContent.getId(),
                        learningContent.getModule());
            }
        } catch (SQLException ex) {
            System.err.println("Could not track resource access: "
                    + ex.getMessage());
        }
    }

    /** Builds the static parts of the layout: header, canvas, footer. */
    private void buildShell() {
        getStyleClass().add("bg-app");
        setTop(buildHeader());
        setCenter(buildCanvas());
        footer = buildFooter();
        setBottom(footer);
    }

    /**
     * Builds the top header showing the title, counter and close button.
     *
     * @return the header node.
     */
    private HBox buildHeader() {
        Label title = new Label(learningContent.getTitle());
        title.getStyleClass().add("text-dark");
        title.setStyle("-fx-font-size: " + UIConstants.FONT_SECTION + "px;"
                + " -fx-font-weight: bold;");

        counterLabel = new Label();
        counterLabel.getStyleClass().add("text-muted");
        counterLabel.setStyle("-fx-font-size: "
                + UIConstants.FONT_CAPTION + "px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("Close");
        close.getStyleClass().add("btn-close");
        close.setOnAction(e -> {
            stopActiveModules();
            if (onClose != null) {
                onClose.run();
            }
        });

        HBox header = new HBox(UIConstants.SPACING_LG,
                title, counterLabel, spacer, close);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H,
                UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H));
        header.getStyleClass().add("panel-header");
        return header;
    }

    /**
     * Builds the central scroll pane that hosts each slide's modules.
     *
     * @return the configured slide canvas.
     */
    private ScrollPane buildCanvas() {
        slideCanvas = new ScrollPane();
        slideCanvas.setFitToWidth(true);
        slideCanvas.setBorder(Border.EMPTY);
        return slideCanvas;
    }

    /**
     * Builds the bottom footer with Prev / Next (or Finish) buttons.
     *
     * @return the configured footer bar.
     */
    private HBox buildFooter() {
        prevBtn = new Button("← Prev");
        nextBtn = new Button("Next →");
        prevBtn.getStyleClass().add("btn-nav");
        nextBtn.getStyleClass().add("btn-nav");

        prevBtn.setOnAction(e -> showSlide(currentSlide - 1));
        nextBtn.setOnAction(e -> handleNextOrFinish());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footerBox = new HBox(UIConstants.SPACING_LG,
                prevBtn, spacer, nextBtn);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H,
                UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H));
        footerBox.getStyleClass().add("panel-footer");
        return footerBox;
    }

    /**
     * Dispatches the Next button: either advances a slide or, on the
     * final slide of a quiz, finalises the attempt.
     */
    private void handleNextOrFinish() {
        boolean isLast =
                currentSlide >= learningContent.getSlides().size() - 1;
        if (learningContent.isQuiz() && isLast) {
            finishQuiz();
        } else {
            showSlide(currentSlide + 1);
        }
    }

    /**
     * Calls {@link MediaModule#onExit()} on every currently mounted
     * module, then clears the active list so a fresh set can be built.
     */
    private void stopActiveModules() {
        for (MediaModule m : activeModules) {
            try {
                m.onExit();
            } catch (RuntimeException ex) {
                System.err.println("Module onExit failed: "
                        + ex.getMessage());
            }
        }
        activeModules.clear();
    }

    /**
     * Switches the canvas to render the slide at {@code index}. Out-of-
     * range indices are clamped silently.
     *
     * @param index the zero-based slide index.
     */
    private void showSlide(final int index) {
        int total = learningContent.getSlides().size();
        if (total == 0) {
            slideCanvas.setContent(emptyState());
            updateFooter();
            return;
        }
        int clamped = Math.max(0, Math.min(index, total - 1));
        stopActiveModules();
        currentSlide = clamped;
        Slide slide = learningContent.getSlides().get(clamped);
        VBox slideView = renderSlide(slide);
        slideCanvas.setContent(wrap(slideView));
        for (MediaModule m : activeModules) {
            m.onEnter();
        }
        counterLabel.setText("Slide " + (clamped + 1) + " / " + total);
        updateFooter();
    }

    /** Updates the Next button label and enables/disables nav buttons. */
    private void updateFooter() {
        int total = learningContent.getSlides().size();
        boolean isLast = currentSlide >= total - 1;
        boolean singleNonQuiz = !learningContent.isQuiz() && total == 1;
        setBottom(singleNonQuiz ? null : footer);
        prevBtn.setDisable(currentSlide <= 0);
        nextBtn.setDisable(total == 0);
        nextBtn.setText(learningContent.isQuiz() && isLast
                ? "Finish ✓" : "Next →");
    }

    /**
     * Builds the JavaFX subtree for a single slide. Side-effect: appends
     * the constructed modules to {@link #activeModules}.
     *
     * @param slide the slide to render.
     * @return the constructed view.
     */
    private VBox renderSlide(final Slide slide) {
        VBox content = new VBox(UIConstants.SPACING_XL);
        content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        if (learningContent.isQuiz()) {
            final int idx = currentSlide;
            QuizModule qm = new QuizModule(resolveSlideMedia(slide),
                    choice -> quizAnswers[idx] = choice);
            activeModules.add(qm);
            content.getChildren().add(qm.getView());
            return content;
        }
        addTypedModules(slide, content);
        return content;
    }

    /**
     * Buckets {@link MediaItem}s by type and constructs one module per
     * non-empty bucket, appending each module's view to {@code content}.
     * Media items that reference filesystem-relative paths are pre-resolved
     * using the content's {@link MediaAssetRegistry} before being passed
     * to the rendering modules.
     *
     * @param slide   the slide to inspect.
     * @param content the container being populated.
     */
    private void addTypedModules(final Slide slide, final VBox content) {
        MediaAssetRegistry reg = learningContent.getRegistry();
        List<TextItem> texts = new ArrayList<>();
        List<ImageItem> images = new ArrayList<>();
        List<AudioItem> audios = new ArrayList<>();
        List<VideoItem> videos = new ArrayList<>();
        List<MathItem> maths = new ArrayList<>();
        for (MediaItem item : slide.getItems()) {
            if (item instanceof TextItem) {
                texts.add((TextItem) item);
            } else if (item instanceof ImageItem) {
                images.add(resolveImage((ImageItem) item, reg));
            } else if (item instanceof AudioItem) {
                audios.add(resolveAudio((AudioItem) item, reg));
            } else if (item instanceof VideoItem) {
                videos.add(resolveVideo((VideoItem) item, reg));
            } else if (item instanceof MathItem) {
                maths.add((MathItem) item);
            }
        }
        if (!texts.isEmpty()) {
            mount(new TextModule(texts), content);
        }
        if (!maths.isEmpty()) {
            mount(new MathModule(maths), content);
        }
        if (!images.isEmpty()) {
            mount(new ImageSetModule(images), content);
        }
        if (!audios.isEmpty()) {
            mount(new AudioModule(audios), content);
        }
        if (!videos.isEmpty()) {
            mount(new VideoModule(videos), content);
        }
    }

    /**
     * Returns a copy of {@code slide} with all media items pre-resolved to
     * absolute URLs via the content's registry. Quiz slides bypass
     * {@link #addTypedModules} and need the same path resolution applied
     * before their items are handed to the sub-modules inside
     * {@link QuizModule}.
     *
     * @param slide the original slide.
     * @return a slide with resolved src values, or the original when no
     *         registry is attached.
     */
    private Slide resolveSlideMedia(final Slide slide) {
        MediaAssetRegistry reg = learningContent.getRegistry();
        if (reg == null) {
            return slide;
        }
        List<MediaItem> resolved = new ArrayList<>();
        for (MediaItem item : slide.getItems()) {
            if (item instanceof ImageItem) {
                resolved.add(resolveImage((ImageItem) item, reg));
            } else if (item instanceof AudioItem) {
                resolved.add(resolveAudio((AudioItem) item, reg));
            } else if (item instanceof VideoItem) {
                resolved.add(resolveVideo((VideoItem) item, reg));
            } else {
                resolved.add(item);
            }
        }
        return new Slide(slide.getId(), resolved, slide.getQuestionData());
    }

    /**
     * Returns a pre-resolved {@link ImageItem} whose {@code src} is an
     * absolute URL when the registry can resolve it, otherwise the original.
     *
     * @param item the image item to resolve.
     * @param reg  the registry; may be {@code null}.
     * @return a resolved item or the original.
     */
    private static ImageItem resolveImage(final ImageItem item,
            final MediaAssetRegistry reg) {
        if (reg == null || !item.hasSrc()) {
            return item;
        }
        String resolved = reg.resolveUrl(item.getSrc());
        if (resolved != null && !resolved.equals(item.getSrc())) {
            return new ImageItem(resolved, item.getAlt());
        }
        return item;
    }

    /**
     * Returns a pre-resolved {@link AudioItem} whose {@code src} is an
     * absolute URL when the registry can resolve it, otherwise the original.
     *
     * @param item the audio item to resolve.
     * @param reg  the registry; may be {@code null}.
     * @return a resolved item or the original.
     */
    private static AudioItem resolveAudio(final AudioItem item,
            final MediaAssetRegistry reg) {
        if (reg == null || !item.hasSrc()) {
            return item;
        }
        String resolved = reg.resolveUrl(item.getSrc());
        if (resolved != null && !resolved.equals(item.getSrc())) {
            return new AudioItem(resolved);
        }
        return item;
    }

    /**
     * Returns a pre-resolved {@link VideoItem} whose {@code src} is an
     * absolute URL when the registry can resolve it, otherwise the original.
     *
     * @param item the video item to resolve.
     * @param reg  the registry; may be {@code null}.
     * @return a resolved item or the original.
     */
    private static VideoItem resolveVideo(final VideoItem item,
            final MediaAssetRegistry reg) {
        if (reg == null || !item.hasSrc()) {
            return item;
        }
        String resolved = reg.resolveUrl(item.getSrc());
        if (resolved != null && !resolved.equals(item.getSrc())) {
            return new VideoItem(resolved);
        }
        return item;
    }

    /**
     * Adds a freshly built module to the active list and the container.
     *
     * @param module    the module to mount.
     * @param container the parent container.
     */
    private void mount(final MediaModule module, final VBox container) {
        activeModules.add(module);
        container.getChildren().add(module.getView());
    }

    /**
     * Wraps a slide's root in a centred container so wide canvases don't
     * cause horizontal scroll on shorter slides.
     *
     * @param slideView the slide view.
     * @return the wrapping container.
     */
    private VBox wrap(final VBox slideView) {
        VBox outer = new VBox(slideView);
        outer.setAlignment(Pos.CENTER);
        outer.setPadding(new Insets(UIConstants.SPACING_4XL));
        outer.getStyleClass().add("bg-app");
        outer.minHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> slideCanvas.getViewportBounds().getHeight(),
                        slideCanvas.viewportBoundsProperty()));
        return outer;
    }

    /**
     * Builds the empty-state shown when the learning content has no slides.
     *
     * @return the empty-state VBox.
     */
    private VBox emptyState() {
        Label l = new Label("This learning content has no slides.");
        l.getStyleClass().add("text-muted");
        l.setStyle("-fx-font-size: " + UIConstants.FONT_BODY + "px;");
        VBox outer = new VBox(l);
        outer.setAlignment(Pos.CENTER);
        outer.setPadding(new Insets(UIConstants.PADDING_NAV_H));
        return outer;
    }

    /**
     * Persists a quiz attempt for the active user. Compares user answers
     * against correct answers from QuestionData to calculate the actual score.
     */
    private void finishQuiz() {
        stopActiveModules();
        User user = Session.get();
        if (user == null) {
            renderCompletionMessage("Quiz finished. (No user signed in.)");
            return;
        }

        // Calculate score by comparing user answers against correct answers
        int score = 0;
        List<Slide> slides = learningContent.getSlides();
        StringBuilder answersJson = new StringBuilder("[");

        for (int i = 0; i < quizAnswers.length && i < slides.size(); i++) {
            int userAnswer = quizAnswers[i];
            QuestionData qData = slides.get(i).getQuestionData();

            // Add user's answer to JSON array
            if (i > 0) {
                answersJson.append(",");
            }
            answersJson.append(userAnswer);

            // Compare against correct answer if question data is available
            if (qData != null && userAnswer > 0) {
                if (userAnswer == qData.correctAnswerIndex()) {
                    score++;
                }
            }
        }
        answersJson.append("]");

        int max = quizAnswers.length;
        QuizResult result = new QuizResult(user.getId(),
                learningContent.getId(), score, max, answersJson.toString());
        try {
            quizRepo.save(result);
            if (onQuizFinished != null) {
                onQuizFinished.accept(result);
            } else {
                renderCompletionMessage("Quiz finished. Score: "
                        + score + " / " + max + ". Result saved.");
            }
        } catch (SQLException ex) {
            renderCompletionMessage(
                    "Quiz finished, but the result could not be saved: "
                            + ex.getMessage());
        }
        nextBtn.setDisable(true);
    }

    /**
     * Replaces the canvas with a centred message — used after submitting
     * a quiz so the user gets feedback without leaving the player.
     *
     * @param message the message text.
     */
    private void renderCompletionMessage(final String message) {
        Label l = new Label(message);
        l.getStyleClass().add("text-dark");
        l.setStyle("-fx-font-size: " + UIConstants.FONT_SECTION + "px;"
                + " -fx-font-weight: bold;");
        VBox outer = new VBox(l);
        outer.setAlignment(Pos.CENTER);
        outer.setPadding(new Insets(UIConstants.PADDING_NAV_H));
        slideCanvas.setContent(outer);
    }
}
