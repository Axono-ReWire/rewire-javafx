package com.axono.content;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses a learning-content XML file into the immutable model types
 * ({@link LearningContent}, {@link Slide}, {@link MediaItem}).
 *
 * <p>The parser is intentionally tolerant: empty placeholder tags
 * (e.g. {@code <text/>}, {@code <image src=""/>}) are dropped so
 * the resulting object only contains meaningful items. Unknown elements
 * such as {@code <hr/>} are ignored rather than causing failure.
 *
 * <p>Quiz detection is by filename — any file whose name contains
 * {@code "quiz"} (case-insensitive) becomes a {@link Quiz}.
 */
public final class LearningContentParser {

    /** Tag name for text content items. */
    private static final String TAG_TEXT = "text";

    /** Tag name for image content items. */
    private static final String TAG_IMAGE = "image";

    /** Tag name for audio content items. */
    private static final String TAG_AUDIO = "audio";

    /** Tag name for video content items. */
    private static final String TAG_VIDEO = "video";

    /** Tag name for LaTeX math content items. */
    private static final String TAG_MATH = "math";

    /** Tag name for the alt-text child of an {@code <image>} element. */
    private static final String TAG_ALT = "alt";

    /** Tag name for the mediaAssets block. */
    private static final String TAG_MEDIA_ASSETS = "mediaAssets";

    /** Tag name for a single asset entry in the mediaAssets block. */
    private static final String TAG_ASSET = "asset";

    /** Constraint: quizzes must have exactly this many answer options. */
    private static final int ANSWER_OPTION_COUNT = 4;

    /** Constraint: correct answer index must be at least this value. */
    private static final int MIN_CORRECT_INDEX = 1;

    /** Constraint: correct answer index must not exceed this value. */
    private static final int MAX_CORRECT_INDEX = 4;

    /** Private constructor to prevent instantiation. */
    private LearningContentParser() {
    }

    /**
     * Parses the XML file at {@code xmlFile} into a {@link LearningContent}.
     *
     * @param xmlFile    the absolute path to the learning content XML file.
     * @param moduleName the module folder name (e.g. "2.Mathematics").
     * @param topicName  the topic folder name; may be empty.
     * @return a parsed {@link LearningResource} or {@link Quiz} depending
     *         on the filename.
     * @throws LearningContentParseException if the file cannot be read or
     *                                       contains invalid XML.
     */
    public static LearningContent parse(final Path xmlFile,
            final String moduleName,
            final String topicName)
            throws LearningContentParseException {
        try {
            DocumentBuilder db = createSecureDocumentBuilder();
            Document doc;
            try (var in = Files.newInputStream(xmlFile)) {
                doc = db.parse(in);
            }
            doc.getDocumentElement().normalize();
            return buildContent(doc, xmlFile, moduleName, topicName);
        } catch (ParserConfigurationException | SAXException
                | IOException ex) {
            throw new LearningContentParseException(
                    "Failed to parse " + xmlFile, ex);
        }
    }

    /**
     * Parses an XML entry from a JAR file into a {@link LearningContent} and
     * appends it to {@code out}.
     *
     * @param jar        the JAR file.
     * @param entryName  the entry path (e.g.,
     *                   "learning-content/2.Math/Topic/Title.xml").
     * @param moduleName the module folder name.
     * @param topicName  the topic folder name.
     * @param out        the accumulating result list.
     * @throws LearningContentParseException if the XML is invalid.
     * @throws IOException                   if the entry cannot be read.
     */
    public static void parseFromJar(final JarFile jar,
            final String entryName,
            final String moduleName,
            final String topicName,
            final List<LearningContent> out)
            throws LearningContentParseException, IOException {
        try {
            DocumentBuilder db = createSecureDocumentBuilder();
            Document doc;
            try (InputStream in = jar.getInputStream(
                    jar.getJarEntry(entryName))) {
                doc = db.parse(in);
            }
            doc.getDocumentElement().normalize();
            LearningContent content = buildContentFromJar(doc, entryName,
                    moduleName, topicName);
            out.add(content);
        } catch (ParserConfigurationException | SAXException
                | IOException ex) {
            throw new LearningContentParseException(
                    "Failed to parse " + entryName, ex);
        }
    }

    /**
     * Constructs a {@link LearningContent} from a parsed DOM document in a JAR
     * context.
     *
     * @param doc        the parsed XML document.
     * @param entryName  the entry name within the JAR.
     * @param moduleName the module folder name.
     * @param topicName  the topic folder name.
     * @return the constructed {@link LearningContent}.
     */
    private static LearningContent buildContentFromJar(final Document doc,
            final String entryName,
            final String moduleName,
            final String topicName) {
        String fileName = entryName.substring(entryName.lastIndexOf('/') + 1);
        return assembleContent(doc, entryName, fileName, null,
                moduleName, topicName, null);
    }

