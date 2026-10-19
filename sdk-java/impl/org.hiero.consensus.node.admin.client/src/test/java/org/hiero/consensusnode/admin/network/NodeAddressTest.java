package org.hiero.consensusnode.admin.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.consensusnode.admin.nodes.ServiceEndpoint;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.IpAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NodeAddress`, generated from the specs.
@DisplayName("NodeAddress")
class NodeAddressTest {

    @Test
    @DisplayName("creates a NodeAddress and returns the values")
    @Tag("testId:consensusnode.admin.network.NodeAddress/create")
    void shouldCreateAndReturnTheValues() {
        final long nodeId = nodeIdValue();
        final AccountId accountId = accountIdValue();
        final List<ServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final String description = descriptionValue();
        final byte[] gossipCaCertificate = gossipCaCertificateValue();
        final byte[] grpcCertificateHash = grpcCertificateHashValue();
        final byte[] rsaPublicKey = rsaPublicKeyValue();
        final NodeAddress subject = new NodeAddress(nodeId, accountId, serviceEndpoints, description, gossipCaCertificate, grpcCertificateHash, rsaPublicKey);
        assertValue(nodeId, subject.nodeId());
        assertValue(accountId, subject.accountId());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(description, subject.description());
        assertValue(gossipCaCertificate, subject.gossipCaCertificate());
        assertValue(grpcCertificateHash, subject.grpcCertificateHash());
        assertValue(rsaPublicKey, subject.rsaPublicKey());
    }

    @Test
    @DisplayName("creates a NodeAddress with the default values")
    @Tag("testId:consensusnode.admin.network.NodeAddress/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final NodeAddress subject = new NodeAddress(nodeIdValue(), accountIdValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), rsaPublicKeyValue());
        assertValue(List.of(), subject.serviceEndpoints());
        assertValue("", subject.description());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.admin.network.NodeAddress/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long nodeId = nodeIdValue();
        final AccountId accountId = accountIdValue();
        final List<ServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final String description = descriptionValue();
        final byte[] gossipCaCertificate = gossipCaCertificateValue();
        final byte[] grpcCertificateHash = grpcCertificateHashValue();
        final byte[] rsaPublicKey = rsaPublicKeyValue();
        final NodeAddress subject = NodeAddress.builder()
                .nodeId(nodeId)
                .accountId(accountId)
                .serviceEndpoints(serviceEndpoints)
                .description(description)
                .gossipCaCertificate(gossipCaCertificate)
                .grpcCertificateHash(grpcCertificateHash)
                .rsaPublicKey(rsaPublicKey)
                .build();
        assertValue(nodeId, subject.nodeId());
        assertValue(accountId, subject.accountId());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(description, subject.description());
        assertValue(gossipCaCertificate, subject.gossipCaCertificate());
        assertValue(grpcCertificateHash, subject.grpcCertificateHash());
        assertValue(rsaPublicKey, subject.rsaPublicKey());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.admin.network.NodeAddress/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NodeAddress subject = NodeAddress.builder()
                .nodeId(nodeIdValue())
                .accountId(accountIdValue())
                .gossipCaCertificate(gossipCaCertificateValue())
                .grpcCertificateHash(grpcCertificateHashValue())
                .rsaPublicKey(rsaPublicKeyValue())
                .build();
        assertValue(List.of(), subject.serviceEndpoints());
        assertValue("", subject.description());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.admin.network.NodeAddress/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NodeAddress.builder().build());
    }

    @Test
    @DisplayName("nodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.admin.network.NodeAddress/nodeId/accepts-at-minimum")
    void shouldAcceptNodeIdAtMinimum() {
        final long value = -9223372036854775808L;
        final NodeAddress subject = new NodeAddress(value, accountIdValue(), serviceEndpointsValue(), descriptionValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), rsaPublicKeyValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("nodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.admin.network.NodeAddress/nodeId/accepts-at-maximum")
    void shouldAcceptNodeIdAtMaximum() {
        final long value = 9223372036854775807L;
        final NodeAddress subject = new NodeAddress(value, accountIdValue(), serviceEndpointsValue(), descriptionValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), rsaPublicKeyValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:consensusnode.admin.network.NodeAddress/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new NodeAddress(nodeIdValue(), (AccountId) null, serviceEndpointsValue(), descriptionValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), rsaPublicKeyValue()));
    }

    @Test
    @DisplayName("rejects null for serviceEndpoints")
    @Tag("testId:consensusnode.admin.network.NodeAddress/serviceEndpoints/rejects-null")
    void shouldRejectNullServiceEndpoints() {
        assertThrows(NullPointerException.class, () -> new NodeAddress(nodeIdValue(), accountIdValue(), (List<ServiceEndpoint>) null, descriptionValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), rsaPublicKeyValue()));
    }

    @Test
    @DisplayName("the constructor copies serviceEndpoints; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.admin.network.NodeAddress/serviceEndpoints/copies-in")
    void shouldCopyServiceEndpointsIn() {
        final List<ServiceEndpoint> value = new ArrayList<>(serviceEndpointsValue());
        final List<ServiceEndpoint> expected = List.copyOf(value);
        final NodeAddress subject = new NodeAddress(nodeIdValue(), accountIdValue(), value, descriptionValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), rsaPublicKeyValue());
        value.clear();
        assertValue(expected, subject.serviceEndpoints());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.serviceEndpoints()).clear());
    }

    @Test
    @DisplayName("rejects null for description")
    @Tag("testId:consensusnode.admin.network.NodeAddress/description/rejects-null")
    void shouldRejectNullDescription() {
        assertThrows(NullPointerException.class, () -> new NodeAddress(nodeIdValue(), accountIdValue(), serviceEndpointsValue(), (String) null, gossipCaCertificateValue(), grpcCertificateHashValue(), rsaPublicKeyValue()));
    }

    @Test
    @DisplayName("rejects null for gossipCaCertificate")
    @Tag("testId:consensusnode.admin.network.NodeAddress/gossipCaCertificate/rejects-null")
    void shouldRejectNullGossipCaCertificate() {
        assertThrows(NullPointerException.class, () -> new NodeAddress(nodeIdValue(), accountIdValue(), serviceEndpointsValue(), descriptionValue(), (byte[]) null, grpcCertificateHashValue(), rsaPublicKeyValue()));
    }

    @Test
    @DisplayName("the constructor copies gossipCaCertificate; the accessor returns a copy")
    @Tag("testId:consensusnode.admin.network.NodeAddress/gossipCaCertificate/copies-in")
    void shouldCopyGossipCaCertificateIn() {
        final byte[] value = gossipCaCertificateValue();
        final byte[] expected = value.clone();
        final NodeAddress subject = new NodeAddress(nodeIdValue(), accountIdValue(), serviceEndpointsValue(), descriptionValue(), value, grpcCertificateHashValue(), rsaPublicKeyValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.gossipCaCertificate());
        final byte[] returned = subject.gossipCaCertificate();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.gossipCaCertificate());
    }

    @Test
    @DisplayName("rejects null for grpcCertificateHash")
    @Tag("testId:consensusnode.admin.network.NodeAddress/grpcCertificateHash/rejects-null")
    void shouldRejectNullGrpcCertificateHash() {
        assertThrows(NullPointerException.class, () -> new NodeAddress(nodeIdValue(), accountIdValue(), serviceEndpointsValue(), descriptionValue(), gossipCaCertificateValue(), (byte[]) null, rsaPublicKeyValue()));
    }

    @Test
    @DisplayName("the constructor copies grpcCertificateHash; the accessor returns a copy")
    @Tag("testId:consensusnode.admin.network.NodeAddress/grpcCertificateHash/copies-in")
    void shouldCopyGrpcCertificateHashIn() {
        final byte[] value = grpcCertificateHashValue();
        final byte[] expected = value.clone();
        final NodeAddress subject = new NodeAddress(nodeIdValue(), accountIdValue(), serviceEndpointsValue(), descriptionValue(), gossipCaCertificateValue(), value, rsaPublicKeyValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.grpcCertificateHash());
        final byte[] returned = subject.grpcCertificateHash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.grpcCertificateHash());
    }

    @Test
    @DisplayName("rejects null for rsaPublicKey")
    @Tag("testId:consensusnode.admin.network.NodeAddress/rsaPublicKey/rejects-null")
    void shouldRejectNullRsaPublicKey() {
        assertThrows(NullPointerException.class, () -> new NodeAddress(nodeIdValue(), accountIdValue(), serviceEndpointsValue(), descriptionValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies rsaPublicKey; the accessor returns a copy")
    @Tag("testId:consensusnode.admin.network.NodeAddress/rsaPublicKey/copies-in")
    void shouldCopyRsaPublicKeyIn() {
        final byte[] value = rsaPublicKeyValue();
        final byte[] expected = value.clone();
        final NodeAddress subject = new NodeAddress(nodeIdValue(), accountIdValue(), serviceEndpointsValue(), descriptionValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.rsaPublicKey());
        final byte[] returned = subject.rsaPublicKey();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.rsaPublicKey());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.admin.network.NodeAddress/value-semantics")
    void shouldCompareByValue() {
        final NodeAddress subject = create();
        final NodeAddress other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long nodeIdValue() {
        return 1L;
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static List<ServiceEndpoint> serviceEndpointsValue() {
        return List.of(new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211));
    }

    private static String descriptionValue() {
        return "value";
    }

    private static byte[] gossipCaCertificateValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] grpcCertificateHashValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] rsaPublicKeyValue() {
        return new byte[] {1, 2, 3};
    }

    private static NodeAddress create() {
        return new NodeAddress(nodeIdValue(), accountIdValue(), serviceEndpointsValue(), descriptionValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), rsaPublicKeyValue());
    }

    private static NodeAddress defaultSubject() {
        return new NodeAddress(0L, new AccountId(0L, 0L, "", 3L, null, null), List.of(new ServiceEndpoint(null, "network-node1-svc", 50211)), "node1", new byte[] {(byte) 1, (byte) 2, (byte) 3}, new byte[] {(byte) 4, (byte) 5, (byte) 6}, new byte[] {(byte) 7, (byte) 8, (byte) 9});
    }
}
