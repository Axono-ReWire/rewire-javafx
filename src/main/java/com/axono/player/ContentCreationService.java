package com.axono.player;

import com.axono.content.MediaAsset;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Service for creating, managing, and persisting user-authored learning
 * content.
 *
 * <p>User-created content is stored in two complementary places:
 * <ul>
 *   <li><b>Filesystem</b>: media files (images, video, audio) are copied into
 *       a per-content directory under the application's user-data folder
 *       ({@code ~/.axono/user-content/[module]/[topic]/[id]/media/[type]/}).
 *   </li>
 *   <li><b>Database</b>: the full XML text (metadata + slides + mediaAssets)
 *       is stored in the {@code user_content} table together with the
 *       {@code base_directory} path so the app knows where to find the files.
 *   </li>
 * </ul>
 *
 * <p>Callers obtain the base content directory via
 * {@link #getBaseDir(String, String, String)} before adding media files and
 * calling {@link #save}.
 */
public final class ContentCreationService {

    /** Application user-data directory name. */
    private static final String APP_DIR = ".axono";

    /** Sub-directory for user-created content. */
    private static final String CONTENT_DIR = "user-content";

    /** Sub-directory for media files within a content folder. */
    private static final String MEDIA_DIR = "media";

    /** Known image extensions (lower-case). */
    private static final HashSet<String> IMAGE_EXTS =
            new HashSet<>(java.util.Arrays.asList(
                    ".png", ".jpg", ".jpeg", ".gif",
                    ".heic", ".bmp", ".webp"));

    /** Known video extensions (lower-case). */
    private static final HashSet<String> VIDEO_EXTS =
            new HashSet<>(java.util.Arrays.asList(
                    ".mp4", ".webm", ".mov", ".mkv",
                    ".avi", ".m4v", ".mpg", ".mpeg", ".ts"));

    /** Known audio extensions (lower-case). */
    private static final HashSet<String> AUDIO_EXTS =
            new HashSet<>(java.util.Arrays.asList(
                    ".mp3", ".wav", ".ogg", ".flac",
                    ".aac", ".m4a", ".opus", ".weba"));

    /** Buffer size for file hashing operations (in bytes). */
    private static final int HASH_BUFFER_SIZE = 8192;

    /** Private constructor — static service. */
    private ContentCreationService() {
    }

    // ── Directory helpers ────────────────────────────────────────────────────

    /**
     * Returns the root user-data directory ({@code ~/.axono/user-content}).
     *
     * @return the absolute path; the directory is created if absent.
     */
    public static Path getUserContentRoot() {
        Path dir = Paths.get(System.getProperty("user.home"),
                APP_DIR, CONTENT_DIR);
        try {
            Files.createDirectories(dir);
        } catch (IOException ex) {
            System.err.println("Could not create user-content root: "
                    + ex.getMessage());
        }
        return dir;
    }

    /**
     * Returns and creates the base directory for a specific piece of content.
     *
     * @param module    the module name (may contain path-unsafe chars — they
     *                  are sanitised).
     * @param topic     the topic name; may be empty.
     * @param contentId the unique content id (UUID).
     * @return the absolute base directory path.
     */
    public static Path getBaseDir(final String module,
            final String topic,
            final String contentId) {
        Path root = getUserContentRoot();
        String safeModule = sanitise(module.isEmpty() ? "General" : module);
        String safeTopic = sanitise(topic.isEmpty() ? "_" : topic);
        String safeId = sanitise(contentId);
        Path dir = root.resolve(safeModule).resolve(safeTopic).resolve(safeId);
        try {
            Files.createDirectories(dir);
        } catch (IOException ex) {
            System.err.println("Could not create content base dir: "
                    + ex.getMessage());
        }
        return dir;
    }

    /**
     * Generates a fresh content id (UUID).
     *
     * @return a non-null UUID string.
     */
    public static String newId() {
        return UUID.randomUUID().toString();
    }

    // ── Media file management ────────────────────────────────────────────────

    /**
     * Copies a media file into the content's media sub-directory and returns
     * a {@link MediaAsset} describing it. The file is deduplicated by SHA-256
     * checksum — if an identical file already exists it is not copied again.
     *
     * @param sourceFile the file to copy (must exist and be readable).
     * @param baseDir    the content's base directory (from
     *                   {@link #getBaseDir(String, String, String)}).
     * @param assetId    the unique id for this asset within the content.
     * @return a {@link MediaAsset} with the relative path populated.
     * @throws IOException if the file cannot be read or copied.
     */
    public static MediaAsset addMediaFile(final File sourceFile,
            final Path baseDir,
            final String assetId) throws IOException {
        String name = sourceFile.getName();
        String ext = extension(name);
        String type = detectType(ext);

        String subDir = MEDIA_DIR + "/" + type.toLowerCase() + "s";
        Path targetDir = baseDir.resolve(subDir);
        Files.createDirectories(targetDir);

        String checksum = sha256(sourceFile);
        Path targetFile = targetDir.resolve(name);

        // Deduplicate: don't overwrite if same checksum
        if (!Files.exists(targetFile)
                || !sha256(targetFile.toFile()).equals(checksum)) {
            Files.copy(sourceFile.toPath(), targetFile,
                    StandardCopyOption.REPLACE_EXISTING);
        }

        long size = Files.size(targetFile);
        // relativePath is relative to baseDir using forward slashes
        String relativePath = subDir + "/" + name;
        String mimeType = guessMime(ext, type);

        return new MediaAsset.Builder()
                .id(assetId)
                .filename(name)
                .relativePath(relativePath)
                .mediaType(type)
                .mimeType(mimeType)
                .fileSize(size)
                .checksum(checksum)
                .build();
    }

    // ── XML serialisation ────────────────────────────────────────────────────

    /**
     * Serialises the content specification to a well-formed XML string
     * suitable for storage in the {@code user_content} database table.
     *
     * @param title       the display title.
     * @param author      the author name (may be empty).
     * @param description the description (may be empty).
     * @param slides      the slide specifications in order.
     * @param assets      the registered media assets.
     * @param isQuiz      {@code true} to mark this content as a quiz.
     * @return the XML string.
     */
    public static String buildXml(final String title,
            final String author,
            final String description,
            final List<SlideSpec> slides,
            final List<MediaAsset> assets,
            final boolean isQuiz) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<learningContent>\n");
        sb.append("  <metadata>\n");
        sb.append("    <title>").append(escape(title)).append("</title>\n");
        sb.append("    <author>").append(escape(author))
                .append("</author>\n");
        sb.append("    <version>1.0</version>\n");
        sb.append("    <created>").append(LocalDate.now())
                .append("</created>\n");
        sb.append("    <description>")
                .append(escape(description)).append("</description>\n");
        if (isQuiz) {
            sb.append("    <contentType>quiz</contentType>\n");
        }
        sb.append("  </metadata>\n");
        appendMediaAssets(sb, assets);
        sb.append("  <slides>\n");
        if (slides != null) {
            int idx = 1;
            for (SlideSpec slide : slides) {
                sb.append("    <slide id=\"slide-").append(idx++)
                        .append("\">\n");
                sb.append("      <content>\n");
                for (ItemSpec item : slide.items()) {
                    appendItem(sb, item);
                }
                if (slide.questionSpec() != null) {
                    appendQuestion(sb, slide.questionSpec());
                }
                sb.append("      </content>\n");
                sb.append("    </slide>\n");
            }
        }
        sb.append("  </slides>\n");
        sb.append("</learningContent>\n");
        return sb.toString();
    }

    /**
     * Appends the {@code <mediaAssets>} block to the XML builder if there are
     * any assets; does nothing when the list is null or empty.
     *
     * @param sb     the builder.
     * @param assets the media asset list; may be {@code null}.
     */
    private static void appendMediaAssets(final StringBuilder sb,
            final List<MediaAsset> assets) {
        if (assets == null || assets.isEmpty()) {
            return;
        }
        sb.append("  <mediaAssets>\n");
        for (MediaAsset a : assets) {
            sb.append("    <asset")
                    .append(" id=\"").append(escape(a.getId())).append("\"")
                    .append(" filename=\"")
                    .append(escape(a.getFilename())).append("\"")
                    .append(" relativePath=\"")
                    .append(escape(a.getRelativePath())).append("\"")
                    .append(" mediaType=\"")
                    .append(escape(a.getMediaType())).append("\"")
                    .append(" mimeType=\"")
                    .append(escape(a.getMimeType())).append("\"")
                    .append(" fileSize=\"").append(a.getFileSize())
                    .append("\"")
                    .append(" duration=\"").append(a.getDurationSeconds())
                    .append("\"")
                    .append(" checksum=\"")
                    .append(escape(a.getChecksum())).append("\"")
                    .append("/>\n");
        }
        sb.append("  </mediaAssets>\n");
    }

    /**
     * Appends a {@code <question>} block to the XML builder for a quiz slide.
     *
     * @param sb the builder.
     * @param q  the question specification.
     */
    private static void appendQuestion(final StringBuilder sb,
            final QuestionSpec q) {
        sb.append("        <question>\n");
        sb.append("          <text>").append(escape(q.questionText()))
                .append("</text>\n");
        sb.append("          <answers>\n");
        for (String opt : q.options()) {
            sb.append("            <answer>").append(escape(opt))
                    .append("</answer>\n");
        }
        sb.append("          </answers>\n");
        sb.append("          <correctAnswer>").append(q.correctAnswerIndex())
                .append("</correctAnswer>\n");
        sb.append("          <explanation>")
                .append(escape(q.explanation())).append("</explanation>\n");
        sb.append("        </question>\n");
    }

    /**
     * Appends a single media item element to the XML builder.
     *
     * @param sb   the builder.
     * @param item the item specification.
     */
    private static void appendItem(final StringBuilder sb,
            final ItemSpec item) {
        switch (item.type()) {
            case "TEXT":
                sb.append("        <text>")
                        .append(escape(item.value()))
                        .append("</text>\n");
                break;
            case "IMAGE":
                sb.append("        <image src=\"")
                        .append(escape(item.value())).append("\">");
                if (item.alt() != null && !item.alt().isEmpty()) {
                    sb.append("<alt>").append(escape(item.alt()))
                            .append("</alt>");
                }
                sb.append("</image>\n");
                break;
            case "AUDIO":
                sb.append("        <audio src=\"")
                        .append(escape(item.value())).append("\"/>\n");
                break;
            case "VIDEO":
                sb.append("        <video src=\"")
                        .append(escape(item.value())).append("\"/>\n");
                break;
            case "MATH":
                sb.append("        <math>")
                        .append(escape(item.value()))
                        .append("</math>\n");
                break;
            default:
                break;
        }
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    /**
     * Specification for saving user-created content. Bundles all fields to
     * avoid exceeding the project's seven-parameter limit on methods.
     */
    public static final class ContentSpec {

        /** Unique content identifier (UUID). */
        private final String specId;

        /** Display title. */
        private final String specTitle;

        /** Author name (may be empty). */
        private final String specAuthor;

        /** Description text (may be empty). */
        private final String specDescription;

        /** Module folder name. */
        private final String specModuleName;

        /** Topic folder name. */
        private final String specTopicName;

        /**
         * Constructs a {@code ContentSpec} (first half of fields).
         *
         * @param id     the unique content id.
         * @param title  the display title.
         * @param author the author name; may be empty.
         * @param desc   the description; may be empty.
         * @param module the module folder name.
         * @param topic  the topic folder name.
         */
        public ContentSpec(final String id, final String title,
                final String author, final String desc,
                final String module, final String topic) {
            this.specId = id;
            this.specTitle = title;
            this.specAuthor = author == null ? "" : author;
            this.specDescription = desc == null ? "" : desc;
            this.specModuleName = module == null ? "" : module;
            this.specTopicName = topic == null ? "" : topic;
        }
    }

    /**
     * Saves user content to the database. Builds the XML, stores it with the
     * base directory so the app can later resolve media files.
     *
     * @param spec      the content specification (title, module, topic, etc.).
     * @param baseDir   the base directory for media files.
     * @param slides    the ordered slide specifications.
     * @param assets    the registered media assets.
     * @param createdBy the authenticated user's id.
     * @param isQuiz    {@code true} to persist this content as a quiz.
     * @throws SQLException if the database write fails.
     */
    public static void save(final ContentSpec spec,
            final Path baseDir,
            final List<SlideSpec> slides,
            final List<MediaAsset> assets,
            final int createdBy,
            final boolean isQuiz) throws SQLException {
        String xml = buildXml(spec.specTitle, spec.specAuthor,
                spec.specDescription, slides, assets, isQuiz);
        UserContentRepository.save(
                new UserContentRepository.UserContentRecord.Builder()
                .id(spec.specId).title(spec.specTitle)
                .moduleName(spec.specModuleName).topicName(spec.specTopicName)
                .xmlContent(xml).baseDir(baseDir)
                .createdBy(createdBy).isQuiz(isQuiz)
                .build());
    }

    // ── Data specs ───────────────────────────────────────────────────────────

    /**
     * Immutable specification for a single slide during content creation.
     */
    public static final class SlideSpec {

        /** Ordered item specifications for this slide. */
        private final List<ItemSpec> slideItems;

        /** Optional quiz question for this slide; {@code null} if none. */
        private final QuestionSpec slideQuestionSpec;

        /**
         * Constructs a {@code SlideSpec} without a question.
         *
         * @param items the ordered item specs.
         */
        public SlideSpec(final List<ItemSpec> items) {
            this(items, null);
        }

        /**
         * Constructs a {@code SlideSpec} with an optional quiz question.
         *
         * @param items        the ordered item specs.
         * @param questionSpec the optional quiz question; may be {@code null}.
         */
        public SlideSpec(final List<ItemSpec> items,
                final QuestionSpec questionSpec) {
            this.slideItems = items == null
                    ? new ArrayList<>()
                    : Collections.unmodifiableList(items);
            this.slideQuestionSpec = questionSpec;
        }

        /** @return the ordered item specifications. */
        public List<ItemSpec> items() {
            return slideItems;
        }

        /** @return the optional quiz question spec, or {@code null}. */
        public QuestionSpec questionSpec() {
            return slideQuestionSpec;
        }
    }

    /**
     * Immutable specification for a single media item during content creation.
     */
    public static final class ItemSpec {

        /** Item type: TEXT, IMAGE, AUDIO, VIDEO, or MATH. */
        private final String itemType;

        /** Text content or asset relative path / src. */
        private final String itemValue;

        /** Alt text for images; empty for other types. */
        private final String itemAlt;

        /**
         * Constructs an {@code ItemSpec} with explicit alt text.
         *
         * @param type  one of {@code TEXT}, {@code IMAGE}, {@code AUDIO},
         *              {@code VIDEO}, {@code MATH}.
         * @param value the text content or asset relative path / src.
         * @param alt   alt text for images; may be empty for other types.
         */
        public ItemSpec(final String type,
                final String value,
                final String alt) {
            this.itemType = type == null ? "" : type;
            this.itemValue = value == null ? "" : value;
            this.itemAlt = alt == null ? "" : alt;
        }

        /**
         * Convenience constructor without alt text (for non-image items).
         *
         * @param type  the item type.
         * @param value the content or src value.
         */
        public ItemSpec(final String type, final String value) {
            this(type, value, "");
        }

        /** @return the item type. */
        public String type() {
            return itemType;
        }

        /** @return the content or src value. */
        public String value() {
            return itemValue;
        }

        /** @return the alt text (may be empty). */
        public String alt() {
            return itemAlt;
        }
    }

    /**
     * Immutable specification for a quiz question on a slide during content
     * creation.
     */
    public static final class QuestionSpec {

        /** The question prompt text. */
        private final String qText;

        /** Exactly four answer option strings. */
        private final List<String> qOptions;

        /** 1-based index of the correct answer (1–4). */
        private final int qCorrectIndex;

        /** Explanation text for the correct answer. */
        private final String qExplanation;

        /**
         * Constructs a {@code QuestionSpec}.
         *
         * @param questionText       the question prompt.
         * @param options            exactly four answer options.
         * @param correctAnswerIndex 1-based correct answer index.
         * @param explanation        explanation text.
         */
        public QuestionSpec(final String questionText,
                final List<String> options,
                final int correctAnswerIndex,
                final String explanation) {
            this.qText = questionText == null ? "" : questionText;
            this.qOptions = Collections.unmodifiableList(
                    options == null ? new ArrayList<>() : options);
            this.qCorrectIndex = correctAnswerIndex;
            this.qExplanation = explanation == null ? "" : explanation;
        }

        /** @return the question prompt text. */
        public String questionText() {
            return qText;
        }

        /** @return the four answer options. */
        public List<String> options() {
            return qOptions;
        }

        /** @return the 1-based correct answer index. */
        public int correctAnswerIndex() {
            return qCorrectIndex;
        }

        /** @return the explanation text. */
        public String explanation() {
            return qExplanation;
        }
    }

    // ── Private utilities ────────────────────────────────────────────────────

    /**
     * Sanitises a string for use as a filesystem path segment by replacing
     * unsafe characters with underscores.
     *
     * @param name the raw name.
     * @return a safe path segment.
     */
    private static String sanitise(final String name) {
        return name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    /**
     * Escapes a string for safe embedding in XML attribute values and text
     * content (replaces {@code &}, {@code <}, {@code >}, {@code "}).
     *
     * @param value the raw string; {@code null} returns an empty string.
     * @return the XML-safe string.
     */
    private static String escape(final String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * Returns the lower-case file extension including the dot, or an empty
     * string if the filename has no extension.
     *
     * @param filename the filename.
     * @return the extension (e.g. {@code ".mp4"}).
     */
    private static String extension(final String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : "";
    }

    /**
     * Detects the broad media type ({@code IMAGE}, {@code VIDEO},
     * {@code AUDIO}) from a file extension.
     *
     * @param ext the lower-case extension including the dot.
     * @return the media type string.
     */
    private static String detectType(final String ext) {
        if (IMAGE_EXTS.contains(ext)) {
            return "IMAGE";
        }
        if (VIDEO_EXTS.contains(ext)) {
            return "VIDEO";
        }
        if (AUDIO_EXTS.contains(ext)) {
            return "AUDIO";
        }
        return "IMAGE"; // fallback
    }

    /**
     * Returns a best-guess MIME type for a given extension and media type.
     *
     * @param ext       the lower-case extension.
     * @param mediaType the detected media type.
     * @return a MIME type string.
     */
    private static String guessMime(final String ext,
            final String mediaType) {
        switch (ext) {
            case ".mp4":  return "video/mp4";
            case ".webm": return "video/webm";
            case ".mov":  return "video/quicktime";
            case ".mkv":  return "video/x-matroska";
            case ".mp3":  return "audio/mpeg";
            case ".wav":  return "audio/wav";
            case ".ogg":  return "audio/ogg";
            case ".flac": return "audio/flac";
            case ".aac":  return "audio/aac";
            case ".m4a":  return "audio/mp4";
            case ".png":  return "image/png";
            case ".jpg":
            case ".jpeg": return "image/jpeg";
            case ".gif":  return "image/gif";
            case ".heic": return "image/heic";
            case ".webp": return "image/webp";
            default:
                if ("VIDEO".equals(mediaType)) {
                    return "video/mp4";
                }
                if ("AUDIO".equals(mediaType)) {
                    return "audio/mpeg";
                }
                return "image/jpeg";
        }
    }

    /**
     * Computes the SHA-256 hex checksum of a file.
     *
     * @param file the file to hash.
     * @return a 64-character hex string.
     * @throws IOException if the file cannot be read.
     */
    private static String sha256(final File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file.toPath())) {
                byte[] buf = new byte[HASH_BUFFER_SIZE];
                int read;
                while ((read = in.read(buf)) != -1) {
                    digest.update(buf, 0, read);
                }
            }
            return String.format("%064x",
                    new BigInteger(1, digest.digest()));
        } catch (NoSuchAlgorithmException ex) {
            return ""; // SHA-256 is always available in Java
        }
    }
}
