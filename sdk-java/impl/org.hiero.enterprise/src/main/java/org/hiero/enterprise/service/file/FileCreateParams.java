package org.hiero.enterprise.service.file;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.jspecify.annotations.Nullable;

/// The parameters of a new file: everything that can be set when it is created. Nothing is required.
///
/// @param contents The initial contents; more can be appended later.
/// @param authority Allows to change and delete the file; an authority that all must sign is an AuthorityList.
/// @param expirationTime When the file expires; the network default if absent.
public record FileCreateParams(
        byte[] contents,
        @Nullable Authority authority,
        @Nullable ZonedDateTime expirationTime,
        @Nullable String fileMemo) {

    /// Creates a new `FileCreateParams`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public FileCreateParams {
        Objects.requireNonNull(contents, "contents must not be null");
        contents = contents.clone();
        if (fileMemo != null && fileMemo.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("fileMemo must be at most 100 bytes long in UTF-8");
        }
    }

    /// Creates a new `FileCreateParams` with the default value of `contents`.
    public FileCreateParams(
            final @Nullable Authority authority,
            final @Nullable ZonedDateTime expirationTime,
            final @Nullable String fileMemo) {
        this(new byte[0], authority, expirationTime, fileMemo);
    }

    /// The initial contents; more can be appended later.
    ///
    /// Returns a copy of the `contents` array.
    @Override
    public byte[] contents() {
        return contents.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof FileCreateParams other
            && Arrays.equals(contents, other.contents)
            && Objects.equals(authority, other.authority)
            && Objects.equals(expirationTime, other.expirationTime)
            && Objects.equals(fileMemo, other.fileMemo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(contents), authority, expirationTime, fileMemo);
    }

    @Override
    public String toString() {
        return "FileCreateParams[contents=" + "byte[" + contents.length + "]" + ", authority=" + authority + ", expirationTime=" + expirationTime + ", fileMemo=" + fileMemo + "]";
    }

    /// Returns a builder for a new `FileCreateParams`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `FileCreateParams`.
    ///
    /// `build()` creates the `FileCreateParams` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private byte[] contents = new byte[0];
        private @Nullable Authority authority;
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable String fileMemo;

        private Builder() {
            // created by FileCreateParams.builder()
        }

        /// Sets the `contents`.
        ///
        /// The initial contents; more can be appended later.
        ///
        /// @param contents the value
        /// @return this builder
        public Builder contents(final byte[] contents) {
            this.contents = contents;
            return this;
        }

        /// Sets the `authority`.
        ///
        /// Allows to change and delete the file; an authority that all must sign is an AuthorityList.
        ///
        /// @param authority the value
        /// @return this builder
        public Builder authority(final @Nullable Authority authority) {
            this.authority = authority;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// When the file expires; the network default if absent.
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final @Nullable ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
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

        /// Creates the `FileCreateParams` of the values set.
        ///
        /// @return the new `FileCreateParams`
        /// @throws IllegalArgumentException if a value violates its constraints
        public FileCreateParams build() {
            return new FileCreateParams(contents, authority, expirationTime, fileMemo);
        }
    }
}
