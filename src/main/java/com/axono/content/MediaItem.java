package com.axono.content;

/**
 * Marker interface for any media element that can appear inside a
 * {@link Slide}'s {@code <content>} block. Implementations are immutable
 * value objects produced by {@link LearningContentParser}.
 *
 * <p>Concrete kinds: {@link TextItem}, {@link ImageItem},
 * {@link AudioItem}, {@link VideoItem}.
 */
public interface MediaItem {
}
