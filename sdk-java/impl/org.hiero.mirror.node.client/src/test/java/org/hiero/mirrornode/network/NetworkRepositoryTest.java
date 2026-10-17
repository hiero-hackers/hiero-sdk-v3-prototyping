package org.hiero.mirrornode.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NetworkRepository`, generated from the specs.
@DisplayName("NetworkRepository")
class NetworkRepositoryTest {

    @Test
    @DisplayName("creates a NetworkRepository and returns the values")
    @Tag("testId:mirrornode.network.NetworkRepository/create")
    void shouldCreateAndReturnTheValues() {
        final NetworkRepository subject = new NetworkRepository();
    }

    @Test
    @DisplayName("exchangeRates() can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/exchangeRates/call")
    void shouldCallExchangeRates() {
        assertNotNull(create().exchangeRates());
    }

    @Test
    @DisplayName("fees() can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/fees/call")
    void shouldCallFees() {
        assertNotNull(create().fees());
    }

    @Test
    @DisplayName("stake() can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/stake/call")
    void shouldCallStake() {
        assertNotNull(create().stake());
    }

    @Test
    @DisplayName("supplies() can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/supplies/call")
    void shouldCallSupplies() {
        assertNotNull(create().supplies());
    }

    @Test
    @DisplayName("nodes() can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/nodes/call")
    void shouldCallNodes() {
        assertNotNull(create().nodes());
    }

    @Test
    @DisplayName("estimateFees(transaction) can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/estimateFees/call")
    void shouldCallEstimateFees() {
        assertNotNull(create().estimateFees(transactionValue()));
    }

    private static NetworkRepository create() {
        return new NetworkRepository();
    }

    private static byte[] transactionValue() {
        return new byte[] {1, 2, 3};
    }
}
