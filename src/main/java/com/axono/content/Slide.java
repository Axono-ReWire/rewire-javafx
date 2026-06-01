package com.axono.content;

import java.util.Collections;
import java.util.List;

/**
 * A single slide within a {@link LearningContent}, holding an ordered list of
 * {@link MediaItem}s parsed from the slide's {@code <content>} block.
 */
public final class Slide {

    /** The {@code id} attribute of the slide. May be empty. */
    private final String id;

    /** Ordered media items rendered when the slide is shown. */
    private final List<MediaItem> items;

    /** Optional: structured question data if this slide is a quiz question. */
    private final QuestionData questionData;

    /**
     * Constructs a {@code Slide}.
     *
     * @param slideId    the slide id; {@code null} is normalised to "".
     * @param slideItems the parsed media items;
     *                   stored as an unmodifiable copy.
     */
    public Slide(final String slideId, final List<MediaItem> slideItems) {
        this(slideId, slideItems, null);
    }

    /**
     * Constructs a {@code Slide} with optional question data.
     *
     * @param slideId      the slide id; {@code null} is normalised to "".
     * @param slideItems   the parsed media items;
     *                     stored as an unmodifiable copy.
     * @param qData        optional question data for quiz slides.
     */
    public Slide(final String slideId, final List<MediaItem> slideItems,
            final QuestionData qData) {
        this.id = slideId == null ? "" : slideId;
        this.items = slideItems == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(slideItems);
        this.questionData = qData;
    }

    /** @return the slide id. */
    public String getId() {
        return id;
    }

    /** @return an unmodifiable list of media items on the slide. */
    public List<MediaItem> getItems() {
        return items;
    }

    /** @return the optional question data, or null if not a quiz question. */
    public QuestionData getQuestionData() {
        return questionData;
    }
}
