package org.hiero.consensusnode.queries.topics;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// Full topic metadata snapshot. Returned by `TopicInfoQuery`.
///
/// @param topicMemo short human-readable label
/// @param runningHash SHA-384 over all submitted messages so far
/// @param sequenceNumber number of messages submitted so far
/// @param adminAuthority absent → topic is immutable (no update / delete)
/// @param submitAuthority absent → topic is public (any account may submit)
/// @param autoRenewAccount pays auto-renewal; if absent, the topic itself pays
public record TopicInfo(
        Address topicId,
        @Nullable String topicMemo,
        byte[] runningHash,
        long sequenceNumber,
        ZonedDateTime expirationTime,
        @Nullable Authority adminAuthority,
        @Nullable Authority submitAuthority,
        @Nullable Duration autoRenewPeriod,
        @Nullable AccountId autoRenewAccount) {

    /// Creates a new `TopicInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TopicInfo {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(runningHash, "runningHash must not be null");
        runningHash = runningHash.clone();
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
    }

    /// SHA-384 over all submitted messages so far
    ///
    /// Returns a copy of the `runningHash` array.
    @Override
    public byte[] runningHash() {
        return runningHash.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof TopicInfo other
            && Objects.equals(topicId, other.topicId)
            && Objects.equals(topicMemo, other.topicMemo)
            && Arrays.equals(runningHash, other.runningHash)
            && sequenceNumber == other.sequenceNumber
            && Objects.equals(expirationTime, other.expirationTime)
            && Objects.equals(adminAuthority, other.adminAuthority)
            && Objects.equals(submitAuthority, other.submitAuthority)
            && Objects.equals(autoRenewPeriod, other.autoRenewPeriod)
            && Objects.equals(autoRenewAccount, other.autoRenewAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicId, topicMemo, Arrays.hashCode(runningHash), sequenceNumber, expirationTime, adminAuthority, submitAuthority, autoRenewPeriod, autoRenewAccount);
    }

    @Override
    public String toString() {
        return "TopicInfo[topicId=" + topicId + ", topicMemo=" + topicMemo + ", runningHash=" + "byte[" + runningHash.length + "]" + ", sequenceNumber=" + sequenceNumber + ", expirationTime=" + expirationTime + ", adminAuthority=" + adminAuthority + ", submitAuthority=" + submitAuthority + ", autoRenewPeriod=" + autoRenewPeriod + ", autoRenewAccount=" + autoRenewAccount + "]";
    }

    /// Returns a builder for a new `TopicInfo`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TopicInfo`.
    ///
    /// `build()` creates the `TopicInfo` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address topicId;
        private @Nullable String topicMemo;
        private byte @Nullable [] runningHash;
        private @Nullable Long sequenceNumber;
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable Authority adminAuthority;
        private @Nullable Authority submitAuthority;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;

        private Builder() {
            // created by TopicInfo.builder()
        }

        /// Sets the `topicId`.
        ///
        /// @param topicId the value
        /// @return this builder
        public Builder topicId(final Address topicId) {
            this.topicId = topicId;
            return this;
        }

        /// Sets the `topicMemo`.
        ///
        /// short human-readable label
        ///
        /// @param topicMemo the value
        /// @return this builder
        public Builder topicMemo(final @Nullable String topicMemo) {
            this.topicMemo = topicMemo;
            return this;
        }

        /// Sets the `runningHash`.
        ///
        /// SHA-384 over all submitted messages so far
        ///
        /// @param runningHash the value
        /// @return this builder
        public Builder runningHash(final byte[] runningHash) {
            this.runningHash = runningHash;
            return this;
        }

        /// Sets the `sequenceNumber`.
        ///
        /// number of messages submitted so far
        ///
        /// @param sequenceNumber the value
        /// @return this builder
        public Builder sequenceNumber(final long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// absent → topic is immutable (no update / delete)
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `submitAuthority`.
        ///
        /// absent → topic is public (any account may submit)
        ///
        /// @param submitAuthority the value
        /// @return this builder
        public Builder submitAuthority(final @Nullable Authority submitAuthority) {
            this.submitAuthority = submitAuthority;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// pays auto-renewal; if absent, the topic itself pays
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Creates the `TopicInfo` of the values set.
        ///
        /// @return the new `TopicInfo`
        /// @throws NullPointerException if a required value is not set
        public TopicInfo build() {
            return new TopicInfo(
                    Objects.requireNonNull(topicId, "topicId must not be null"),
                    topicMemo,
                    Objects.requireNonNull(runningHash, "runningHash must not be null"),
                    Objects.requireNonNull(sequenceNumber, "sequenceNumber must not be null"),
                    Objects.requireNonNull(expirationTime, "expirationTime must not be null"),
                    adminAuthority,
                    submitAuthority,
                    autoRenewPeriod,
                    autoRenewAccount);
        }
    }
}
