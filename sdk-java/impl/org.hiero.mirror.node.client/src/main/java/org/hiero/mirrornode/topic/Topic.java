package org.hiero.mirrornode.topic;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.FixedFee;
import org.jspecify.annotations.Nullable;

/// @param feeExemptAuthorities empty if no authority is exempt from the custom fees
public record Topic(
        Address topicId,
        @Nullable Authority adminAuthority,
        @Nullable Authority submitAuthority,
        @Nullable Authority feeScheduleAuthority,
        @Nullable AccountId autoRenewAccount,
        Duration autoRenewPeriod,
        ZonedDateTime createdTimestamp,
        boolean deleted,
        String memo,
        List<FixedFee> fixedFees,
        List<Authority> feeExemptAuthorities,
        ZonedDateTime fromTimestamp,
        ZonedDateTime toTimestamp) {

    /// Creates a new `Topic`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Topic {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(autoRenewPeriod, "autoRenewPeriod must not be null");
        Objects.requireNonNull(createdTimestamp, "createdTimestamp must not be null");
        Objects.requireNonNull(memo, "memo must not be null");
        Objects.requireNonNull(fixedFees, "fixedFees must not be null");
        fixedFees = List.copyOf(fixedFees);
        Objects.requireNonNull(feeExemptAuthorities, "feeExemptAuthorities must not be null");
        feeExemptAuthorities = List.copyOf(feeExemptAuthorities);
        Objects.requireNonNull(fromTimestamp, "fromTimestamp must not be null");
        Objects.requireNonNull(toTimestamp, "toTimestamp must not be null");
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
        private @Nullable Boolean deleted;
        private @Nullable String memo;
        private @Nullable List<FixedFee> fixedFees;
        private @Nullable List<Authority> feeExemptAuthorities;
        private @Nullable ZonedDateTime fromTimestamp;
        private @Nullable ZonedDateTime toTimestamp;

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
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
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

        /// Sets the `deleted`.
        ///
        /// @param deleted the value
        /// @return this builder
        public Builder deleted(final boolean deleted) {
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

        /// Sets the `fromTimestamp`.
        ///
        /// @param fromTimestamp the value
        /// @return this builder
        public Builder fromTimestamp(final ZonedDateTime fromTimestamp) {
            this.fromTimestamp = fromTimestamp;
            return this;
        }

        /// Sets the `toTimestamp`.
        ///
        /// @param toTimestamp the value
        /// @return this builder
        public Builder toTimestamp(final ZonedDateTime toTimestamp) {
            this.toTimestamp = toTimestamp;
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
                    Objects.requireNonNull(autoRenewPeriod, "autoRenewPeriod must not be null"),
                    Objects.requireNonNull(createdTimestamp, "createdTimestamp must not be null"),
                    Objects.requireNonNull(deleted, "deleted must not be null"),
                    Objects.requireNonNull(memo, "memo must not be null"),
                    Objects.requireNonNull(fixedFees, "fixedFees must not be null"),
                    Objects.requireNonNull(feeExemptAuthorities, "feeExemptAuthorities must not be null"),
                    Objects.requireNonNull(fromTimestamp, "fromTimestamp must not be null"),
                    Objects.requireNonNull(toTimestamp, "toTimestamp must not be null"));
        }
    }
}
