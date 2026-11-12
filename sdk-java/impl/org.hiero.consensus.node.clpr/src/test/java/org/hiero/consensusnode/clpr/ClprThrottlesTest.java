package org.hiero.consensusnode.clpr;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClprThrottles`, generated from the specs.
@DisplayName("ClprThrottles")
class ClprThrottlesTest {

    @Test
    @DisplayName("creates a ClprThrottles and returns the values")
    @Tag("testId:consensusnode.clpr.ClprThrottles/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final long maxMessagesPerBundle = maxMessagesPerBundleValue();
        final long maxMessagePayloadBytes = maxMessagePayloadBytesValue();
        final long maxGasPerMessage = maxGasPerMessageValue();
        final long maxQueueDepth = maxQueueDepthValue();
        final long maxSyncBytes = maxSyncBytesValue();
        final long maxLocalEndpoints = maxLocalEndpointsValue();
        final long maxPeerEndpoints = maxPeerEndpointsValue();
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundle, maxMessagePayloadBytes, maxGasPerMessage, maxQueueDepth, maxSyncBytes, maxLocalEndpoints, maxPeerEndpoints);
        assertValue(maxMessagesPerBundle, subject.maxMessagesPerBundle());
        assertValue(maxMessagePayloadBytes, subject.maxMessagePayloadBytes());
        assertValue(maxGasPerMessage, subject.maxGasPerMessage());
        assertValue(maxQueueDepth, subject.maxQueueDepth());
        assertValue(maxSyncBytes, subject.maxSyncBytes());
        assertValue(maxLocalEndpoints, subject.maxLocalEndpoints());
        assertValue(maxPeerEndpoints, subject.maxPeerEndpoints());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.clpr.ClprThrottles/builder/builds")
    @Tag("hip:1535")
    void shouldBuildTheValuesOfTheConstructor() {
        final long maxMessagesPerBundle = maxMessagesPerBundleValue();
        final long maxMessagePayloadBytes = maxMessagePayloadBytesValue();
        final long maxGasPerMessage = maxGasPerMessageValue();
        final long maxQueueDepth = maxQueueDepthValue();
        final long maxSyncBytes = maxSyncBytesValue();
        final long maxLocalEndpoints = maxLocalEndpointsValue();
        final long maxPeerEndpoints = maxPeerEndpointsValue();
        final ClprThrottles subject = ClprThrottles.builder()
                .maxMessagesPerBundle(maxMessagesPerBundle)
                .maxMessagePayloadBytes(maxMessagePayloadBytes)
                .maxGasPerMessage(maxGasPerMessage)
                .maxQueueDepth(maxQueueDepth)
                .maxSyncBytes(maxSyncBytes)
                .maxLocalEndpoints(maxLocalEndpoints)
                .maxPeerEndpoints(maxPeerEndpoints)
                .build();
        assertValue(maxMessagesPerBundle, subject.maxMessagesPerBundle());
        assertValue(maxMessagePayloadBytes, subject.maxMessagePayloadBytes());
        assertValue(maxGasPerMessage, subject.maxGasPerMessage());
        assertValue(maxQueueDepth, subject.maxQueueDepth());
        assertValue(maxSyncBytes, subject.maxSyncBytes());
        assertValue(maxLocalEndpoints, subject.maxLocalEndpoints());
        assertValue(maxPeerEndpoints, subject.maxPeerEndpoints());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.clpr.ClprThrottles/builder/optional-values")
    @Tag("hip:1535")
    void shouldBuildWithoutTheOptionalValues() {
        final ClprThrottles subject = ClprThrottles.builder()
                .maxMessagesPerBundle(maxMessagesPerBundleValue())
                .maxMessagePayloadBytes(maxMessagePayloadBytesValue())
                .maxGasPerMessage(maxGasPerMessageValue())
                .maxQueueDepth(maxQueueDepthValue())
                .maxSyncBytes(maxSyncBytesValue())
                .maxLocalEndpoints(maxLocalEndpointsValue())
                .maxPeerEndpoints(maxPeerEndpointsValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.clpr.ClprThrottles/builder/rejects-missing-value")
    @Tag("hip:1535")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ClprThrottles.builder().build());
    }

    @Test
    @DisplayName("maxMessagesPerBundle: accepts the minimum (1)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxMessagesPerBundle/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptMaxMessagesPerBundleAtMinimum() {
        final long value = 1L;
        final ClprThrottles subject = new ClprThrottles(value, maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxMessagesPerBundle());
    }

    @Test
    @DisplayName("maxMessagesPerBundle: accepts the maximum (4294967295)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxMessagesPerBundle/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptMaxMessagesPerBundleAtMaximum() {
        final long value = 4294967295L;
        final ClprThrottles subject = new ClprThrottles(value, maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxMessagesPerBundle());
    }

    @Test
    @DisplayName("maxMessagesPerBundle: rejects a value below the minimum (0)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxMessagesPerBundle/rejects-below-minimum")
    @Tag("hip:1535")
    void shouldRejectMaxMessagesPerBundleBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(0L, maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxMessagesPerBundle: rejects a value above the maximum (4294967296)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxMessagesPerBundle/rejects-above-maximum")
    @Tag("hip:1535")
    void shouldRejectMaxMessagesPerBundleAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(4294967296L, maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxMessagePayloadBytes: accepts the minimum (1)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxMessagePayloadBytes/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptMaxMessagePayloadBytesAtMinimum() {
        final long value = 1L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), value, maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxMessagePayloadBytes());
    }

    @Test
    @DisplayName("maxMessagePayloadBytes: accepts the maximum (4294967295)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxMessagePayloadBytes/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptMaxMessagePayloadBytesAtMaximum() {
        final long value = 4294967295L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), value, maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxMessagePayloadBytes());
    }

    @Test
    @DisplayName("maxMessagePayloadBytes: rejects a value below the minimum (0)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxMessagePayloadBytes/rejects-below-minimum")
    @Tag("hip:1535")
    void shouldRejectMaxMessagePayloadBytesBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), 0L, maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxMessagePayloadBytes: rejects a value above the maximum (4294967296)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxMessagePayloadBytes/rejects-above-maximum")
    @Tag("hip:1535")
    void shouldRejectMaxMessagePayloadBytesAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), 4294967296L, maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxGasPerMessage: accepts the minimum (1)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxGasPerMessage/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptMaxGasPerMessageAtMinimum() {
        final long value = 1L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), value, maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxGasPerMessage());
    }

    @Test
    @DisplayName("maxGasPerMessage: accepts the maximum (18446744073709551615)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxGasPerMessage/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptMaxGasPerMessageAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), value, maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxGasPerMessage());
    }

    @Test
    @DisplayName("maxGasPerMessage: rejects a value below the minimum (0)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxGasPerMessage/rejects-below-minimum")
    @Tag("hip:1535")
    void shouldRejectMaxGasPerMessageBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), 0L, maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxQueueDepth: accepts the minimum (1)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxQueueDepth/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptMaxQueueDepthAtMinimum() {
        final long value = 1L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), value, maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxQueueDepth());
    }

    @Test
    @DisplayName("maxQueueDepth: accepts the maximum (4294967295)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxQueueDepth/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptMaxQueueDepthAtMaximum() {
        final long value = 4294967295L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), value, maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxQueueDepth());
    }

    @Test
    @DisplayName("maxQueueDepth: rejects a value below the minimum (0)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxQueueDepth/rejects-below-minimum")
    @Tag("hip:1535")
    void shouldRejectMaxQueueDepthBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), 0L, maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxQueueDepth: rejects a value above the maximum (4294967296)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxQueueDepth/rejects-above-maximum")
    @Tag("hip:1535")
    void shouldRejectMaxQueueDepthAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), 4294967296L, maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxSyncBytes: accepts the minimum (1)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxSyncBytes/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptMaxSyncBytesAtMinimum() {
        final long value = 1L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), value, maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxSyncBytes());
    }

    @Test
    @DisplayName("maxSyncBytes: accepts the maximum (18446744073709551615)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxSyncBytes/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptMaxSyncBytesAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), value, maxLocalEndpointsValue(), maxPeerEndpointsValue());
        assertValue(value, subject.maxSyncBytes());
    }

    @Test
    @DisplayName("maxSyncBytes: rejects a value below the minimum (0)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxSyncBytes/rejects-below-minimum")
    @Tag("hip:1535")
    void shouldRejectMaxSyncBytesBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), 0L, maxLocalEndpointsValue(), maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxLocalEndpoints: accepts the minimum (0)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxLocalEndpoints/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptMaxLocalEndpointsAtMinimum() {
        final long value = 0L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), value, maxPeerEndpointsValue());
        assertValue(value, subject.maxLocalEndpoints());
    }

    @Test
    @DisplayName("maxLocalEndpoints: accepts the maximum (4294967295)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxLocalEndpoints/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptMaxLocalEndpointsAtMaximum() {
        final long value = 4294967295L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), value, maxPeerEndpointsValue());
        assertValue(value, subject.maxLocalEndpoints());
    }

    @Test
    @DisplayName("maxLocalEndpoints: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxLocalEndpoints/rejects-below-minimum")
    @Tag("hip:1535")
    void shouldRejectMaxLocalEndpointsBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), -1L, maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxLocalEndpoints: rejects a value above the maximum (4294967296)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxLocalEndpoints/rejects-above-maximum")
    @Tag("hip:1535")
    void shouldRejectMaxLocalEndpointsAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), 4294967296L, maxPeerEndpointsValue()));
    }

    @Test
    @DisplayName("maxPeerEndpoints: accepts the minimum (0)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxPeerEndpoints/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptMaxPeerEndpointsAtMinimum() {
        final long value = 0L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), value);
        assertValue(value, subject.maxPeerEndpoints());
    }

    @Test
    @DisplayName("maxPeerEndpoints: accepts the maximum (4294967295)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxPeerEndpoints/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptMaxPeerEndpointsAtMaximum() {
        final long value = 4294967295L;
        final ClprThrottles subject = new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), value);
        assertValue(value, subject.maxPeerEndpoints());
    }

    @Test
    @DisplayName("maxPeerEndpoints: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxPeerEndpoints/rejects-below-minimum")
    @Tag("hip:1535")
    void shouldRejectMaxPeerEndpointsBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), -1L));
    }

    @Test
    @DisplayName("maxPeerEndpoints: rejects a value above the maximum (4294967296)")
    @Tag("testId:consensusnode.clpr.ClprThrottles/maxPeerEndpoints/rejects-above-maximum")
    @Tag("hip:1535")
    void shouldRejectMaxPeerEndpointsAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), 4294967296L));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.clpr.ClprThrottles/value-semantics")
    @Tag("hip:1535")
    void shouldCompareByValue() {
        final ClprThrottles subject = create();
        final ClprThrottles other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long maxMessagesPerBundleValue() {
        return 1L;
    }

    private static long maxMessagePayloadBytesValue() {
        return 1L;
    }

    private static long maxGasPerMessageValue() {
        return 1L;
    }

    private static long maxQueueDepthValue() {
        return 1L;
    }

    private static long maxSyncBytesValue() {
        return 1L;
    }

    private static long maxLocalEndpointsValue() {
        return 1L;
    }

    private static long maxPeerEndpointsValue() {
        return 1L;
    }

    private static ClprThrottles create() {
        return new ClprThrottles(maxMessagesPerBundleValue(), maxMessagePayloadBytesValue(), maxGasPerMessageValue(), maxQueueDepthValue(), maxSyncBytesValue(), maxLocalEndpointsValue(), maxPeerEndpointsValue());
    }

    private static ClprThrottles defaultSubject() {
        return new ClprThrottles(10L, 6144L, 1000000L, 100L, 71680L, 0L, 0L);
    }
}
