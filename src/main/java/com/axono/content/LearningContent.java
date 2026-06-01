package com.axono.content;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base type for everything the user can open from the browser.
 * Two concrete subtypes exist: {@link LearningResource} for normal
 * study materials and {@link Quiz} for assessable quizzes.
 *
 * <p>Subtypes are deliberately distinct so callers (e.g. the browser list
 * or {@code ContentPlayer}) can render them differently without
 * sprinkling {@code instanceof} checks across the UI layer.
 *
 * <p>Content that declares a {@code <mediaAssets>} block in its XML carries
 * a {@link MediaAssetRegistry} that maps relative file paths to the
 * filesystem URLs. The registry is attached immediately after construction by
 * {@link LearningContentParser} via
 * {@link #attachRegistry(MediaAssetRegistry)}; it cannot be changed
 * afterwards. The {@link com.axono.player.ContentPlayer} uses the registry
 * to resolve media {@code src} attributes at runtime.
 */
public abstract class LearningContent {

    /** Stable identifier derived from the file path or database id. */
    private final String id;

    /** Human-readable title (from metadata, with filename fallback). */
    private final String title;

    /** Module folder name (e.g. "2.Mathematics"). May be empty. */
    private final String module;

    /** Topic folder name (e.g. "Integration By Substitution"). May be empty. */
    private final String topic;

    /** Absolute path of the source XML file. */
    private final Path sourcePath;

    /** Parsed metadata block. */
    private final Metadata metadata;

    /** Parsed slides in document order. */
    private final List<Slide> slides;

    /**
     * Registry for resolving media {@code src} paths to filesystem URLs.
     * Set exactly once after construction by
     * {@link #attachRegistry(MediaAssetRegistry)}; {@code null} until then.
     */
    private MediaAssetRegistry registry;

    /**
     * Constructs a {@code LearningContent}.
     *
     * @param contentId    the stable id.
     * @param contentTitle the display title.
     * @param contentModule the module folder name.
     * @param contentTopic  the topic folder name.
     * @param contentSource the source XML file path.
     * @param contentMeta   the parsed metadata.
     * @param contentSlides the parsed slide list.
     */
    protected LearningContent(final String contentId,
            final String contentTitle,
            final String contentModule,
            final String contentTopic,
            final Path contentSource,
            final Metadata contentMeta,
            final List<Slide> contentSlides) {
        this.id = contentId;
        this.title = contentTitle;
        this.module = contentModule;
        this.topic = contentTopic;
        this.sourcePath = contentSource;
        this.metadata = contentMeta;
        this.slides = contentSlides == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(contentSlides);
        this.registry = null;
    }

    /**
     * Attaches a {@link MediaAssetRegistry} to this content. May only be
     * called once, immediately after construction (before this object is
     * handed to any caller). Subsequent calls throw
     * {@link IllegalStateException}.
     *
     * <p>This is package-private so only {@link LearningContentParser} (which
     * lives in the same package) can call it.</p>
     *
     * @param reg the registry to attach; {@code null} is silently ignored.
     * @throws IllegalStateException if a registry has already been attached.
     */
    final void attachRegistry(final MediaAssetRegistry reg) {
        if (reg == null) {
            return;
        }
        if (this.registry != null) {
            throw new IllegalStateException(
                    "MediaAssetRegistry already attached to " + id);
        }
        this.registry = reg;
    }

    /** @return the stable id of this learning content. */
    public final String getId() {
        return id;
    }

    /** @return the display title. */
    public final String getTitle() {
        return title;
    }

    /** @return the module folder name; never {@code null}. */
    public final String getModule() {
        return module;
    }

    /** @return the topic folder name; never {@code null}. */
    public final String getTopic() {
        return topic;
    }

    /** @return the source XML file path. */
    public final Path getSourcePath() {
        return sourcePath;
    }

    /** @return the parsed metadata block. */
    public final Metadata getMetadata() {
        return metadata;
    }

    /** @return an unmodifiable list of slides in document order. */
    public final List<Slide> getSlides() {
        return slides;
    }

    /**
     * @return the {@link MediaAssetRegistry} for resolving media paths, or
     *         {@code null} if this content has no registered assets.
     */
    public final MediaAssetRegistry getRegistry() {
        return registry;
    }

    /**
     * @return {@code true} when this content has a non-null, non-empty
     *         {@link MediaAssetRegistry} (i.e. it declares media assets in
     *         its XML).
     */
    public final boolean hasMediaAssets() {
        return registry != null && !registry.isEmpty();
    }

    /** @return {@code true} when this content is a quiz. */
    public abstract boolean isQuiz();
}
