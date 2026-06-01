package com.axono.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MetadataTest {

    @Test
    void allNullsNormalizedToEmpty() {
        Metadata m = new Metadata(null, null, null, null, null);
        assertEquals("", m.getTitle());
        assertEquals("", m.getAuthor());
        assertEquals("", m.getVersion());
        assertEquals("", m.getCreated());
        assertEquals("", m.getDescription());
    }

    @Test
    void validValuesPreserved() {
        Metadata m = new Metadata("Title", "Author", "1.0",
                "2024-01-01", "A description.");
        assertEquals("Title", m.getTitle());
        assertEquals("Author", m.getAuthor());
        assertEquals("1.0", m.getVersion());
        assertEquals("2024-01-01", m.getCreated());
        assertEquals("A description.", m.getDescription());
    }

    @Test
    void getTitleNeverReturnsNull() {
        Metadata m = new Metadata(null, "a", "b", "c", "d");
        assertNotNull(m.getTitle());
    }

    @Test
    void getAuthorNeverReturnsNull() {
        Metadata m = new Metadata("a", null, "b", "c", "d");
        assertNotNull(m.getAuthor());
    }
}
