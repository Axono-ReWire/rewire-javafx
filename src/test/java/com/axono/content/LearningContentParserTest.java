package com.axono.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LearningContentParserTest {

    /** Content id without "quiz" — forces metadata-only detection. */
    private static final String NON_QUIZ_ID = "content-abc-123";

    /** Number of answer options per quiz question. */
    private static final int ANSWER_OPTION_COUNT = 4;

    /** Minimal XML with 2 slides: text+image on slide 1, audio on slide 2. */
    private static final String RESOURCE_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<learningContent>\n"
            + "  <metadata>"
            + "<title>Unit Test Resource</title></metadata>\n"
            + "  <slides>\n"
            + "    <slide id=\"s1\"><content>\n"
            + "      <text>Hello world</text>\n"
            + "      <image src=\"img.png\">"
            + "<alt>A picture</alt></image>\n"
            + "    </content></slide>\n"
            + "    <slide id=\"s2\"><content>\n"
            + "      <audio src=\"sound.mp3\"/>\n"
            + "    </content></slide>\n"
            + "  </slides>\n"
            + "</learningContent>\n";

    /** Minimal quiz XML with a single question slide. */
    private static final String QUIZ_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<learningContent>\n"
            + "  <metadata>"
            + "<title>Unit Test Quiz</title></metadata>\n"
            + "  <slides>\n"
            + "    <slide id=\"q1\"><content>"
            + "<text>What is 2+2?</text></content></slide>\n"
            + "  </slides>\n"
            + "</learningContent>\n";

    @Test
    void parseResourceXmlReturnsTwoSlides(
            @TempDir final Path dir) throws Exception {
        Path xmlFile = dir.resolve("unit_test.xml");
        Files.write(xmlFile,
                RESOURCE_XML.getBytes(StandardCharsets.UTF_8));

        LearningContent lc = LearningContentParser.parse(
                xmlFile, "TestModule", "TestTopic");

        assertEquals(2, lc.getSlides().size());
        assertEquals("Unit Test Resource", lc.getTitle());
        assertEquals("TestModule", lc.getModule());
        assertEquals("TestTopic", lc.getTopic());
        assertFalse(lc.isQuiz());
    }

    @Test
    void parseResourceXmlSlide1HasTextAndImage(
            @TempDir final Path dir) throws Exception {
        Path xmlFile = dir.resolve("unit_test.xml");
        Files.write(xmlFile,
                RESOURCE_XML.getBytes(StandardCharsets.UTF_8));

        LearningContent lc =
                LearningContentParser.parse(xmlFile, "", "");
        List<MediaItem> items = lc.getSlides().get(0).getItems();

        assertEquals(2, items.size());
        assertInstanceOf(TextItem.class, items.get(0));
        assertInstanceOf(ImageItem.class, items.get(1));
    }

    @Test
    void parseResourceXmlSlide2HasAudio(
            @TempDir final Path dir) throws Exception {
        Path xmlFile = dir.resolve("unit_test.xml");
        Files.write(xmlFile,
                RESOURCE_XML.getBytes(StandardCharsets.UTF_8));

        LearningContent lc =
                LearningContentParser.parse(xmlFile, "", "");
        List<MediaItem> items = lc.getSlides().get(1).getItems();

        assertEquals(1, items.size());
        assertInstanceOf(AudioItem.class, items.get(0));
    }

    @Test
    void parseFileNameContainsQuizReturnsQuiz(
            @TempDir final Path dir) throws Exception {
        Path xmlFile = dir.resolve("my_quiz_test.xml");
        Files.write(xmlFile,
                QUIZ_XML.getBytes(StandardCharsets.UTF_8));

        LearningContent lc =
                LearningContentParser.parse(xmlFile, "", "");

        assertTrue(lc.isQuiz());
        assertEquals(1, lc.getSlides().size());
    }

    @Test
    void parseInvalidXmlThrowsParseException(
            @TempDir final Path dir) throws IOException {
        Path xmlFile = dir.resolve("bad.xml");
        Files.write(xmlFile,
                "<unclosed>".getBytes(StandardCharsets.UTF_8));

        assertThrows(LearningContentParseException.class,
                () -> LearningContentParser.parse(xmlFile, "", ""));
    }

    // ── parseFromString: contentType quiz detection ──────────────────

    @Test
    void parseFromStringContentTypeQuizReturnsQuizInstance()
            throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<learningContent>\n"
                + "  <metadata>\n"
                + "    <title>My Quiz</title>\n"
                + "    <contentType>quiz</contentType>\n"
                + "  </metadata>\n"
                + "  <slides><slide id=\"s1\">"
                + "<content><text>Hi</text></content></slide></slides>\n"
                + "</learningContent>\n";

        LearningContent lc = LearningContentParser.parseFromString(
                xml, NON_QUIZ_ID, "Mod", "Topic", null);

        assertTrue(lc.isQuiz());
    }

    @Test
    void parseFromStringWithoutContentTypeReturnsLearningResource()
            throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<learningContent>\n"
                + "  <metadata><title>Plain Resource</title></metadata>\n"
                + "  <slides><slide id=\"s1\">"
                + "<content><text>Hi</text></content></slide></slides>\n"
                + "</learningContent>\n";

        LearningContent lc = LearningContentParser.parseFromString(
                xml, NON_QUIZ_ID, "Mod", "Topic", null);

        assertFalse(lc.isQuiz());
    }

    @Test
    void parseFromStringContentTypeCaseInsensitive()
            throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<learningContent>\n"
                + "  <metadata>\n"
                + "    <title>My Quiz</title>\n"
                + "    <contentType>QUIZ</contentType>\n"
                + "  </metadata>\n"
                + "  <slides><slide id=\"s1\">"
                + "<content><text>Hi</text></content></slide></slides>\n"
                + "</learningContent>\n";

        LearningContent lc = LearningContentParser.parseFromString(
                xml, NON_QUIZ_ID, "Mod", "Topic", null);

        assertTrue(lc.isQuiz());
    }

    @Test
    void parseFromStringQuizSlideHasQuestionData()
            throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<learningContent>\n"
                + "  <metadata>\n"
                + "    <title>Full Quiz</title>\n"
                + "    <contentType>quiz</contentType>\n"
                + "  </metadata>\n"
                + "  <slides>\n"
                + "    <slide id=\"q1\"><content>\n"
                + "      <question>\n"
                + "        <text>What is 2+2?</text>\n"
                + "        <answers>\n"
                + "          <answer>1</answer>\n"
                + "          <answer>2</answer>\n"
                + "          <answer>4</answer>\n"
                + "          <answer>8</answer>\n"
                + "        </answers>\n"
                + "        <correctAnswer>3</correctAnswer>\n"
                + "        <explanation>2+2=4</explanation>\n"
                + "      </question>\n"
                + "    </content></slide>\n"
                + "  </slides>\n"
                + "</learningContent>\n";

        LearningContent lc = LearningContentParser.parseFromString(
                xml, NON_QUIZ_ID, "Mod", "Topic", null);

        assertNotNull(lc.getSlides().get(0).getQuestionData());
    }

    @Test
    void parseFromStringQuestionDataHasCorrectQuestionText()
            throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<learningContent>\n"
                + "  <metadata>\n"
                + "    <contentType>quiz</contentType>\n"
                + "  </metadata>\n"
                + "  <slides>\n"
                + "    <slide id=\"q1\"><content>\n"
                + "      <question>\n"
                + "        <text>Capital of France?</text>\n"
                + "        <answers>\n"
                + "          <answer>London</answer>\n"
                + "          <answer>Paris</answer>\n"
                + "          <answer>Berlin</answer>\n"
                + "          <answer>Rome</answer>\n"
                + "        </answers>\n"
                + "        <correctAnswer>2</correctAnswer>\n"
                + "        <explanation>Paris is the capital.</explanation>\n"
                + "      </question>\n"
                + "    </content></slide>\n"
                + "  </slides>\n"
                + "</learningContent>\n";

        LearningContent lc = LearningContentParser.parseFromString(
                xml, NON_QUIZ_ID, "Mod", "Topic", null);
        QuestionData qd = lc.getSlides().get(0).getQuestionData();

        assertEquals("Capital of France?", qd.questionText());
    }

    @Test
    void parseFromStringQuestionDataHasFourOptions()
            throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<learningContent>\n"
                + "  <metadata><contentType>quiz</contentType></metadata>\n"
                + "  <slides>\n"
                + "    <slide id=\"q1\"><content>\n"
                + "      <question>\n"
                + "        <text>Q?</text>\n"
                + "        <answers>\n"
                + "          <answer>A</answer>\n"
                + "          <answer>B</answer>\n"
                + "          <answer>C</answer>\n"
                + "          <answer>D</answer>\n"
                + "        </answers>\n"
                + "        <correctAnswer>1</correctAnswer>\n"
                + "        <explanation>Exp</explanation>\n"
                + "      </question>\n"
                + "    </content></slide>\n"
                + "  </slides>\n"
                + "</learningContent>\n";

        LearningContent lc = LearningContentParser.parseFromString(
                xml, NON_QUIZ_ID, "Mod", "Topic", null);
        QuestionData qd = lc.getSlides().get(0).getQuestionData();

        assertEquals(ANSWER_OPTION_COUNT, qd.answerOptions().size());
    }

    @Test
    void parseFromStringQuestionDataCorrectAnswerIndexMatches()
            throws Exception {
        int expectedIndex = 2;
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<learningContent>\n"
                + "  <metadata><contentType>quiz</contentType></metadata>\n"
                + "  <slides>\n"
                + "    <slide id=\"q1\"><content>\n"
                + "      <question>\n"
                + "        <text>Q?</text>\n"
                + "        <answers>\n"
                + "          <answer>A</answer>\n"
                + "          <answer>B</answer>\n"
                + "          <answer>C</answer>\n"
                + "          <answer>D</answer>\n"
                + "        </answers>\n"
                + "        <correctAnswer>" + expectedIndex
                + "</correctAnswer>\n"
                + "        <explanation>Exp</explanation>\n"
                + "      </question>\n"
                + "    </content></slide>\n"
                + "  </slides>\n"
                + "</learningContent>\n";

        LearningContent lc = LearningContentParser.parseFromString(
                xml, NON_QUIZ_ID, "Mod", "Topic", null);
        QuestionData qd = lc.getSlides().get(0).getQuestionData();

        assertEquals(expectedIndex, qd.correctAnswerIndex());
    }

    @Test
    void parseFromStringNonQuizSlideHasNullQuestionData()
            throws Exception {
        LearningContent lc = LearningContentParser.parseFromString(
                RESOURCE_XML, NON_QUIZ_ID, "Mod", "Topic", null);

        assertNull(lc.getSlides().get(0).getQuestionData());
    }

    @Test
    void parseFromStringQuizTitleAndModuleArePreserved()
            throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<learningContent>\n"
                + "  <metadata>\n"
                + "    <title>Algebra Quiz</title>\n"
                + "    <contentType>quiz</contentType>\n"
                + "  </metadata>\n"
                + "  <slides><slide id=\"s1\">"
                + "<content><text>Hi</text></content></slide></slides>\n"
                + "</learningContent>\n";

        LearningContent lc = LearningContentParser.parseFromString(
                xml, NON_QUIZ_ID, "Mathematics", "Algebra", null);

        assertEquals("Algebra Quiz", lc.getTitle());
        assertEquals("Mathematics", lc.getModule());
    }
}
