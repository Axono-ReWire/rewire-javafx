package com.axono.player.module;

import com.axono.ui.UIConstants;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Shared utility methods for building media player controls in
 * {@link VideoModule} and {@link AudioModule}.
 *
 * <p>All methods are static. Controls are wired to a {@link MediaPlayer}
 * after it has been created (typically in {@code onEnter()}), so this
 * class supports lazy-player initialisation patterns.</p>
 *
 * <p>Controls are styled to remain consistent with the rest of
 * the application.</p>
 */
public final class MediaPlayerUtils {

    /** Icon size used on play/pause and volume buttons. */
    private static final int ICON_SIZE = 20;

    /** Smaller icon for the volume indicator. */
    private static final int ICON_SIZE_SM = 16;

    /** Width of the volume slider in pixels. */
    private static final double VOLUME_SLIDER_WIDTH = 80.0;

    /** Maximum value of the volume slider (maps to 1.0 gain). */
    private static final double VOLUME_MAX = 100.0;

    /** Seconds per minute used when formatting durations. */
    private static final int SECONDS_PER_MINUTE = 60;

    /** Utility class — not instantiable. */
    private MediaPlayerUtils() {
    }

    /**
     * Formats a {@link Duration} as {@code "M:SS"} (e.g. {@code "2:05"}).
     * Returns {@code "0:00"} for null or unknown durations.
     *
     * @param d the duration to format; may be {@code null} or
     *          {@link Duration#UNKNOWN}.
     * @return a non-null, formatted string.
     */
    public static String formatDuration(final Duration d) {
        if (d == null || d.isUnknown() || d.isIndefinite()) {
            return "0:00";
        }
        int totalSeconds = (int) d.toSeconds();
        int minutes = totalSeconds / SECONDS_PER_MINUTE;
        int seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Formats a duration in seconds as {@code "M:SS"} (e.g. {@code "2:05"}).
     * Returns {@code "0:00"} for negative values.
     *
     * @param secs the duration in seconds.
     * @return a non-null, formatted string.
     */
    public static String formatSeconds(final double secs) {
        int totalSecs = (int) Math.max(0.0, secs);
        int minutes = totalSecs / SECONDS_PER_MINUTE;
        int seconds = totalSecs % SECONDS_PER_MINUTE;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Wires a {@link Label} to display {@code "current / total"} playback
     * time, updated on every current-time tick.
     *
     * @param player the media player to observe.
     * @param label  the label to update.
     * @return the {@link ChangeListener} installed, so callers can remove it.
     */
    public static ChangeListener<Duration> bindTimeLabel(
            final MediaPlayer player, final Label label) {
        label.setText("0:00 / 0:00");
        label.getStyleClass().add("text-muted");
        label.setStyle("-fx-font-size: " + UIConstants.FONT_CAPTION + "px;");

        ChangeListener<Duration> listener = (obs, oldVal, newVal) -> {
            Duration total = player.getTotalDuration();
            label.setText(
                    formatDuration(newVal) + " / " + formatDuration(total));
        };
        player.currentTimeProperty().addListener(listener);
        return listener;
    }

    /**
     * Binds a {@link ProgressBar} to the player's current playback position
     * (range 0.0–1.0) and wires a mouse-click handler so that clicking seeks
     * to that proportional position in the media.
     *
     * @param player the media player to bind.
     * @param bar    the progress bar to update and wire for seeking.
     * @return the {@link ChangeListener} installed, so callers can remove it.
     */
    public static ChangeListener<Duration> bindProgressBar(
            final MediaPlayer player, final ProgressBar bar) {
        bar.setProgress(0.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);

        ChangeListener<Duration> listener = (obs, oldVal, newVal) -> {
            Duration total = player.getTotalDuration();
            if (total != null && !total.isUnknown() && total.toMillis() > 0) {
                bar.setProgress(newVal.toMillis() / total.toMillis());
            }
        };
        player.currentTimeProperty().addListener(listener);

        bar.setOnMouseClicked((MouseEvent e) -> {
            Duration total = player.getTotalDuration();
            if (total != null && !total.isUnknown()) {
                double fraction = e.getX() / bar.getWidth();
                fraction = Math.max(0.0, Math.min(1.0, fraction));
                player.seek(total.multiply(fraction));
            }
        });
        bar.setStyle("-fx-cursor: hand;");
        return listener;
    }

    /**
     * Creates a {@link Slider} wired for volume control. The range is 0–100
     * (display) mapped to 0.0–1.0 gain. The initial position reflects the
     * player's current volume.
     *
     * @param player the media player whose volume to control.
     * @return a configured, bound {@link Slider}.
     */
    public static Slider createVolumeSlider(final MediaPlayer player) {
        Slider slider = new Slider(0, VOLUME_MAX,
                player.getVolume() * VOLUME_MAX);
        slider.setPrefWidth(VOLUME_SLIDER_WIDTH);
        slider.setStyle("-fx-cursor: hand;");

        slider.valueProperty().addListener((obs, oldVal, newVal) ->
                player.setVolume(newVal.doubleValue() / VOLUME_MAX));
        return slider;
    }

    /**
     * Wires a {@link Button} as a play/pause toggle for the given player.
     * The button's graphic (icon) changes to reflect the current state.
     *
     * @param player the media player to control.
     * @param button the button to wire and update.
     */
    public static void bindPlayPauseButton(
            final MediaPlayer player, final Button button) {
        button.getStyleClass().add("btn-play");
        updatePlayPauseIcon(button, false);

        button.setOnAction(e -> {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
            } else {
                player.play();
            }
        });

        player.statusProperty().addListener((obs, oldS, newS) ->
                updatePlayPauseIcon(button,
                        newS == MediaPlayer.Status.PLAYING));
    }

    /**
     * Builds a complete control bar (play/pause, time, progress, volume)
     * bound to the supplied {@link MediaPlayer}.
     *
     * @param player the media player to control.
     * @return a {@link Controls} containing the HBox view and an unbind action.
     */
    public static Controls buildControlBar(final MediaPlayer player) {
        Button playPauseBtn = new Button();
        Label timeLabel = new Label("0:00 / 0:00");
        ProgressBar progressBar = new ProgressBar(0);
        Slider volumeSlider = createVolumeSlider(player);

        bindPlayPauseButton(player, playPauseBtn);
        ChangeListener<Duration> timeCl = bindTimeLabel(player, timeLabel);
        ChangeListener<Duration> progCl = bindProgressBar(player, progressBar);

        FontIcon volIcon = new FontIcon(FontAwesomeSolid.VOLUME_UP);
        volIcon.setIconSize(ICON_SIZE_SM);
        volIcon.getStyleClass().add("icon-muted");

        HBox bar = new HBox(UIConstants.SPACING_MD,
                playPauseBtn, timeLabel, progressBar, volIcon, volumeSlider);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(UIConstants.SPACING_MD, 0, 0, 0));

        return new Controls(bar, () -> {
            player.currentTimeProperty().removeListener(timeCl);
            player.currentTimeProperty().removeListener(progCl);
        });
    }

