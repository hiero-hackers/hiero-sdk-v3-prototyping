package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.mirrornode.common.TimestampRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NetworkStake`, generated from the specs.
@DisplayName("NetworkStake")
class NetworkStakeTest {

    @Test
    @DisplayName("creates a NetworkStake and returns the values")
    @Tag("testId:mirrornode.network.NetworkStake/create")
    void shouldCreateAndReturnTheValues() {
        final long maxStakeRewarded = maxStakeRewardedValue();
        final long maxStakingRewardRatePerHbar = maxStakingRewardRatePerHbarValue();
        final long maxTotalReward = maxTotalRewardValue();
        final double nodeRewardFeeFraction = nodeRewardFeeFractionValue();
        final long reservedStakingRewards = reservedStakingRewardsValue();
        final long rewardBalanceThreshold = rewardBalanceThresholdValue();
        final long stakeTotal = stakeTotalValue();
        final TimestampRange stakingPeriod = stakingPeriodValue();
        final Duration stakingPeriodDuration = stakingPeriodDurationValue();
        final long stakingPeriodsStored = stakingPeriodsStoredValue();
        final double stakingRewardFeeFraction = stakingRewardFeeFractionValue();
        final long stakingRewardRate = stakingRewardRateValue();
        final long stakingStartThreshold = stakingStartThresholdValue();
        final long unreservedStakingRewardBalance = unreservedStakingRewardBalanceValue();
        final NetworkStake subject = new NetworkStake(maxStakeRewarded, maxStakingRewardRatePerHbar, maxTotalReward, nodeRewardFeeFraction, reservedStakingRewards, rewardBalanceThreshold, stakeTotal, stakingPeriod, stakingPeriodDuration, stakingPeriodsStored, stakingRewardFeeFraction, stakingRewardRate, stakingStartThreshold, unreservedStakingRewardBalance);
        assertValue(maxStakeRewarded, subject.maxStakeRewarded());
        assertValue(maxStakingRewardRatePerHbar, subject.maxStakingRewardRatePerHbar());
        assertValue(maxTotalReward, subject.maxTotalReward());
        assertValue(nodeRewardFeeFraction, subject.nodeRewardFeeFraction());
        assertValue(reservedStakingRewards, subject.reservedStakingRewards());
        assertValue(rewardBalanceThreshold, subject.rewardBalanceThreshold());
        assertValue(stakeTotal, subject.stakeTotal());
        assertValue(stakingPeriod, subject.stakingPeriod());
        assertValue(stakingPeriodDuration, subject.stakingPeriodDuration());
        assertValue(stakingPeriodsStored, subject.stakingPeriodsStored());
        assertValue(stakingRewardFeeFraction, subject.stakingRewardFeeFraction());
        assertValue(stakingRewardRate, subject.stakingRewardRate());
        assertValue(stakingStartThreshold, subject.stakingStartThreshold());
        assertValue(unreservedStakingRewardBalance, subject.unreservedStakingRewardBalance());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.network.NetworkStake/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long maxStakeRewarded = maxStakeRewardedValue();
        final long maxStakingRewardRatePerHbar = maxStakingRewardRatePerHbarValue();
        final long maxTotalReward = maxTotalRewardValue();
        final double nodeRewardFeeFraction = nodeRewardFeeFractionValue();
        final long reservedStakingRewards = reservedStakingRewardsValue();
        final long rewardBalanceThreshold = rewardBalanceThresholdValue();
        final long stakeTotal = stakeTotalValue();
        final TimestampRange stakingPeriod = stakingPeriodValue();
        final Duration stakingPeriodDuration = stakingPeriodDurationValue();
        final long stakingPeriodsStored = stakingPeriodsStoredValue();
        final double stakingRewardFeeFraction = stakingRewardFeeFractionValue();
        final long stakingRewardRate = stakingRewardRateValue();
        final long stakingStartThreshold = stakingStartThresholdValue();
        final long unreservedStakingRewardBalance = unreservedStakingRewardBalanceValue();
        final NetworkStake subject = NetworkStake.builder()
                .maxStakeRewarded(maxStakeRewarded)
                .maxStakingRewardRatePerHbar(maxStakingRewardRatePerHbar)
                .maxTotalReward(maxTotalReward)
                .nodeRewardFeeFraction(nodeRewardFeeFraction)
                .reservedStakingRewards(reservedStakingRewards)
                .rewardBalanceThreshold(rewardBalanceThreshold)
                .stakeTotal(stakeTotal)
                .stakingPeriod(stakingPeriod)
                .stakingPeriodDuration(stakingPeriodDuration)
                .stakingPeriodsStored(stakingPeriodsStored)
                .stakingRewardFeeFraction(stakingRewardFeeFraction)
                .stakingRewardRate(stakingRewardRate)
                .stakingStartThreshold(stakingStartThreshold)
                .unreservedStakingRewardBalance(unreservedStakingRewardBalance)
                .build();
        assertValue(maxStakeRewarded, subject.maxStakeRewarded());
        assertValue(maxStakingRewardRatePerHbar, subject.maxStakingRewardRatePerHbar());
        assertValue(maxTotalReward, subject.maxTotalReward());
        assertValue(nodeRewardFeeFraction, subject.nodeRewardFeeFraction());
        assertValue(reservedStakingRewards, subject.reservedStakingRewards());
        assertValue(rewardBalanceThreshold, subject.rewardBalanceThreshold());
        assertValue(stakeTotal, subject.stakeTotal());
        assertValue(stakingPeriod, subject.stakingPeriod());
        assertValue(stakingPeriodDuration, subject.stakingPeriodDuration());
        assertValue(stakingPeriodsStored, subject.stakingPeriodsStored());
        assertValue(stakingRewardFeeFraction, subject.stakingRewardFeeFraction());
        assertValue(stakingRewardRate, subject.stakingRewardRate());
        assertValue(stakingStartThreshold, subject.stakingStartThreshold());
        assertValue(unreservedStakingRewardBalance, subject.unreservedStakingRewardBalance());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.network.NetworkStake/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NetworkStake subject = NetworkStake.builder()
                .maxStakeRewarded(maxStakeRewardedValue())
                .maxStakingRewardRatePerHbar(maxStakingRewardRatePerHbarValue())
                .maxTotalReward(maxTotalRewardValue())
                .nodeRewardFeeFraction(nodeRewardFeeFractionValue())
                .reservedStakingRewards(reservedStakingRewardsValue())
                .rewardBalanceThreshold(rewardBalanceThresholdValue())
                .stakeTotal(stakeTotalValue())
                .stakingPeriod(stakingPeriodValue())
                .stakingPeriodDuration(stakingPeriodDurationValue())
                .stakingPeriodsStored(stakingPeriodsStoredValue())
                .stakingRewardFeeFraction(stakingRewardFeeFractionValue())
                .stakingRewardRate(stakingRewardRateValue())
                .stakingStartThreshold(stakingStartThresholdValue())
                .unreservedStakingRewardBalance(unreservedStakingRewardBalanceValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.network.NetworkStake/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NetworkStake.builder().build());
    }

    @Test
    @DisplayName("maxStakeRewarded: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/maxStakeRewarded/accepts-at-minimum")
    void shouldAcceptMaxStakeRewardedAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(value, maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.maxStakeRewarded());
    }

    @Test
    @DisplayName("maxStakeRewarded: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/maxStakeRewarded/accepts-at-maximum")
    void shouldAcceptMaxStakeRewardedAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(value, maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.maxStakeRewarded());
    }

    @Test
    @DisplayName("maxStakingRewardRatePerHbar: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/maxStakingRewardRatePerHbar/accepts-at-minimum")
    void shouldAcceptMaxStakingRewardRatePerHbarAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), value, maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.maxStakingRewardRatePerHbar());
    }

    @Test
    @DisplayName("maxStakingRewardRatePerHbar: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/maxStakingRewardRatePerHbar/accepts-at-maximum")
    void shouldAcceptMaxStakingRewardRatePerHbarAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), value, maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.maxStakingRewardRatePerHbar());
    }

    @Test
    @DisplayName("maxTotalReward: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/maxTotalReward/accepts-at-minimum")
    void shouldAcceptMaxTotalRewardAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), value, nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.maxTotalReward());
    }

    @Test
    @DisplayName("maxTotalReward: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/maxTotalReward/accepts-at-maximum")
    void shouldAcceptMaxTotalRewardAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), value, nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.maxTotalReward());
    }

    @Test
    @DisplayName("reservedStakingRewards: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/reservedStakingRewards/accepts-at-minimum")
    void shouldAcceptReservedStakingRewardsAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), value, rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.reservedStakingRewards());
    }

    @Test
    @DisplayName("reservedStakingRewards: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/reservedStakingRewards/accepts-at-maximum")
    void shouldAcceptReservedStakingRewardsAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), value, rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.reservedStakingRewards());
    }

    @Test
    @DisplayName("rewardBalanceThreshold: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/rewardBalanceThreshold/accepts-at-minimum")
    void shouldAcceptRewardBalanceThresholdAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), value, stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.rewardBalanceThreshold());
    }

    @Test
    @DisplayName("rewardBalanceThreshold: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/rewardBalanceThreshold/accepts-at-maximum")
    void shouldAcceptRewardBalanceThresholdAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), value, stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.rewardBalanceThreshold());
    }

    @Test
    @DisplayName("stakeTotal: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/stakeTotal/accepts-at-minimum")
    void shouldAcceptStakeTotalAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), value, stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.stakeTotal());
    }

    @Test
    @DisplayName("stakeTotal: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/stakeTotal/accepts-at-maximum")
    void shouldAcceptStakeTotalAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), value, stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.stakeTotal());
    }

    @Test
    @DisplayName("rejects null for stakingPeriod")
    @Tag("testId:mirrornode.network.NetworkStake/stakingPeriod/rejects-null")
    void shouldRejectNullStakingPeriod() {
        assertThrows(NullPointerException.class, () -> new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), (TimestampRange) null, stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue()));
    }

    @Test
    @DisplayName("rejects null for stakingPeriodDuration")
    @Tag("testId:mirrornode.network.NetworkStake/stakingPeriodDuration/rejects-null")
    void shouldRejectNullStakingPeriodDuration() {
        assertThrows(NullPointerException.class, () -> new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), (Duration) null, stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue()));
    }

    @Test
    @DisplayName("stakingPeriodsStored: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/stakingPeriodsStored/accepts-at-minimum")
    void shouldAcceptStakingPeriodsStoredAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), value, stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.stakingPeriodsStored());
    }

    @Test
    @DisplayName("stakingPeriodsStored: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/stakingPeriodsStored/accepts-at-maximum")
    void shouldAcceptStakingPeriodsStoredAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), value, stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.stakingPeriodsStored());
    }

    @Test
    @DisplayName("stakingRewardRate: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/stakingRewardRate/accepts-at-minimum")
    void shouldAcceptStakingRewardRateAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), value, stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.stakingRewardRate());
    }

    @Test
    @DisplayName("stakingRewardRate: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/stakingRewardRate/accepts-at-maximum")
    void shouldAcceptStakingRewardRateAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), value, stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
        assertValue(value, subject.stakingRewardRate());
    }

    @Test
    @DisplayName("stakingStartThreshold: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/stakingStartThreshold/accepts-at-minimum")
    void shouldAcceptStakingStartThresholdAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), value, unreservedStakingRewardBalanceValue());
        assertValue(value, subject.stakingStartThreshold());
    }

    @Test
    @DisplayName("stakingStartThreshold: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/stakingStartThreshold/accepts-at-maximum")
    void shouldAcceptStakingStartThresholdAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), value, unreservedStakingRewardBalanceValue());
        assertValue(value, subject.stakingStartThreshold());
    }

    @Test
    @DisplayName("unreservedStakingRewardBalance: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkStake/unreservedStakingRewardBalance/accepts-at-minimum")
    void shouldAcceptUnreservedStakingRewardBalanceAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), value);
        assertValue(value, subject.unreservedStakingRewardBalance());
    }

    @Test
    @DisplayName("unreservedStakingRewardBalance: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkStake/unreservedStakingRewardBalance/accepts-at-maximum")
    void shouldAcceptUnreservedStakingRewardBalanceAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkStake subject = new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), value);
        assertValue(value, subject.unreservedStakingRewardBalance());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.NetworkStake/value-semantics")
    void shouldCompareByValue() {
        final NetworkStake subject = create();
        final NetworkStake other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long maxStakeRewardedValue() {
        return 1L;
    }

    private static long maxStakingRewardRatePerHbarValue() {
        return 1L;
    }

    private static long maxTotalRewardValue() {
        return 1L;
    }

    private static double nodeRewardFeeFractionValue() {
        return 1.5;
    }

    private static long reservedStakingRewardsValue() {
        return 1L;
    }

    private static long rewardBalanceThresholdValue() {
        return 1L;
    }

    private static long stakeTotalValue() {
        return 1L;
    }

    private static TimestampRange stakingPeriodValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static Duration stakingPeriodDurationValue() {
        return Duration.ofSeconds(1L);
    }

    private static long stakingPeriodsStoredValue() {
        return 1L;
    }

    private static double stakingRewardFeeFractionValue() {
        return 1.5;
    }

    private static long stakingRewardRateValue() {
        return 1L;
    }

    private static long stakingStartThresholdValue() {
        return 1L;
    }

    private static long unreservedStakingRewardBalanceValue() {
        return 1L;
    }

    private static NetworkStake create() {
        return new NetworkStake(maxStakeRewardedValue(), maxStakingRewardRatePerHbarValue(), maxTotalRewardValue(), nodeRewardFeeFractionValue(), reservedStakingRewardsValue(), rewardBalanceThresholdValue(), stakeTotalValue(), stakingPeriodValue(), stakingPeriodDurationValue(), stakingPeriodsStoredValue(), stakingRewardFeeFractionValue(), stakingRewardRateValue(), stakingStartThresholdValue(), unreservedStakingRewardBalanceValue());
    }
}
