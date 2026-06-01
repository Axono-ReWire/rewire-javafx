package com.axono.content;

/**
 * An image element with {@code src} and {@code alt} attributes,
 * parsed from an image tag. Both are optional; empty strings are kept.
 */
public final class ImageItem implements MediaItem {

    /** Resource path or filename of the image. May be empty. */
    private final String src;

    /** Accessible alternative text for the image. May be empty. */
    private final String alt;

    /**
     * Constructs an {@code ImageItem}.
     *
     * @param imageSrc the {@code src} attribute value;
     *                 {@code null} is normalised to an empty string.
     * @param imageAlt the {@code <alt>} text;
     *                 {@code null} is normalised to an empty string.
     */
    public ImageItem(final String imageSrc, final String imageAlt) {
        this.src = imageSrc == null ? "" : imageSrc;
        this.alt = imageAlt == null ? "" : imageAlt;
    }

    /** @return the image source path. */
    public String getSrc() {
        return src;
    }

    /** @return the alternative text. */
    public String getAlt() {
        return alt;
    }

    /** @return {@code true} if {@link #getSrc()} is non-empty. */
    public boolean hasSrc() {
        return !src.isEmpty();
    }
}
