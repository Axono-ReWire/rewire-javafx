package com.axono.content;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-content registry that maps media-asset relative paths to playable
 * file-system URLs. Constructed by {@link LearningContentParser} when a
 * {@code <mediaAssets>} block is present in the content XML, and stored on
 * the {@link LearningContent} object.
 *
 * <p>Used by {@link com.axono.player.ContentPlayer} to resolve media
 * {@code src} attribute values that are relative to a content's base directory
 * rather than the classpath. Resolution order for a given {@code src}:
 * <ol>
 *   <li>Absolute URLs ({@code file:}, {@code http:}, {@code https:}) are
 *       returned unchanged.</li>
 *   <li>The src is resolved directly as a path relative to
 *       {@link #getBaseDir()}; if the resulting file exists its {@code file:}
 *       URI is returned.</li>
 *   <li>The registry is searched for an asset whose
 *       {@link MediaAsset#getRelativePath()} or
 *       {@link MediaAsset#getFilename()} matches {@code src}; if found the
 *       asset's relative path is resolved against the base directory.</li>
 *   <li>{@code null} is returned — the caller may then fall back to
 *       {@link com.axono.player.Assets#resolve(String)}.</li>
 * </ol>
 */
public final class MediaAssetRegistry {

    /**
     * Map of relative-path / filename key → {@link MediaAsset}.
     * Populated from the {@code <mediaAssets>} XML block.
     */
    private final Map<String, MediaAsset> byKey;

    /** Absolute path of the directory containing the content XML (or media). */
    private final Path baseDir;

    /**
     * Constructs a {@code MediaAssetRegistry}.
     *
     * @param assets      the media assets declared in the content XML.
     *                    May be {@code null} or empty.
     * @param contentBase the absolute path to the content's base directory
     *                    (the folder that contains the XML file and the
     *                    {@code media/} sub-directories).
     *                    May be {@code null} for classpath-only content.
     */
    public MediaAssetRegistry(final List<MediaAsset> assets,
            final Path contentBase) {
        this.baseDir = contentBase;
        Map<String, MediaAsset> map = new HashMap<>();
        if (assets != null) {
            for (MediaAsset a : assets) {
                if (!a.getRelativePath().isEmpty()) {
                    map.put(a.getRelativePath(), a);
                }
                if (!a.getFilename().isEmpty()) {
                    map.putIfAbsent(a.getFilename(), a);
                }
                if (!a.getId().isEmpty()) {
                    map.putIfAbsent(a.getId(), a);
                }
            }
        }
        this.byKey = Collections.unmodifiableMap(map);
    }

    /**
     * Attempts to resolve a media {@code src} attribute value to a
     * {@code file:} URL using the registered assets and the base directory.
     *
     * @param src the raw {@code src} value from the slide XML; may be empty.
     * @return an absolute {@code file:} URL string if the file exists on disk,
     *         or {@code null} if this registry cannot resolve it.
     */
    public String resolveUrl(final String src) {
        if (src == null || src.isEmpty()) {
            return null;
        }
        if (src.startsWith("file:")
                || src.startsWith("http:")
                || src.startsWith("https:")) {
            return src;
        }
        if (baseDir == null) {
            return null;
        }

        // Attempt 1: resolve src directly against baseDir
        String direct = tryResolve(baseDir, src);
        if (direct != null) {
            return direct;
        }

        // Attempt 2: look up via registered asset and use its relativePath
        MediaAsset asset = byKey.get(src);
        if (asset != null && !asset.getRelativePath().isEmpty()) {
            String via = tryResolve(baseDir, asset.getRelativePath());
            if (via != null) {
                return via;
            }
        }

        return null;
    }

    /**
     * Resolves {@code relative} against {@code base} and returns the
     * {@code file:} URI string if the result exists on disk, else
     * {@code null}.
     *
     * @param base     the base directory.
     * @param relative the relative path string (may use forward or back
     *                 slashes).
     * @return the URI string or {@code null}.
     */
    private static String tryResolve(final Path base, final String relative) {
        try {
            Path resolved = base.resolve(relative).normalize();
            File file = resolved.toFile();
            if (file.exists() && file.isFile()) {
                return file.toURI().toString();
            }
        } catch (Exception ignored) {
            // Invalid path — fall through
        }
        return null;
    }

    /**
     * @return the base directory used for resolution, or {@code null} if
     *         this registry was created without a known base.
     */
    public Path getBaseDir() {
        return baseDir;
    }

    /**
     * @return an unmodifiable view of the asset map; keys are relative paths
     *         and filenames.
     */
    public Map<String, MediaAsset> getAssets() {
        return byKey;
    }

    /** @return {@code true} if this registry contains no registered assets. */
    public boolean isEmpty() {
        return byKey.isEmpty();
    }
}
