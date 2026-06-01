package com.axono.player.module;

import com.axono.player.Assets;
import com.axono.content.AudioItem;
import com.axono.ui.UIConstants;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Plays one or more audio tracks for the active slide with full UI controls,
 * backed by FFmpeg via {@link JavaCVPlayer}.
 *
 * <p>Supports any audio codec that FFmpeg can decode (MP3, AAC, FLAC, Opus,
 * Vorbis, ALAC, etc.) without requiring an external FFmpeg installation.
 * Tracks play sequentially: when a track ends it automatically advances to
 * the next one. A row of track-selector buttons lets the user jump directly
 * to any track when more than one is present. Each track's control bar
 * provides play/pause, a scrubbing progress bar, a time display, and a
 * volume slider.
 * </p>
 *
 * <p>Players are created in {@link #onEnter()} and fully disposed in
 * {@link #onExit()}, releasing all native media resources when the user
 * navigates away from the slide.</p>
 *
 * <p>Items whose {@code src} cannot be resolved via
 * {@link Assets#resolve(String)} are rendered as muted placeholder labels.</p>
 */
public final class AudioModule implements MediaModule {

    /** Root container returned to the slide canvas. */
    private final VBox view;

    /** Resolved URLs, one per playable audio track. */
    private final List<String> resolvedUrls = new ArrayList<>();

    /**
     * Human-readable names shown on track-selector buttons.
     * Parallel to {@link #resolvedUrls}.
     */
    private final List<String> trackNames = new ArrayList<>();

    /** Live players, created on enter and disposed on exit. */
    private final List<JavaCVPlayer> players = new ArrayList<>();

    /**
     * Control-bar bundles per track; parallel to {@link #players}.
     * Entries are {@code null} until the corresponding player is selected.
     */
    private final List<MediaPlayerUtils.Controls> controlsList
            = new ArrayList<>();

    /** Index of the track currently playing; -1 before the first entry. */
    private int currentTrack = -1;

    /** Displays the name of the active track. */
    private final Label trackNameLabel;

    /** Shows format and file-size metadata for the active track. */
    private final Label metaLabel;

    /** Swapped out when a different track's control bar is shown. */
    private final VBox controlBarContainer;

    /** Track-selector buttons; parallel to {@link #resolvedUrls}. */
    private final List<Button> trackButtons = new ArrayList<>();

    /**
     * Constructs an {@code AudioModule}.
     *
     * @param items the audio items; unresolvable items render as labels.
     */
    public AudioModule(final List<AudioItem> items) {
        this.view = new VBox(UIConstants.SPACING_LG);
        this.trackNameLabel = buildTrackNameLabel();
        this.metaLabel = buildMetaLabel();
        this.controlBarContainer = new VBox();

        for (AudioItem item : items) {
            String url = Assets.resolve(item.getSrc());
            if (url != null) {
                resolvedUrls.add(url);
                trackNames.add(stripExtension(item.getSrc()));
            } else {
                view.getChildren().add(buildUnresolvedLabel(item.getSrc()));
            }
        }

        if (!resolvedUrls.isEmpty()) {
            buildPlayerUI();
        }
    }

    // ── View construction ────────────────────────────────────────────────────

    /** Assembles the track-info panel, control-bar slot, and track selector. */
    private void buildPlayerUI() {
        VBox infoBox = new VBox(UIConstants.SPACING_SM,
                trackNameLabel, metaLabel);

        view.getChildren().addAll(infoBox, controlBarContainer);

        if (resolvedUrls.size() > 1) {
            view.getChildren().add(buildTrackSelector());
        }
    }

    /**
     * Builds the horizontal row of track-selector buttons.
     *
     * @return the configured {@link HBox}.
     */
    private HBox buildTrackSelector() {
        HBox row = new HBox(UIConstants.SPACING_MD);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(UIConstants.SPACING_SM, 0, 0, 0));

        for (int i = 0; i < resolvedUrls.size(); i++) {
            final int index = i;
            Button btn = new Button(trackNames.get(i));
            btn.getStyleClass().add("track-btn");
            btn.setOnAction(e -> switchToTrack(index));
            trackButtons.add(btn);
            row.getChildren().add(btn);
        }
        return row;
    }

    /** @return a new styled track-name label. */
    private static Label buildTrackNameLabel() {
        Label l = new Label("No track selected");
        l.getStyleClass().addAll("text-dark", "bold");
        l.setStyle("-fx-font-size: " + UIConstants.FONT_BODY + "px;");
        return l;
    }

    /** @return a new styled metadata label (format + file size). */
    private static Label buildMetaLabel() {
        Label l = new Label();
        l.getStyleClass().add("text-muted");
        l.setStyle("-fx-font-size: " + UIConstants.FONT_CAPTION + "px;");
        return l;
    }

    /**
     * Builds a metadata string for the given resolved audio URL, showing
     * the audio format and file size (e.g. {@code "FLAC • 24.3 MB"}).
     *
     * @param url the {@code file:///} URL of the audio file.
     * @return a non-null metadata string; format name only if size is unknown.
     */
    private static String buildMeta(final String url) {
        int dot = url.lastIndexOf('.');
        String format = dot >= 0
                ? url.substring(dot + 1).toUpperCase() : "Audio";
        long bytes = 0;
        try {
            bytes = new File(new URI(url)).length();
        } catch (Exception ignored) {
            // leave bytes = 0
        }
        if (bytes > 0) {
            final long bytesPerMb = 1_048_576L;
            double mb = bytes / (double) bytesPerMb;
            return format + " • " + String.format("%.1f MB", mb);
        }
        return format;
    }

    /**
     * Builds a placeholder label for an audio item that could not be resolved.
     *
     * @param src the raw source attribute value.
     * @return a styled label.
     */
    private static Label buildUnresolvedLabel(final String src) {
        String shown = src.isEmpty() ? "[audio]" : "[audio] " + src;
        Label l = new Label(shown);
        l.getStyleClass().add("text-muted");
        l.setStyle("-fx-font-size: " + UIConstants.FONT_SMALL + "px;");
        return l;
    }

    // ── MediaModule lifecycle ────────────────────────────────────────────────

    @Override
    public Node getView() {
        return view;
    }

    /**
     * Creates one {@link JavaCVPlayer} per resolved track (if not yet done),
     * then starts playback at track 0.
     */
    @Override
    public void onEnter() {
        if (resolvedUrls.isEmpty()) {
            return;
        }
        if (players.isEmpty()) {
            createPlayers();
        }
        switchToTrack(0);
    }

    /**
     * Stops and disposes every active {@link JavaCVPlayer}, releasing all
     * native resources. Control-bar listeners are removed first.
     */
    @Override
    public void onExit() {
        for (int i = 0; i < players.size(); i++) {
            JavaCVPlayer p = players.get(i);
            if (p != null) {
                if (i < controlsList.size()) {
                    MediaPlayerUtils.unbindControls(controlsList.get(i));
                }
                p.dispose();
            }
        }
        players.clear();
        controlsList.clear();
        currentTrack = -1;
        controlBarContainer.getChildren().clear();
        metaLabel.setText("");
        trackNameLabel.setText("No track selected");

        // Reset all selector buttons to inactive style
        for (Button btn : trackButtons) {
            btn.getStyleClass().remove("track-btn-active");
        }
    }

    // ── Playback logic ───────────────────────────────────────────────────────

    /**
     * Creates one {@link JavaCVPlayer} per resolved URL, registers the
     * end-of-media callback and playing-state listener, and pre-fills
     * {@link #controlsList} with {@code null} placeholders.
     */
    private void createPlayers() {
        for (int i = 0; i < resolvedUrls.size(); i++) {
            String url = resolvedUrls.get(i);
            try {
                JavaCVPlayer p = new JavaCVPlayer(url);
                p.setOnEndOfMedia(this::advanceTrack);
                players.add(p);
                controlsList.add(null);
            } catch (RuntimeException ex) {
                System.err.println("Audio failed for " + url
                        + ": " + ex.getMessage());
                players.add(null);
                controlsList.add(null);
            }
        }
    }

    /**
     * Switches playback to the track at {@code index}. Pauses the previously
     * active player, builds the control bar for the new player (if not yet
     * built), and starts playback.
     *
     * @param index zero-based track index.
     */
    private void switchToTrack(final int index) {
        if (index < 0 || index >= players.size()) {
            return;
        }

        // Pause the previously active track
        if (currentTrack >= 0 && currentTrack < players.size()) {
            JavaCVPlayer prev = players.get(currentTrack);
            if (prev != null) {
                prev.pause();
            }
        }

        currentTrack = index;
        JavaCVPlayer p = players.get(index);
        if (p == null) {
            return;
        }

        trackNameLabel.setText(trackNames.get(index));
        metaLabel.setText(buildMeta(resolvedUrls.get(index)));

        // Build control bar the first time this track is selected
        if (controlsList.get(index) == null) {
            controlsList.set(index, MediaPlayerUtils.buildControlBar(p));
        }

        controlBarContainer.getChildren().clear();
        controlBarContainer.getChildren()
                .add(controlsList.get(index).getBar());

        // Highlight the active selector button
        for (int i = 0; i < trackButtons.size(); i++) {
            Button tb = trackButtons.get(i);
            if (i == index) {
                tb.getStyleClass().add("track-btn-active");
            } else {
                tb.getStyleClass().remove("track-btn-active");
            }
        }

        p.play();
    }

    /**
     * Advances to the next track when the current one ends.
     * Once the last track finishes the control bar's replay icon signals
     * end-of-stream — no extra label update is needed.
     */
    private void advanceTrack() {
        int next = currentTrack + 1;
        if (next < resolvedUrls.size()) {
            switchToTrack(next);
        }
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    /**
     * Returns the filename without its extension, used as a human-readable
     * track name on selector buttons. URL-encoded characters (e.g. {@code %20})
     * are decoded so that filenames with spaces display correctly.
     *
     * @param src the raw {@code src} attribute value (may be a file URI).
     * @return the decoded base name without file extension.
     */
    private static String stripExtension(final String src) {
        int slash = Math.max(src.lastIndexOf('/'), src.lastIndexOf('\\'));
        String name = slash >= 0 ? src.substring(slash + 1) : src;
        try {
            name = java.net.URLDecoder.decode(name, "UTF-8");
        } catch (java.io.UnsupportedEncodingException ignored) {
            // keep as-is
        }
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
