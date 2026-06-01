package com.axono.content;

/**
 * Immutable metadata block parsed from {@code <learningContent><metadata>}.
 * All fields are optional in the source XML; missing values are stored
 * as empty strings rather than {@code null}.
 */
public final class Metadata {

    /** Presentation title, possibly empty. */
    private final String title;

    /** Author name, possibly empty. */
    private final String author;

    /** Version string, possibly empty. */
    private final String version;

    /** Created date string, possibly empty. */
    private final String created;

    /** Description, possibly empty. */
    private final String description;

    /**
     * Constructs a {@code Metadata} record.
     *
     * @param metaTitle       the presentation title.
     * @param metaAuthor      the author.
     * @param metaVersion     the version string.
     * @param metaCreated     the created-on date string.
     * @param metaDescription the description.
     */
    public Metadata(final String metaTitle,
            final String metaAuthor,
            final String metaVersion,
            final String metaCreated,
            final String metaDescription) {
        this.title = nullToEmpty(metaTitle);
        this.author = nullToEmpty(metaAuthor);
        this.version = nullToEmpty(metaVersion);
        this.created = nullToEmpty(metaCreated);
        this.description = nullToEmpty(metaDescription);
    }

    /** @return the title; never {@code null}. */
    public String getTitle() {
        return title;
    }

    /** @return the author; never {@code null}. */
    public String getAuthor() {
        return author;
    }

    /** @return the version string; never {@code null}. */
    public String getVersion() {
        return version;
    }

    /** @return the created-on string; never {@code null}. */
    public String getCreated() {
        return created;
    }

    /** @return the description; never {@code null}. */
    public String getDescription() {
        return description;
    }

    /**
     * Null-safe helper that converts {@code null} to an empty string.
     *
     * @param s the input.
     * @return {@code s}, or {@code ""} if {@code s} was {@code null}.
     */
    private static String nullToEmpty(final String s) {
        return s == null ? "" : s;
    }
}
