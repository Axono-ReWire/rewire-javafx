package com.axono.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MediaItemTest {

    @Test
    void textItemNullContentNormalizedToEmpty() {
        assertEquals("", new TextItem(null).getText());
    }

    @Test
    void textItemGetTextReturnsValue() {
        assertEquals("Hello world", new TextItem("Hello world").getText());
    }

    @Test
    void imageItemNullSrcNormalizedToEmpty() {
        assertEquals("", new ImageItem(null, "alt").getSrc());
    }

    @Test
    void imageItemNullAltNormalizedToEmpty() {
        assertEquals("", new ImageItem("img.png", null).getAlt());
    }

    @Test
    void imageItemHasSrcWithValueReturnsTrue() {
        assertTrue(new ImageItem("img.png", "alt").hasSrc());
    }

    @Test
    void imageItemHasSrcWithEmptyReturnsFalse() {
        assertFalse(new ImageItem("", "alt").hasSrc());
    }

    @Test
    void imageItemHasSrcWithNullReturnsFalse() {
        assertFalse(new ImageItem(null, "alt").hasSrc());
    }

    @Test
    void audioItemNullSrcNormalizedToEmpty() {
        assertEquals("", new AudioItem(null).getSrc());
    }

    @Test
    void audioItemHasSrcWithValueReturnsTrue() {
        assertTrue(new AudioItem("clip.mp3").hasSrc());
    }

    @Test
    void audioItemHasSrcWithEmptyReturnsFalse() {
        assertFalse(new AudioItem("").hasSrc());
    }

    @Test
    void videoItemNullSrcNormalizedToEmpty() {
        assertEquals("", new VideoItem(null).getSrc());
    }

    @Test
    void videoItemHasSrcWithValueReturnsTrue() {
        assertTrue(new VideoItem("video.mp4").hasSrc());
    }

    @Test
    void videoItemHasSrcWithEmptyReturnsFalse() {
        assertFalse(new VideoItem("").hasSrc());
    }

    @Test
    void mathItemNullLatexNormalizedToEmpty() {
        assertEquals("", new MathItem(null).getLatex());
    }

    @Test
    void mathItemGetLatexReturnsValue() {
        assertEquals("x^2", new MathItem("x^2").getLatex());
    }
}
