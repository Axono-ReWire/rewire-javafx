package com.axono.player.module;

import com.axono.player.Assets;
import com.axono.content.VideoItem;
import com.axono.ui.UIConstants;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Renders one or more video clips with full playback controls, backed by
 * FFmpeg via JavaCV.
 *
 * <p>Supports any codec that FFmpeg can decode (H.264, H.265, AV1, VP9,
 * etc.) without requiring an external install. Each video gets its own
 * {@link JavaCVPlayer} created lazily on {@link #onEnter()} and disposed on
 * {@link #onExit()}, releasing all native resources when the user navigates
 * away from the slide.</p>
 *
 * <p>A fullscreen button in the control bar opens the video in a separate
 * full-screen window sharing the same decoded frame buffer.</p>
 *
 * <p>Items whose {@code src} cannot be resolved via
 * {@link Assets#resolve(String)} are shown as muted placeholder labels.</p>
 */
public final class VideoModule implements MediaModule {

    /** Icon size used on the play/pause and fullscreen buttons. */
    private static final int ICON_SIZE = 20;

    /** Smaller icon size for the volume indicator. */
    private static final int ICON_SIZE_SM = 16;

    /** Preferred width of the volume slider in pixels. */
    private static final double VOLUME_SLIDER_WIDTH = 80.0;

    /** Maximum value of the volume slider (maps to 1.0 gain). */
    private static final double VOLUME_MAX = 100.0;

    /** Minutes-to-seconds divisor. */
    private static final int SECONDS_PER_MINUTE = 60;

    /**
     * Tolerance in seconds for end-of-stream detection: playing stopped
     * within this many seconds of total duration is treated as "ended".
     */
    private static final double END_TOLERANCE_SECS = 1.0;

    /** Seconds jumped per press of the backward / forward skip buttons. */
    private static final double SKIP_SECONDS = 10.0;

    /** Root container returned to the slide canvas. */
    private final VBox view;

    /** Per-video state holders, one per successfully resolved asset URL. */
    private final List<VideoEntry> entries = new ArrayList<>();

    /** Index of the video currently shown; -1 before first entry. */
    private int currentVideo = -1;

    /** Shows the filename of the active clip; null in single-video mode. */
    private Label videoNameLabel;

    /** Swapped when a different clip is selected; null in single-video mode. */
    private VBox videoArea;

    /** Clip-selector buttons; parallel to {@link #entries}. */
    private final List<Button> videoButtons = new ArrayList<>();

    /**
     * Constructs a {@code VideoModule}.
     *
     * @param items the video items to display on this slide.
     */
    public VideoModule(final List<VideoItem> items) {
        this.view = new VBox(UIConstants.SPACING_LG);
        this.view.setAlignment(Pos.CENTER);
        for (VideoItem item : items) {
            String url = Assets.resolve(item.getSrc());
            if (url != null) {
                entries.add(new VideoEntry(url));
            } else {
                view.getChildren().add(buildIndicator(item.getSrc()));
            }
        }
        if (!entries.isEmpty()) {
            buildPlayerUI();
        }
    }

    /**
     * Builds a muted placeholder label for an unresolvable video source.
     * The filename is URL-decoded so percent-encoded characters display
     * correctly (e.g. {@code %20} → space).
     *
     * @param src the raw {@code src} attribute value.
     * @return a styled label.
     */
    private static Label buildIndicator(final String src) {
        String decoded = src;
        try {
            decoded = java.net.URLDecoder.decode(src, "UTF-8");
        } catch (java.io.UnsupportedEncodingException ignored) {
            // keep original
        }
        String shown = decoded.isEmpty() ? "[video]" : "[video] " + decoded;
        Label l = new Label(shown);
        l.getStyleClass().add("text-muted");
        l.setStyle("-fx-font-size: " + UIConstants.FONT_SMALL + "px;");
        return l;
    }

    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void onEnter() {
        if (entries.size() == 1) {
            entries.get(0).startPlayback();
        } else if (entries.size() > 1) {
            switchToVideo(currentVideo >= 0 ? currentVideo : 0);
        }
    }

    @Override
    public void onExit() {
        for (VideoEntry entry : entries) {
            entry.stopPlayback();
        }
        if (videoArea != null) {
            videoArea.getChildren().clear();
        }
        if (videoNameLabel != null) {
            videoNameLabel.setText("No video selected");
        }
        for (Button btn : videoButtons) {
            btn.getStyleClass().remove("track-btn-active");
        }
        currentVideo = -1;
    }

    // ── Multi-video helpers ──────────────────────────────────────────────────

    /** Sets up the view for single or multi-video layout. */
    private void buildPlayerUI() {
        if (entries.size() == 1) {
            view.getChildren().add(entries.get(0).getContainer());
        } else {
            videoNameLabel = buildVideoNameLabel();
            videoArea = new VBox();
            videoArea.setAlignment(Pos.CENTER);
            view.getChildren().addAll(
                    videoNameLabel, videoArea, buildVideoSelector());
        }
    }

    /**
     * Builds the row of clip-selector buttons shown in multi-video mode.
     *
     * @return a configured {@link HBox}.
     */
    private HBox buildVideoSelector() {
        HBox row = new HBox(UIConstants.SPACING_MD);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(UIConstants.SPACING_SM, 0, 0, 0));
        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            Button btn = new Button("Video " + (i + 1));
            btn.getStyleClass().add("track-btn");
            btn.setOnAction(e -> switchToVideo(index));
            videoButtons.add(btn);
            row.getChildren().add(btn);
        }
        return row;
    }

    /** @return a new styled clip-name label. */
    private static Label buildVideoNameLabel() {
        Label l = new Label("No video selected");
        l.getStyleClass().addAll("text-dark", "bold");
        l.setStyle("-fx-font-size: " + UIConstants.FONT_BODY + "px;");
        return l;
    }

    /**
     * Switches the visible clip to {@code index}. Pauses the previously
     * active entry, swaps the container, and starts the new one.
     *
     * @param index zero-based clip index.
     */
    private void switchToVideo(final int index) {
        if (index < 0 || index >= entries.size()) {
            return;
        }
        if (currentVideo >= 0 && currentVideo < entries.size()) {
            entries.get(currentVideo).pausePlayback();
        }
        currentVideo = index;
        videoNameLabel.setText(stripExtension(entries.get(index).getUrl()));
        videoArea.getChildren().setAll(entries.get(index).getContainer());
        for (int i = 0; i < videoButtons.size(); i++) {
            Button btn = videoButtons.get(i);
            if (i == index) {
                btn.getStyleClass().add("track-btn-active");
            } else {
                btn.getStyleClass().remove("track-btn-active");
            }
        }
        entries.get(index).startPlayback();
    }

    /**
     * Returns the filename without its extension, used as the clip label.
     * URL-encoded characters (e.g. {@code %20}) are decoded.
     *
     * @param src the raw {@code src} attribute or resolved URL.
     * @return the decoded base name without extension.
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

    // ── Control bar builder ──────────────────────────────────────────────────

    /**
     * Builds the playback control bar for a video player, including
     * play/pause (with replay icon at end-of-stream), time display,
     * progress bar, volume slider, and a fullscreen button.
     *
     * @param player the player to control.
     * @return the configured {@link HBox} control bar.
     */
    static HBox buildBar(final JavaCVPlayer player) {
        Button btn = new Button();
        Button backBtn = buildSkipBtn(player, -SKIP_SECONDS);
        Button fwdBtn = buildSkipBtn(player, SKIP_SECONDS);
        Label time = new Label("0:00 / 0:00");
        ProgressBar progress = new ProgressBar(0);
        Slider vol = new Slider(0, VOLUME_MAX, VOLUME_MAX);
        Button fsBtn = new Button();

        // ── Play/pause button ────────────────────────────────────────────────
        styleBtn(btn);
        refreshPlayPauseIcon(btn, false);
        player.playingProperty().addListener((obs, o, isPlaying) -> {
            boolean atEnd = !isPlaying
                    && player.getTotalSeconds() > 0
                    && player.getCurrentSeconds()
                            >= player.getTotalSeconds() - END_TOLERANCE_SECS;
            if (atEnd) {
                FontIcon replayIcon = new FontIcon(FontAwesomeSolid.REDO);
                replayIcon.setIconSize(ICON_SIZE);
                replayIcon.setIconColor(Color.WHITE);
                btn.setGraphic(replayIcon);
                btn.setOnAction(e -> player.play());
            } else {
                refreshPlayPauseIcon(btn, isPlaying);
                btn.setOnAction(e -> togglePlay(player));
            }
        });
        btn.setOnAction(e -> togglePlay(player));

        // ── Time label ───────────────────────────────────────────────────────
        time.getStyleClass().add("text-muted");
        time.setStyle("-fx-font-size: " + UIConstants.FONT_CAPTION + "px;");
        player.currentSecondsProperty().addListener((obs, o, n) ->
                time.setText(fmt(n.doubleValue()) + " / "
                        + fmt(player.getTotalSeconds())));
        player.totalSecondsProperty().addListener((obs, o, n) ->
                time.setText(fmt(player.getCurrentSeconds()) + " / "
                        + fmt(n.doubleValue())));

        // ── Progress bar ─────────────────────────────────────────────────────
        wireProgress(player, progress);

        // ── Volume slider ────────────────────────────────────────────────────
        vol.setPrefWidth(VOLUME_SLIDER_WIDTH);
        vol.setStyle("-fx-cursor: hand;");
        vol.valueProperty().addListener((obs, o, n) ->
                player.volumeProperty().set(n.doubleValue() / VOLUME_MAX));

        FontIcon volIcon = new FontIcon(FontAwesomeSolid.VOLUME_UP);
        volIcon.setIconSize(ICON_SIZE_SM);
        volIcon.getStyleClass().add("icon-muted");

        // ── Fullscreen button ────────────────────────────────────────────────
        styleBtn(fsBtn);
        FontIcon fsIcon = new FontIcon(FontAwesomeSolid.EXPAND);
        fsIcon.setIconSize(ICON_SIZE);
        fsIcon.setIconColor(Color.WHITE);
        fsBtn.setGraphic(fsIcon);
        fsBtn.setOnAction(e -> openFullscreen(player, fsBtn));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(UIConstants.SPACING_MD,
                btn, backBtn, fwdBtn, time, progress, volIcon, vol,
                spacer, fsBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(UIConstants.SPACING_MD, 0, 0, 0));
        return bar;
    }

    /**
     * Opens an in-app fullscreen overlay covering the entire scene. A
     * secondary {@link ImageView} that shares the player's decoded frame
     * buffer is placed in a black {@link StackPane} that is layered on top
     * of the scene root. No second decoder is created.
     *
     * <p>Pressing Esc or clicking the ✕ button closes the overlay and
     * removes all registered listeners.</p>
     *
     * @param player the player whose frames to display full-screen.
     * @param anchor any node currently in the scene (used to reach the root).
     */
    private static void openFullscreen(
            final JavaCVPlayer player, final Node anchor) {
        if (anchor.getScene() == null
                || !(anchor.getScene().getRoot() instanceof StackPane)) {
            return;
        }
        StackPane sceneRoot = (StackPane) anchor.getScene().getRoot();
        Scene scene = anchor.getScene();
        Stage stage = (Stage) scene.getWindow();

        ImageView fsView = player.createSecondaryImageView();
        fsView.setPreserveRatio(true);
        fsView.setOnMouseClicked(e -> togglePlay(player));
        MediaPlayerUtils.Controls fsControls =
                MediaPlayerUtils.buildControlBar(player);
        HBox fsBar = fsControls.getBar();
        fsBar.setMaxSize(Double.MAX_VALUE, Region.USE_PREF_SIZE);
        fsBar.getChildren().add(1, buildSkipBtn(player, -SKIP_SECONDS));
        fsBar.getChildren().add(2, buildSkipBtn(player, SKIP_SECONDS));
        fsBar.setStyle("-fx-background-color: rgba(0,0,0,0.75);");
        fsBar.setPadding(new Insets(UIConstants.SPACING_MD,
                UIConstants.SPACING_3XL,
                UIConstants.SPACING_MD, UIConstants.SPACING_3XL));

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: black;");
        overlay.prefWidthProperty().bind(sceneRoot.widthProperty());
        overlay.prefHeightProperty().bind(sceneRoot.heightProperty());
        fsView.fitWidthProperty().bind(overlay.widthProperty());
        fsView.fitHeightProperty().bind(overlay.heightProperty());

        Button closeBtn = buildOverlayCloseBtn();
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn,
                new Insets(UIConstants.SPACING_MD));
        StackPane.setAlignment(fsBar, Pos.BOTTOM_CENTER);
        overlay.getChildren().addAll(fsView, fsBar, closeBtn);

        Runnable[] closeRef = new Runnable[1];
        EventHandler<KeyEvent> escHandler = e -> {
            if (e.getCode() == KeyCode.ESCAPE && closeRef[0] != null) {
                closeRef[0].run();
            }
        };
        scene.addEventFilter(KeyEvent.KEY_PRESSED, escHandler);
        closeRef[0] = () -> {
            stage.setFullScreen(false);
            sceneRoot.getChildren().remove(overlay);
            player.removeSecondaryImageView(fsView);
            fsControls.unbind();
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, escHandler);
            fsView.fitWidthProperty().unbind();
            fsView.fitHeightProperty().unbind();
        };
        closeBtn.setOnAction(e -> closeRef[0].run());
        sceneRoot.getChildren().add(overlay);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
    }

    /**
     * Builds the ✕ button displayed in the top-right corner of the
     * fullscreen overlay, styled to be visible against a dark background.
     *
     * @return a configured close {@link Button}.
     */
    private static Button buildOverlayCloseBtn() {
        Button btn = new Button("✕");
        btn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.5);"
                + " -fx-text-fill: white;"
                + " -fx-font-size: " + UIConstants.FONT_BODY + "px;"
                + " -fx-background-radius: 4px;"
                + " -fx-cursor: hand;"
                + " -fx-padding: 4px 10px;");
        return btn;
    }

    private static void wireProgress(
            final JavaCVPlayer player, final ProgressBar progress) {
        progress.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progress, Priority.ALWAYS);
        progress.setStyle("-fx-cursor: hand;");
        player.currentSecondsProperty().addListener((obs, o, n) -> {
            double total = player.getTotalSeconds();
            if (total > 0) {
                progress.setProgress(n.doubleValue() / total);
            }
        });
        progress.setOnMouseClicked((MouseEvent e) -> {
            double frac = e.getX() / progress.getWidth();
            frac = Math.max(0.0, Math.min(1.0, frac));
            player.seek(frac * player.getTotalSeconds());
        });
    }

    private static void togglePlay(final JavaCVPlayer player) {
        if (player.playingProperty().get()) {
            player.pause();
        } else {
            player.play();
        }
    }

    private static void styleBtn(final Button btn) {
        btn.getStyleClass().add("btn-play");
    }

    /**
     * Builds a styled skip button that seeks {@code deltaSecs} seconds
     * relative to the current playback position. Negative values skip
     * backward; positive values skip forward. Seeking before 0 is clamped.
     *
     * @param player    the player to seek.
     * @param deltaSecs seconds to add to the current position (may be
     *                  negative).
     * @return a configured {@link Button}.
     */
    private static Button buildSkipBtn(
            final JavaCVPlayer player, final double deltaSecs) {
        Button b = new Button();
        styleBtn(b);
        FontIcon icon = new FontIcon(deltaSecs < 0
                ? FontAwesomeSolid.UNDO : FontAwesomeSolid.REDO);
        icon.setIconSize(ICON_SIZE);
        icon.setIconColor(Color.WHITE);
        b.setGraphic(icon);
        b.setOnAction(e -> player.seek(
                Math.max(0.0, player.getCurrentSeconds() + deltaSecs)));
        return b;
    }

    private static void refreshPlayPauseIcon(
            final Button btn, final boolean isPlaying) {
        FontIcon icon = new FontIcon(
                isPlaying ? FontAwesomeSolid.PAUSE : FontAwesomeSolid.PLAY);
        icon.setIconSize(ICON_SIZE);
        icon.setIconColor(Color.WHITE);
        btn.setGraphic(icon);
        btn.setText("");
    }

    private static String fmt(final double secs) {
        int total = (int) secs;
        int minutes = total / SECONDS_PER_MINUTE;
        int seconds = total % SECONDS_PER_MINUTE;
        return minutes + ":" + String.format("%02d", seconds);
    }

    // ── Inner state holder ───────────────────────────────────────────────────

    /**
     * Holds the per-clip container and lazily-created JavaCVPlayer.
     * A fresh player is created each time the slide is entered and fully
     * disposed on exit.
     */
    private static final class VideoEntry {

        /** Resolved asset URL. */
        private final String url;

        /** Outer VBox: holds the player's ImageView and control bar. */
        private final VBox container;

        /**
         * Player instance; null between slide visits.
         * Recreated on each startPlayback().
         */
        private JavaCVPlayer player;

        /** Control bar node; null between visits. */
        private HBox controlBar;

        VideoEntry(final String videoUrl) {
            this.url = videoUrl;
            container = new VBox(UIConstants.SPACING_MD);
            container.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(0, 0, UIConstants.SPACING_MD, 0));
        }

        String getUrl() {
            return url;
        }

        VBox getContainer() {
            return container;
        }

        void pausePlayback() {
            if (player != null) {
                player.pause();
            }
        }

        void startPlayback() {
            if (player != null) {
                player.play();
                return;
            }
            player = new JavaCVPlayer(url);
            ImageView imgView = player.getImageView();
            imgView.setOnMouseClicked(e -> togglePlay(player));
            imgView.setStyle("-fx-cursor: hand;");
            imgView.fitWidthProperty().bind(container.widthProperty());
            imgView.sceneProperty().addListener((obs, o, scene) -> {
                if (scene != null) {
                    imgView.fitHeightProperty().bind(
                            scene.heightProperty()
                                    .subtract(UIConstants.THUMBNAIL_HEIGHT));
                } else {
                    imgView.fitHeightProperty().unbind();
                }
            });
            controlBar = buildBar(player);
            container.getChildren().addAll(imgView, controlBar);
            player.errorMessageProperty().addListener((obs, o, err) -> {
                if (err != null) {
                    showError(err);
                }
            });
            player.play();
        }

        private void showError(final String err) {
            container.getChildren().clear();
            Label lbl = new Label("Video failed to load: " + err);
            lbl.getStyleClass().add("text-muted");
            lbl.setStyle("-fx-font-size: " + UIConstants.FONT_SMALL + "px;"
                    + " -fx-wrap-text: true;");
            container.getChildren().add(lbl);
        }

        void stopPlayback() {
            if (player == null) {
                return;
            }
            player.getImageView().fitWidthProperty().unbind();
            player.getImageView().fitHeightProperty().unbind();
            player.dispose();
            player = null;
            container.getChildren().clear();
            controlBar = null;
        }
    }
}
