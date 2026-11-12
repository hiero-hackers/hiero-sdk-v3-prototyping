package org.hiero.consensusnode.clpr.queries;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.consensusnode.clpr.ClprThrottles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClprLedgerConfiguration`, generated from the specs.
@DisplayName("ClprLedgerConfiguration")
class ClprLedgerConfigurationTest {

    @Test
    @DisplayName("creates a ClprLedgerConfiguration and returns the values")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final long protocolVersion = protocolVersionValue();
        final String chainId = chainIdValue();
        final byte[] serviceAddress = serviceAddressValue();
        final ZonedDateTime timestamp = timestampValue();
        final ClprThrottles throttles = throttlesValue();
        final byte[] initialTrustAnchor = initialTrustAnchorValue();
        final byte[] initialTrustAnchorId = initialTrustAnchorIdValue();
        final byte[] configurationStateProof = configurationStateProofValue();
        final ClprLedgerConfiguration subject = new ClprLedgerConfiguration(protocolVersion, chainId, serviceAddress, timestamp, throttles, initialTrustAnchor, initialTrustAnchorId, configurationStateProof);
        assertValue(protocolVersion, subject.protocolVersion());
        assertValue(chainId, subject.chainId());
        assertValue(serviceAddress, subject.serviceAddress());
        assertValue(timestamp, subject.timestamp());
        assertValue(throttles, subject.throttles());
        assertValue(initialTrustAnchor, subject.initialTrustAnchor());
        assertValue(initialTrustAnchorId, subject.initialTrustAnchorId());
        assertValue(configurationStateProof, subject.configurationStateProof());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/builder/builds")
    @Tag("hip:1535")
    void shouldBuildTheValuesOfTheConstructor() {
        final long protocolVersion = protocolVersionValue();
        final String chainId = chainIdValue();
        final byte[] serviceAddress = serviceAddressValue();
        final ZonedDateTime timestamp = timestampValue();
        final ClprThrottles throttles = throttlesValue();
        final byte[] initialTrustAnchor = initialTrustAnchorValue();
        final byte[] initialTrustAnchorId = initialTrustAnchorIdValue();
        final byte[] configurationStateProof = configurationStateProofValue();
        final ClprLedgerConfiguration subject = ClprLedgerConfiguration.builder()
                .protocolVersion(protocolVersion)
                .chainId(chainId)
                .serviceAddress(serviceAddress)
                .timestamp(timestamp)
                .throttles(throttles)
                .initialTrustAnchor(initialTrustAnchor)
                .initialTrustAnchorId(initialTrustAnchorId)
                .configurationStateProof(configurationStateProof)
                .build();
        assertValue(protocolVersion, subject.protocolVersion());
        assertValue(chainId, subject.chainId());
        assertValue(serviceAddress, subject.serviceAddress());
        assertValue(timestamp, subject.timestamp());
        assertValue(throttles, subject.throttles());
        assertValue(initialTrustAnchor, subject.initialTrustAnchor());
        assertValue(initialTrustAnchorId, subject.initialTrustAnchorId());
        assertValue(configurationStateProof, subject.configurationStateProof());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/builder/optional-values")
    @Tag("hip:1535")
    void shouldBuildWithoutTheOptionalValues() {
        final ClprLedgerConfiguration subject = ClprLedgerConfiguration.builder()
                .protocolVersion(protocolVersionValue())
                .chainId(chainIdValue())
                .serviceAddress(serviceAddressValue())
                .timestamp(timestampValue())
                .throttles(throttlesValue())
                .initialTrustAnchor(initialTrustAnchorValue())
                .initialTrustAnchorId(initialTrustAnchorIdValue())
                .configurationStateProof(configurationStateProofValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/builder/rejects-missing-value")
    @Tag("hip:1535")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ClprLedgerConfiguration.builder().build());
    }

    @Test
    @DisplayName("protocolVersion: accepts the minimum (0)")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/protocolVersion/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptProtocolVersionAtMinimum() {
        final long value = 0L;
        final ClprLedgerConfiguration subject = new ClprLedgerConfiguration(value, chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue());
        assertValue(value, subject.protocolVersion());
    }

    @Test
    @DisplayName("protocolVersion: accepts the maximum (4294967295)")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/protocolVersion/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptProtocolVersionAtMaximum() {
        final long value = 4294967295L;
        final ClprLedgerConfiguration subject = new ClprLedgerConfiguration(value, chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue());
        assertValue(value, subject.protocolVersion());
    }

    @Test
    @DisplayName("protocolVersion: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/protocolVersion/rejects-below-minimum")
    @Tag("hip:1535")
    void shouldRejectProtocolVersionBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprLedgerConfiguration(-1L, chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue()));
    }

    @Test
    @DisplayName("protocolVersion: rejects a value above the maximum (4294967296)")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/protocolVersion/rejects-above-maximum")
    @Tag("hip:1535")
    void shouldRejectProtocolVersionAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprLedgerConfiguration(4294967296L, chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue()));
    }

    @Test
    @DisplayName("rejects null for chainId")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/chainId/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullChainId() {
        assertThrows(NullPointerException.class, () -> new ClprLedgerConfiguration(protocolVersionValue(), (String) null, serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue()));
    }

    @Test
    @DisplayName("rejects null for serviceAddress")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/serviceAddress/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullServiceAddress() {
        assertThrows(NullPointerException.class, () -> new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), (byte[]) null, timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue()));
    }

    @Test
    @DisplayName("the constructor copies serviceAddress; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/serviceAddress/copies-in")
    @Tag("hip:1535")
    void shouldCopyServiceAddressIn() {
        final byte[] value = serviceAddressValue();
        final byte[] expected = value.clone();
        final ClprLedgerConfiguration subject = new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), value, timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.serviceAddress());
        final byte[] returned = subject.serviceAddress();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.serviceAddress());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/timestamp/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), serviceAddressValue(), (ZonedDateTime) null, throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue()));
    }

    @Test
    @DisplayName("rejects null for throttles")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/throttles/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullThrottles() {
        assertThrows(NullPointerException.class, () -> new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), serviceAddressValue(), timestampValue(), (ClprThrottles) null, initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue()));
    }

    @Test
    @DisplayName("rejects null for initialTrustAnchor")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/initialTrustAnchor/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullInitialTrustAnchor() {
        assertThrows(NullPointerException.class, () -> new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), (byte[]) null, initialTrustAnchorIdValue(), configurationStateProofValue()));
    }

    @Test
    @DisplayName("the constructor copies initialTrustAnchor; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/initialTrustAnchor/copies-in")
    @Tag("hip:1535")
    void shouldCopyInitialTrustAnchorIn() {
        final byte[] value = initialTrustAnchorValue();
        final byte[] expected = value.clone();
        final ClprLedgerConfiguration subject = new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), value, initialTrustAnchorIdValue(), configurationStateProofValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.initialTrustAnchor());
        final byte[] returned = subject.initialTrustAnchor();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.initialTrustAnchor());
    }

    @Test
    @DisplayName("rejects null for initialTrustAnchorId")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/initialTrustAnchorId/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullInitialTrustAnchorId() {
        assertThrows(NullPointerException.class, () -> new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), (byte[]) null, configurationStateProofValue()));
    }

    @Test
    @DisplayName("the constructor copies initialTrustAnchorId; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/initialTrustAnchorId/copies-in")
    @Tag("hip:1535")
    void shouldCopyInitialTrustAnchorIdIn() {
        final byte[] value = initialTrustAnchorIdValue();
        final byte[] expected = value.clone();
        final ClprLedgerConfiguration subject = new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), value, configurationStateProofValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.initialTrustAnchorId());
        final byte[] returned = subject.initialTrustAnchorId();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.initialTrustAnchorId());
    }

    @Test
    @DisplayName("rejects null for configurationStateProof")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/configurationStateProof/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullConfigurationStateProof() {
        assertThrows(NullPointerException.class, () -> new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies configurationStateProof; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/configurationStateProof/copies-in")
    @Tag("hip:1535")
    void shouldCopyConfigurationStateProofIn() {
        final byte[] value = configurationStateProofValue();
        final byte[] expected = value.clone();
        final ClprLedgerConfiguration subject = new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.configurationStateProof());
        final byte[] returned = subject.configurationStateProof();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.configurationStateProof());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.clpr.queries.ClprLedgerConfiguration/value-semantics")
    @Tag("hip:1535")
    void shouldCompareByValue() {
        final ClprLedgerConfiguration subject = create();
        final ClprLedgerConfiguration other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long protocolVersionValue() {
        return 1L;
    }

    private static String chainIdValue() {
        return "value";
    }

    private static byte[] serviceAddressValue() {
        return new byte[] {1, 2, 3};
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ClprThrottles throttlesValue() {
        return new ClprThrottles(10L, 6144L, 1000000L, 100L, 71680L, 0L, 0L);
    }

    private static byte[] initialTrustAnchorValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] initialTrustAnchorIdValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] configurationStateProofValue() {
        return new byte[] {1, 2, 3};
    }

    private static ClprLedgerConfiguration create() {
        return new ClprLedgerConfiguration(protocolVersionValue(), chainIdValue(), serviceAddressValue(), timestampValue(), throttlesValue(), initialTrustAnchorValue(), initialTrustAnchorIdValue(), configurationStateProofValue());
    }

    private static ClprLedgerConfiguration defaultSubject() {
        return new ClprLedgerConfiguration(1L, "hedera:testnet", new byte[] {(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 110}, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), new ClprThrottles(10L, 6144L, 1000000L, 100L, 71680L, 0L, 0L), new byte[] {(byte) 1, (byte) 2, (byte) 3}, new byte[] {(byte) 4, (byte) 5, (byte) 6}, new byte[0]);
    }
}
