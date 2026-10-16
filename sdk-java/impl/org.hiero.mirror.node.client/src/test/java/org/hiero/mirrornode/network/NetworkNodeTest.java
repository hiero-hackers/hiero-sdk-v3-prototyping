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
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `NetworkNode`, generated from the specs.
@DisplayName("NetworkNode")
class NetworkNodeTest {

    @Test
    @DisplayName("creates a NetworkNode and returns the values")
    void shouldCreateAndReturnTheValues() {
        final long nodeId = nodeIdValue();
        final AccountId accountId = accountIdValue();
        final @Nullable String description = descriptionValue();
        final @Nullable String publicKey = publicKeyValue();
        final @Nullable String nodeCertHash = nodeCertHashValue();
        final List<ServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final long stake = stakeValue();
        final @Nullable ZonedDateTime timestamp = timestampValue();
        final NetworkNode subject = new NetworkNode(nodeId, accountId, description, publicKey, nodeCertHash, serviceEndpoints, adminKey, stake, timestamp);
        assertValue(nodeId, subject.nodeId());
        assertValue(accountId, subject.accountId());
        assertValue(description, subject.description());
        assertValue(publicKey, subject.publicKey());
        assertValue(nodeCertHash, subject.nodeCertHash());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(adminKey, subject.adminKey());
        assertValue(stake, subject.stake());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("creates a NetworkNode with the default values")
    void shouldCreateWithTheDefaultValues() {
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), descriptionValue(), publicKeyValue(), nodeCertHashValue(), adminKeyValue(), stakeValue(), timestampValue());
        assertValue(List.of(), subject.serviceEndpoints());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final long nodeId = nodeIdValue();
        final AccountId accountId = accountIdValue();
        final @Nullable String description = descriptionValue();
        final @Nullable String publicKey = publicKeyValue();
        final @Nullable String nodeCertHash = nodeCertHashValue();
        final List<ServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final long stake = stakeValue();
        final @Nullable ZonedDateTime timestamp = timestampValue();
        final NetworkNode subject = NetworkNode.builder()
                .nodeId(nodeId)
                .accountId(accountId)
                .description(description)
                .publicKey(publicKey)
                .nodeCertHash(nodeCertHash)
                .serviceEndpoints(serviceEndpoints)
                .adminKey(adminKey)
                .stake(stake)
                .timestamp(timestamp)
                .build();
        assertValue(nodeId, subject.nodeId());
        assertValue(accountId, subject.accountId());
        assertValue(description, subject.description());
        assertValue(publicKey, subject.publicKey());
        assertValue(nodeCertHash, subject.nodeCertHash());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(adminKey, subject.adminKey());
        assertValue(stake, subject.stake());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final NetworkNode subject = NetworkNode.builder()
                .nodeId(nodeIdValue())
                .accountId(accountIdValue())
                .stake(stakeValue())
                .build();
        assertNull(subject.description());
        assertNull(subject.publicKey());
        assertNull(subject.nodeCertHash());
        assertValue(List.of(), subject.serviceEndpoints());
        assertNull(subject.adminKey());
        assertNull(subject.timestamp());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NetworkNode.builder().build());
    }

    @Test
    @DisplayName("nodeId: accepts the minimum (-9223372036854775808)")
    void shouldAcceptNodeIdAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkNode subject = new NetworkNode(value, accountIdValue(), descriptionValue(), publicKeyValue(), nodeCertHashValue(), serviceEndpointsValue(), adminKeyValue(), stakeValue(), timestampValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("nodeId: accepts the maximum (9223372036854775807)")
    void shouldAcceptNodeIdAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkNode subject = new NetworkNode(value, accountIdValue(), descriptionValue(), publicKeyValue(), nodeCertHashValue(), serviceEndpointsValue(), adminKeyValue(), stakeValue(), timestampValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("rejects null for accountId")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new NetworkNode(nodeIdValue(), (AccountId) null, descriptionValue(), publicKeyValue(), nodeCertHashValue(), serviceEndpointsValue(), adminKeyValue(), stakeValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for serviceEndpoints")
    void shouldRejectNullServiceEndpoints() {
        assertThrows(NullPointerException.class, () -> new NetworkNode(nodeIdValue(), accountIdValue(), descriptionValue(), publicKeyValue(), nodeCertHashValue(), (List<ServiceEndpoint>) null, adminKeyValue(), stakeValue(), timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies serviceEndpoints; the accessor returns an unmodifiable collection")
    void shouldCopyServiceEndpointsIn() {
        final List<ServiceEndpoint> value = new ArrayList<>(serviceEndpointsValue());
        final List<ServiceEndpoint> expected = List.copyOf(value);
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), descriptionValue(), publicKeyValue(), nodeCertHashValue(), value, adminKeyValue(), stakeValue(), timestampValue());
        value.clear();
        assertValue(expected, subject.serviceEndpoints());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.serviceEndpoints()).clear());
    }

    @Test
    @DisplayName("stake: accepts the minimum (-9223372036854775808)")
    void shouldAcceptStakeAtMinimum() {
        final long value = -9223372036854775808L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), descriptionValue(), publicKeyValue(), nodeCertHashValue(), serviceEndpointsValue(), adminKeyValue(), value, timestampValue());
        assertValue(value, subject.stake());
    }

    @Test
    @DisplayName("stake: accepts the maximum (9223372036854775807)")
    void shouldAcceptStakeAtMaximum() {
        final long value = 9223372036854775807L;
        final NetworkNode subject = new NetworkNode(nodeIdValue(), accountIdValue(), descriptionValue(), publicKeyValue(), nodeCertHashValue(), serviceEndpointsValue(), adminKeyValue(), value, timestampValue());
        assertValue(value, subject.stake());
    }

    private static long nodeIdValue() {
        return 1L;
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static String descriptionValue() {
        return "value";
    }

    private static String publicKeyValue() {
        return "value";
    }

    private static String nodeCertHashValue() {
        return "value";
    }

    private static List<ServiceEndpoint> serviceEndpointsValue() {
        return List.of(new ServiceEndpoint(null, 1, null));
    }

    private static Authority adminKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static long stakeValue() {
        return 1L;
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static NetworkNode create() {
        return new NetworkNode(nodeIdValue(), accountIdValue(), descriptionValue(), publicKeyValue(), nodeCertHashValue(), serviceEndpointsValue(), adminKeyValue(), stakeValue(), timestampValue());
    }
}
