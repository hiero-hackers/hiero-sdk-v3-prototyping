package org.hiero.consensusnode.queries.files;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// Full file metadata snapshot. Returned by `FileInfoQuery`.
///
/// @param size current file size in bytes
/// @param expirationTime when the file expires
/// @param deleted true if the file has been deleted
/// @param authority write-access authorization; absent if the file is immutable
public record FileInfo(
        Address fileId,
        long size,
        ZonedDateTime expirationTime,
        boolean deleted,
        @Nullable Authority authority,
        @Nullable String fileMemo) {

    /// Creates a new `FileInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FileInfo {
        Objects.requireNonNull(fileId, "fileId must not be null");
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
    }

    /// Returns a builder for a new `FileInfo`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `FileInfo`.
    ///
    /// `build()` creates the `FileInfo` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address fileId;
        private @Nullable Long size;
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable Boolean deleted;
        private @Nullable Authority authority;
        private @Nullable String fileMemo;

        private Builder() {
            // created by FileInfo.builder()
        }

        /// Sets the `fileId`.
        ///
        /// @param fileId the value
        /// @return this builder
        public Builder fileId(final Address fileId) {
            this.fileId = fileId;
            return this;
        }

        /// Sets the `size`.
        ///
        /// current file size in bytes
        ///
        /// @param size the value
        /// @return this builder
        public Builder size(final long size) {
            this.size = size;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// when the file expires
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /// Sets the `deleted`.
        ///
        /// true if the file has been deleted
        ///
        /// @param deleted the value
        /// @return this builder
        public Builder deleted(final boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /// Sets the `authority`.
        ///
        /// write-access authorization; absent if the file is immutable
        ///
        /// @param authority the value
        /// @return this builder
        public Builder authority(final @Nullable Authority authority) {
            this.authority = authority;
            return this;
        }

        /// Sets the `fileMemo`.
        ///
        /// @param fileMemo the value
        /// @return this builder
        public Builder fileMemo(final @Nullable String fileMemo) {
            this.fileMemo = fileMemo;
            return this;
        }

        /// Creates the `FileInfo` of the values set.
        ///
        /// @return the new `FileInfo`
        /// @throws NullPointerException if a required value is not set
        public FileInfo build() {
            return new FileInfo(
                    Objects.requireNonNull(fileId, "fileId must not be null"),
                    Objects.requireNonNull(size, "size must not be null"),
                    Objects.requireNonNull(expirationTime, "expirationTime must not be null"),
                    Objects.requireNonNull(deleted, "deleted must not be null"),
                    authority,
                    fileMemo);
        }
    }
}
