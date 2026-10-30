package org.hiero.mirrornode.schedule;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// A schedule of a scheduled transaction.
///
/// @param adminKey the key that can delete the schedule
/// @param consensusTimestamp consensus time at which the schedule was created
/// @param executedTimestamp consensus time of the execution; null if not executed
/// @param payerAccountId the account that pays for the scheduled transaction
/// @param signatures the signatures collected so far
/// @param transactionBody the scheduled transaction body (protobuf)
/// @param waitForExpiry whether the transaction is executed at expiration time only (HIP-423)
public record Schedule(
        @Nullable Authority adminKey,
        ZonedDateTime consensusTimestamp,
        AccountId creatorAccountId,
        boolean deleted,
        @Nullable ZonedDateTime executedTimestamp,
        @Nullable ZonedDateTime expirationTime,
        String memo,
        AccountId payerAccountId,
        Address scheduleId,
        List<ScheduleSignature> signatures,
        byte[] transactionBody,
        boolean waitForExpiry) {

    /// Creates a new `Schedule`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Schedule {
        Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null");
        Objects.requireNonNull(creatorAccountId, "creatorAccountId must not be null");
        Objects.requireNonNull(memo, "memo must not be null");
        Objects.requireNonNull(payerAccountId, "payerAccountId must not be null");
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");
        Objects.requireNonNull(signatures, "signatures must not be null");
        signatures = List.copyOf(signatures);
        Objects.requireNonNull(transactionBody, "transactionBody must not be null");
        transactionBody = transactionBody.clone();
    }

    /// Creates a new `Schedule` with the default value of `signatures`.
    public Schedule(
            final @Nullable Authority adminKey,
            final ZonedDateTime consensusTimestamp,
            final AccountId creatorAccountId,
            final boolean deleted,
            final @Nullable ZonedDateTime executedTimestamp,
            final @Nullable ZonedDateTime expirationTime,
            final String memo,
            final AccountId payerAccountId,
            final Address scheduleId,
            final byte[] transactionBody,
            final boolean waitForExpiry) {
        this(adminKey, consensusTimestamp, creatorAccountId, deleted, executedTimestamp, expirationTime, memo, payerAccountId, scheduleId, List.of(), transactionBody, waitForExpiry);
    }

    /// the scheduled transaction body (protobuf)
    ///
    /// Returns a copy of the `transactionBody` array.
    @Override
    public byte[] transactionBody() {
        return transactionBody.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof Schedule other
            && Objects.equals(adminKey, other.adminKey)
            && Objects.equals(consensusTimestamp, other.consensusTimestamp)
            && Objects.equals(creatorAccountId, other.creatorAccountId)
            && deleted == other.deleted
            && Objects.equals(executedTimestamp, other.executedTimestamp)
            && Objects.equals(expirationTime, other.expirationTime)
            && Objects.equals(memo, other.memo)
            && Objects.equals(payerAccountId, other.payerAccountId)
            && Objects.equals(scheduleId, other.scheduleId)
            && Objects.equals(signatures, other.signatures)
            && Arrays.equals(transactionBody, other.transactionBody)
            && waitForExpiry == other.waitForExpiry;
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminKey, consensusTimestamp, creatorAccountId, deleted, executedTimestamp, expirationTime, memo, payerAccountId, scheduleId, signatures, Arrays.hashCode(transactionBody), waitForExpiry);
    }

    @Override
    public String toString() {
        return "Schedule[adminKey=" + adminKey + ", consensusTimestamp=" + consensusTimestamp + ", creatorAccountId=" + creatorAccountId + ", deleted=" + deleted + ", executedTimestamp=" + executedTimestamp + ", expirationTime=" + expirationTime + ", memo=" + memo + ", payerAccountId=" + payerAccountId + ", scheduleId=" + scheduleId + ", signatures=" + signatures + ", transactionBody=" + "byte[" + transactionBody.length + "]" + ", waitForExpiry=" + waitForExpiry + "]";
    }

    /// Returns a builder for a new `Schedule`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `Schedule`.
    ///
    /// `build()` creates the `Schedule` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Authority adminKey;
        private @Nullable ZonedDateTime consensusTimestamp;
        private @Nullable AccountId creatorAccountId;
        private @Nullable Boolean deleted;
        private @Nullable ZonedDateTime executedTimestamp;
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable String memo;
        private @Nullable AccountId payerAccountId;
        private @Nullable Address scheduleId;
        private List<ScheduleSignature> signatures = List.of();
        private byte @Nullable [] transactionBody;
        private @Nullable Boolean waitForExpiry;

        private Builder() {
            // created by Schedule.builder()
        }

        /// Sets the `adminKey`.
        ///
        /// the key that can delete the schedule
        ///
        /// @param adminKey the value
        /// @return this builder
        public Builder adminKey(final @Nullable Authority adminKey) {
            this.adminKey = adminKey;
            return this;
        }

        /// Sets the `consensusTimestamp`.
        ///
        /// consensus time at which the schedule was created
        ///
        /// @param consensusTimestamp the value
        /// @return this builder
        public Builder consensusTimestamp(final ZonedDateTime consensusTimestamp) {
            this.consensusTimestamp = consensusTimestamp;
            return this;
        }

        /// Sets the `creatorAccountId`.
        ///
        /// @param creatorAccountId the value
        /// @return this builder
        public Builder creatorAccountId(final AccountId creatorAccountId) {
            this.creatorAccountId = creatorAccountId;
            return this;
        }

        /// Sets the `deleted`.
        ///
        /// @param deleted the value
        /// @return this builder
        public Builder deleted(final boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /// Sets the `executedTimestamp`.
        ///
        /// consensus time of the execution; null if not executed
        ///
        /// @param executedTimestamp the value
        /// @return this builder
        public Builder executedTimestamp(final @Nullable ZonedDateTime executedTimestamp) {
            this.executedTimestamp = executedTimestamp;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final @Nullable ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
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

        /// Sets the `payerAccountId`.
        ///
        /// the account that pays for the scheduled transaction
        ///
        /// @param payerAccountId the value
        /// @return this builder
        public Builder payerAccountId(final AccountId payerAccountId) {
            this.payerAccountId = payerAccountId;
            return this;
        }

        /// Sets the `scheduleId`.
        ///
        /// @param scheduleId the value
        /// @return this builder
        public Builder scheduleId(final Address scheduleId) {
            this.scheduleId = scheduleId;
            return this;
        }

        /// Sets the `signatures`.
        ///
        /// the signatures collected so far
        ///
        /// @param signatures the value
        /// @return this builder
        public Builder signatures(final List<ScheduleSignature> signatures) {
            this.signatures = signatures;
            return this;
        }

        /// Sets the `transactionBody`.
        ///
        /// the scheduled transaction body (protobuf)
        ///
        /// @param transactionBody the value
        /// @return this builder
        public Builder transactionBody(final byte[] transactionBody) {
            this.transactionBody = transactionBody;
            return this;
        }

        /// Sets the `waitForExpiry`.
        ///
        /// whether the transaction is executed at expiration time only (HIP-423)
        ///
        /// @param waitForExpiry the value
        /// @return this builder
        public Builder waitForExpiry(final boolean waitForExpiry) {
            this.waitForExpiry = waitForExpiry;
            return this;
        }

        /// Creates the `Schedule` of the values set.
        ///
        /// @return the new `Schedule`
        /// @throws NullPointerException if a required value is not set
        public Schedule build() {
            return new Schedule(
                    adminKey,
                    Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null"),
                    Objects.requireNonNull(creatorAccountId, "creatorAccountId must not be null"),
                    Objects.requireNonNull(deleted, "deleted must not be null"),
                    executedTimestamp,
                    expirationTime,
                    Objects.requireNonNull(memo, "memo must not be null"),
                    Objects.requireNonNull(payerAccountId, "payerAccountId must not be null"),
                    Objects.requireNonNull(scheduleId, "scheduleId must not be null"),
                    signatures,
                    Objects.requireNonNull(transactionBody, "transactionBody must not be null"),
                    Objects.requireNonNull(waitForExpiry, "waitForExpiry must not be null"));
        }
    }
}
