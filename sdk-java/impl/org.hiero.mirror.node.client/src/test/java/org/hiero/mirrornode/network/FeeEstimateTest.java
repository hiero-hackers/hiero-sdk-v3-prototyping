package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FeeEstimate`, generated from the specs.
@DisplayName("FeeEstimate")
class FeeEstimateTest {

    @Test
    @DisplayName("creates a FeeEstimate and returns the values")
    @Tag("testId:mirrornode.network.FeeEstimate/create")
    void shouldCreateAndReturnTheValues() {
        final long highVolumeMultiplier = highVolumeMultiplierValue();
        final FeeEstimateNetwork network = networkValue();
        final FeeEstimateComponent node = nodeValue();
        final FeeEstimateComponent service = serviceValue();
        final long total = totalValue();
        final FeeEstimate subject = new FeeEstimate(highVolumeMultiplier, network, node, service, total);
        assertValue(highVolumeMultiplier, subject.highVolumeMultiplier());
        assertValue(network, subject.network());
        assertValue(node, subject.node());
        assertValue(service, subject.service());
        assertValue(total, subject.total());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.network.FeeEstimate/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long highVolumeMultiplier = highVolumeMultiplierValue();
        final FeeEstimateNetwork network = networkValue();
        final FeeEstimateComponent node = nodeValue();
        final FeeEstimateComponent service = serviceValue();
        final long total = totalValue();
        final FeeEstimate subject = FeeEstimate.builder()
                .highVolumeMultiplier(highVolumeMultiplier)
                .network(network)
                .node(node)
                .service(service)
                .total(total)
                .build();
        assertValue(highVolumeMultiplier, subject.highVolumeMultiplier());
        assertValue(network, subject.network());
        assertValue(node, subject.node());
        assertValue(service, subject.service());
        assertValue(total, subject.total());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.network.FeeEstimate/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final FeeEstimate subject = FeeEstimate.builder()
                .highVolumeMultiplier(highVolumeMultiplierValue())
                .network(networkValue())
                .node(nodeValue())
                .service(serviceValue())
                .total(totalValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.network.FeeEstimate/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> FeeEstimate.builder().build());
    }

    @Test
    @DisplayName("highVolumeMultiplier: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.FeeEstimate/highVolumeMultiplier/accepts-at-minimum")
    void shouldAcceptHighVolumeMultiplierAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeEstimate subject = new FeeEstimate(value, networkValue(), nodeValue(), serviceValue(), totalValue());
        assertValue(value, subject.highVolumeMultiplier());
    }

    @Test
    @DisplayName("highVolumeMultiplier: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.FeeEstimate/highVolumeMultiplier/accepts-at-maximum")
    void shouldAcceptHighVolumeMultiplierAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeEstimate subject = new FeeEstimate(value, networkValue(), nodeValue(), serviceValue(), totalValue());
        assertValue(value, subject.highVolumeMultiplier());
    }

    @Test
    @DisplayName("rejects null for network")
    @Tag("testId:mirrornode.network.FeeEstimate/network/rejects-null")
    void shouldRejectNullNetwork() {
        assertThrows(NullPointerException.class, () -> new FeeEstimate(highVolumeMultiplierValue(), (FeeEstimateNetwork) null, nodeValue(), serviceValue(), totalValue()));
    }

    @Test
    @DisplayName("rejects null for node")
    @Tag("testId:mirrornode.network.FeeEstimate/node/rejects-null")
    void shouldRejectNullNode() {
        assertThrows(NullPointerException.class, () -> new FeeEstimate(highVolumeMultiplierValue(), networkValue(), (FeeEstimateComponent) null, serviceValue(), totalValue()));
    }

    @Test
    @DisplayName("rejects null for service")
    @Tag("testId:mirrornode.network.FeeEstimate/service/rejects-null")
    void shouldRejectNullService() {
        assertThrows(NullPointerException.class, () -> new FeeEstimate(highVolumeMultiplierValue(), networkValue(), nodeValue(), (FeeEstimateComponent) null, totalValue()));
    }

    @Test
    @DisplayName("total: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.FeeEstimate/total/accepts-at-minimum")
    void shouldAcceptTotalAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeEstimate subject = new FeeEstimate(highVolumeMultiplierValue(), networkValue(), nodeValue(), serviceValue(), value);
        assertValue(value, subject.total());
    }

    @Test
    @DisplayName("total: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.FeeEstimate/total/accepts-at-maximum")
    void shouldAcceptTotalAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeEstimate subject = new FeeEstimate(highVolumeMultiplierValue(), networkValue(), nodeValue(), serviceValue(), value);
        assertValue(value, subject.total());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.FeeEstimate/value-semantics")
    void shouldCompareByValue() {
        final FeeEstimate subject = create();
        final FeeEstimate other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long highVolumeMultiplierValue() {
        return 1L;
    }

    private static FeeEstimateNetwork networkValue() {
        return new FeeEstimateNetwork(1, 1L);
    }

    private static FeeEstimateComponent nodeValue() {
        return new FeeEstimateComponent(1L, List.of(new FeeExtra(1L, 1L, 1L, 1L, "value", 1L)));
    }

    private static FeeEstimateComponent serviceValue() {
        return new FeeEstimateComponent(1L, List.of(new FeeExtra(1L, 1L, 1L, 1L, "value", 1L)));
    }

    private static long totalValue() {
        return 1L;
    }

    private static FeeEstimate create() {
        return new FeeEstimate(highVolumeMultiplierValue(), networkValue(), nodeValue(), serviceValue(), totalValue());
    }
}
