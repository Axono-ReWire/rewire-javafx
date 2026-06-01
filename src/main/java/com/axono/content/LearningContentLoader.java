package com.axono.content;

import com.axono.player.UserContentRepository;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Discovers and loads every learning-content XML file bundled under the
 * classpath resource root. Each file is parsed via
 * {@link LearningContentParser}; files that fail to parse are logged to
 * {@code System.err} and skipped so a single bad XML cannot prevent the
 * browser from opening.
 *
 * <p>The well-known {@code curriculum.xml} file (used by the onboarding
 * subject picker, not learning content) is excluded.
 */
public final class LearningContentLoader {

    /** Filename of the curriculum file, excluded from discovery. */
    private static final String CURRICULUM_FILE = "curriculum.xml";

    /** Length of the "file:" URL scheme prefix. */
    private static final int FILE_SCHEME_LENGTH = 5;

    /** Minimum path segments expected in learning-content entry names. */
    private static final int MIN_PATH_SEGMENTS = 4;

    /** Private constructor to prevent instantiation. */
    private LearningContentLoader() {
    }

    /**
     * Scans the learning content directory, parses every {@code *.xml} file
     * (apart from {@link #CURRICULUM_FILE}), merges any user-created content
     * from the database, and returns the combined list sorted by module then
     * by title. Tries file-system walking first (development mode), then
     * falls back to classpath scanning (JAR mode).
     *
     * @return an immutable list of successfully parsed learning content.
     *         Returns an empty list if no content is found.
     */
    public static List<LearningContent> loadAll() {
        List<LearningContent> result = new ArrayList<>();
        Path root = resolveResourceRoot();
        if (root != null) {
            tryLoadFromFileSystem(root, result);
        } else {
            tryLoadFromClasspath(result);
        }

        // Merge user-created content from the database
        try {
            List<LearningContent> userContent = UserContentRepository.loadAll();
            result.addAll(userContent);
        } catch (SQLException ex) {
            System.err.println(
                    "Could not load user content from database: "
                            + ex.getMessage());
        }

        result.sort(byModuleThenTitle());
        return Collections.unmodifiableList(result);
    }

