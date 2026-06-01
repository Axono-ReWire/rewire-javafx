package com.axono.content;

/**
 * An audio element parsed from a {@code <audio src="..."/>} tag.
 * The {@code src} attribute is optional in the source schema.
 */
public final class AudioItem implements MediaItem {

    /** Resource path or filename of the audio clip. May be empty. */
    private final String src;

    /**
     * Constructs an {@code AudioItem}.
     *
     * @param audioSrc the {@code src} attribute value;
     *                 {@code null} is normalised to an empty string.
     */
    public AudioItem(final String audioSrc) {
        this.src = audioSrc == null ? "" : audioSrc;
    }

    /** @return the audio source path. */
    public String getSrc() {
        return src;
    }

    /** @return {@code true} if {@link #getSrc()} is non-empty. */
    public boolean hasSrc() {
        return !src.isEmpty();
    }
}
