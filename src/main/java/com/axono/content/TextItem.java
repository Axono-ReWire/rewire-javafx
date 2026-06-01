package com.axono.content;

/**
 * A single block of slide text parsed from a {@code <text>} element.
 * Whitespace is preserved as it appears in the source XML.
 */
public final class TextItem implements MediaItem {

    /** The text content. Never {@code null}; may be empty. */
    private final String text;

    /**
     * Constructs a {@code TextItem} with the given text.
     *
     * @param textContent the text body; {@code null} is normalised
     *                    to an empty string.
     */
    public TextItem(final String textContent) {
        this.text = textContent == null ? "" : textContent;
    }

    /** @return the text content of this item. */
    public String getText() {
        return text;
    }
}
