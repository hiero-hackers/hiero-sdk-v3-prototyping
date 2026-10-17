package org.hiero.consensusnode.admin.nodes;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NodeUpdateTransaction`, generated from the specs.
@DisplayName("NodeUpdateTransaction")
class NodeUpdateTransactionTest {

    @Test
    @DisplayName("creates a NodeUpdateTransaction and returns the values")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final long nodeId = nodeIdValue();
        final @Nullable AccountId accountId = accountIdValue();
        final @Nullable String description = descriptionValue();
        final List<ServiceEndpoint> gossipEndpoints = gossipEndpointsValue();
        final List<ServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final byte @Nullable [] gossipCaCertificate = gossipCaCertificateValue();
        final byte @Nullable [] grpcCertificateHash = grpcCertificateHashValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Boolean declineReward = declineRewardValue();
        final @Nullable ServiceEndpoint grpcWebProxyEndpoint = grpcWebProxyEndpointValue();
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(nodeId, accountId, description, gossipEndpoints, serviceEndpoints, gossipCaCertificate, grpcCertificateHash, adminAuthority, declineReward, grpcWebProxyEndpoint);
        assertValue(nodeId, subject.nodeId());
        assertValue(accountId, subject.accountId());
        assertValue(description, subject.description());
        assertValue(gossipEndpoints, subject.gossipEndpoints());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(gossipCaCertificate, subject.gossipCaCertificate());
        assertValue(grpcCertificateHash, subject.grpcCertificateHash());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(declineReward, subject.declineReward());
        assertValue(grpcWebProxyEndpoint, subject.grpcWebProxyEndpoint());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("creates a NodeUpdateTransaction with the default values")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        assertValue(List.of(), subject.gossipEndpoints());
        assertValue(List.of(), subject.serviceEndpoints());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long nodeId = nodeIdValue();
        final @Nullable AccountId accountId = accountIdValue();
        final @Nullable String description = descriptionValue();
        final List<ServiceEndpoint> gossipEndpoints = gossipEndpointsValue();
        final List<ServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final byte @Nullable [] gossipCaCertificate = gossipCaCertificateValue();
        final byte @Nullable [] grpcCertificateHash = grpcCertificateHashValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Boolean declineReward = declineRewardValue();
        final @Nullable ServiceEndpoint grpcWebProxyEndpoint = grpcWebProxyEndpointValue();
        final NodeUpdateTransaction subject = NodeUpdateTransaction.builder()
                .nodeId(nodeId)
                .accountId(accountId)
                .description(description)
                .gossipEndpoints(gossipEndpoints)
                .serviceEndpoints(serviceEndpoints)
                .gossipCaCertificate(gossipCaCertificate)
                .grpcCertificateHash(grpcCertificateHash)
                .adminAuthority(adminAuthority)
                .declineReward(declineReward)
                .grpcWebProxyEndpoint(grpcWebProxyEndpoint)
                .build();
        assertValue(nodeId, subject.nodeId());
        assertValue(accountId, subject.accountId());
        assertValue(description, subject.description());
        assertValue(gossipEndpoints, subject.gossipEndpoints());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(gossipCaCertificate, subject.gossipCaCertificate());
        assertValue(grpcCertificateHash, subject.grpcCertificateHash());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(declineReward, subject.declineReward());
        assertValue(grpcWebProxyEndpoint, subject.grpcWebProxyEndpoint());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NodeUpdateTransaction subject = NodeUpdateTransaction.builder()
                .nodeId(nodeIdValue())
                .build();
        assertNull(subject.accountId());
        assertNull(subject.description());
        assertValue(List.of(), subject.gossipEndpoints());
        assertValue(List.of(), subject.serviceEndpoints());
        assertNull(subject.gossipCaCertificate());
        assertNull(subject.grpcCertificateHash());
        assertNull(subject.adminAuthority());
        assertNull(subject.declineReward());
        assertNull(subject.grpcWebProxyEndpoint());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NodeUpdateTransaction.builder().build());
    }

    @Test
    @DisplayName("nodeId: accepts the minimum (0)")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/nodeId/accepts-at-minimum")
    void shouldAcceptNodeIdAtMinimum() {
        final long value = 0L;
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(value, accountIdValue(), descriptionValue(), gossipEndpointsValue(), serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("nodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/nodeId/accepts-at-maximum")
    void shouldAcceptNodeIdAtMaximum() {
        final long value = 9223372036854775807L;
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(value, accountIdValue(), descriptionValue(), gossipEndpointsValue(), serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("nodeId: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/nodeId/rejects-below-minimum")
    void shouldRejectNodeIdBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new NodeUpdateTransaction(-1L, accountIdValue(), descriptionValue(), gossipEndpointsValue(), serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue()));
    }

    @Test
    @DisplayName("description: accepts more elements than the maximum")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/description/accepts-at-maximum-bytes")
    void shouldAcceptDescriptionAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), value, gossipEndpointsValue(), serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        assertValue(value, subject.description());
    }

    @Test
    @DisplayName("description: rejects more elements than the maximum")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/description/rejects-above-maximum-bytes")
    void shouldRejectDescriptionAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", gossipEndpointsValue(), serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue()));
    }

    @Test
    @DisplayName("rejects null for gossipEndpoints")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/gossipEndpoints/rejects-null")
    void shouldRejectNullGossipEndpoints() {
        assertThrows(NullPointerException.class, () -> new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), (List<ServiceEndpoint>) null, serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue()));
    }

    @Test
    @DisplayName("gossipEndpoints: accepts the maximum number of elements")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/gossipEndpoints/accepts-at-maximum-size")
    void shouldAcceptGossipEndpointsAtMaximumSize() {
        final List<ServiceEndpoint> value = List.of(new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211));
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), value, serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        assertValue(value, subject.gossipEndpoints());
    }

    @Test
    @DisplayName("gossipEndpoints: rejects more elements than the maximum")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/gossipEndpoints/rejects-above-maximum-size")
    void shouldRejectGossipEndpointsAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), List.of(new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211)), serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue()));
    }

    @Test
    @DisplayName("the constructor copies gossipEndpoints; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/gossipEndpoints/copies-in")
    void shouldCopyGossipEndpointsIn() {
        final List<ServiceEndpoint> value = new ArrayList<>(gossipEndpointsValue());
        final List<ServiceEndpoint> expected = List.copyOf(value);
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), value, serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        value.clear();
        assertValue(expected, subject.gossipEndpoints());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.gossipEndpoints()).clear());
    }

    @Test
    @DisplayName("rejects null for serviceEndpoints")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/serviceEndpoints/rejects-null")
    void shouldRejectNullServiceEndpoints() {
        assertThrows(NullPointerException.class, () -> new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), gossipEndpointsValue(), (List<ServiceEndpoint>) null, gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue()));
    }

    @Test
    @DisplayName("serviceEndpoints: accepts the maximum number of elements")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/serviceEndpoints/accepts-at-maximum-size")
    void shouldAcceptServiceEndpointsAtMaximumSize() {
        final List<ServiceEndpoint> value = List.of(new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211));
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), gossipEndpointsValue(), value, gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        assertValue(value, subject.serviceEndpoints());
    }

    @Test
    @DisplayName("serviceEndpoints: rejects more elements than the maximum")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/serviceEndpoints/rejects-above-maximum-size")
    void shouldRejectServiceEndpointsAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), gossipEndpointsValue(), List.of(new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211), new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211)), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue()));
    }

    @Test
    @DisplayName("the constructor copies serviceEndpoints; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/serviceEndpoints/copies-in")
    void shouldCopyServiceEndpointsIn() {
        final List<ServiceEndpoint> value = new ArrayList<>(serviceEndpointsValue());
        final List<ServiceEndpoint> expected = List.copyOf(value);
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), gossipEndpointsValue(), value, gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        value.clear();
        assertValue(expected, subject.serviceEndpoints());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.serviceEndpoints()).clear());
    }

    @Test
    @DisplayName("the constructor copies gossipCaCertificate; the accessor returns a copy")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/gossipCaCertificate/copies-in")
    void shouldCopyGossipCaCertificateIn() {
        final byte[] value = gossipCaCertificateValue();
        final byte[] expected = value.clone();
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), gossipEndpointsValue(), serviceEndpointsValue(), value, grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.gossipCaCertificate());
        final byte[] returned = subject.gossipCaCertificate();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.gossipCaCertificate());
    }

    @Test
    @DisplayName("the constructor copies grpcCertificateHash; the accessor returns a copy")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/grpcCertificateHash/copies-in")
    void shouldCopyGrpcCertificateHashIn() {
        final byte[] value = grpcCertificateHashValue();
        final byte[] expected = value.clone();
        final NodeUpdateTransaction subject = new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), gossipEndpointsValue(), serviceEndpointsValue(), gossipCaCertificateValue(), value, adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.grpcCertificateHash());
        final byte[] returned = subject.grpcCertificateHash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.grpcCertificateHash());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final NodeUpdateTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final NodeUpdateTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final NodeUpdateTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final NodeUpdateTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/memo/set")
    void shouldSetMemo() {
        final NodeUpdateTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final NodeUpdateTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final NodeUpdateTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final NodeUpdateTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.admin.nodes.NodeUpdateTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
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

    private static List<ServiceEndpoint> gossipEndpointsValue() {
        return List.of(new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211));
    }

    private static List<ServiceEndpoint> serviceEndpointsValue() {
        return List.of(new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211));
    }

    private static byte[] gossipCaCertificateValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] grpcCertificateHashValue() {
        return new byte[] {1, 2, 3};
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static boolean declineRewardValue() {
        return true;
    }

    private static ServiceEndpoint grpcWebProxyEndpointValue() {
        return new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211);
    }

    private static NodeUpdateTransaction create() {
        return new NodeUpdateTransaction(nodeIdValue(), accountIdValue(), descriptionValue(), gossipEndpointsValue(), serviceEndpointsValue(), gossipCaCertificateValue(), grpcCertificateHashValue(), adminAuthorityValue(), declineRewardValue(), grpcWebProxyEndpointValue());
    }

    private static NativeToken<?, ?> maxTransactionFeeOtherValue() {
        return new Hbar(2L, HbarUnit.TINYBAR);
    }

    private static Duration validDurationOtherValue() {
        return Duration.ofSeconds(2L);
    }

    private static String memoOtherValue() {
        return "value1";
    }

    private static Account payerValue() {
        return new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static List<AccountId> nodesValue() {
        return List.of(new AccountId(0L, 0L, "", 1001L, null, null));
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }

    private static AccountId payerIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionSigner signerValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"))).transactionSigner();
    }

    private static Authority batchKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }
}
