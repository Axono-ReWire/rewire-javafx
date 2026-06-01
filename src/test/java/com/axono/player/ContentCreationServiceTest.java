package com.axono.player;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContentCreationServiceTest {

    /** Test question text. */
    private static final String QUESTION_TEXT = "What is 2 + 2?";

    /** Test answer options. */
    private static final List<String> OPTIONS = List.of(
            "One", "Two", "Four", "Eight");

    /** Correct answer index (1-based). */
    private static final int CORRECT_INDEX = 3;

    /** Test explanation. */
    private static final String EXPLANATION = "Addition result.";

    /** Test content title. */
    private static final String TITLE = "My Lesson";

    /** Test content author. */
    private static final String AUTHOR = "Tester";

    /** Test content description. */
    private static final String DESC = "A description.";

    // ── QuestionSpec ──────────────────────────────────────────────

    @Test
    void questionSpecAccessorsReturnConstructedValues() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, EXPLANATION);
        assertEquals(QUESTION_TEXT, qs.questionText());
        assertEquals(CORRECT_INDEX, qs.correctAnswerIndex());
        assertEquals(EXPLANATION, qs.explanation());
        assertEquals(OPTIONS.size(), qs.options().size());
    }

    @Test
    void questionSpecOptionsMatchInputOrder() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, EXPLANATION);
        assertEquals(OPTIONS, qs.options());
    }

    @Test
    void questionSpecNullTextDefaultsToEmpty() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                null, OPTIONS, CORRECT_INDEX, EXPLANATION);
        assertEquals("", qs.questionText());
    }

    @Test
    void questionSpecNullExplanationDefaultsToEmpty() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, null);
        assertEquals("", qs.explanation());
    }

    @Test
    void questionSpecNullOptionsDefaultsToEmptyList() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, null, CORRECT_INDEX, EXPLANATION);
        assertNotNull(qs.options());
        assertTrue(qs.options().isEmpty());
    }

    @Test
    void questionSpecOptionsListIsImmutable() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, new ArrayList<>(OPTIONS),
                CORRECT_INDEX, EXPLANATION);
        assertThrows(UnsupportedOperationException.class,
                () -> qs.options().add("Extra"));
    }

    @Test
    void questionSpecConstructionDoesNotThrow() {
        assertDoesNotThrow(() -> new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, EXPLANATION));
    }

    // ── SlideSpec ─────────────────────────────────────────────────

    @Test
    void slideSpecSingleArgConstructorHasNullQuestionSpec() {
        var itemSpec = new ContentCreationService.ItemSpec("TEXT", "hello");
        ContentCreationService.SlideSpec ss =
                new ContentCreationService.SlideSpec(List.of(itemSpec));
        assertNull(ss.questionSpec());
    }

    @Test
    void slideSpecTwoArgConstructorStoresQuestionSpec() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, EXPLANATION);
        ContentCreationService.SlideSpec ss =
                new ContentCreationService.SlideSpec(List.of(), qs);
        assertEquals(qs, ss.questionSpec());
    }

    @Test
    void slideSpecTwoArgConstructorWithNullQuestionSpecIsNull() {
        ContentCreationService.SlideSpec ss =
                new ContentCreationService.SlideSpec(List.of(), null);
        assertNull(ss.questionSpec());
    }

    @Test
    void slideSpecItemsListIsImmutable() {
        List<ContentCreationService.ItemSpec> items = new ArrayList<>();
        items.add(new ContentCreationService.ItemSpec("TEXT", "hello"));
        ContentCreationService.SlideSpec ss =
                new ContentCreationService.SlideSpec(items);
        assertThrows(UnsupportedOperationException.class,
                () -> ss.items().add(
                        new ContentCreationService.ItemSpec("TEXT", "more")));
    }

    @Test
    void slideSpecNullItemsListDefaultsToEmpty() {
        ContentCreationService.SlideSpec ss =
                new ContentCreationService.SlideSpec(null);
        assertNotNull(ss.items());
        assertTrue(ss.items().isEmpty());
    }

    // ── buildXml: quiz flag ───────────────────────────────────────

    @Test
    void buildXmlIsQuizFalseOmitsContentTypeTag() {
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of()));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, false);
        assertFalse(xml.contains("<contentType>"));
    }

    @Test
    void buildXmlIsQuizTrueIncludesContentTypeQuiz() {
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of()));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        assertTrue(xml.contains("<contentType>quiz</contentType>"));
    }

    @Test
    void buildXmlContentTypeAppearsInsideMetadata() {
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of()));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        int metadataEnd = xml.indexOf("</metadata>");
        int contentTypePos = xml.indexOf("<contentType>");
        assertTrue(contentTypePos < metadataEnd,
                "<contentType> must appear before </metadata>");
    }

    // ── buildXml: question block ──────────────────────────────────

    @Test
    void buildXmlSlideWithQuestionSpecEmitsQuestionBlock() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, EXPLANATION);
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of(), qs));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        assertTrue(xml.contains("<question>"));
        assertTrue(xml.contains("</question>"));
    }

    @Test
    void buildXmlQuestionBlockContainsQuestionText() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, EXPLANATION);
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of(), qs));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        assertTrue(xml.contains("<text>" + QUESTION_TEXT + "</text>"));
    }

    @Test
    void buildXmlQuestionBlockContainsAllFourOptions() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, EXPLANATION);
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of(), qs));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        for (String opt : OPTIONS) {
            assertTrue(xml.contains("<answer>" + opt + "</answer>"),
                    "Missing option: " + opt);
        }
    }

    @Test
    void buildXmlQuestionBlockContainsCorrectAnswerIndex() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, EXPLANATION);
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of(), qs));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        assertTrue(xml.contains(
                "<correctAnswer>" + CORRECT_INDEX + "</correctAnswer>"));
    }

    @Test
    void buildXmlQuestionBlockContainsExplanation() {
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                QUESTION_TEXT, OPTIONS, CORRECT_INDEX, EXPLANATION);
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of(), qs));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        assertTrue(xml.contains(
                "<explanation>" + EXPLANATION + "</explanation>"));
    }

    @Test
    void buildXmlSlideWithNullQuestionSpecOmitsQuestionBlock() {
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of()));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        assertFalse(xml.contains("<question>"));
    }

    @Test
    void buildXmlQuestionTextAmpersandIsXmlEscaped() {
        String rawText = "A & B?";
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                rawText, OPTIONS, CORRECT_INDEX, EXPLANATION);
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of(), qs));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        assertTrue(xml.contains("A &amp; B?"));
        assertFalse(xml.contains("A & B?"));
    }

    @Test
    void buildXmlQuestionTextAngleBracketsAreXmlEscaped() {
        String rawText = "x < y > z?";
        ContentCreationService.QuestionSpec qs =
                new ContentCreationService.QuestionSpec(
                rawText, OPTIONS, CORRECT_INDEX, EXPLANATION);
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of(), qs));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        assertTrue(xml.contains("x &lt; y &gt; z?"));
    }

    @Test
    void buildXmlMultipleSlidesEachGetTheirOwnQuestion() {
        ContentCreationService.QuestionSpec qs1 =
                new ContentCreationService.QuestionSpec(
                "Question one?", OPTIONS, 1, "Exp1");
        ContentCreationService.QuestionSpec qs2 =
                new ContentCreationService.QuestionSpec(
                "Question two?", OPTIONS, 2, "Exp2");
        List<ContentCreationService.SlideSpec> slides = List.of(
                new ContentCreationService.SlideSpec(List.of(), qs1),
                new ContentCreationService.SlideSpec(List.of(), qs2));
        String xml = ContentCreationService.buildXml(
                TITLE, AUTHOR, DESC, slides, null, true);
        assertTrue(xml.contains("Question one?"));
        assertTrue(xml.contains("Question two?"));
        assertTrue(xml.contains("slide-1"));
        assertTrue(xml.contains("slide-2"));
    }
}
