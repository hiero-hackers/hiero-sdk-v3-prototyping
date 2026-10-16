package org.hiero.consensusnode.admin.network;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// A semantic version (major.minor.patch) with optional pre-release and build metadata labels, as defined by
/// semver.org.
///
/// @param major major version
/// @param minor minor version
/// @param patch patch version
/// @param preReleaseLabel pre-release label such as "rc.1"; absent for a release version
/// @param buildMetadata build metadata such as "exp.sha.5114f85"; absent if none
public record SemanticVersion(
        int major,
        int minor,
        int patch,
        @Nullable String preReleaseLabel,
        @Nullable String buildMetadata) {

    /// Canonical semver string, e.g. "0.49.1-rc.1+exp.sha.5114f85"
    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not implemented yet: SemanticVersion.toString");
    }

    /// Returns a builder for a new `SemanticVersion`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `SemanticVersion`.
    ///
    /// `build()` creates the `SemanticVersion` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Integer major;
        private @Nullable Integer minor;
        private @Nullable Integer patch;
        private @Nullable String preReleaseLabel;
        private @Nullable String buildMetadata;

        private Builder() {
            // created by SemanticVersion.builder()
        }

        /// Sets the `major`.
        ///
        /// major version
        ///
        /// @param major the value
        /// @return this builder
        public Builder major(final int major) {
            this.major = major;
            return this;
        }

        /// Sets the `minor`.
        ///
        /// minor version
        ///
        /// @param minor the value
        /// @return this builder
        public Builder minor(final int minor) {
            this.minor = minor;
            return this;
        }

        /// Sets the `patch`.
        ///
        /// patch version
        ///
        /// @param patch the value
        /// @return this builder
        public Builder patch(final int patch) {
            this.patch = patch;
            return this;
        }

        /// Sets the `preReleaseLabel`.
        ///
        /// pre-release label such as "rc.1"; absent for a release version
        ///
        /// @param preReleaseLabel the value
        /// @return this builder
        public Builder preReleaseLabel(final @Nullable String preReleaseLabel) {
            this.preReleaseLabel = preReleaseLabel;
            return this;
        }

        /// Sets the `buildMetadata`.
        ///
        /// build metadata such as "exp.sha.5114f85"; absent if none
        ///
        /// @param buildMetadata the value
        /// @return this builder
        public Builder buildMetadata(final @Nullable String buildMetadata) {
            this.buildMetadata = buildMetadata;
            return this;
        }

        /// Creates the `SemanticVersion` of the values set.
        ///
        /// @return the new `SemanticVersion`
        /// @throws NullPointerException if a required value is not set
        public SemanticVersion build() {
            return new SemanticVersion(
                    Objects.requireNonNull(major, "major must not be null"),
                    Objects.requireNonNull(minor, "minor must not be null"),
                    Objects.requireNonNull(patch, "patch must not be null"),
                    preReleaseLabel,
                    buildMetadata);
        }
    }
}
