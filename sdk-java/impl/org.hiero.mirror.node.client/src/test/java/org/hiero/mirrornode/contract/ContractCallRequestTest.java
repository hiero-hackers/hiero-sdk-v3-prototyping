package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractCallRequest`, generated from the specs.
@DisplayName("ContractCallRequest")
class ContractCallRequestTest {

    @Test
    @DisplayName("creates a ContractCallRequest and returns the values")
    @Tag("testId:mirrornode.contract.ContractCallRequest/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable String block = blockValue();
        final @Nullable String data = dataValue();
        final @Nullable Boolean estimate = estimateValue();
        final @Nullable EvmAddress from = fromValue();
        final @Nullable Long gas = gasValue();
        final @Nullable Long gasPrice = gasPriceValue();
        final EvmAddress to = toValue();
        final @Nullable Long valueArgument = valueValue();
        final ContractCallRequest subject = new ContractCallRequest(block, data, estimate, from, gas, gasPrice, to, valueArgument);
        assertValue(block, subject.block());
        assertValue(data, subject.data());
        assertValue(estimate, subject.estimate());
        assertValue(from, subject.from());
        assertValue(gas, subject.gas());
        assertValue(gasPrice, subject.gasPrice());
        assertValue(to, subject.to());
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.ContractCallRequest/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable String block = blockValue();
        final @Nullable String data = dataValue();
        final @Nullable Boolean estimate = estimateValue();
        final @Nullable EvmAddress from = fromValue();
        final @Nullable Long gas = gasValue();
        final @Nullable Long gasPrice = gasPriceValue();
        final EvmAddress to = toValue();
        final @Nullable Long valueArgument = valueValue();
        final ContractCallRequest subject = ContractCallRequest.builder()
                .block(block)
                .data(data)
                .estimate(estimate)
                .from(from)
                .gas(gas)
                .gasPrice(gasPrice)
                .to(to)
                .value(valueArgument)
                .build();
        assertValue(block, subject.block());
        assertValue(data, subject.data());
        assertValue(estimate, subject.estimate());
        assertValue(from, subject.from());
        assertValue(gas, subject.gas());
        assertValue(gasPrice, subject.gasPrice());
        assertValue(to, subject.to());
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.ContractCallRequest/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractCallRequest subject = ContractCallRequest.builder()
                .to(toValue())
                .build();
        assertNull(subject.block());
        assertNull(subject.data());
        assertNull(subject.estimate());
        assertNull(subject.from());
        assertNull(subject.gas());
        assertNull(subject.gasPrice());
        assertNull(subject.value());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.ContractCallRequest/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractCallRequest.builder().build());
    }

    @Test
    @DisplayName("gas: accepts the minimum (0)")
    @Tag("testId:mirrornode.contract.ContractCallRequest/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final @Nullable Long value = 0L;
        final ContractCallRequest subject = new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), value, gasPriceValue(), toValue(), valueValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractCallRequest/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractCallRequest subject = new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), value, gasPriceValue(), toValue(), valueValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: rejects a value below the minimum (-1)")
    @Tag("testId:mirrornode.contract.ContractCallRequest/gas/rejects-below-minimum")
    void shouldRejectGasBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), -1L, gasPriceValue(), toValue(), valueValue()));
    }

    @Test
    @DisplayName("gasPrice: accepts the minimum (0)")
    @Tag("testId:mirrornode.contract.ContractCallRequest/gasPrice/accepts-at-minimum")
    void shouldAcceptGasPriceAtMinimum() {
        final @Nullable Long value = 0L;
        final ContractCallRequest subject = new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), gasValue(), value, toValue(), valueValue());
        assertValue(value, subject.gasPrice());
    }

    @Test
    @DisplayName("gasPrice: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractCallRequest/gasPrice/accepts-at-maximum")
    void shouldAcceptGasPriceAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractCallRequest subject = new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), gasValue(), value, toValue(), valueValue());
        assertValue(value, subject.gasPrice());
    }

    @Test
    @DisplayName("gasPrice: rejects a value below the minimum (-1)")
    @Tag("testId:mirrornode.contract.ContractCallRequest/gasPrice/rejects-below-minimum")
    void shouldRejectGasPriceBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), gasValue(), -1L, toValue(), valueValue()));
    }

    @Test
    @DisplayName("rejects null for to")
    @Tag("testId:mirrornode.contract.ContractCallRequest/to/rejects-null")
    void shouldRejectNullTo() {
        assertThrows(NullPointerException.class, () -> new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), gasValue(), gasPriceValue(), (EvmAddress) null, valueValue()));
    }

    @Test
    @DisplayName("value: accepts the minimum (0)")
    @Tag("testId:mirrornode.contract.ContractCallRequest/value/accepts-at-minimum")
    void shouldAcceptValueAtMinimum() {
        final @Nullable Long value = 0L;
        final ContractCallRequest subject = new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), gasValue(), gasPriceValue(), toValue(), value);
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("value: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractCallRequest/value/accepts-at-maximum")
    void shouldAcceptValueAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractCallRequest subject = new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), gasValue(), gasPriceValue(), toValue(), value);
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("value: rejects a value below the minimum (-1)")
    @Tag("testId:mirrornode.contract.ContractCallRequest/value/rejects-below-minimum")
    void shouldRejectValueBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), gasValue(), gasPriceValue(), toValue(), -1L));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.ContractCallRequest/value-semantics")
    void shouldCompareByValue() {
        final ContractCallRequest subject = create();
        final ContractCallRequest other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static String blockValue() {
        return "value";
    }

    private static String dataValue() {
        return "value";
    }

    private static boolean estimateValue() {
        return true;
    }

    private static EvmAddress fromValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static long gasValue() {
        return 1L;
    }

    private static long gasPriceValue() {
        return 1L;
    }

    private static EvmAddress toValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static long valueValue() {
        return 1L;
    }

    private static ContractCallRequest create() {
        return new ContractCallRequest(blockValue(), dataValue(), estimateValue(), fromValue(), gasValue(), gasPriceValue(), toValue(), valueValue());
    }
}
