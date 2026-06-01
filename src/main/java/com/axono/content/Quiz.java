package com.axono.content;

import java.nio.file.Path;
import java.util.List;

/**
 * An assessable quiz. Detected by filename pattern (any file whose name
 * contains "quiz", case-insensitive). The quiz module interprets the slide
 * contents as multiple-choice questions and persists results to the
 * {@code quiz_results} table.
 */
public final class Quiz extends LearningContent {

    /**
     * Constructs a {@code Quiz}. Parameters mirror
     * {@link LearningContent#LearningContent(String, String, String,
     * String, Path, Metadata, List)}.
     *
     * @param contentId     the stable id.
     * @param contentTitle  the display title.
     * @param contentModule the module folder name.
     * @param contentTopic  the topic folder name.
     * @param contentSource the source XML file path.
     * @param contentMeta   the parsed metadata.
     * @param contentSlides the parsed slide list.
     */
    public Quiz(final String contentId,
            final String contentTitle,
            final String contentModule,
            final String contentTopic,
            final Path contentSource,
            final Metadata contentMeta,
            final List<Slide> contentSlides) {
        super(contentId, contentTitle, contentModule, contentTopic,
                contentSource, contentMeta, contentSlides);
    }

    @Override
    public boolean isQuiz() {
        return true;
    }
}
