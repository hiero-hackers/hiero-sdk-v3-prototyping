package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractState`, generated from the specs.
@DisplayName("ContractState")
class ContractStateTest {

    @Test
    @DisplayName("creates a ContractState and returns the values")
    @Tag("testId:mirrornode.contract.ContractState/create")
    void shouldCreateAndReturnTheValues() {
        final EvmAddress address = addressValue();
        final ContractId contractId = contractIdValue();
        final ZonedDateTime timestamp = timestampValue();
        final String slot = slotValue();
        final String valueArgument = valueValue();
        final ContractState subject = new ContractState(address, contractId, timestamp, slot, valueArgument);
        assertValue(address, subject.address());
        assertValue(contractId, subject.contractId());
        assertValue(timestamp, subject.timestamp());
        assertValue(slot, subject.slot());
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.ContractState/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final EvmAddress address = addressValue();
        final ContractId contractId = contractIdValue();
        final ZonedDateTime timestamp = timestampValue();
        final String slot = slotValue();
        final String valueArgument = valueValue();
        final ContractState subject = ContractState.builder()
                .address(address)
                .contractId(contractId)
                .timestamp(timestamp)
                .slot(slot)
                .value(valueArgument)
                .build();
        assertValue(address, subject.address());
        assertValue(contractId, subject.contractId());
        assertValue(timestamp, subject.timestamp());
        assertValue(slot, subject.slot());
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.ContractState/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractState subject = ContractState.builder()
                .address(addressValue())
                .contractId(contractIdValue())
                .timestamp(timestampValue())
                .slot(slotValue())
                .value(valueValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.ContractState/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractState.builder().build());
    }

    @Test
    @DisplayName("rejects null for address")
    @Tag("testId:mirrornode.contract.ContractState/address/rejects-null")
    void shouldRejectNullAddress() {
        assertThrows(NullPointerException.class, () -> new ContractState((EvmAddress) null, contractIdValue(), timestampValue(), slotValue(), valueValue()));
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:mirrornode.contract.ContractState/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractState(addressValue(), (ContractId) null, timestampValue(), slotValue(), valueValue()));
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.contract.ContractState/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new ContractState(addressValue(), contractIdValue(), (ZonedDateTime) null, slotValue(), valueValue()));
    }

    @Test
    @DisplayName("rejects null for slot")
    @Tag("testId:mirrornode.contract.ContractState/slot/rejects-null")
    void shouldRejectNullSlot() {
        assertThrows(NullPointerException.class, () -> new ContractState(addressValue(), contractIdValue(), timestampValue(), (String) null, valueValue()));
    }

    @Test
    @DisplayName("rejects null for value")
    @Tag("testId:mirrornode.contract.ContractState/value/rejects-null")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new ContractState(addressValue(), contractIdValue(), timestampValue(), slotValue(), (String) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.ContractState/value-semantics")
    void shouldCompareByValue() {
        final ContractState subject = create();
        final ContractState other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static EvmAddress addressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static String slotValue() {
        return "value";
    }

    private static String valueValue() {
        return "value";
    }

    private static ContractState create() {
        return new ContractState(addressValue(), contractIdValue(), timestampValue(), slotValue(), valueValue());
    }
}