    /**
     * Constructs a {@link LearningContent} from a parsed DOM document.
     *
     * @param doc        the parsed XML document.
     * @param xmlFile    the source file path (used for id + classification).
     * @param moduleName the module folder name.
     * @param topicName  the topic folder name.
     * @return the constructed {@link LearningContent}.
     */
    private static LearningContent buildContent(final Document doc,
            final Path xmlFile,
            final String moduleName,
            final String topicName) {
        return assembleContent(doc,
                xmlFile.toAbsolutePath().toString(),
                xmlFile.getFileName().toString(),
                xmlFile,
                moduleName, topicName,
                xmlFile.getParent());
    }

    /**
     * Core assembly: parses metadata, slides, and assets from {@code doc},
     * determines the quiz/resource type, and attaches a media registry if
     * needed.
     *
     * @param doc        the parsed XML document.
     * @param id         stable identifier for the content.
     * @param fileName   filename used for quiz detection.
     * @param filePath   filesystem path, or {@code null} for JAR/DB content.
     * @param moduleName the module folder name.
     * @param topicName  the topic folder name.
     * @param baseDir    base directory for media resolution; may be null.
     * @return the assembled {@link LearningContent}.
     */
    private static LearningContent assembleContent(
            final Document doc,
            final String id,
            final String fileName,
            final Path filePath,
            final String moduleName,
            final String topicName,
            final Path baseDir) {
        Metadata metadata = parseMetadata(doc);
        List<Slide> slides = parseSlides(doc);
        List<MediaAsset> assets = parseMediaAssets(doc);
        String title = pickTitle(metadata, fileName);
        boolean isQuiz = fileName.toLowerCase().contains("quiz")
                || isQuizByMetadata(doc);

        LearningContent content;
        if (isQuiz) {
            content = new Quiz(id, title, moduleName, topicName,
                    filePath, metadata, slides);
        } else {
            content = new LearningResource(id, title, moduleName, topicName,
                    filePath, metadata, slides);
        }
        if (!assets.isEmpty()) {
            content.attachRegistry(new MediaAssetRegistry(assets, baseDir));
        }
        return content;
    }

    /**
     * Constructs a {@link LearningContent} from an XML string (e.g. stored
     * in the database) with an explicit base directory for media resolution.
     *
     * @param xmlContent the full XML string.
     * @param contentId  the stable id (usually a UUID from the database).
     * @param moduleName the module folder name.
     * @param topicName  the topic folder name.
     * @param baseDir    the base directory for resolving relative media paths.
     * @return the parsed {@link LearningContent}.
     * @throws LearningContentParseException if the XML is invalid.
     */
    public static LearningContent parseFromString(final String xmlContent,
            final String contentId,
            final String moduleName,
            final String topicName,
            final Path baseDir) throws LearningContentParseException {
        try {
            DocumentBuilder db = createSecureDocumentBuilder();
            Document doc = db.parse(
                    new org.xml.sax.InputSource(
                            new java.io.StringReader(xmlContent)));
            doc.getDocumentElement().normalize();
            return buildContentFromString(doc, contentId, moduleName,
                    topicName, baseDir);
        } catch (ParserConfigurationException | SAXException
                | IOException ex) {
            throw new LearningContentParseException(
                    "Failed to parse XML string for id=" + contentId, ex);
        }
    }

    /**
     * Builds a {@link LearningContent} from an already-parsed DOM document
     * and an explicit base directory.
     *
     * @param doc        the parsed document.
     * @param contentId  the stable id and filename for quiz detection.
     * @param moduleName the module folder name.
     * @param topicName  the topic folder name.
     * @param baseDir    the base directory for media resolution.
     * @return the constructed content.
     */
    private static LearningContent buildContentFromString(final Document doc,
            final String contentId,
            final String moduleName,
            final String topicName,
            final Path baseDir) {
        return assembleContent(doc, contentId, contentId, null,
                moduleName, topicName, baseDir);
    }

