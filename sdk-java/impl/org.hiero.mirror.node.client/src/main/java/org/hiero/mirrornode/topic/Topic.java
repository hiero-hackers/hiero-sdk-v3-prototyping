package org.hiero.mirrornode.topic;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.FixedFee;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;

/// @param autoRenewPeriod how long the lifetime is extended after expiration
/// @param customFeesCreatedTimestamp since when the custom fees apply
/// @param fixedFees charged for each message submitted to the topic
/// @param feeExemptAuthorities empty if no authority is exempt from the custom fees
/// @param timestamp the period this state of the topic is valid for
public record Topic(
        Address topicId,
        @Nullable Authority adminAuthority,
        @Nullable Authority submitAuthority,
        @Nullable Authority feeScheduleAuthority,
        @Nullable AccountId autoRenewAccount,
        @Nullable Duration autoRenewPeriod,
        @Nullable ZonedDateTime createdTimestamp,
        @Nullable ZonedDateTime customFeesCreatedTimestamp,
        @Nullable Boolean deleted,
        String memo,
        List<FixedFee> fixedFees,
        List<Authority> feeExemptAuthorities,
        TimestampRange timestamp) {

    /// Creates a new `Topic`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Topic {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(memo, "memo must not be null");
        Objects.requireNonNull(fixedFees, "fixedFees must not be null");
        fixedFees = List.copyOf(fixedFees);
        Objects.requireNonNull(feeExemptAuthorities, "feeExemptAuthorities must not be null");
        feeExemptAuthorities = List.copyOf(feeExemptAuthorities);
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// Creates a new `Topic` with the default value of `fixedFees`, `feeExemptAuthorities`.
    public Topic(
            final Address topicId,
            final @Nullable Authority adminAuthority,
            final @Nullable Authority submitAuthority,
            final @Nullable Authority feeScheduleAuthority,
            final @Nullable AccountId autoRenewAccount,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable ZonedDateTime createdTimestamp,
            final @Nullable ZonedDateTime customFeesCreatedTimestamp,
            final @Nullable Boolean deleted,
            final String memo,
            final TimestampRange timestamp) {
        this(topicId, adminAuthority, submitAuthority, feeScheduleAuthority, autoRenewAccount, autoRenewPeriod, createdTimestamp, customFeesCreatedTimestamp, deleted, memo, List.of(), List.of(), timestamp);
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
        private @Nullable Authority feeScheduleAuthority;
        private @Nullable AccountId autoRenewAccount;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable ZonedDateTime createdTimestamp;
        private @Nullable ZonedDateTime customFeesCreatedTimestamp;
        private @Nullable Boolean deleted;
        private @Nullable String memo;
        private List<FixedFee> fixedFees = List.of();
        private List<Authority> feeExemptAuthorities = List.of();
        private @Nullable TimestampRange timestamp;

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

        /// Sets the `feeScheduleAuthority`.
        ///
        /// @param feeScheduleAuthority the value
        /// @return this builder
        public Builder feeScheduleAuthority(final @Nullable Authority feeScheduleAuthority) {
            this.feeScheduleAuthority = feeScheduleAuthority;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// how long the lifetime is extended after expiration
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `createdTimestamp`.
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final @Nullable ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        /// Sets the `customFeesCreatedTimestamp`.
        ///
        /// since when the custom fees apply
        ///
        /// @param customFeesCreatedTimestamp the value
        /// @return this builder
        public Builder customFeesCreatedTimestamp(final @Nullable ZonedDateTime customFeesCreatedTimestamp) {
            this.customFeesCreatedTimestamp = customFeesCreatedTimestamp;
            return this;
        }

        /// Sets the `deleted`.
        ///
        /// @param deleted the value
        /// @return this builder
        public Builder deleted(final @Nullable Boolean deleted) {
            this.deleted = deleted;
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

        /// Sets the `fixedFees`.
        ///
        /// charged for each message submitted to the topic
        ///
        /// @param fixedFees the value
        /// @return this builder
        public Builder fixedFees(final List<FixedFee> fixedFees) {
            this.fixedFees = fixedFees;
            return this;
        }

        /// Sets the `feeExemptAuthorities`.
        ///
        /// empty if no authority is exempt from the custom fees
        ///
        /// @param feeExemptAuthorities the value
        /// @return this builder
        public Builder feeExemptAuthorities(final List<Authority> feeExemptAuthorities) {
            this.feeExemptAuthorities = feeExemptAuthorities;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// the period this state of the topic is valid for
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final TimestampRange timestamp) {
            this.timestamp = timestamp;
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
                    feeScheduleAuthority,
                    autoRenewAccount,
                    autoRenewPeriod,
                    createdTimestamp,
                    customFeesCreatedTimestamp,
                    deleted,
                    Objects.requireNonNull(memo, "memo must not be null"),
                    fixedFees,
                    feeExemptAuthorities,
                    Objects.requireNonNull(timestamp, "timestamp must not be null"));
        }
    }
}
