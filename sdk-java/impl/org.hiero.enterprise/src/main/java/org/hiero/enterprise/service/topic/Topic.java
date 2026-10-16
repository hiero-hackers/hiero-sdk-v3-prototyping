package org.hiero.enterprise.service.topic;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

public record Topic(
        Address topicId,
        @Nullable Authority adminAuthority,
        @Nullable Authority submitAuthority,
        ZonedDateTime createdTimestamp,
        String memo) {

    /// Creates a new `Topic`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Topic {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(createdTimestamp, "createdTimestamp must not be null");
        Objects.requireNonNull(memo, "memo must not be null");
    }

    /// Returns a builder for a new `Topic`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `Topic`.
    ///
    /// `build()` creates the `Topic` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address topicId;
        private @Nullable Authority adminAuthority;
        private @Nullable Authority submitAuthority;
        private @Nullable ZonedDateTime createdTimestamp;
        private @Nullable String memo;

        private Builder() {
            // created by Topic.builder()
        }

        /// Sets the `topicId`.
        ///
        /// @param topicId the value
        /// @return this builder
        public Builder topicId(final Address topicId) {
            this.topicId = topicId;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `submitAuthority`.
        ///
        /// @param submitAuthority the value
        /// @return this builder
        public Builder submitAuthority(final @Nullable Authority submitAuthority) {
            this.submitAuthority = submitAuthority;
            return this;
        }

        /// Sets the `createdTimestamp`.
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        /// Sets the `memo`.
        ///
        /// @param memo the value
        /// @return this builder
        public Builder memo(final String memo) {
            this.memo = memo;
            return this;
        }

        /// Creates the `Topic` of the values set.
        ///
        /// @return the new `Topic`
        /// @throws NullPointerException if a required value is not set
        public Topic build() {
            return new Topic(
                    Objects.requireNonNull(topicId, "topicId must not be null"),
                    adminAuthority,
                    submitAuthority,
                    Objects.requireNonNull(createdTimestamp, "createdTimestamp must not be null"),
                    Objects.requireNonNull(memo, "memo must not be null"));
        }
    }
}
