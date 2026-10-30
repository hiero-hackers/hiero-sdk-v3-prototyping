package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `OpcodeTrace`, generated from the specs.
@DisplayName("OpcodeTrace")
class OpcodeTraceTest {

    @Test
    @DisplayName("creates an OpcodeTrace and returns the values")
    @Tag("testId:mirrornode.contract.OpcodeTrace/create")
    void shouldCreateAndReturnTheValues() {
        final EvmAddress address = addressValue();
        final @Nullable ContractId contractId = contractIdValue();
        final boolean failed = failedValue();
        final long gas = gasValue();
        final List<Opcode> opcodes = opcodesValue();
        final String returnValue = returnValueValue();
        final OpcodeTrace subject = new OpcodeTrace(address, contractId, failed, gas, opcodes, returnValue);
        assertValue(address, subject.address());
        assertValue(contractId, subject.contractId());
        assertValue(failed, subject.failed());
        assertValue(gas, subject.gas());
        assertValue(opcodes, subject.opcodes());
        assertValue(returnValue, subject.returnValue());
    }

    @Test
    @DisplayName("creates an OpcodeTrace with the default values")
    @Tag("testId:mirrornode.contract.OpcodeTrace/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final OpcodeTrace subject = new OpcodeTrace(addressValue(), contractIdValue(), failedValue(), gasValue(), returnValueValue());
        assertValue(List.of(), subject.opcodes());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.OpcodeTrace/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final EvmAddress address = addressValue();
        final @Nullable ContractId contractId = contractIdValue();
        final boolean failed = failedValue();
        final long gas = gasValue();
        final List<Opcode> opcodes = opcodesValue();
        final String returnValue = returnValueValue();
        final OpcodeTrace subject = OpcodeTrace.builder()
                .address(address)
                .contractId(contractId)
                .failed(failed)
                .gas(gas)
                .opcodes(opcodes)
                .returnValue(returnValue)
                .build();
        assertValue(address, subject.address());
        assertValue(contractId, subject.contractId());
        assertValue(failed, subject.failed());
        assertValue(gas, subject.gas());
        assertValue(opcodes, subject.opcodes());
        assertValue(returnValue, subject.returnValue());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.OpcodeTrace/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final OpcodeTrace subject = OpcodeTrace.builder()
                .address(addressValue())
                .failed(failedValue())
                .gas(gasValue())
                .returnValue(returnValueValue())
                .build();
        assertNull(subject.contractId());
        assertValue(List.of(), subject.opcodes());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.OpcodeTrace/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> OpcodeTrace.builder().build());
    }

    @Test
    @DisplayName("rejects null for address")
    @Tag("testId:mirrornode.contract.OpcodeTrace/address/rejects-null")
    void shouldRejectNullAddress() {
        assertThrows(NullPointerException.class, () -> new OpcodeTrace((EvmAddress) null, contractIdValue(), failedValue(), gasValue(), opcodesValue(), returnValueValue()));
    }

    @Test
    @DisplayName("gas: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.OpcodeTrace/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final long value = -9223372036854775808L;
        final OpcodeTrace subject = new OpcodeTrace(addressValue(), contractIdValue(), failedValue(), value, opcodesValue(), returnValueValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.OpcodeTrace/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final long value = 9223372036854775807L;
        final OpcodeTrace subject = new OpcodeTrace(addressValue(), contractIdValue(), failedValue(), value, opcodesValue(), returnValueValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("rejects null for opcodes")
    @Tag("testId:mirrornode.contract.OpcodeTrace/opcodes/rejects-null")
    void shouldRejectNullOpcodes() {
        assertThrows(NullPointerException.class, () -> new OpcodeTrace(addressValue(), contractIdValue(), failedValue(), gasValue(), (List<Opcode>) null, returnValueValue()));
    }

    @Test
    @DisplayName("the constructor copies opcodes; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.OpcodeTrace/opcodes/copies-in")
    void shouldCopyOpcodesIn() {
        final List<Opcode> value = new ArrayList<>(opcodesValue());
        final List<Opcode> expected = List.copyOf(value);
        final OpcodeTrace subject = new OpcodeTrace(addressValue(), contractIdValue(), failedValue(), gasValue(), value, returnValueValue());
        value.clear();
        assertValue(expected, subject.opcodes());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.opcodes()).clear());
    }

    @Test
    @DisplayName("rejects null for returnValue")
    @Tag("testId:mirrornode.contract.OpcodeTrace/returnValue/rejects-null")
    void shouldRejectNullReturnValue() {
        assertThrows(NullPointerException.class, () -> new OpcodeTrace(addressValue(), contractIdValue(), failedValue(), gasValue(), opcodesValue(), (String) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.OpcodeTrace/value-semantics")
    void shouldCompareByValue() {
        final OpcodeTrace subject = create();
        final OpcodeTrace other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static EvmAddress addressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static boolean failedValue() {
        return true;
    }

    private static long gasValue() {
        return 1L;
    }

    private static List<Opcode> opcodesValue() {
        return List.of(new Opcode(1, 1L, 1L, List.of("value"), "value", 1, null, List.of("value"), Map.ofEntries(Map.entry("value", "value"))));
    }

    private static String returnValueValue() {
        return "value";
    }

    private static OpcodeTrace create() {
        return new OpcodeTrace(addressValue(), contractIdValue(), failedValue(), gasValue(), opcodesValue(), returnValueValue());
    }
}
