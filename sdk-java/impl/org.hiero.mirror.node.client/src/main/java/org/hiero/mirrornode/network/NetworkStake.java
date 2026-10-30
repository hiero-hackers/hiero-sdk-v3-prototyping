package org.hiero.mirrornode.network;

import java.time.Duration;
import java.util.Objects;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;

/// @param maxStakeRewarded maximum tinybars that can be staked for reward at the maximum per-HBAR reward rate
/// @param maxTotalReward tinybars paid as staking rewards in the ending period
/// @param reservedStakingRewards tinybars of account 0.0.800 reserved for earned, uncollected rewards
/// @param rewardBalanceThreshold unreserved tinybars of 0.0.800 needed for the maximum reward rate
/// @param stakingPeriod the current staking period
/// @param stakingPeriodDuration the length of a staking period
/// @param unreservedStakingRewardBalance unreserved balance of 0.0.800 at the close of the ending period
public record NetworkStake(
        long maxStakeRewarded,
        long maxStakingRewardRatePerHbar,
        long maxTotalReward,
        double nodeRewardFeeFraction,
        long reservedStakingRewards,
        long rewardBalanceThreshold,
        long stakeTotal,
        TimestampRange stakingPeriod,
        Duration stakingPeriodDuration,
        long stakingPeriodsStored,
        double stakingRewardFeeFraction,
        long stakingRewardRate,
        long stakingStartThreshold,
        long unreservedStakingRewardBalance) {

    /// Creates a new `NetworkStake`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NetworkStake {
        Objects.requireNonNull(stakingPeriod, "stakingPeriod must not be null");
        Objects.requireNonNull(stakingPeriodDuration, "stakingPeriodDuration must not be null");
    }

    /// Returns a builder for a new `NetworkStake`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NetworkStake`.
    ///
    /// `build()` creates the `NetworkStake` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long maxStakeRewarded;
        private @Nullable Long maxStakingRewardRatePerHbar;
        private @Nullable Long maxTotalReward;
        private @Nullable Double nodeRewardFeeFraction;
        private @Nullable Long reservedStakingRewards;
        private @Nullable Long rewardBalanceThreshold;
        private @Nullable Long stakeTotal;
        private @Nullable TimestampRange stakingPeriod;
        private @Nullable Duration stakingPeriodDuration;
        private @Nullable Long stakingPeriodsStored;
        private @Nullable Double stakingRewardFeeFraction;
        private @Nullable Long stakingRewardRate;
        private @Nullable Long stakingStartThreshold;
        private @Nullable Long unreservedStakingRewardBalance;

        private Builder() {
            // created by NetworkStake.builder()
        }

        /// Sets the `maxStakeRewarded`.
        ///
        /// maximum tinybars that can be staked for reward at the maximum per-HBAR reward rate
        ///
        /// @param maxStakeRewarded the value
        /// @return this builder
        public Builder maxStakeRewarded(final long maxStakeRewarded) {
            this.maxStakeRewarded = maxStakeRewarded;
            return this;
        }

        /// Sets the `maxStakingRewardRatePerHbar`.
        ///
        /// @param maxStakingRewardRatePerHbar the value
        /// @return this builder
        public Builder maxStakingRewardRatePerHbar(final long maxStakingRewardRatePerHbar) {
            this.maxStakingRewardRatePerHbar = maxStakingRewardRatePerHbar;
            return this;
        }

        /// Sets the `maxTotalReward`.
        ///
        /// tinybars paid as staking rewards in the ending period
        ///
        /// @param maxTotalReward the value
        /// @return this builder
        public Builder maxTotalReward(final long maxTotalReward) {
            this.maxTotalReward = maxTotalReward;
            return this;
        }

        /// Sets the `nodeRewardFeeFraction`.
        ///
        /// @param nodeRewardFeeFraction the value
        /// @return this builder
        public Builder nodeRewardFeeFraction(final double nodeRewardFeeFraction) {
            this.nodeRewardFeeFraction = nodeRewardFeeFraction;
            return this;
        }

        /// Sets the `reservedStakingRewards`.
        ///
        /// tinybars of account 0.0.800 reserved for earned, uncollected rewards
        ///
        /// @param reservedStakingRewards the value
        /// @return this builder
        public Builder reservedStakingRewards(final long reservedStakingRewards) {
            this.reservedStakingRewards = reservedStakingRewards;
            return this;
        }

        /// Sets the `rewardBalanceThreshold`.
        ///
        /// unreserved tinybars of 0.0.800 needed for the maximum reward rate
        ///
        /// @param rewardBalanceThreshold the value
        /// @return this builder
        public Builder rewardBalanceThreshold(final long rewardBalanceThreshold) {
            this.rewardBalanceThreshold = rewardBalanceThreshold;
            return this;
        }

        /// Sets the `stakeTotal`.
        ///
        /// @param stakeTotal the value
        /// @return this builder
        public Builder stakeTotal(final long stakeTotal) {
            this.stakeTotal = stakeTotal;
            return this;
        }

        /// Sets the `stakingPeriod`.
        ///
        /// the current staking period
        ///
        /// @param stakingPeriod the value
        /// @return this builder
        public Builder stakingPeriod(final TimestampRange stakingPeriod) {
            this.stakingPeriod = stakingPeriod;
            return this;
        }

        /// Sets the `stakingPeriodDuration`.
        ///
        /// the length of a staking period
        ///
        /// @param stakingPeriodDuration the value
        /// @return this builder
        public Builder stakingPeriodDuration(final Duration stakingPeriodDuration) {
            this.stakingPeriodDuration = stakingPeriodDuration;
            return this;
        }

        /// Sets the `stakingPeriodsStored`.
        ///
        /// @param stakingPeriodsStored the value
        /// @return this builder
        public Builder stakingPeriodsStored(final long stakingPeriodsStored) {
            this.stakingPeriodsStored = stakingPeriodsStored;
            return this;
        }

        /// Sets the `stakingRewardFeeFraction`.
        ///
        /// @param stakingRewardFeeFraction the value
        /// @return this builder
        public Builder stakingRewardFeeFraction(final double stakingRewardFeeFraction) {
            this.stakingRewardFeeFraction = stakingRewardFeeFraction;
            return this;
        }

        /// Sets the `stakingRewardRate`.
        ///
        /// @param stakingRewardRate the value
        /// @return this builder
        public Builder stakingRewardRate(final long stakingRewardRate) {
            this.stakingRewardRate = stakingRewardRate;
            return this;
        }

        /// Sets the `stakingStartThreshold`.
        ///
        /// @param stakingStartThreshold the value
        /// @return this builder
        public Builder stakingStartThreshold(final long stakingStartThreshold) {
            this.stakingStartThreshold = stakingStartThreshold;
            return this;
        }

        /// Sets the `unreservedStakingRewardBalance`.
        ///
        /// unreserved balance of 0.0.800 at the close of the ending period
        ///
        /// @param unreservedStakingRewardBalance the value
        /// @return this builder
        public Builder unreservedStakingRewardBalance(final long unreservedStakingRewardBalance) {
            this.unreservedStakingRewardBalance = unreservedStakingRewardBalance;
            return this;
        }

        /// Creates the `NetworkStake` of the values set.
        ///
        /// @return the new `NetworkStake`
        /// @throws NullPointerException if a required value is not set
        public NetworkStake build() {
            return new NetworkStake(
                    Objects.requireNonNull(maxStakeRewarded, "maxStakeRewarded must not be null"),
                    Objects.requireNonNull(maxStakingRewardRatePerHbar, "maxStakingRewardRatePerHbar must not be null"),
                    Objects.requireNonNull(maxTotalReward, "maxTotalReward must not be null"),
                    Objects.requireNonNull(nodeRewardFeeFraction, "nodeRewardFeeFraction must not be null"),
                    Objects.requireNonNull(reservedStakingRewards, "reservedStakingRewards must not be null"),
                    Objects.requireNonNull(rewardBalanceThreshold, "rewardBalanceThreshold must not be null"),
                    Objects.requireNonNull(stakeTotal, "stakeTotal must not be null"),
                    Objects.requireNonNull(stakingPeriod, "stakingPeriod must not be null"),
                    Objects.requireNonNull(stakingPeriodDuration, "stakingPeriodDuration must not be null"),
                    Objects.requireNonNull(stakingPeriodsStored, "stakingPeriodsStored must not be null"),
                    Objects.requireNonNull(stakingRewardFeeFraction, "stakingRewardFeeFraction must not be null"),
                    Objects.requireNonNull(stakingRewardRate, "stakingRewardRate must not be null"),
                    Objects.requireNonNull(stakingStartThreshold, "stakingStartThreshold must not be null"),
                    Objects.requireNonNull(unreservedStakingRewardBalance, "unreservedStakingRewardBalance must not be null"));
        }
    }
}
