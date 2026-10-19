package org.hiero.consensusnode.queries.schedule;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// Full metadata snapshot of a schedule. Returned by `ScheduleInfoQuery`. At most one of `executionTime` and
/// `deletionTime` is set; if neither is set, the schedule is still pending.
///
/// @param scheduleId the id of the schedule
/// @param creatorAccountId account that created the schedule
/// @param payerAccountId pays the inner transaction's fee when it executes
/// @param scheduledTransaction read-only reconstruction of the captured inner transaction
/// @param scheduledTransactionId the id the inner transaction carries when it executes (scheduled flag set)
/// @param signers public keys whose signatures have been credited so far
/// @param adminKey may delete the schedule; absent if the schedule is immutable
/// @param scheduleMemo free-form memo on the schedule entity; empty if none
/// @param expirationTime when the schedule expires (the requested long-term expiry, or the default expiry)
/// @param waitForExpiry if true, execute only at expirationTime even if all signatures are collected earlier (long-term schedule, HIP-423)
/// @param executionTime set once the inner transaction has executed
/// @param deletionTime set once the schedule has been deleted before execution
public record ScheduleInfo(
        Address scheduleId,
        AccountId creatorAccountId,
        AccountId payerAccountId,
        Transaction<?, ?> scheduledTransaction,
        TransactionId scheduledTransactionId,
        List<PublicKey> signers,
        @Nullable Authority adminKey,
        String scheduleMemo,
        ZonedDateTime expirationTime,
        boolean waitForExpiry,
        @Nullable ZonedDateTime executionTime,
        @Nullable ZonedDateTime deletionTime) {

    /// Creates a new `ScheduleInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ScheduleInfo {
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");
        Objects.requireNonNull(creatorAccountId, "creatorAccountId must not be null");
        Objects.requireNonNull(payerAccountId, "payerAccountId must not be null");
        Objects.requireNonNull(scheduledTransaction, "scheduledTransaction must not be null");
        Objects.requireNonNull(scheduledTransactionId, "scheduledTransactionId must not be null");
        Objects.requireNonNull(signers, "signers must not be null");
        signers = List.copyOf(signers);
        Objects.requireNonNull(scheduleMemo, "scheduleMemo must not be null");
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
        if ((executionTime != null ? 1 : 0) + (deletionTime != null ? 1 : 0) > 1) {
            throw new IllegalArgumentException("at most one of executionTime, deletionTime may be set");
        }
    }

    /// Creates a new `ScheduleInfo` with the default value of `signers`, `scheduleMemo`, `waitForExpiry`.
    public ScheduleInfo(
            final Address scheduleId,
            final AccountId creatorAccountId,
            final AccountId payerAccountId,
            final Transaction<?, ?> scheduledTransaction,
            final TransactionId scheduledTransactionId,
            final @Nullable Authority adminKey,
            final ZonedDateTime expirationTime,
            final @Nullable ZonedDateTime executionTime,
            final @Nullable ZonedDateTime deletionTime) {
        this(scheduleId, creatorAccountId, payerAccountId, scheduledTransaction, scheduledTransactionId, List.of(), adminKey, "", expirationTime, false, executionTime, deletionTime);
    }

    /// Returns a builder for a new `ScheduleInfo`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ScheduleInfo`.
    ///
    /// `build()` creates the `ScheduleInfo` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address scheduleId;
        private @Nullable AccountId creatorAccountId;
        private @Nullable AccountId payerAccountId;
        private @Nullable Transaction<?, ?> scheduledTransaction;
        private @Nullable TransactionId scheduledTransactionId;
        private List<PublicKey> signers = List.of();
        private @Nullable Authority adminKey;
        private String scheduleMemo = "";
        private @Nullable ZonedDateTime expirationTime;
        private boolean waitForExpiry = false;
        private @Nullable ZonedDateTime executionTime;
        private @Nullable ZonedDateTime deletionTime;

        private Builder() {
            // created by ScheduleInfo.builder()
        }

        /// Sets the `scheduleId`.
        ///
        /// the id of the schedule
        ///
        /// @param scheduleId the value
        /// @return this builder
        public Builder scheduleId(final Address scheduleId) {
            this.scheduleId = scheduleId;
            return this;
        }

        /// Sets the `creatorAccountId`.
        ///
        /// account that created the schedule
        ///
        /// @param creatorAccountId the value
        /// @return this builder
        public Builder creatorAccountId(final AccountId creatorAccountId) {
            this.creatorAccountId = creatorAccountId;
            return this;
        }

        /// Sets the `payerAccountId`.
        ///
        /// pays the inner transaction's fee when it executes
        ///
        /// @param payerAccountId the value
        /// @return this builder
        public Builder payerAccountId(final AccountId payerAccountId) {
            this.payerAccountId = payerAccountId;
            return this;
        }

        /// Sets the `scheduledTransaction`.
        ///
        /// read-only reconstruction of the captured inner transaction
        ///
        /// @param scheduledTransaction the value
        /// @return this builder
        public Builder scheduledTransaction(final Transaction<?, ?> scheduledTransaction) {
            this.scheduledTransaction = scheduledTransaction;
            return this;
        }

        /// Sets the `scheduledTransactionId`.
        ///
        /// the id the inner transaction carries when it executes (scheduled flag set)
        ///
        /// @param scheduledTransactionId the value
        /// @return this builder
        public Builder scheduledTransactionId(final TransactionId scheduledTransactionId) {
            this.scheduledTransactionId = scheduledTransactionId;
            return this;
        }

        /// Sets the `signers`.
        ///
        /// public keys whose signatures have been credited so far
        ///
        /// @param signers the value
        /// @return this builder
        public Builder signers(final List<PublicKey> signers) {
            this.signers = signers;
            return this;
        }

        /// Sets the `adminKey`.
        ///
        /// may delete the schedule; absent if the schedule is immutable
        ///
        /// @param adminKey the value
        /// @return this builder
        public Builder adminKey(final @Nullable Authority adminKey) {
            this.adminKey = adminKey;
            return this;
        }

        /// Sets the `scheduleMemo`.
        ///
        /// free-form memo on the schedule entity; empty if none
        ///
        /// @param scheduleMemo the value
        /// @return this builder
        public Builder scheduleMemo(final String scheduleMemo) {
            this.scheduleMemo = scheduleMemo;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// when the schedule expires (the requested long-term expiry, or the default expiry)
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /// Sets the `waitForExpiry`.
        ///
        /// if true, execute only at expirationTime even if all signatures are collected earlier (long-term schedule, HIP-423)
        ///
        /// @param waitForExpiry the value
        /// @return this builder
        public Builder waitForExpiry(final boolean waitForExpiry) {
            this.waitForExpiry = waitForExpiry;
            return this;
        }

        /// Sets the `executionTime`.
        ///
        /// set once the inner transaction has executed
        ///
        /// @param executionTime the value
        /// @return this builder
        public Builder executionTime(final @Nullable ZonedDateTime executionTime) {
            this.executionTime = executionTime;
            return this;
        }

        /// Sets the `deletionTime`.
        ///
        /// set once the schedule has been deleted before execution
        ///
        /// @param deletionTime the value
        /// @return this builder
        public Builder deletionTime(final @Nullable ZonedDateTime deletionTime) {
            this.deletionTime = deletionTime;
            return this;
        }

        /// Creates the `ScheduleInfo` of the values set.
        ///
        /// @return the new `ScheduleInfo`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ScheduleInfo build() {
            return new ScheduleInfo(
                    Objects.requireNonNull(scheduleId, "scheduleId must not be null"),
                    Objects.requireNonNull(creatorAccountId, "creatorAccountId must not be null"),
                    Objects.requireNonNull(payerAccountId, "payerAccountId must not be null"),
                    Objects.requireNonNull(scheduledTransaction, "scheduledTransaction must not be null"),
                    Objects.requireNonNull(scheduledTransactionId, "scheduledTransactionId must not be null"),
                    signers,
                    adminKey,
                    scheduleMemo,
                    Objects.requireNonNull(expirationTime, "expirationTime must not be null"),
                    waitForExpiry,
                    executionTime,
                    deletionTime);
        }
    }
}
