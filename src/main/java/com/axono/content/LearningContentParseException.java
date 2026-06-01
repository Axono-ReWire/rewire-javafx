package com.axono.content;

/**
 * Checked exception thrown when a learning content XML file cannot be parsed.
 * The cause typically wraps an underlying {@link org.xml.sax.SAXException},
 * {@link java.io.IOException}, or
 * {@link javax.xml.parsers.ParserConfigurationException}.
 */
public class LearningContentParseException extends Exception {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a {@code LearningContentParseException} with a message.
     *
     * @param message human-readable description of the failure.
     */
    public LearningContentParseException(final String message) {
        super(message);
    }

    /**
     * Constructs a {@code LearningContentParseException} with a message and
     * underlying cause.
     *
     * @param message human-readable description of the failure.
     * @param cause   the underlying cause.
     */
    public LearningContentParseException(final String message,
            final Throwable cause) {
        super(message, cause);
    }
}
