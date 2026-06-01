package com.axono.content;

/**
 * Broad classification of a {@link LearningContent} item for UI filtering.
 * Used by the browser tabs and module detail view to group content by type.
 */
public enum ContentType {

    /** Video-based lecture or lesson (contains at least one VideoItem). */
    VIDEO,

    /** Text/image/audio-based article or learning resource. */
    ARTICLE,

    /** Assessable quiz. */
    QUIZ;

    /**
     * Classifies a {@link LearningContent} into one of the three types.
     * Quizzes are identified first via {@link LearningContent#isQuiz()}.
     * Any non-quiz that contains at least one {@link VideoItem} on any slide
     * is classified as {@link #VIDEO}. Everything else is {@link #ARTICLE}.
     *
     * @param content the content to classify.
     * @return the matching {@code ContentType}.
     */
    public static ContentType of(final LearningContent content) {
        if (content.isQuiz()) {
            return QUIZ;
        }
        for (Slide slide : content.getSlides()) {
            for (MediaItem item : slide.getItems()) {
                if (item instanceof VideoItem) {
                    return VIDEO;
                }
            }
        }
        return ARTICLE;
    }
}
