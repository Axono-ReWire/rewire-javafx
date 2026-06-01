package com.axono.player.module;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * A self-contained video player backed by FFmpeg via JavaCV.
 *
 * <p>
 * Decoding runs on a dedicated daemon thread. Video frames are rendered
 * into a JavaFX {@link WritableImage} via {@link Platform#runLater}. Audio
 * is played through a {@link SourceDataLine} whose blocking {@code write}
 * calls naturally pace the decode loop. A PTS-based sleep provides
 * video-to-wall-clock synchronisation.
 * </p>
 *
 * <p>
 * Supports play, pause, seek, and volume control. Call {@link #dispose()}
 * when the player is no longer needed to release all native resources.
 * </p>
 */
public final class JavaCVPlayer {

    /** Maximum rendered width of the video frame in pixels. */
    private static final int MAX_VIDEO_WIDTH = 720;

    // fitHeight is intentionally left at 0 so preserveRatio=true calculates
    // the height automatically from fitWidth once the player is bound.

    /**
     * FFmpeg sample format constant for 16-bit signed interleaved PCM.
     * Matches {@code avutil.AV_SAMPLE_FMT_S16 = 1}.
     */
    private static final int SAMPLE_FMT_S16 = 1;

    /**
     * Fixed audio output sample rate. Forcing 44 100 Hz ensures the FFmpeg
     * resampler activates for any source rate and that Java Sound can always
     * open the line (some Windows drivers reject 48 000 Hz).
     */
    private static final int AUDIO_OUT_RATE = 44100;

    /**
     * Fixed audio output channel count. Sources with more or fewer channels
     * are remixed by the FFmpeg resampler.
     */
    private static final int AUDIO_OUT_CHANNELS = 2;

    /** Audio line buffer: approximately 100 ms at 44 100 Hz, stereo, 16-bit. */
    private static final int AUDIO_BUFFER_BYTES = 17640;

    /** Minimum nanoseconds to bother sleeping for A/V sync. */
    private static final long MIN_SLEEP_NS = 1_000_000L;

    /** Conversion factor from microseconds (PTS) to nanoseconds. */
    private static final long NS_PER_US = 1_000L;

    /** Divisor for PTS timestamp microseconds to seconds conversion. */
    private static final double PTS_MICROSECONDS_DIVISOR = 1_000_000.0;

    /** PTS divisor for seek timestamp calculation. */
    private static final long PTS_DIVISOR = 1_000_000L;

    /** Audio format: 16-bit PCM samples. */
    private static final int AUDIO_BITS = 16;

    /** Decibel multiplier for volume logarithm calculation. */
    private static final double DB_MULTIPLIER = 20.0;

    /** Minimum volume threshold for logarithm calculation. */
    private static final double MIN_VOLUME = 0.0001;

    /** Sleep divisor: nanoseconds to milliseconds. */
    private static final long SLEEP_DIVISOR = 1_000_000L;

    // ── Identity ─────────────────────────────────────────────────────────────

    /** Resolved URL or local file path to the media asset. */
    private final String url;

    // ── JavaFX surface ───────────────────────────────────────────────────────

    /** Pixel buffer written by the decode thread via runLater. */
    private WritableImage writableImage;

    /** ImageView backed by writableImage; returned to slide layout. */
    private final ImageView imageView;

    /** Additional ImageViews that share the same WritableImage. */
    private final java.util.List<ImageView> secondaryViews
            = new java.util.ArrayList<>();

    // ── Observable properties ────────────────────────────────────────────────

    /** Current playback position in seconds. */
    private final SimpleDoubleProperty currentSeconds
            = new SimpleDoubleProperty(0.0);

    /** Total media duration in seconds. */
    private final SimpleDoubleProperty totalSeconds
            = new SimpleDoubleProperty(0.0);

    /** True when actively decoding and playing. */
    private final SimpleBooleanProperty playing
            = new SimpleBooleanProperty(false);

    /** Volume in [0.0, 1.0]. Writable by the control bar. */
    private final SimpleDoubleProperty volume = new SimpleDoubleProperty(1.0);

    /** Error message from decode thread; null during normal operation. */
    private final SimpleStringProperty errorMessage
            = new SimpleStringProperty(null);

    /** Callback invoked on the JavaFX thread when the stream ends naturally. */
    private Runnable onEndOfMedia;

    // ── Decode thread control (volatile for cross-thread visibility) ─────────

    /** Active state of the decode thread. */
    private volatile boolean active;

    /** Paused state of playback. */
    private volatile boolean paused;

    /** Pending seek target in seconds; negative means no pending seek. */
    private volatile double seekTarget = -1.0;

    // ── Native resources (owned exclusively by the decode thread) ────────────

    /** Decode worker thread. */
    private Thread decodeThread;

    /** FFmpeg frame grabber for decoding. */
    private FFmpegFrameGrabber grabber;

    /** Audio output line for playback. */
    private SourceDataLine audioLine;

    /** Total audio samples decoded so far (for duration estimation). */
    private long totalSamplesDecoded = 0;

    // ── Decode-thread timing state ───────────────────────────────────────────

    /** Wall-clock nanoseconds at the reference PTS. */
    private long clockWallNs;

    /** Media PTS (microseconds) corresponding to clockWallNs. */
    private long clockPtsUs;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Constructs a player for the given media URL.
     *
     * @param mediaUrl a {@code file:///} URI or HTTP URL.
     */
    public JavaCVPlayer(final String mediaUrl) {
        this.url = mediaUrl;
        this.writableImage = new WritableImage(1, 1);
        this.imageView = new ImageView(writableImage);
        this.imageView.setPreserveRatio(true);
        this.imageView.setFitWidth(MAX_VIDEO_WIDTH);
    }

    /**
     * Returns the JavaFX node that renders video frames.
     *
     * @return the ImageView displaying video frames
     */
    public ImageView getImageView() {
        return imageView;
    }

    /**
     * Creates an additional {@link ImageView} that shares the same
     * {@link WritableImage} backing store as the primary view. Any frame
     * rendered by the decode thread will appear in both views simultaneously
     * without requiring a second decoder instance. The view is kept in sync
     * automatically if the underlying image is resized after metadata loads.
     *
     * <p>
     * Useful for fullscreen overlays: place this view in a separate
     * {@link javafx.stage.Stage} while leaving the primary view in the
     * main slide layout. Call {@link #removeSecondaryImageView(ImageView)}
     * when the overlay is closed.
     * </p>
     *
     * @return a new {@link ImageView} registered with this player.
     */
    public ImageView createSecondaryImageView() {
        ImageView secondary = new ImageView(writableImage);
        secondary.setPreserveRatio(true);
        secondaryViews.add(secondary);
        return secondary;
    }

    /**
     * Removes a secondary {@link ImageView} previously created by
     * {@link #createSecondaryImageView()} so it is no longer updated.
     *
     * @param view the view to deregister.
     */
    public void removeSecondaryImageView(final ImageView view) {
        secondaryViews.remove(view);
    }

    /**
     * Current playback position, in seconds. Updated on the JavaFX thread.
     *
     * @return property for current playback position
     */
    public ReadOnlyDoubleProperty currentSecondsProperty() {
        return currentSeconds;
    }

    /**
     * Total media duration in seconds (0 until media metadata is ready).
     *
     * @return property for total media duration
     */
    public ReadOnlyDoubleProperty totalSecondsProperty() {
        return totalSeconds;
    }

    /**
     * Whether the player is actively decoding and playing.
     *
     * @return property indicating active playback state
     */
    public ReadOnlyBooleanProperty playingProperty() {
        return playing;
    }

    /**
     * Volume property in [0.0, 1.0]; writable.
     *
     * @return property for volume control
     */
    public SimpleDoubleProperty volumeProperty() {
        return volume;
    }

    /**
     * Error message from the decode thread.
     *
     * @return property for error messages
     */
    public ReadOnlyStringProperty errorMessageProperty() {
        return errorMessage;
    }

    /**
     * Snapshot of the current position, safe to call from any thread.
     *
     * @return current playback position in seconds
     */
    public double getCurrentSeconds() {
        return currentSeconds.get();
    }

    /**
     * Snapshot of the total duration, safe to call from any thread.
     *
     * @return total media duration in seconds
     */
    public double getTotalSeconds() {
        return totalSeconds.get();
    }

    /**
     * Starts or resumes playback.
     *
     * <ul>
     * <li>First call: creates the decode thread and begins decoding.</li>
     * <li>After {@link #pause()}: wakes the paused loop.</li>
     * <li>After end-of-stream: restarts the decode thread from the
     * beginning of the media (replay).</li>
     * </ul>
     */
    public void play() {
        if (active && !paused && playing.get()) {
            return; // genuinely already playing — nothing to do
        }
        // If the stream ended (active=true, paused=false, playing=false),
        // shut down the dead decode thread and restart from the beginning.
        if (active && !paused) {
            active = false;
            if (decodeThread != null) {
                decodeThread.interrupt();
            }
            seekTarget = 0.0; // restart from the beginning
        }
        if (!active) {
            active = true;
            decodeThread = new Thread(this::runDecodeLoop, "javacv-decode");
            decodeThread.setDaemon(true);
            decodeThread.start();
        }
        paused = false;
        playing.set(true);
        synchronized (this) {
            notifyAll();
        }
    }

    /** Pauses playback without releasing any resources. */
    public void pause() {
        paused = true;
        playing.set(false);
    }

    /**
     * Seeks to the given position. Works while paused or playing; the decode
     * thread renders one frame at the target position before re-pausing.
     *
     * @param seconds the target position in seconds.
     */
    public void seek(final double seconds) {
        seekTarget = seconds;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Sets a callback invoked on the JavaFX thread when the media stream ends
     * naturally (i.e. not because of {@link #dispose()} or {@link #pause()}).
     *
     * @param callback the runnable to invoke at end-of-stream, or {@code null}
     *                 to clear any previously set callback.
     */
    public void setOnEndOfMedia(final Runnable callback) {
        this.onEndOfMedia = callback;
    }

    /** Stops playback and releases all native resources. */
    public void dispose() {
        active = false;
        paused = false;
        playing.set(false);
        synchronized (this) {
            notifyAll();
        }
        if (decodeThread != null) {
            decodeThread.interrupt();
        }
    }

    // ── Decode loop ──────────────────────────────────────────────────────────

    private void runDecodeLoop() {
        try {
            openAndPlay();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.println("[JavaCVPlayer] Failed to play: " + url);
            ex.printStackTrace();
            final String msg = ex.toString();
            Platform.runLater(() -> errorMessage.set(msg));
        } finally {
            closeResources();
        }
    }

    private void openAndPlay() throws Exception {
        grabber = new FFmpegFrameGrabber(toFilePath(url));
        grabber.setSampleFormat(SAMPLE_FMT_S16);
        grabber.start();
        // Capture native sample rate before overriding with the forced output
        // rate. A non-zero value means the file has an audio stream.
        boolean hasAudio = grabber.getSampleRate() > 0;
        if (hasAudio) {
            // Force output to 44 100 Hz stereo. The FFmpeg resampler is lazy
            // (created on first audio grab), so setting these after start() is
            // safe and guarantees a universally-supported Java Sound format.
            grabber.setSampleRate(AUDIO_OUT_RATE);
            grabber.setAudioChannels(AUDIO_OUT_CHANNELS);
        }
        publishMediaInfo();
        if (hasAudio) {
            openAudioLine(AUDIO_OUT_RATE, AUDIO_OUT_CHANNELS);
        }

        Java2DFrameConverter converter = new Java2DFrameConverter();
        clockWallNs = System.nanoTime();
        clockPtsUs = -1L;
        // reset sample counter for new playback session
        totalSamplesDecoded = 0;

        while (active) {
            Frame frame = grabber.grab();
            if (frame == null) {
                // For audio-only files whose container doesn't report a
                // duration upfront (e.g. some MP3/VBR files), use the final
                // decoded position as the total so the timer is correct after
                // the stream ends.
                if (totalSeconds.get() <= 0) {
                    final double last = currentSeconds.get();
                    Platform.runLater(() -> totalSeconds.set(last));
                }
                final Runnable cb = onEndOfMedia;
                if (cb != null) {
                    Platform.runLater(cb);
                }
                break;
            }
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return;
            }
            if (handleSeek()) {
                continue;
            }
            awaitResume();
            if (!active) {
                break;
            }
            if (frame.image != null) {
                handleVideoFrame(frame, converter);
            } else if (frame.samples != null && audioLine != null) {
                writeAudio(frame.samples);
            }
        }

        Platform.runLater(() -> playing.set(false));
    }

    private void publishMediaInfo() {
        long rawDur = grabber.getLengthInTime();
        double dur = rawDur > 0 ? rawDur / PTS_MICROSECONDS_DIVISOR : 0.0;
        int vw = grabber.getImageWidth();
        int vh = grabber.getImageHeight();
        Platform.runLater(() -> {
            totalSeconds.set(dur);
            if (vw > 0 && vh > 0) {
                writableImage = new WritableImage(vw, vh);
                imageView.setImage(writableImage);
                // fitHeight left unset — preserveRatio=true auto-calculates
                // from fitWidth, which is bound to the container externally.
                for (ImageView sv : secondaryViews) {
                    sv.setImage(writableImage);
                }
            }
        });
    }

    private void openAudioLine(final int sampleRate, final int channels) {
        if (sampleRate <= 0 || channels <= 0) {
            return;
        }
        try {
            AudioFormat fmt = new AudioFormat(
                    sampleRate, AUDIO_BITS, channels, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(fmt, AUDIO_BUFFER_BYTES);
            audioLine.start();
            applyVolume();
            volume.addListener((obs, o, n) -> applyVolume());
        } catch (LineUnavailableException ex) {
            String msg = "[JavaCVPlayer] Audio unavailable: "
                    + ex.getMessage();
            System.err.println(msg);
            audioLine = null;
        }
    }

    private boolean handleSeek() throws Exception {
        double seek = seekTarget;
        if (seek < 0.0) {
            return false;
        }
        seekTarget = -1.0;
        long seekTimestamp = (long) (seek * PTS_DIVISOR);
        grabber.setTimestamp(seekTimestamp, true);
        if (audioLine != null) {
            audioLine.flush();
        }
        clockWallNs = System.nanoTime();
        clockPtsUs = -1L;
        final double seekCopy = seek;
        Platform.runLater(() -> currentSeconds.set(seekCopy));
        return true;
    }

    private synchronized void awaitResume() throws InterruptedException {
        while (paused && active && seekTarget < 0.0) {
            wait();
        }
    }

    private void handleVideoFrame(
            final Frame frame,
            final Java2DFrameConverter converter) throws InterruptedException {
        long pts = grabber.getTimestamp();
        if (clockPtsUs < 0L) {
            clockPtsUs = pts;
            clockWallNs = System.nanoTime();
        }
        long ptsOffset = (pts - clockPtsUs) * NS_PER_US;
        long sleepNs = clockWallNs + ptsOffset - System.nanoTime();
        if (sleepNs > MIN_SLEEP_NS) {
            Thread.sleep(sleepNs / SLEEP_DIVISOR,
                    (int) (sleepNs % SLEEP_DIVISOR));
        }
        BufferedImage bi = copyImage(converter.convert(frame));
        if (bi == null) {
            return;
        }
        final long ptsCopy = pts;
        final BufferedImage biCopy = bi;
        Platform.runLater(() -> {
            // Only drive the timer from video PTS when there is no audio line.
            // When audio is present, writeAudio() is the single source of truth
            // for currentSeconds, which prevents flickering caused by the two
            // streams having slightly different PTS values.
            if (audioLine == null) {
                currentSeconds.set(ptsCopy / PTS_MICROSECONDS_DIVISOR);
            }
            renderFrame(biCopy);
        });
    }

    private void writeAudio(final Buffer[] samples) {
        if (samples == null || samples.length == 0) {
            return;
        }

        // Count samples in this batch BEFORE processing, for duration
        // estimation when the file lacks proper duration metadata.
        int samplesPerChannel = 0;
        if (samples[0] instanceof ShortBuffer) {
            samplesPerChannel = ((ShortBuffer) samples[0]).remaining();
        } else if (samples[0] instanceof FloatBuffer) {
            samplesPerChannel = ((FloatBuffer) samples[0]).remaining();
        }
        totalSamplesDecoded += samplesPerChannel;

        // Planar formats (FLTP, S16P) deliver one buffer per channel; they
        // must be interleaved before writing to a multi-channel SourceDataLine.
        byte[] bytes = samples.length == 1
                ? toAudioBytes(samples[0])
                : interleaveSamples(samples);
        if (bytes != null) {
            audioLine.write(bytes, 0, bytes.length);
        }
        final long pts = grabber.getTimestamp();
        clockWallNs = System.nanoTime();
        clockPtsUs = pts;
        final long decodedSamples = totalSamplesDecoded;
        Platform.runLater(() -> {
            currentSeconds.set(pts / PTS_MICROSECONDS_DIVISOR);

            // If duration is unknown, estimate from decoded sample count.
            // This allows playback of audio files with missing STREAMINFO
            // metadata (e.g., some FLAC files) to show progress.
            // Use the output sample rate we configured (not
            // grabber.getSampleRate(), which may be ambiguous after
            // setSampleRate() is called).
            if (totalSeconds.get() <= 0 && audioLine != null) {
                double estimatedDuration = decodedSamples
                        / (double) AUDIO_OUT_RATE;
                totalSeconds.set(estimatedDuration);
            }
        });
    }

    /**
     * Interleaves per-channel sample buffers (planar audio) into a single
     * PCM S16 LE byte array suitable for a multi-channel
     * {@link SourceDataLine}. Handles both {@link ShortBuffer} (S16P) and
     * {@link FloatBuffer} (FLTP) channel buffers.
     *
     * @param samples per-channel buffers, one per audio channel.
     * @return interleaved PCM S16 LE bytes, or {@code null} if unsupported.
     */
    private static byte[] interleaveSamples(final Buffer[] samples) {
        int numCh = samples.length;
        Buffer first = samples[0];
        int perCh;
        if (first instanceof ShortBuffer) {
            perCh = ((ShortBuffer) first).remaining();
        } else if (first instanceof FloatBuffer) {
            perCh = ((FloatBuffer) first).remaining();
        } else {
            return null;
        }
        byte[] out = new byte[perCh * numCh * 2];
        ByteBuffer bb = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN);
        if (first instanceof FloatBuffer) {
            FloatBuffer[] fbs = new FloatBuffer[numCh];
            for (int ch = 0; ch < numCh; ch++) {
                fbs[ch] = ((FloatBuffer) samples[ch]).duplicate();
            }
            for (int i = 0; i < perCh; i++) {
                for (int ch = 0; ch < numCh; ch++) {
                    float s = Math.max(-1.0f, Math.min(1.0f, fbs[ch].get()));
                    bb.putShort((short) (s * Short.MAX_VALUE));
                }
            }
        } else {
            ShortBuffer[] sbs = new ShortBuffer[numCh];
            for (int ch = 0; ch < numCh; ch++) {
                sbs[ch] = ((ShortBuffer) samples[ch]).duplicate();
            }
            for (int i = 0; i < perCh; i++) {
                for (int ch = 0; ch < numCh; ch++) {
                    bb.putShort(sbs[ch].get());
                }
            }
        }
        return out;
    }

    /**
     * Converts a decoded audio sample buffer to raw PCM S16 LE bytes.
     * Handles both interleaved S16 ({@link ShortBuffer}) and planar/interleaved
     * FLTP ({@link FloatBuffer}) formats, which both appear in MOV containers
     * depending on the audio codec and whether FFmpeg's resampler activates.
     *
     * @param buf a sample buffer from {@link Frame#samples}.
     * @return PCM bytes ready for {@link SourceDataLine#write}, or {@code null}
     *         if the buffer type is not supported.
     */
    private static byte[] toAudioBytes(final Buffer buf) {
        if (buf instanceof ShortBuffer) {
            ShortBuffer sb = ((ShortBuffer) buf).duplicate();
            byte[] bytes = new byte[sb.remaining() * 2];
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer().put(sb);
            return bytes;
        }
        if (buf instanceof FloatBuffer) {
            FloatBuffer fb = ((FloatBuffer) buf).duplicate();
            byte[] bytes = new byte[fb.remaining() * 2];
            ByteBuffer bb = ByteBuffer.wrap(bytes)
                    .order(ByteOrder.LITTLE_ENDIAN);
            while (fb.hasRemaining()) {
                float s = Math.max(-1.0f, Math.min(1.0f, fb.get()));
                bb.putShort((short) (s * Short.MAX_VALUE));
            }
            return bytes;
        }
        if (buf instanceof IntBuffer) {
            // S32 (24-bit LPCM stored in upper bits): top 16 bits → S16
            IntBuffer ib = ((IntBuffer) buf).duplicate();
            byte[] bytes = new byte[ib.remaining() * 2];
            ByteBuffer bb = ByteBuffer.wrap(bytes)
                    .order(ByteOrder.LITTLE_ENDIAN);
            final int shiftBits = 16;
            while (ib.hasRemaining()) {
                bb.putShort((short) (ib.get() >> shiftBits));
            }
            return bytes;
        }
        return null;
    }

    private void renderFrame(final BufferedImage bi) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        int imgWidth = (int) writableImage.getWidth();
        int imgHeight = (int) writableImage.getHeight();
        if (imgWidth != w || imgHeight != h) {
            writableImage = new WritableImage(w, h);
            imageView.setImage(writableImage);
            for (ImageView sv : secondaryViews) {
                sv.setImage(writableImage);
            }
        }
        int[] pixels = new int[w * h];
        bi.getRGB(0, 0, w, h, pixels, 0, w);
        writableImage.getPixelWriter().setPixels(
                0, 0, w, h,
                PixelFormat.getIntArgbInstance(), pixels, 0, w);
    }

    private void applyVolume() {
        if (audioLine == null
                || !audioLine.isControlSupported(
                        FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) audioLine.getControl(
                FloatControl.Type.MASTER_GAIN);
        double v = volume.get();
        float db = v <= 0.0
                ? gain.getMinimum()
                : (float) (DB_MULTIPLIER
                        * Math.log10(Math.max(v, MIN_VOLUME)));
        float minGain = gain.getMinimum();
        float maxGain = gain.getMaximum();
        gain.setValue(Math.max(minGain, Math.min(maxGain, db)));
    }

    private static String toFilePath(final String rawUrl) {
        if (rawUrl == null || !rawUrl.startsWith("file:")) {
            return rawUrl;
        }
        try {
            return new File(new URI(rawUrl)).getAbsolutePath();
        } catch (URISyntaxException ex) {
            return rawUrl.replaceFirst("^file:/{1,3}", "");
        }
    }

    private static BufferedImage copyImage(final BufferedImage src) {
        if (src == null) {
            return null;
        }
        BufferedImage copy = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.createGraphics().drawImage(src, 0, 0, null);
        return copy;
    }

    private void closeResources() {
        if (audioLine != null) {
            try {
                audioLine.drain();
                audioLine.stop();
                audioLine.close();
            } catch (Exception ignored) {
                audioLine = null;
            }
            audioLine = null;
        }
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception ignored) {
                grabber = null;
            }
            grabber = null;
        }
    }
}
