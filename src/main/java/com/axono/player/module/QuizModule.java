package com.axono.player.module;

import com.axono.content.AudioItem;
import com.axono.content.ImageItem;
import com.axono.ui.UIConstants;
import com.axono.content.MathItem;
import com.axono.content.MediaItem;
import com.axono.content.QuestionData;
import com.axono.content.Slide;
import com.axono.content.TextItem;
import com.axono.content.VideoItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 * Renders a single quiz question parsed from a slide. The slide's content
 * is shown verbatim using the same text/image renderers as resource
 * slides, and a numbered set of radio buttons (one per detected choice)
 * is appended so the user can select an answer.
 *
 * <p>Choice detection is conservative: any {@link TextItem} whose trimmed
 * text matches a numbered-prefix pattern (e.g. "1.", "2)") is treated as
 * the start of a new choice. If no choices are detected, a sensible
 * default of four radio buttons is added so the question can still be
 * answered.
 *
 * <p>Selection changes invoke the {@code onAnswerSelected} callback with
 * the 1-based choice index, so the host {@code ContentPlayer} can
 * track responses for scoring on submit.
 */
public final class QuizModule implements MediaModule {

    /** Default number of choices when none can be auto-detected. */
    private static final int DEFAULT_CHOICE_COUNT = 4;

    /** Maximum image height (px) on quiz slides (prevents dominance). */
    private static final double QUIZ_IMAGE_MAX_HEIGHT = 840.0;

    /** The composite view returned by {@link #getView()}. */
    private final VBox view;

    /** Embedded text module — delegated for text rendering. */
    private final TextModule textModule;

    /** Embedded image module — delegated for image rendering. */
    private final ImageSetModule imageModule;

    /** Embedded math module — delegated for LaTeX rendering. */
    private final MathModule mathModule;

    /** Embedded audio module — delegated for audio playback. */
    private final AudioModule audioModule;

    /** Embedded video module — delegated for video playback. */
    private final VideoModule videoModule;

    /**
     * Constructs a {@code QuizModule} for a single slide.
     *
     * @param slide              the quiz slide.
     * @param onAnswerSelected   callback invoked with the user's 1-based
     *                           choice on each selection change.
     */
    public QuizModule(final Slide slide,
            final IntConsumer onAnswerSelected) {
        List<TextItem> texts = new ArrayList<>();
        List<ImageItem> images = new ArrayList<>();
        List<MathItem> maths = new ArrayList<>();
        List<AudioItem> audios = new ArrayList<>();
        List<VideoItem> videos = new ArrayList<>();
        for (MediaItem item : slide.getItems()) {
            if (item instanceof TextItem) {
                texts.add((TextItem) item);
            } else if (item instanceof ImageItem) {
                images.add((ImageItem) item);
            } else if (item instanceof MathItem) {
                maths.add((MathItem) item);
            } else if (item instanceof AudioItem) {
                audios.add((AudioItem) item);
            } else if (item instanceof VideoItem) {
                videos.add((VideoItem) item);
            }
        }
        this.textModule = new TextModule(texts);
        this.imageModule = new ImageSetModule(images, QUIZ_IMAGE_MAX_HEIGHT);
        this.mathModule = new MathModule(maths);
        this.audioModule = new AudioModule(audios);
        this.videoModule = new VideoModule(videos);

        QuestionData qData = slide.getQuestionData();
        VBox choicesView = qData != null
                ? buildChoicesFromData(qData, onAnswerSelected)
                : buildChoices(countChoices(texts), onAnswerSelected);

        this.view = new VBox(UIConstants.SPACING_XL,
                textModule.getView(),
                mathModule.getView(),
                imageModule.getView(),
                audioModule.getView(),
                videoModule.getView(),
                choicesView);
    }

    /**
     * Counts the number of items whose text matches a numbered-choice
     * prefix ("1.", "2)" etc.).
     *
     * @param texts the slide's text items.
     * @return the detected choice count, or
     *         {@link #DEFAULT_CHOICE_COUNT} when none are found.
     */
    private static int countChoices(final List<TextItem> texts) {
        int detected = 0;
        for (TextItem t : texts) {
            String trimmed = t.getText().trim();
            if (trimmed.matches("^\\d+[.)].*")) {
                detected++;
            }
        }
        return detected > 0 ? detected : DEFAULT_CHOICE_COUNT;
    }

    /**
     * Builds the question text label, radio-button group, and answer header
     * from structured question data.
     *
     * @param qData             the question data containing question text and
     *                          answer options.
     * @param onAnswerSelected  selection callback.
     * @return the question + choice block.
     */
    private static VBox buildChoicesFromData(final QuestionData qData,
            final IntConsumer onAnswerSelected) {
        Label questionLabel = new Label(qData.questionText());
        questionLabel.setWrapText(true);
        questionLabel.getStyleClass().add("text-dark");
        questionLabel.setStyle("-fx-font-size: 15px;");

        Label header = new Label("Your answer:");
        header.getStyleClass().addAll("text-dark", "bold");
        header.setStyle("-fx-font-size: 13px;");

        ToggleGroup group = new ToggleGroup();
        VBox box = new VBox(UIConstants.SPACING_SM,
                questionLabel, header);
        for (int i = 0; i < qData.answerOptions().size(); i++) {
            String option = qData.answerOptions().get(i);
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(group);
            final int choice = i + 1; // 1-based index
            rb.setOnAction(e -> onAnswerSelected.accept(choice));
            box.getChildren().add(rb);
        }
        return box;
    }

    /**
     * Builds the radio-button group used to collect the user's answer.
     *
     * @param choiceCount       the number of radio buttons to render.
     * @param onAnswerSelected  selection callback.
     * @return the choice block.
     */
    private static VBox buildChoices(final int choiceCount,
            final IntConsumer onAnswerSelected) {
        Label header = new Label("Your answer:");
        header.getStyleClass().addAll("text-dark", "bold");
        header.setStyle("-fx-font-size: 13px;");

        ToggleGroup group = new ToggleGroup();
        VBox box = new VBox(UIConstants.SPACING_SM, header);
        for (int i = 1; i <= choiceCount; i++) {
            RadioButton rb = new RadioButton("Option " + i);
            rb.setToggleGroup(group);
            final int choice = i;
            rb.setOnAction(e -> onAnswerSelected.accept(choice));
            box.getChildren().add(rb);
        }
        return box;
    }

    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void onEnter() {
        textModule.onEnter();
        mathModule.onEnter();
        imageModule.onEnter();
        audioModule.onEnter();
        videoModule.onEnter();
    }

    @Override
    public void onExit() {
        textModule.onExit();
        mathModule.onExit();
        imageModule.onExit();
        audioModule.onExit();
        videoModule.onExit();
    }
}