    /**
     * Builds a complete control bar (play/pause, time, progress, volume)
     * bound to the supplied {@link JavaCVPlayer}.
     *
     * <p>This overload uses {@link JavaCVPlayer#currentSecondsProperty()} and
     * {@link JavaCVPlayer#playingProperty()} rather than JavaFX
     * {@link MediaPlayer} APIs, enabling FFmpeg-backed audio playback.</p>
     *
     * @param player the JavaCV player to control.
     * @return a {@link Controls} containing the HBox view and an unbind action.
     */
    public static Controls buildControlBar(final JavaCVPlayer player) {
        Button btn = new Button();
        Label time = new Label("0:00 / 0:00");
        ProgressBar progress = new ProgressBar(0);
        Slider vol = new Slider(0, VOLUME_MAX,
                player.volumeProperty().get() * VOLUME_MAX);

        stylePlayBtn(btn);
        updatePlayPauseIcon(btn, false);
        ChangeListener<Boolean> playCl = buildPlayingListener(player, btn);
        btn.setOnAction(e -> toggleCvPlay(player));
        player.playingProperty().addListener(playCl);

        ChangeListener<Number> timeCl = bindCvTimeLabel(player, time);
        ChangeListener<Number> totalCl = bindCvTotalLabel(player, time);
        ChangeListener<Number> progCl = bindCvProgress(player, progress);

        vol.setPrefWidth(VOLUME_SLIDER_WIDTH);
        vol.setStyle("-fx-cursor: hand;");
        vol.valueProperty().addListener((obs, o, n) ->
                player.volumeProperty().set(n.doubleValue() / VOLUME_MAX));

        FontIcon volIcon = new FontIcon(FontAwesomeSolid.VOLUME_UP);
        volIcon.setIconSize(ICON_SIZE_SM);
        volIcon.getStyleClass().add("icon-muted");

        HBox bar = new HBox(UIConstants.SPACING_MD,
                btn, time, progress, volIcon, vol);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(UIConstants.SPACING_MD, 0, 0, 0));

