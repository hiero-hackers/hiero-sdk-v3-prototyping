package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NetworkSupplies`, generated from the specs.
@DisplayName("NetworkSupplies")
class NetworkSuppliesTest {

    @Test
    @DisplayName("creates a NetworkSupplies and returns the values")
    @Tag("testId:mirrornode.network.NetworkSupplies/create")
    void shouldCreateAndReturnTheValues() {
        final BigInteger releasedSupply = releasedSupplyValue();
        final BigInteger totalSupply = totalSupplyValue();
        final @Nullable ZonedDateTime timestamp = timestampValue();
        final NetworkSupplies subject = new NetworkSupplies(releasedSupply, totalSupply, timestamp);
        assertValue(releasedSupply, subject.releasedSupply());
        assertValue(totalSupply, subject.totalSupply());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("rejects null for releasedSupply")
    @Tag("testId:mirrornode.network.NetworkSupplies/releasedSupply/rejects-null")
    void shouldRejectNullReleasedSupply() {
        assertThrows(NullPointerException.class, () -> new NetworkSupplies((BigInteger) null, totalSupplyValue(), timestampValue()));
    }

    @Test
    @DisplayName("releasedSupply: accepts the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.network.NetworkSupplies/releasedSupply/accepts-at-minimum")
    void shouldAcceptReleasedSupplyAtMinimum() {
        final BigInteger value = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
        final NetworkSupplies subject = new NetworkSupplies(value, totalSupplyValue(), timestampValue());
        assertValue(value, subject.releasedSupply());
    }

    @Test
    @DisplayName("releasedSupply: accepts the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819967)")
    @Tag("testId:mirrornode.network.NetworkSupplies/releasedSupply/accepts-at-maximum")
    void shouldAcceptReleasedSupplyAtMaximum() {
        final BigInteger value = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
        final NetworkSupplies subject = new NetworkSupplies(value, totalSupplyValue(), timestampValue());
        assertValue(value, subject.releasedSupply());
    }

    @Test
    @DisplayName("releasedSupply: rejects a value below the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819969)")
    @Tag("testId:mirrornode.network.NetworkSupplies/releasedSupply/rejects-below-minimum")
    void shouldRejectReleasedSupplyBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkSupplies(new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819969"), totalSupplyValue(), timestampValue()));
    }

    @Test
    @DisplayName("releasedSupply: rejects a value above the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.network.NetworkSupplies/releasedSupply/rejects-above-maximum")
    void shouldRejectReleasedSupplyAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkSupplies(new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819968"), totalSupplyValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for totalSupply")
    @Tag("testId:mirrornode.network.NetworkSupplies/totalSupply/rejects-null")
    void shouldRejectNullTotalSupply() {
        assertThrows(NullPointerException.class, () -> new NetworkSupplies(releasedSupplyValue(), (BigInteger) null, timestampValue()));
    }

    @Test
    @DisplayName("totalSupply: accepts the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.network.NetworkSupplies/totalSupply/accepts-at-minimum")
    void shouldAcceptTotalSupplyAtMinimum() {
        final BigInteger value = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
        final NetworkSupplies subject = new NetworkSupplies(releasedSupplyValue(), value, timestampValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("totalSupply: accepts the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819967)")
    @Tag("testId:mirrornode.network.NetworkSupplies/totalSupply/accepts-at-maximum")
    void shouldAcceptTotalSupplyAtMaximum() {
        final BigInteger value = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
        final NetworkSupplies subject = new NetworkSupplies(releasedSupplyValue(), value, timestampValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("totalSupply: rejects a value below the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819969)")
    @Tag("testId:mirrornode.network.NetworkSupplies/totalSupply/rejects-below-minimum")
    void shouldRejectTotalSupplyBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkSupplies(releasedSupplyValue(), new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819969"), timestampValue()));
    }

    @Test
    @DisplayName("totalSupply: rejects a value above the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.network.NetworkSupplies/totalSupply/rejects-above-maximum")
    void shouldRejectTotalSupplyAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkSupplies(releasedSupplyValue(), new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819968"), timestampValue()));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.NetworkSupplies/value-semantics")
    void shouldCompareByValue() {
        final NetworkSupplies subject = create();
        final NetworkSupplies other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static BigInteger releasedSupplyValue() {
        return new BigInteger("1");
    }

    private static BigInteger totalSupplyValue() {
        return new BigInteger("1");
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static NetworkSupplies create() {
        return new NetworkSupplies(releasedSupplyValue(), totalSupplyValue(), timestampValue());
    }
}
