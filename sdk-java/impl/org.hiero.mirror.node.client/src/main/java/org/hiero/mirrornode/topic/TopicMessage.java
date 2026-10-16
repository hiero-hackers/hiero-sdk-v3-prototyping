package org.hiero.mirrornode.topic;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

public record TopicMessage(
        @Nullable ChunkInfo chunkInfo,
        ZonedDateTime consensusTimestamp,
        String message,
        AccountId payerAccountId,
        byte[] runningHash,
        int runningHashVersion,
        long sequenceNumber,
        Address topicId) {

    /// Creates a new `TopicMessage`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TopicMessage {
        Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(payerAccountId, "payerAccountId must not be null");
        Objects.requireNonNull(runningHash, "runningHash must not be null");
        runningHash = runningHash.clone();
        Objects.requireNonNull(topicId, "topicId must not be null");
    }

    /// Returns a copy of the `runningHash` array.
    @Override
    public byte[] runningHash() {
        return runningHash.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof TopicMessage other
            && Objects.equals(chunkInfo, other.chunkInfo)
            && Objects.equals(consensusTimestamp, other.consensusTimestamp)
            && Objects.equals(message, other.message)
            && Objects.equals(payerAccountId, other.payerAccountId)
            && Arrays.equals(runningHash, other.runningHash)
            && runningHashVersion == other.runningHashVersion
            && sequenceNumber == other.sequenceNumber
            && Objects.equals(topicId, other.topicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkInfo, consensusTimestamp, message, payerAccountId, Arrays.hashCode(runningHash), runningHashVersion, sequenceNumber, topicId);
    }

    @Override
    public String toString() {
        return "TopicMessage[chunkInfo=" + chunkInfo + ", consensusTimestamp=" + consensusTimestamp + ", message=" + message + ", payerAccountId=" + payerAccountId + ", runningHash=" + "byte[" + runningHash.length + "]" + ", runningHashVersion=" + runningHashVersion + ", sequenceNumber=" + sequenceNumber + ", topicId=" + topicId + "]";
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

        private @Nullable ChunkInfo chunkInfo;
        private @Nullable ZonedDateTime consensusTimestamp;
        private @Nullable String message;
        private @Nullable AccountId payerAccountId;
        private byte @Nullable [] runningHash;
        private @Nullable Integer runningHashVersion;
        private @Nullable Long sequenceNumber;
        private @Nullable Address topicId;

        private Builder() {
            // created by TopicMessage.builder()
        }

        /// Sets the `chunkInfo`.
        ///
        /// @param chunkInfo the value
        /// @return this builder
        public Builder chunkInfo(final @Nullable ChunkInfo chunkInfo) {
            this.chunkInfo = chunkInfo;
            return this;
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

        /// Sets the `runningHash`.
        ///
        /// @param runningHash the value
        /// @return this builder
        public Builder runningHash(final byte[] runningHash) {
            this.runningHash = runningHash;
            return this;
        }

        /// Sets the `runningHashVersion`.
        ///
        /// @param runningHashVersion the value
        /// @return this builder
        public Builder runningHashVersion(final int runningHashVersion) {
            this.runningHashVersion = runningHashVersion;
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
                    chunkInfo,
                    Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null"),
                    Objects.requireNonNull(message, "message must not be null"),
                    Objects.requireNonNull(payerAccountId, "payerAccountId must not be null"),
                    Objects.requireNonNull(runningHash, "runningHash must not be null"),
                    Objects.requireNonNull(runningHashVersion, "runningHashVersion must not be null"),
                    Objects.requireNonNull(sequenceNumber, "sequenceNumber must not be null"),
                    Objects.requireNonNull(topicId, "topicId must not be null"));
        }
    }
}
