package com.axono.player;

/**
 * Resolves media asset paths to classpath URLs under {@code /assets/}.
 *
 * <p>Per the Q6/Q7 decisions in {@code PLAN.md}, XML {@code src=""} values
 * are relative paths inside {@code /assets/} on the classpath, e.g.
 * {@code src="images/communications/NRZI.png"} maps to the classpath
 * resource {@code /assets/images/communications/NRZI.png}.
 */
public final class Assets {

    /** Private constructor to prevent instantiation. */
    private Assets() {
    }

    /**
     * Returns a URL string for a media asset. If {@code src} is already an
     * absolute URL (starts with {@code file:}, {@code http:}, or
     * {@code https:}) it is returned as-is, allowing locally opened files to
     * bypass classpath lookup. Otherwise, {@code src} is treated as a path
     * relative to {@code /assets/} on the classpath.
     *
     * @param src the relative path within {@code /assets/}, or an absolute
     *            URL; may be empty.
     * @return a URL string suitable for {@link javafx.scene.image.Image}
     *         or {@link javafx.scene.media.Media}, or {@code null}.
     */
    public static String resolve(final String src) {
        if (src == null || src.isEmpty()) {
            return null;
        }
        if (src.startsWith("file:")
                || src.startsWith("http:")
                || src.startsWith("https:")) {
            return src;
        }
        java.net.URL url = Assets.class.getResource("/assets/" + src);
        return url == null ? null : url.toExternalForm();
    }
}
