package com.axono.content;

import java.nio.file.Path;
import java.util.List;

/**
 * A study-material learning resource: read-only slides intended for learning.
 * Distinct from {@link Quiz} so the UI can render the two categories
 * differently without {@code instanceof} checks.
 */
public final class LearningResource extends LearningContent {

    /**
     * Constructs a {@code LearningResource}. Parameters mirror
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
    public LearningResource(final String contentId,
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
        return false;
    }
}