    /**
     * Picks the best display title: metadata title if non-empty, otherwise
     * the filename with its {@code .xml} extension stripped.
     *
     * @param metadata the parsed metadata.
     * @param fileName the source filename.
     * @return a non-empty display title.
     */
    private static String pickTitle(final Metadata metadata,
            final String fileName) {
        String metaTitle = metadata.getTitle().trim();
        if (!metaTitle.isEmpty()) {
            return metaTitle;
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    /**
     * Parses the optional {@code <mediaAssets>} block from the document root,
     * returning a list of {@link MediaAsset} objects. Returns an empty list
     * if the block is absent.
     *
     * @param doc the parsed XML document.
     * @return the list of declared media assets (never {@code null}).
     */
    private static List<MediaAsset> parseMediaAssets(final Document doc) {
        NodeList blocks = doc.getElementsByTagName(TAG_MEDIA_ASSETS);
        if (blocks.getLength() == 0) {
            return Collections.emptyList();
        }
        Element block = (Element) blocks.item(0);
        NodeList assetNodes = block.getElementsByTagName(TAG_ASSET);
        List<MediaAsset> result = new ArrayList<>();
        for (int i = 0; i < assetNodes.getLength(); i++) {
            Element el = (Element) assetNodes.item(i);
            result.add(new MediaAsset.Builder()
                    .id(el.getAttribute("id"))
                    .filename(el.getAttribute("filename"))
                    .relativePath(el.getAttribute("relativePath"))
                    .mediaType(el.getAttribute("mediaType"))
                    .mimeType(el.getAttribute("mimeType"))
                    .fileSize(parseLong(el.getAttribute("fileSize")))
                    .durationSeconds(parseInt(el.getAttribute("duration")))
                    .checksum(el.getAttribute("checksum"))
                    .build());
        }
        return result;
    }

    /**
     * Safely parses a long from a string, returning {@code 0} on failure.
     *
     * @param value the string to parse.
     * @return the parsed value or {@code 0}.
     */
    private static long parseLong(final String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Safely parses an int from a string, returning {@code 0} on failure.
     *
     * @param value the string to parse.
     * @return the parsed value or {@code 0}.
     */
    private static int parseInt(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parses the {@code <metadata>} child of the root element.
     *
     * @param doc the parsed XML document.
     * @return a {@link Metadata} value (all fields default to "").
     */
    private static Metadata parseMetadata(final Document doc) {
        NodeList list = doc.getElementsByTagName("metadata");
        if (list.getLength() == 0) {
            return new Metadata("", "", "", "", "");
        }
        Element el = (Element) list.item(0);
        return new Metadata(
                childText(el, "title"),
                childText(el, "author"),
                childText(el, "version"),
                childText(el, "created"),
                childText(el, "description"));
    }

    /**
     * Returns the trimmed text content of the first child element with the
     * given tag name, or {@code ""} if no such child exists.
     *
     * @param parent the parent element.
     * @param tag    the child tag name.
     * @return the trimmed text, or an empty string.
     */
    private static String childText(final Element parent, final String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) {
            return "";
        }
        String text = list.item(0).getTextContent();
        return text == null ? "" : text.trim();
    }

    /**
     * Parses every {@code <slide>} under {@code <slides>}.
     *
     * @param doc the parsed XML document.
     * @return the slide list in document order.
     */
    private static List<Slide> parseSlides(final Document doc) {
        List<Slide> result = new ArrayList<>();
        NodeList slideNodes = doc.getElementsByTagName("slide");
        for (int i = 0; i < slideNodes.getLength(); i++) {
            Element slideEl = (Element) slideNodes.item(i);
            String id = slideEl.getAttribute("id");
            List<MediaItem> mediaItems = parseContent(slideEl);
            QuestionData questionData = parseQuestionData(slideEl);
            result.add(new Slide(id, mediaItems, questionData));
        }
        return result;
    }

    /**
     * Walks the children of the first {@code <content>} block under
     * {@code slideEl} in document order, producing {@link MediaItem}s.
     *
     * @param slideEl the {@code <slide>} element.
     * @return the ordered media items found inside {@code <content>}.
     */
    private static List<MediaItem> parseContent(final Element slideEl) {
        List<MediaItem> items = new ArrayList<>();
        NodeList contents = slideEl.getElementsByTagName("content");
        if (contents.getLength() == 0) {
            return items;
        }
        NodeList kids = contents.item(0).getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            MediaItem item = toMediaItem((Element) n);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Maps a content child element to a {@link MediaItem}, dropping empty
     * placeholders and unknown tags.
     *
     * @param el the child element under {@code <content>}.
     * @return a {@link MediaItem}, or {@code null} to skip the element.
     */
    private static MediaItem toMediaItem(final Element el) {
        String name = el.getTagName();
        if (TAG_TEXT.equals(name)) {
            String txt = el.getTextContent();
            if (txt == null || txt.trim().isEmpty()) {
                return null;
            }
            return new TextItem(txt);
        }
        if (TAG_IMAGE.equals(name)) {
            return toImageItem(el);
        }
        if (TAG_AUDIO.equals(name)) {
            String src = el.getAttribute("src");
            return src.isEmpty() ? null : new AudioItem(src);
        }
        if (TAG_VIDEO.equals(name)) {
            String src = el.getAttribute("src");
            return src.isEmpty() ? null : new VideoItem(src);
        }
        if (TAG_MATH.equals(name)) {
            String latex = el.getTextContent();
            if (latex == null || latex.trim().isEmpty()) {
                return null;
            }
            return new MathItem(latex.trim());
        }
        return null;
    }

    /**
     * Builds an {@link ImageItem} from an {@code <image>} element, reading
     * its {@code src} attribute and nested {@code <alt>} child. Returns
     * {@code null} when both are empty.
     *
     * @param el the {@code <image>} element.
     * @return the parsed item or {@code null}.
     */
    private static ImageItem toImageItem(final Element el) {
        String src = el.getAttribute("src");
        String alt = childText(el, TAG_ALT);
        if (src.isEmpty() && alt.isEmpty()) {
            return null;
        }
        return new ImageItem(src, alt);
    }

    /**
     * Extracts structured question data from a slide element if present.
     *
     * @param slideEl the slide element.
     * @return a {@link QuestionData}, or {@code null} if no question found.
     */
    private static QuestionData parseQuestionData(final Element slideEl) {
        // Look for <question> element within <content>
        NodeList contents = slideEl.getElementsByTagName("content");
        if (contents.getLength() == 0) {
            return null;
        }

        Element contentEl = (Element) contents.item(0);
        NodeList questionNodes = contentEl.getElementsByTagName("question");
        if (questionNodes.getLength() == 0) {
            return null;
        }

        Element questionEl = (Element) questionNodes.item(0);

        // Extract question text
        String questionText = childText(questionEl, "text");
        if (questionText.isEmpty()) {
            return null;
        }

        // Extract answer options
        List<String> answerOptions = new ArrayList<>();
        Element answersEl = getFirstChild(questionEl, "answers");
        if (answersEl != null) {
            NodeList answerNodes = answersEl.getElementsByTagName("answer");
            for (int i = 0; i < answerNodes.getLength(); i++) {
                Element answerEl = (Element) answerNodes.item(i);
                String answerText = answerEl.getTextContent();
                if (answerText != null && !answerText.trim().isEmpty()) {
                    answerOptions.add(answerText.trim());
                }
            }
        }

        // Extract correct answer index
        String correctAnswerStr = childText(questionEl, "correctAnswer");
        int correctAnswerIndex = 0;
        try {
            correctAnswerIndex = Integer.parseInt(correctAnswerStr);
        } catch (NumberFormatException e) {
            return null; // Invalid format
        }

        // Extract explanation
        String explanation = childText(questionEl, "explanation");

        // Validate and create QuestionData
        if (answerOptions.size() == ANSWER_OPTION_COUNT
                && correctAnswerIndex >= MIN_CORRECT_INDEX
                && correctAnswerIndex <= MAX_CORRECT_INDEX) {
            try {
                return new QuestionData(questionText, answerOptions,
                        correctAnswerIndex, explanation);
            } catch (IllegalArgumentException e) {
                return null; // Failed validation
            }
        }

        return null;
    }

    /**
     * Gets the first child element with the given tag name.
     *
     * @param parent the parent element.
     * @param tag    the tag name.
     * @return the first matching child element, or null if not
     *         found.
     */
    private static Element getFirstChild(
            final Element parent, final String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) {
            return null;
        }
        return (Element) list.item(0);
    }

    /**
     * Returns {@code true} if the document's {@code <metadata>} block contains
     * a {@code <contentType>} tag with value {@code quiz}, enabling quiz
     * detection for user-created content stored in the database.
     *
     * @param doc the parsed XML document.
     * @return {@code true} if the content type is marked as quiz.
     */
    private static boolean isQuizByMetadata(final Document doc) {
        NodeList list = doc.getElementsByTagName("metadata");
        if (list.getLength() == 0) {
            return false;
        }
        Element metaEl = (Element) list.item(0);
        return "quiz".equalsIgnoreCase(childText(metaEl, "contentType"));
    }

    /**
     * Creates a secure {@link DocumentBuilder} with external-entity support
     * disabled (XXE-safe).
     *
     * @return a hardened {@link DocumentBuilder}.
     * @throws ParserConfigurationException if the builder cannot be created.
     */
    private static DocumentBuilder createSecureDocumentBuilder()
            throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        String featDoctype = "http://apache.org/xml/features/"
                + "disallow-doctype-decl";
        dbf.setFeature(featDoctype, true);
        String featGen = "http://xml.org/sax/features/"
                + "external-general-entities";
        dbf.setFeature(featGen, false);
        String featParam = "http://xml.org/sax/features/"
                + "external-parameter-entities";
        dbf.setFeature(featParam, false);
        String featDtd = "http://apache.org/xml/features/"
                + "nonvalidating/load-external-dtd";
        dbf.setFeature(featDtd, false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        return dbf.newDocumentBuilder();
    }
}
