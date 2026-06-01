package com.axono.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Scene;

/**
 * Manages the application-wide dark / light theme toggle.
 *
 * <p>Callers register every {@link Scene} they create via
 * {@link #register(Scene)}, and unregister it when the scene is no
 * longer needed via {@link #unregister(Scene)}.  Calling
 * {@link #toggle()} flips the dark-mode state and adds or removes the
 * dark stylesheet on every registered scene.</p>
 */
public final class ThemeManager {

    /** Whether dark mode is currently active. */
    private static boolean darkMode = false;

    /** Cached URL for the dark stylesheet resource. */
    private static String darkCssUrl = null;

    /** All scenes currently managed by this class. */
    private static final List<Scene> SCENES = new ArrayList<>();

    /** Utility class — not instantiable. */
    private ThemeManager() { }

    /**
     * Returns the external-form URL of the dark stylesheet,
     * resolving it lazily on first call.
     *
     * @return the CSS resource URL.
     */
    private static String darkCss() {
        if (darkCssUrl == null) {
            darkCssUrl = ThemeManager.class
                    .getResource("/styles-dark.css").toExternalForm();
        }
        return darkCssUrl;
    }

    /**
     * Registers a scene so it receives dark-stylesheet updates.
     * If dark mode is already active the stylesheet is applied immediately.
     *
     * @param scene the scene to manage.
     */
    public static void register(final Scene scene) {
        if (scene == null) {
            return;
        }
        SCENES.add(scene);
        if (darkMode) {
            scene.getStylesheets().add(darkCss());
        }
    }

    /**
     * Unregisters a scene so it no longer receives theme updates.
     *
     * @param scene the scene to remove.
     */
    public static void unregister(final Scene scene) {
        SCENES.remove(scene);
    }

    /**
     * Toggles dark mode on or off, updating every registered scene.
     */
    public static void toggle() {
        darkMode = !darkMode;
        String url = darkCss();
        for (Scene scene : SCENES) {
            if (darkMode) {
                if (!scene.getStylesheets().contains(url)) {
                    scene.getStylesheets().add(url);
                }
            } else {
                scene.getStylesheets().remove(url);
            }
        }
    }

    /**
     * Returns whether dark mode is currently active.
     *
     * @return {@code true} when dark mode is on.
     */
    public static boolean isDark() {
        return darkMode;
    }
}