        return new Controls(bar, () -> {
            player.playingProperty().removeListener(playCl);
            player.currentSecondsProperty().removeListener(timeCl);
            player.totalSecondsProperty().removeListener(totalCl);
            player.currentSecondsProperty().removeListener(progCl);
        });
    }

    /**
     * Builds the playing-state listener for a JavaCVPlayer control bar.
     * Shows a replay icon when the stream ends naturally.
     *
     * @param player the player to monitor.
     * @param btn    the play/pause button to update.
     * @return a change listener for the playing property.
     */
    private static ChangeListener<Boolean> buildPlayingListener(
            final JavaCVPlayer player, final Button btn) {
        return (obs, o, isPlaying) -> {
            boolean atEnd = !isPlaying
                    && player.getTotalSeconds() > 0
                    && player.getCurrentSeconds()
                            >= player.getTotalSeconds() - 1.0;
            if (atEnd) {
                FontIcon replayIcon = new FontIcon(FontAwesomeSolid.REDO);
                replayIcon.setIconSize(ICON_SIZE);
                replayIcon.setIconColor(Color.WHITE);
                btn.setGraphic(replayIcon);
                btn.setOnAction(e -> player.play());
            } else {
                updatePlayPauseIcon(btn, isPlaying);
                btn.setOnAction(e -> toggleCvPlay(player));
            }
        };
    }

    /**
     * Binds the time label to the current-seconds property.
     *
     * @param player the player to monitor.
     * @param time   the label to update.
     * @return a change listener for the current-seconds property.
     */
    private static ChangeListener<Number> bindCvTimeLabel(
            final JavaCVPlayer player, final Label time) {
        time.getStyleClass().add("text-muted");
        time.setStyle("-fx-font-size: " + UIConstants.FONT_CAPTION + "px;");
        ChangeListener<Number> cl = (obs, o, n) -> time.setText(
                formatSeconds(n.doubleValue()) + " / "
                + formatSeconds(player.getTotalSeconds()));
        player.currentSecondsProperty().addListener(cl);
        return cl;
    }

    /**
     * Refreshes the time label whenever the total duration becomes known.
     *
     * @param player the player to monitor.
     * @param time   the label to update.
     * @return a change listener for the total-seconds property.
     */
    private static ChangeListener<Number> bindCvTotalLabel(
            final JavaCVPlayer player, final Label time) {
        ChangeListener<Number> cl = (obs, o, n) -> time.setText(
                formatSeconds(player.getCurrentSeconds()) + " / "
                + formatSeconds(n.doubleValue()));
        player.totalSecondsProperty().addListener(cl);
        return cl;
    }

    /**
     * Binds the progress bar to the current/total seconds ratio.
     *
     * @param player   the player to monitor.
     * @param progress the progress bar to update.
     * @return a change listener for the current-seconds property.
     */
    private static ChangeListener<Number> bindCvProgress(
            final JavaCVPlayer player, final ProgressBar progress) {
        progress.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progress, Priority.ALWAYS);
        progress.setStyle("-fx-cursor: hand;");
        ChangeListener<Number> cl = (obs, o, n) -> {
            double total = player.getTotalSeconds();
            if (total > 0) {
                progress.setProgress(n.doubleValue() / total);
            }
        };
        player.currentSecondsProperty().addListener(cl);
        progress.setOnMouseClicked((MouseEvent e) -> {
            double frac = e.getX() / progress.getWidth();
            player.seek(Math.max(0.0, Math.min(1.0, frac))
                    * player.getTotalSeconds());
        });
        return cl;
    }

    /**
     * Applies the primary-coloured button style used by JavaCV control bars.
     *
     * @param btn the button to style.
     */
    private static void stylePlayBtn(final Button btn) {
        btn.getStyleClass().add("btn-play");
    }

    /**
     * Toggles play/pause for a {@link JavaCVPlayer}.
     *
     * @param player the player to control.
     */
    private static void toggleCvPlay(final JavaCVPlayer player) {
        if (player.playingProperty().get()) {
            player.pause();
        } else {
            player.play();
        }
    }

    /**
     * Invokes the unbind action stored in the given {@link Controls} instance,
     * removing any listeners registered during control-bar construction.
     *
     * @param controls the controls to unbind; safely ignored if {@code null}.
     */
    public static void unbindControls(final Controls controls) {
        if (controls != null) {
            controls.unbind();
        }
    }

    /**
     * Updates a button's graphic to a play or pause {@link FontIcon}.
     *
     * @param button  the button to update.
     * @param playing {@code true} to show the pause icon.
     */
    private static void updatePlayPauseIcon(
            final Button button, final boolean playing) {
        FontIcon icon = new FontIcon(
                playing ? FontAwesomeSolid.PAUSE : FontAwesomeSolid.PLAY);
        icon.setIconSize(ICON_SIZE);
        icon.setIconColor(Color.WHITE);
        button.setGraphic(icon);
        button.setText("");
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Holds the rendered control bar and a cleanup action that removes all
     * listeners registered during bar construction.
     *
     * <p>Works with both {@link MediaPlayer}-based and
     * {@link JavaCVPlayer}-based control bars; the concrete listener types
     * are captured in the {@code unbindAction} closure.</p>
     */
    public static final class Controls {

        /** The rendered control bar HBox. */
        private final HBox bar;

        /** Removes all listeners registered during control-bar construction. */
        private final Runnable unbindAction;

        /**
         * Constructs a Controls holder.
         *
         * @param controlBar the rendered HBox.
         * @param action     runnable that removes all registered listeners.
         */
        public Controls(final HBox controlBar,
                final Runnable action) {
            this.bar = controlBar;
            this.unbindAction = action;
        }

        /**
         * Returns the rendered control bar.
         *
         * @return the {@link HBox}.
         */
        public HBox getBar() {
            return bar;
        }

        /**
         * Removes all listeners and handlers registered during control-bar
         * construction, so disposed players do not retain stale UI references.
         */
        public void unbind() {
            if (unbindAction != null) {
                unbindAction.run();
            }
        }
    }
}
