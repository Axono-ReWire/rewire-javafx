package com.axono.content;

/**
 * A mathematical expression parsed from a {@code <math>} element,
 * stored as a LaTeX string for rendering.
 */
public final class MathItem implements MediaItem {

    /** The LaTeX source. Never {@code null}; may be empty. */
    private final String latex;

    /**
     * Constructs a {@code MathItem} with the given LaTeX source.
     *
     * @param latexSource the LaTeX string; {@code null} is normalised
     *                    to an empty string.
     */
    public MathItem(final String latexSource) {
        this.latex = latexSource == null ? "" : latexSource;
    }

    /** @return the LaTeX source string. */
    public String getLatex() {
        return latex;
    }
}
