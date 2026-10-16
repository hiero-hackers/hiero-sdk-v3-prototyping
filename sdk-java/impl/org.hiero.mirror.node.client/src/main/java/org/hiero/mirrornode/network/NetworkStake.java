package org.hiero.mirrornode.network;

import java.time.Duration;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record NetworkStake(
        long maxStakeReward,
        long maxStakeRewardPerHbar,
        long maxTotalReward,
        double nodeRewardFeeFraction,
        long reservedStakingRewards,
        long rewardBalanceThreshold,
        long stakeTotal,
        Duration stakingPeriod,
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

        private @Nullable Long maxStakeReward;
        private @Nullable Long maxStakeRewardPerHbar;
        private @Nullable Long maxTotalReward;
        private @Nullable Double nodeRewardFeeFraction;
        private @Nullable Long reservedStakingRewards;
        private @Nullable Long rewardBalanceThreshold;
        private @Nullable Long stakeTotal;
        private @Nullable Duration stakingPeriod;
        private @Nullable Long stakingPeriodsStored;
        private @Nullable Double stakingRewardFeeFraction;
        private @Nullable Long stakingRewardRate;
        private @Nullable Long stakingStartThreshold;
        private @Nullable Long unreservedStakingRewardBalance;

        private Builder() {
            // created by NetworkStake.builder()
        }

        /// Sets the `maxStakeReward`.
        ///
        /// @param maxStakeReward the value
        /// @return this builder
        public Builder maxStakeReward(final long maxStakeReward) {
            this.maxStakeReward = maxStakeReward;
            return this;
        }

        /// Sets the `maxStakeRewardPerHbar`.
        ///
        /// @param maxStakeRewardPerHbar the value
        /// @return this builder
        public Builder maxStakeRewardPerHbar(final long maxStakeRewardPerHbar) {
            this.maxStakeRewardPerHbar = maxStakeRewardPerHbar;
            return this;
        }

        /// Sets the `maxTotalReward`.
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
        /// @param reservedStakingRewards the value
        /// @return this builder
        public Builder reservedStakingRewards(final long reservedStakingRewards) {
            this.reservedStakingRewards = reservedStakingRewards;
            return this;
        }

        /// Sets the `rewardBalanceThreshold`.
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
        /// @param stakingPeriod the value
        /// @return this builder
        public Builder stakingPeriod(final Duration stakingPeriod) {
            this.stakingPeriod = stakingPeriod;
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
                    Objects.requireNonNull(maxStakeReward, "maxStakeReward must not be null"),
                    Objects.requireNonNull(maxStakeRewardPerHbar, "maxStakeRewardPerHbar must not be null"),
                    Objects.requireNonNull(maxTotalReward, "maxTotalReward must not be null"),
                    Objects.requireNonNull(nodeRewardFeeFraction, "nodeRewardFeeFraction must not be null"),
                    Objects.requireNonNull(reservedStakingRewards, "reservedStakingRewards must not be null"),
                    Objects.requireNonNull(rewardBalanceThreshold, "rewardBalanceThreshold must not be null"),
                    Objects.requireNonNull(stakeTotal, "stakeTotal must not be null"),
                    Objects.requireNonNull(stakingPeriod, "stakingPeriod must not be null"),
                    Objects.requireNonNull(stakingPeriodsStored, "stakingPeriodsStored must not be null"),
                    Objects.requireNonNull(stakingRewardFeeFraction, "stakingRewardFeeFraction must not be null"),
                    Objects.requireNonNull(stakingRewardRate, "stakingRewardRate must not be null"),
                    Objects.requireNonNull(stakingStartThreshold, "stakingStartThreshold must not be null"),
                    Objects.requireNonNull(unreservedStakingRewardBalance, "unreservedStakingRewardBalance must not be null"));
        }
    }
}
