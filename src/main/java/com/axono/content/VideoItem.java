package com.axono.content;

/**
 * A video element parsed from a {@code <video src="..."/>} tag.
 * The {@code src} attribute is optional in the source schema.
 */
public final class VideoItem implements MediaItem {

    /** Resource path or filename of the video clip. May be empty. */
    private final String src;

    /**
     * Constructs a {@code VideoItem}.
     *
     * @param videoSrc the {@code src} attribute value;
     *                 {@code null} is normalised to an empty string.
     */
    public VideoItem(final String videoSrc) {
        this.src = videoSrc == null ? "" : videoSrc;
    }

    /** @return the video source path. */
    public String getSrc() {
        return src;
    }

    /** @return {@code true} if {@link #getSrc()} is non-empty. */
    public boolean hasSrc() {
        return !src.isEmpty();
    }
}