    /**
     * Attempts to load learning content from the file system (development
     * mode: expanded target/classes/learning-content/).
     *
     * @param root the learning-content directory path.
     * @param out  the accumulating result list.
     */
    private static void tryLoadFromFileSystem(final Path root,
            final List<LearningContent> out) {
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(LearningContentLoader::isContentFile)
                    .forEach(p -> tryParseInto(p, root, out));
        } catch (IOException ex) {
            System.err.println(
                    "Failed to walk learning-content directory: "
                            + ex.getMessage());
        }
    }

    /**
     * Attempts to load learning content from the classpath (production mode:
     * packaged JAR). Uses ClassLoader resource enumeration to find all XML
     * files in the learning-content/ directory on the classpath.
     *
     * @param out the accumulating result list.
     */
    private static void tryLoadFromClasspath(final List<LearningContent> out) {
        try {
            ClassLoader loader = LearningContentLoader.class
                    .getClassLoader();
            Enumeration<URL> resources =
                    loader.getResources("learning-content");
            if (resources == null || !resources.hasMoreElements()) {
                System.err.println(
                        "No learning-content resources found on classpath");
                return;
            }
            while (resources.hasMoreElements()) {
                URL resourceUrl = resources.nextElement();
                if ("jar".equals(resourceUrl.getProtocol())) {
                    scanJarResource(resourceUrl, out);
                } else if ("file".equals(resourceUrl.getProtocol())) {
                    try {
                        Path path = Paths.get(resourceUrl.toURI());
                        tryLoadFromFileSystem(path, out);
                    } catch (URISyntaxException ex) {
                        System.err.println(
                                "Failed to parse resource URL: "
                                        + ex.getMessage());
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println(
                    "Failed to scan classpath resources: " + ex.getMessage());
        }
    }

    /**
     * Scans a JAR resource URL and extracts XML files from the learning-content
     * directory inside the JAR.
     *
     * @param resourceUrl the JAR resource URL (jar:file:...!/learning-content).
     * @param out         the accumulating result list.
     */
    private static void scanJarResource(final URL resourceUrl,
            final List<LearningContent> out) {
        String urlStr = resourceUrl.getPath();
        int bangIndex = urlStr.indexOf('!');
        if (bangIndex == -1) {
            return;
        }
        String jarPath = urlStr.substring(0, bangIndex);
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(FILE_SCHEME_LENGTH);
        }
        jarPath = java.net.URLDecoder.decode(jarPath,
                java.nio.charset.StandardCharsets.UTF_8);
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith("learning-content/")
                        && entryName.endsWith(".xml")
                        && !entry.isDirectory()
                        && !entryName.contains("curriculum.xml")) {
                    tryParseJarEntry(entryName, jar, out);
                }
            }
        } catch (IOException ex) {
            System.err.println(
                    "Failed to scan JAR resource: " + ex.getMessage());
        }
    }

    /**
     * Parses a single entry from a JAR and appends the result to {@code out}.
     *
     * @param entryName the entry name (e.g.,
     *                  "learning-content/2.Mathematics/Topic/Title.xml").
     * @param jar       the JAR file.
     * @param out       the accumulating result list.
     */
    private static void tryParseJarEntry(final String entryName,
            final JarFile jar,
            final List<LearningContent> out) {
        String[] parts = entryName.split("/");
        if (parts.length < MIN_PATH_SEGMENTS) {
            return;
        }
        String module = parts[1];
        String topic = parts[2];
        try {
            LearningContentParser.parseFromJar(jar, entryName, module, topic,
                    out);
        } catch (LearningContentParseException ex) {
            System.err.println("Skipping malformed learning content "
                    + entryName + ": " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Failed to read learning content " + entryName
                    + ": " + ex.getMessage());
        }
    }

    /**
     * Resolves the file-system path of the learning-content directory, or
     * {@code null} if it cannot be expressed as a {@link Path}.
     *
     * @return the learning-content path, or {@code null}.
     */
    private static Path resolveResourceRoot() {
        URL url = LearningContentLoader.class
                .getResource("/learning-content");
        if (url == null) {
            return null;
        }
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Filters out non-content files: anything that is not {@code *.xml}
     * and the curriculum file.
     *
     * @param p the candidate path.
     * @return {@code true} if {@code p} should be parsed.
     */
    private static boolean isContentFile(final Path p) {
        String name = p.getFileName().toString();
        if (name.equalsIgnoreCase(CURRICULUM_FILE)) {
            return false;
        }
        return name.toLowerCase().endsWith(".xml");
    }

    /**
     * Parses a single file and appends the result to {@code out}, swallowing
     * (and logging) parse failures so they do not abort discovery.
     *
     * @param file the XML file.
     * @param root the resource root, used to derive module/topic names.
     * @param out  the accumulating result list.
     */
    private static void tryParseInto(final Path file,
            final Path root,
            final List<LearningContent> out) {
        String module = pathSegment(root, file, 0);
        String topic = pathSegment(root, file, 1);
        try {
            out.add(LearningContentParser.parse(file, module, topic));
        } catch (LearningContentParseException ex) {
            System.err.println("Skipping malformed learning content "
                    + file.getFileName() + ": " + ex.getMessage());
        }
    }

    /**
     * Returns the {@code index}-th path segment of {@code file} relative to
     * {@code root}, or {@code ""} if the segment does not exist or the file
     * lies outside the root. The final segment (the filename) is never
     * returned by this helper.
     *
     * @param root  the resource root.
     * @param file  the file under {@code root}.
     * @param index the zero-based segment index to read.
     * @return the segment as a string, or {@code ""}.
     */
    private static String pathSegment(final Path root,
            final Path file,
            final int index) {
        Path rel;
        try {
            rel = root.relativize(file);
        } catch (IllegalArgumentException ex) {
            return "";
        }
        int dirs = rel.getNameCount() - 1;
        if (index < 0 || index >= dirs) {
            return "";
        }
        return rel.getName(index).toString();
    }

    /**
     * Comparator that orders learning content by module folder, then topic
     * folder, then title — all case-insensitively.
     *
     * @return the comparator.
     */
    private static Comparator<LearningContent> byModuleThenTitle() {
        return Comparator
                .comparing(LearningContent::getModule,
                        String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LearningContent::getTopic,
                        String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LearningContent::getTitle,
                        String.CASE_INSENSITIVE_ORDER);
    }
}
