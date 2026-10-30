package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractStateChange`, generated from the specs.
@DisplayName("ContractStateChange")
class ContractStateChangeTest {

    @Test
    @DisplayName("creates a ContractStateChange and returns the values")
    @Tag("testId:mirrornode.contract.ContractStateChange/create")
    void shouldCreateAndReturnTheValues() {
        final EvmAddress address = addressValue();
        final ContractId contractId = contractIdValue();
        final String slot = slotValue();
        final String valueRead = valueReadValue();
        final @Nullable String valueWritten = valueWrittenValue();
        final ContractStateChange subject = new ContractStateChange(address, contractId, slot, valueRead, valueWritten);
        assertValue(address, subject.address());
        assertValue(contractId, subject.contractId());
        assertValue(slot, subject.slot());
        assertValue(valueRead, subject.valueRead());
        assertValue(valueWritten, subject.valueWritten());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.ContractStateChange/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final EvmAddress address = addressValue();
        final ContractId contractId = contractIdValue();
        final String slot = slotValue();
        final String valueRead = valueReadValue();
        final @Nullable String valueWritten = valueWrittenValue();
        final ContractStateChange subject = ContractStateChange.builder()
                .address(address)
                .contractId(contractId)
                .slot(slot)
                .valueRead(valueRead)
                .valueWritten(valueWritten)
                .build();
        assertValue(address, subject.address());
        assertValue(contractId, subject.contractId());
        assertValue(slot, subject.slot());
        assertValue(valueRead, subject.valueRead());
        assertValue(valueWritten, subject.valueWritten());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.ContractStateChange/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractStateChange subject = ContractStateChange.builder()
                .address(addressValue())
                .contractId(contractIdValue())
                .slot(slotValue())
                .valueRead(valueReadValue())
                .build();
        assertNull(subject.valueWritten());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.ContractStateChange/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractStateChange.builder().build());
    }

    @Test
    @DisplayName("rejects null for address")
    @Tag("testId:mirrornode.contract.ContractStateChange/address/rejects-null")
    void shouldRejectNullAddress() {
        assertThrows(NullPointerException.class, () -> new ContractStateChange((EvmAddress) null, contractIdValue(), slotValue(), valueReadValue(), valueWrittenValue()));
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:mirrornode.contract.ContractStateChange/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractStateChange(addressValue(), (ContractId) null, slotValue(), valueReadValue(), valueWrittenValue()));
    }

    @Test
    @DisplayName("rejects null for slot")
    @Tag("testId:mirrornode.contract.ContractStateChange/slot/rejects-null")
    void shouldRejectNullSlot() {
        assertThrows(NullPointerException.class, () -> new ContractStateChange(addressValue(), contractIdValue(), (String) null, valueReadValue(), valueWrittenValue()));
    }

    @Test
    @DisplayName("rejects null for valueRead")
    @Tag("testId:mirrornode.contract.ContractStateChange/valueRead/rejects-null")
    void shouldRejectNullValueRead() {
        assertThrows(NullPointerException.class, () -> new ContractStateChange(addressValue(), contractIdValue(), slotValue(), (String) null, valueWrittenValue()));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.ContractStateChange/value-semantics")
    void shouldCompareByValue() {
        final ContractStateChange subject = create();
        final ContractStateChange other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static EvmAddress addressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static String slotValue() {
        return "value";
    }

    private static String valueReadValue() {
        return "value";
    }

    private static String valueWrittenValue() {
        return "value";
    }

    private static ContractStateChange create() {
        return new ContractStateChange(addressValue(), contractIdValue(), slotValue(), valueReadValue(), valueWrittenValue());
    }
}
