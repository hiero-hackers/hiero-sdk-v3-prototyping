package org.hiero.mirrornode.network;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.hiero.mirrornode.common.Order;
import org.jspecify.annotations.Nullable;
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
    @DisplayName("exchangeRates(timestamp) can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/exchangeRates/call-2")
    void shouldCallExchangeRates2() {
        assertNotNull(create().exchangeRates(timestampValue()));
    }

    @Test
    @DisplayName("fees() can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/fees/call")
    void shouldCallFees() {
        assertNotNull(create().fees());
    }

    @Test
    @DisplayName("fees(order, timestamp) can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/fees/call-2")
    void shouldCallFees2() {
        assertNotNull(create().fees(orderValue(), timestampValue()));
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
    @DisplayName("supplies(timestamp) can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/supplies/call-2")
    void shouldCallSupplies2() {
        assertNotNull(create().supplies(timestampValue()));
    }

    @Test
    @DisplayName("nodes() can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/nodes/call")
    void shouldCallNodes() {
        assertNotNull(create().nodes());
    }

    @Test
    @DisplayName("nodes(fileId, limit, nodeId, order) can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/nodes/call-2")
    void shouldCallNodes2() {
        assertNotNull(create().nodes(fileIdValue(), limitValue(), nodeIdValue(), orderValue()));
    }

    @Test
    @DisplayName("registeredNodes() can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/registeredNodes/call")
    void shouldCallRegisteredNodes() {
        assertNotNull(create().registeredNodes());
    }

    @Test
    @DisplayName("registeredNodes(limit, order, registeredNodeId, type) can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/registeredNodes/call-2")
    void shouldCallRegisteredNodes2() {
        assertNotNull(create().registeredNodes(limitValue(), orderValue(), registeredNodeIdValue(), typeValue()));
    }

    @Test
    @DisplayName("estimateFees(transaction) can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/estimateFees/call")
    void shouldCallEstimateFees() {
        assertNotNull(create().estimateFees(transactionValue()));
    }

    @Test
    @DisplayName("estimateFees(transaction, mode, highVolumeThrottle) can be called and returns a value")
    @Tag("testId:mirrornode.network.NetworkRepository/estimateFees/call-2")
    void shouldCallEstimateFees2() {
        assertNotNull(create().estimateFees(transactionValue(), modeValue(), highVolumeThrottleValue()));
    }

    private static NetworkRepository create() {
        return new NetworkRepository();
    }

    private static List<String> timestampValue() {
        return List.of("value");
    }

    private static Order orderValue() {
        return Order.ASC;
    }

    private static List<String> fileIdValue() {
        return List.of("value");
    }

    private static int limitValue() {
        return 1;
    }

    private static List<String> nodeIdValue() {
        return List.of("value");
    }

    private static List<String> registeredNodeIdValue() {
        return List.of("value");
    }

    private static RegisteredNodeType typeValue() {
        return RegisteredNodeType.BLOCK_NODE;
    }

    private static byte[] transactionValue() {
        return new byte[] {1, 2, 3};
    }

    private static FeeEstimateMode modeValue() {
        return FeeEstimateMode.INTRINSIC;
    }

    private static int highVolumeThrottleValue() {
        return 1;
    }
}
