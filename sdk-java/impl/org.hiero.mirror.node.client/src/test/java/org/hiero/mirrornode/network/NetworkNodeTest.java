package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NetworkNode`, generated from the specs.
@DisplayName("NetworkNode")
class NetworkNodeTest {

    @Test
    @DisplayName("creates a NetworkNode and returns the values")
    @Tag("testId:mirrornode.network.NetworkNode/create")
    void shouldCreateAndReturnTheValues() {
        final long nodeId = nodeIdValue();
        final AccountId accountId = accountIdValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final List<Long> associatedRegisteredNodes = associatedRegisteredNodesValue();
        final @Nullable Boolean declineReward = declineRewardValue();
        final @Nullable String description = descriptionValue();
        final @Nullable Address fileId = fileIdValue();
        final @Nullable ServiceEndpoint grpcProxyEndpoint = grpcProxyEndpointValue();
        final @Nullable Long maxStake = maxStakeValue();
        final @Nullable String memo = memoValue();
        final @Nullable Long minStake = minStakeValue();
        final @Nullable String nodeCertHash = nodeCertHashValue();
        final @Nullable String publicKey = publicKeyValue();
        final @Nullable Long rewardRateStart = rewardRateStartValue();
        final List<ServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final @Nullable Long stake = stakeValue();
        final @Nullable Long stakeNotRewarded = stakeNotRewardedValue();
        final @Nullable Long stakeRewarded = stakeRewardedValue();
        final @Nullable TimestampRange stakingPeriod = stakingPeriodValue();
        final TimestampRange timestamp = timestampValue();
        final NetworkNode subject = new NetworkNode(nodeId, accountId, adminKey, associatedRegisteredNodes, declineReward, description, fileId, grpcProxyEndpoint, maxStake, memo, minStake, nodeCertHash, publicKey, rewardRateStart, serviceEndpoints, stake, stakeNotRewarded, stakeRewarded, stakingPeriod, timestamp);
        assertValue(nodeId, subject.nodeId());
        assertValue(accountId, subject.accountId());
        assertValue(adminKey, subject.adminKey());
        assertValue(associatedRegisteredNodes, subject.associatedRegisteredNodes());
        assertValue(declineReward, subject.declineReward());
        assertValue(description, subject.description());
        assertValue(fileId, subject.fileId());
        assertValue(grpcProxyEndpoint, subject.grpcProxyEndpoint());
        assertValue(maxStake, subject.maxStake());
        assertValue(memo, subject.memo());
        assertValue(minStake, subject.minStake());
        assertValue(nodeCertHash, subject.nodeCertHash());
        assertValue(publicKey, subject.publicKey());
        assertValue(rewardRateStart, subject.rewardRateStart());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(stake, subject.stake());
        assertValue(stakeNotRewarded, subject.stakeNotRewarded());
        assertValue(stakeRewarded, subject.stakeRewarded());
        assertValue(stakingPeriod, subject.stakingPeriod());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("creates a NetworkNode with the default values")
    @Tag("testId:mirrornode.network.NetworkNode/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(List.of(), subject.associatedRegisteredNodes());
        assertValue(List.of(), subject.serviceEndpoints());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.network.NetworkNode/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long nodeId = nodeIdValue();
        final AccountId accountId = accountIdValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final List<Long> associatedRegisteredNodes = associatedRegisteredNodesValue();
        final @Nullable Boolean declineReward = declineRewardValue();
        final @Nullable String description = descriptionValue();
        final @Nullable Address fileId = fileIdValue();
        final @Nullable ServiceEndpoint grpcProxyEndpoint = grpcProxyEndpointValue();
        final @Nullable Long maxStake = maxStakeValue();
        final @Nullable String memo = memoValue();
        final @Nullable Long minStake = minStakeValue();
        final @Nullable String nodeCertHash = nodeCertHashValue();
        final @Nullable String publicKey = publicKeyValue();
        final @Nullable Long rewardRateStart = rewardRateStartValue();
        final List<ServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final @Nullable Long stake = stakeValue();
        final @Nullable Long stakeNotRewarded = stakeNotRewardedValue();
        final @Nullable Long stakeRewarded = stakeRewardedValue();
        final @Nullable TimestampRange stakingPeriod = stakingPeriodValue();
        final TimestampRange timestamp = timestampValue();
        final NetworkNode subject = NetworkNode.builder()
                .nodeId(nodeId)
                .accountId(accountId)
                .adminKey(adminKey)
                .associatedRegisteredNodes(associatedRegisteredNodes)
                .declineReward(declineReward)
                .description(description)
                .fileId(fileId)
                .grpcProxyEndpoint(grpcProxyEndpoint)
                .maxStake(maxStake)
                .memo(memo)
                .minStake(minStake)
                .nodeCertHash(nodeCertHash)
                .publicKey(publicKey)
                .rewardRateStart(rewardRateStart)
                .serviceEndpoints(serviceEndpoints)
                .stake(stake)
                .stakeNotRewarded(stakeNotRewarded)
                .stakeRewarded(stakeRewarded)
                .stakingPeriod(stakingPeriod)
                .timestamp(timestamp)
                .build();
        assertValue(nodeId, subject.nodeId());
        assertValue(accountId, subject.accountId());
        assertValue(adminKey, subject.adminKey());
        assertValue(associatedRegisteredNodes, subject.associatedRegisteredNodes());
        assertValue(declineReward, subject.declineReward());
        assertValue(description, subject.description());
        assertValue(fileId, subject.fileId());
        assertValue(grpcProxyEndpoint, subject.grpcProxyEndpoint());
        assertValue(maxStake, subject.maxStake());
        assertValue(memo, subject.memo());
        assertValue(minStake, subject.minStake());
        assertValue(nodeCertHash, subject.nodeCertHash());
        assertValue(publicKey, subject.publicKey());
        assertValue(rewardRateStart, subject.rewardRateStart());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(stake, subject.stake());
        assertValue(stakeNotRewarded, subject.stakeNotRewarded());
        assertValue(stakeRewarded, subject.stakeRewarded());
        assertValue(stakingPeriod, subject.stakingPeriod());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.network.NetworkNode/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NetworkNode subject = NetworkNode.builder()
                .nodeId(nodeIdValue())
                .accountId(accountIdValue())
                .timestamp(timestampValue())
                .build();
        assertNull(subject.adminKey());
        assertValue(List.of(), subject.associatedRegisteredNodes());
        assertNull(subject.declineReward());
        assertNull(subject.description());
        assertNull(subject.fileId());
        assertNull(subject.grpcProxyEndpoint());
        assertNull(subject.maxStake());
        assertNull(subject.memo());
        assertNull(subject.minStake());
        assertNull(subject.nodeCertHash());
        assertNull(subject.publicKey());
        assertNull(subject.rewardRateStart());
        assertValue(List.of(), subject.serviceEndpoints());
        assertNull(subject.stake());
        assertNull(subject.stakeNotRewarded());
        assertNull(subject.stakeRewarded());
        assertNull(subject.stakingPeriod());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.network.NetworkNode/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NetworkNode.builder().build());
    }

    @Test
    @DisplayName("nodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkNode/nodeId/accepts-at-minimum")
    void shouldAcceptNodeIdAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkNode subject = new NetworkNode(value, accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("nodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkNode/nodeId/accepts-at-maximum")
    void shouldAcceptNodeIdAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkNode subject = new NetworkNode(value, accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:mirrornode.network.NetworkNode/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new NetworkNode(nodeIdValue(), (AccountId) null, adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for associatedRegisteredNodes")
    @Tag("testId:mirrornode.network.NetworkNode/associatedRegisteredNodes/rejects-null")
    void shouldRejectNullAssociatedRegisteredNodes() {
        assertThrows(NullPointerException.class, () -> new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), (List<Long>) null, declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies associatedRegisteredNodes; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.network.NetworkNode/associatedRegisteredNodes/copies-in")
    void shouldCopyAssociatedRegisteredNodesIn() {
        final List<Long> value = new ArrayList<>(associatedRegisteredNodesValue());
        final List<Long> expected = List.copyOf(value);
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), value, declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        value.clear();
        assertValue(expected, subject.associatedRegisteredNodes());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.associatedRegisteredNodes()).clear());
    }

    @Test
    @DisplayName("maxStake: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkNode/maxStake/accepts-at-minimum")
    void shouldAcceptMaxStakeAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), value, memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.maxStake());
    }

    @Test
    @DisplayName("maxStake: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkNode/maxStake/accepts-at-maximum")
    void shouldAcceptMaxStakeAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), value, memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.maxStake());
    }

    @Test
    @DisplayName("minStake: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkNode/minStake/accepts-at-minimum")
    void shouldAcceptMinStakeAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), value, nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.minStake());
    }

    @Test
    @DisplayName("minStake: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkNode/minStake/accepts-at-maximum")
    void shouldAcceptMinStakeAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), value, nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.minStake());
    }

    @Test
    @DisplayName("rewardRateStart: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkNode/rewardRateStart/accepts-at-minimum")
    void shouldAcceptRewardRateStartAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), value, serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.rewardRateStart());
    }

    @Test
    @DisplayName("rewardRateStart: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkNode/rewardRateStart/accepts-at-maximum")
    void shouldAcceptRewardRateStartAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), value, serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.rewardRateStart());
    }

    @Test
    @DisplayName("rejects null for serviceEndpoints")
    @Tag("testId:mirrornode.network.NetworkNode/serviceEndpoints/rejects-null")
    void shouldRejectNullServiceEndpoints() {
        assertThrows(NullPointerException.class, () -> new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), (List<ServiceEndpoint>) null, stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies serviceEndpoints; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.network.NetworkNode/serviceEndpoints/copies-in")
    void shouldCopyServiceEndpointsIn() {
        final List<ServiceEndpoint> value = new ArrayList<>(serviceEndpointsValue());
        final List<ServiceEndpoint> expected = List.copyOf(value);
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), value, stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        value.clear();
        assertValue(expected, subject.serviceEndpoints());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.serviceEndpoints()).clear());
    }

    @Test
    @DisplayName("stake: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkNode/stake/accepts-at-minimum")
    void shouldAcceptStakeAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), value, stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.stake());
    }

    @Test
    @DisplayName("stake: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkNode/stake/accepts-at-maximum")
    void shouldAcceptStakeAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), value, stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.stake());
    }

    @Test
    @DisplayName("stakeNotRewarded: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkNode/stakeNotRewarded/accepts-at-minimum")
    void shouldAcceptStakeNotRewardedAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), value, stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.stakeNotRewarded());
    }

    @Test
    @DisplayName("stakeNotRewarded: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkNode/stakeNotRewarded/accepts-at-maximum")
    void shouldAcceptStakeNotRewardedAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), value, stakeRewardedValue(), stakingPeriodValue(), timestampValue());
        assertValue(value, subject.stakeNotRewarded());
    }

    @Test
    @DisplayName("stakeRewarded: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.NetworkNode/stakeRewarded/accepts-at-minimum")
    void shouldAcceptStakeRewardedAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), value, stakingPeriodValue(), timestampValue());
        assertValue(value, subject.stakeRewarded());
    }

    @Test
    @DisplayName("stakeRewarded: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.NetworkNode/stakeRewarded/accepts-at-maximum")
    void shouldAcceptStakeRewardedAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), value, stakingPeriodValue(), timestampValue());
        assertValue(value, subject.stakeRewarded());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.network.NetworkNode/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), (TimestampRange) null));
    }

    private static long nodeIdValue() {
        return 1L;
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Authority adminKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static List<Long> associatedRegisteredNodesValue() {
        return List.of(1L);
    }

    private static boolean declineRewardValue() {
        return true;
    }

    private static String descriptionValue() {
        return "value";
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static ServiceEndpoint grpcProxyEndpointValue() {
        return new ServiceEndpoint(null, 1, null);
    }

    private static long maxStakeValue() {
        return 1L;
    }

    private static String memoValue() {
        return "value";
    }

    private static long minStakeValue() {
        return 1L;
    }

    private static String nodeCertHashValue() {
        return "value";
    }

    private static String publicKeyValue() {
        return "value";
    }

    private static long rewardRateStartValue() {
        return 1L;
    }

    private static List<ServiceEndpoint> serviceEndpointsValue() {
        return List.of(new ServiceEndpoint(null, 1, null));
    }

    private static long stakeValue() {
        return 1L;
    }

    private static long stakeNotRewardedValue() {
        return 1L;
    }

    private static long stakeRewardedValue() {
        return 1L;
    }

    private static TimestampRange stakingPeriodValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static TimestampRange timestampValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static NetworkNode create() {
        return new NetworkNode(nodeIdValue(), accountIdValue(), adminKeyValue(), associatedRegisteredNodesValue(), declineRewardValue(), descriptionValue(), fileIdValue(), grpcProxyEndpointValue(), maxStakeValue(), memoValue(), minStakeValue(), nodeCertHashValue(), publicKeyValue(), rewardRateStartValue(), serviceEndpointsValue(), stakeValue(), stakeNotRewardedValue(), stakeRewardedValue(), stakingPeriodValue(), timestampValue());
    }
}
