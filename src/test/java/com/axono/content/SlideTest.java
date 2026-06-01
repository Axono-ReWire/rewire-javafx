package com.axono.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SlideTest {

    @Test
    void nullIdNormalizedToEmpty() {
        Slide slide = new Slide(null, List.of());
        assertEquals("", slide.getId());
    }

    @Test
    void nullItemsReturnsEmptyList() {
        Slide slide = new Slide("1", null);
        assertNotNull(slide.getItems());
        assertTrue(slide.getItems().isEmpty());
    }

    @Test
    void itemsAreImmutable() {
        Slide slide = new Slide("1",
                new ArrayList<>(List.of(new TextItem("hello"))));
        assertThrows(UnsupportedOperationException.class, () ->
                slide.getItems().add(new TextItem("extra")));
    }

    @Test
    void questionDataDefaultsToNull() {
        Slide slide = new Slide("1", List.of());
        assertNull(slide.getQuestionData());
    }

    @Test
    void questionDataReturnsSetValue() {
        QuestionData qd = new QuestionData("Q?",
                List.of("A", "B", "C", "D"), 1, "Explanation.");
        Slide slide = new Slide("1", List.of(), qd);
        assertEquals(qd, slide.getQuestionData());
    }
}
