package com.axono.player.module;

import javafx.scene.Node;

/**
 * Common contract for every media renderer in the content player.
 *
 * <p>The {@code ContentPlayer} composes a slide by collecting all the
 * modules required to render its content, calling {@link #getView()} on
 * each to obtain a JavaFX node, then arranging the nodes on the slide
 * canvas. {@link #onEnter()} is invoked once the slide is visible (start
 * playback / animation); {@link #onExit()} is invoked before the slide is
 * replaced and MUST stop any audio or video playback so resources are
 * released cleanly.
 */
public interface MediaModule {

    /**
     * Returns the JavaFX node this module renders into. Implementations
     * should construct the node once and return the same instance across
     * subsequent calls.
     *
     * @return the node hosted on the slide canvas.
     */
    Node getView();

    /**
     * Invoked when the slide hosting this module becomes visible. Audio
     * and video modules begin playback here; non-time-based modules
     * (text, images) may treat this as a no-op.
     */
    void onEnter();

    /**
     * Invoked before the slide is replaced. Implementations MUST stop
     * any active playback and release media resources. After this call,
     * the module is treated as disposed and is not reused.
     */
    void onExit();
}
