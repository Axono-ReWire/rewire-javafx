package com.axono.content;

/**
 * Immutable metadata record for a single media file (image, video, or audio)
 * referenced from learning content. Each asset is serialised as an
 * {@code <asset>} element inside the content XML's {@code <mediaAssets>}
 * block and loaded by {@link LearningContentParser}.
 *
 * <p>Use {@link Builder} to construct instances without violating the
 * seven-parameter limit enforced by the project's Checkstyle rules.</p>
 *
 * <p>The {@link #getRelativePath()} is relative to the content's base
 * directory. Use {@link MediaAssetRegistry#resolveUrl(String)} to convert
 * a {@code src} attribute value to a playable URL.</p>
 *
 * <p>Known {@link #getMediaType()} values: {@code IMAGE}, {@code VIDEO},
 * {@code AUDIO}.</p>
 */
public final class MediaAsset {

    /** Unique identifier within the content. */
    private final String id;

    /** Original filename, e.g. {@code "lecture.mp4"}. */
    private final String filename;

    /** Path relative to the content's base directory. */
    private final String relativePath;

    /** Media type: {@code IMAGE}, {@code VIDEO}, or {@code AUDIO}. */
    private final String mediaType;

    /** MIME type (e.g. {@code "video/mp4"}); empty if unknown. */
    private final String mimeType;

    /** File size in bytes; {@code 0} if unknown. */
    private final long fileSize;

    /** Duration in seconds for audio/video; {@code 0} if not applicable. */
    private final int durationSeconds;

    /** SHA-256 hex checksum; empty if not computed. */
    private final String checksum;

    /**
     * Constructs a {@code MediaAsset} from a {@link Builder}.
     *
     * @param b the builder (never {@code null}).
     */
    private MediaAsset(final Builder b) {
        this.id = b.buildId;
        this.filename = b.buildFilename;
        this.relativePath = b.buildRelativePath;
        this.mediaType = b.buildMediaType;
        this.mimeType = b.buildMimeType;
        this.fileSize = b.buildFileSize;
        this.durationSeconds = b.buildDurationSeconds;
        this.checksum = b.buildChecksum;
    }

    /** @return the unique asset id within this content. */
    public String getId() {
        return id;
    }

    /** @return the original filename (e.g. {@code "lecture.mp4"}). */
    public String getFilename() {
        return filename;
    }

    /**
     * @return the path relative to the content's base directory
     *         (e.g. {@code "media/videos/lecture.mp4"}).
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * @return the media type: {@code IMAGE}, {@code VIDEO}, or
     *         {@code AUDIO}.
     */
    public String getMediaType() {
        return mediaType;
    }

    /** @return the MIME type or empty string. */
    public String getMimeType() {
        return mimeType;
    }

    /** @return the file size in bytes, or {@code 0} if unknown. */
    public long getFileSize() {
        return fileSize;
    }

    /** @return the duration in seconds for audio/video, or {@code 0}. */
    public int getDurationSeconds() {
        return durationSeconds;
    }

    /** @return the SHA-256 hex checksum, or empty string. */
    public String getChecksum() {
        return checksum;
    }

    // ── Builder ──────────────────────────────────────────────────────────────

    /**
     * Fluent builder for {@link MediaAsset} that avoids the parameter-count
     * restriction on constructors.
     */
    public static final class Builder {

        /** Builder field: unique asset id. */
        private String buildId = "";
        /** Builder field: original filename. */
        private String buildFilename = "";
        /** Builder field: relative path. */
        private String buildRelativePath = "";
        /** Builder field: media type. */
        private String buildMediaType = "";
        /** Builder field: MIME type. */
        private String buildMimeType = "";
        /** Builder field: file size. */
        private long buildFileSize;
        /** Builder field: duration in seconds. */
        private int buildDurationSeconds;
        /** Builder field: SHA-256 checksum. */
        private String buildChecksum = "";

        /**
         * @param id the unique asset id; {@code null} treated as empty.
         * @return this builder.
         */
        public Builder id(final String id) {
            this.buildId = id == null ? "" : id;
            return this;
        }

        /**
         * @param fn the original filename; {@code null} treated as empty.
         * @return this builder.
         */
        public Builder filename(final String fn) {
            this.buildFilename = fn == null ? "" : fn;
            return this;
        }

        /**
         * @param rp the relative path; {@code null} treated as empty.
         * @return this builder.
         */
        public Builder relativePath(final String rp) {
            this.buildRelativePath = rp == null ? "" : rp;
            return this;
        }

        /**
         * @param mt the media type (IMAGE/VIDEO/AUDIO); {@code null} → empty.
         * @return this builder.
         */
        public Builder mediaType(final String mt) {
            this.buildMediaType = mt == null ? "" : mt;
            return this;
        }

        /**
         * @param mime the MIME type; {@code null} treated as empty.
         * @return this builder.
         */
        public Builder mimeType(final String mime) {
            this.buildMimeType = mime == null ? "" : mime;
            return this;
        }

        /**
         * @param size the file size in bytes.
         * @return this builder.
         */
        public Builder fileSize(final long size) {
            this.buildFileSize = size;
            return this;
        }

        /**
         * @param secs the duration in seconds.
         * @return this builder.
         */
        public Builder durationSeconds(final int secs) {
            this.buildDurationSeconds = secs;
            return this;
        }

        /**
         * @param sum the SHA-256 checksum; {@code null} treated as empty.
         * @return this builder.
         */
        public Builder checksum(final String sum) {
            this.buildChecksum = sum == null ? "" : sum;
            return this;
        }

        /**
         * Builds and returns the {@link MediaAsset}.
         *
         * @return a new immutable {@code MediaAsset}.
         */
        public MediaAsset build() {
            return new MediaAsset(this);
        }
    }
}
