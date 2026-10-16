package org.hiero.enterprise.service.topic;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

public record TopicMessage(
        ZonedDateTime consensusTimestamp,
        String message,
        AccountId payerAccountId,
        long sequenceNumber,
        Address topicId) {

    /// Creates a new `TopicMessage`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TopicMessage {
        Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(payerAccountId, "payerAccountId must not be null");
        Objects.requireNonNull(topicId, "topicId must not be null");
    }

    /// Returns a builder for a new `TopicMessage`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TopicMessage`.
    ///
    /// `build()` creates the `TopicMessage` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable ZonedDateTime consensusTimestamp;
        private @Nullable String message;
        private @Nullable AccountId payerAccountId;
        private @Nullable Long sequenceNumber;
        private @Nullable Address topicId;

        private Builder() {
            // created by TopicMessage.builder()
        }

        /// Sets the `consensusTimestamp`.
        ///
        /// @param consensusTimestamp the value
        /// @return this builder
        public Builder consensusTimestamp(final ZonedDateTime consensusTimestamp) {
            this.consensusTimestamp = consensusTimestamp;
            return this;
        }

        /// Sets the `message`.
        ///
        /// @param message the value
        /// @return this builder
        public Builder message(final String message) {
            this.message = message;
            return this;
        }

        /// Sets the `payerAccountId`.
        ///
        /// @param payerAccountId the value
        /// @return this builder
        public Builder payerAccountId(final AccountId payerAccountId) {
            this.payerAccountId = payerAccountId;
            return this;
        }

        /// Sets the `sequenceNumber`.
        ///
        /// @param sequenceNumber the value
        /// @return this builder
        public Builder sequenceNumber(final long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        /// Sets the `topicId`.
        ///
        /// @param topicId the value
        /// @return this builder
        public Builder topicId(final Address topicId) {
            this.topicId = topicId;
            return this;
        }

        /// Creates the `TopicMessage` of the values set.
        ///
        /// @return the new `TopicMessage`
        /// @throws NullPointerException if a required value is not set
        public TopicMessage build() {
            return new TopicMessage(
                    Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null"),
                    Objects.requireNonNull(message, "message must not be null"),
                    Objects.requireNonNull(payerAccountId, "payerAccountId must not be null"),
                    Objects.requireNonNull(sequenceNumber, "sequenceNumber must not be null"),
                    Objects.requireNonNull(topicId, "topicId must not be null"));
        }
    }
}
