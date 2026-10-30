package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Opcode`, generated from the specs.
@DisplayName("Opcode")
class OpcodeTest {

    @Test
    @DisplayName("creates an Opcode and returns the values")
    @Tag("testId:mirrornode.contract.Opcode/create")
    void shouldCreateAndReturnTheValues() {
        final int depth = depthValue();
        final long gas = gasValue();
        final long gasCost = gasCostValue();
        final List<String> memory = memoryValue();
        final String op = opValue();
        final int pc = pcValue();
        final @Nullable String reason = reasonValue();
        final List<String> stack = stackValue();
        final Map<String, String> storage = storageValue();
        final Opcode subject = new Opcode(depth, gas, gasCost, memory, op, pc, reason, stack, storage);
        assertValue(depth, subject.depth());
        assertValue(gas, subject.gas());
        assertValue(gasCost, subject.gasCost());
        assertValue(memory, subject.memory());
        assertValue(op, subject.op());
        assertValue(pc, subject.pc());
        assertValue(reason, subject.reason());
        assertValue(stack, subject.stack());
        assertValue(storage, subject.storage());
    }

    @Test
    @DisplayName("creates an Opcode with the default values")
    @Tag("testId:mirrornode.contract.Opcode/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final Opcode subject = new Opcode(depthValue(), gasValue(), gasCostValue(), opValue(), pcValue(), reasonValue(), storageValue());
        assertValue(List.of(), subject.memory());
        assertValue(List.of(), subject.stack());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.Opcode/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final int depth = depthValue();
        final long gas = gasValue();
        final long gasCost = gasCostValue();
        final List<String> memory = memoryValue();
        final String op = opValue();
        final int pc = pcValue();
        final @Nullable String reason = reasonValue();
        final List<String> stack = stackValue();
        final Map<String, String> storage = storageValue();
        final Opcode subject = Opcode.builder()
                .depth(depth)
                .gas(gas)
                .gasCost(gasCost)
                .memory(memory)
                .op(op)
                .pc(pc)
                .reason(reason)
                .stack(stack)
                .storage(storage)
                .build();
        assertValue(depth, subject.depth());
        assertValue(gas, subject.gas());
        assertValue(gasCost, subject.gasCost());
        assertValue(memory, subject.memory());
        assertValue(op, subject.op());
        assertValue(pc, subject.pc());
        assertValue(reason, subject.reason());
        assertValue(stack, subject.stack());
        assertValue(storage, subject.storage());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.Opcode/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final Opcode subject = Opcode.builder()
                .depth(depthValue())
                .gas(gasValue())
                .gasCost(gasCostValue())
                .op(opValue())
                .pc(pcValue())
                .storage(storageValue())
                .build();
        assertValue(List.of(), subject.memory());
        assertNull(subject.reason());
        assertValue(List.of(), subject.stack());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.Opcode/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> Opcode.builder().build());
    }

    @Test
    @DisplayName("depth: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.contract.Opcode/depth/accepts-at-minimum")
    void shouldAcceptDepthAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final Opcode subject = new Opcode(value, gasValue(), gasCostValue(), memoryValue(), opValue(), pcValue(), reasonValue(), stackValue(), storageValue());
        assertValue(value, subject.depth());
    }

    @Test
    @DisplayName("depth: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.contract.Opcode/depth/accepts-at-maximum")
    void shouldAcceptDepthAtMaximum() {
        final int value = 2147483647;
        final Opcode subject = new Opcode(value, gasValue(), gasCostValue(), memoryValue(), opValue(), pcValue(), reasonValue(), stackValue(), storageValue());
        assertValue(value, subject.depth());
    }

    @Test
    @DisplayName("gas: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.Opcode/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final long value = -9223372036854775808L;
        final Opcode subject = new Opcode(depthValue(), value, gasCostValue(), memoryValue(), opValue(), pcValue(), reasonValue(), stackValue(), storageValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.Opcode/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final long value = 9223372036854775807L;
        final Opcode subject = new Opcode(depthValue(), value, gasCostValue(), memoryValue(), opValue(), pcValue(), reasonValue(), stackValue(), storageValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gasCost: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.Opcode/gasCost/accepts-at-minimum")
    void shouldAcceptGasCostAtMinimum() {
        final long value = -9223372036854775808L;
        final Opcode subject = new Opcode(depthValue(), gasValue(), value, memoryValue(), opValue(), pcValue(), reasonValue(), stackValue(), storageValue());
        assertValue(value, subject.gasCost());
    }

    @Test
    @DisplayName("gasCost: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.Opcode/gasCost/accepts-at-maximum")
    void shouldAcceptGasCostAtMaximum() {
        final long value = 9223372036854775807L;
        final Opcode subject = new Opcode(depthValue(), gasValue(), value, memoryValue(), opValue(), pcValue(), reasonValue(), stackValue(), storageValue());
        assertValue(value, subject.gasCost());
    }

    @Test
    @DisplayName("rejects null for memory")
    @Tag("testId:mirrornode.contract.Opcode/memory/rejects-null")
    void shouldRejectNullMemory() {
        assertThrows(NullPointerException.class, () -> new Opcode(depthValue(), gasValue(), gasCostValue(), (List<String>) null, opValue(), pcValue(), reasonValue(), stackValue(), storageValue()));
    }

    @Test
    @DisplayName("the constructor copies memory; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.Opcode/memory/copies-in")
    void shouldCopyMemoryIn() {
        final List<String> value = new ArrayList<>(memoryValue());
        final List<String> expected = List.copyOf(value);
        final Opcode subject = new Opcode(depthValue(), gasValue(), gasCostValue(), value, opValue(), pcValue(), reasonValue(), stackValue(), storageValue());
        value.clear();
        assertValue(expected, subject.memory());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.memory()).clear());
    }

    @Test
    @DisplayName("rejects null for op")
    @Tag("testId:mirrornode.contract.Opcode/op/rejects-null")
    void shouldRejectNullOp() {
        assertThrows(NullPointerException.class, () -> new Opcode(depthValue(), gasValue(), gasCostValue(), memoryValue(), (String) null, pcValue(), reasonValue(), stackValue(), storageValue()));
    }

    @Test
    @DisplayName("pc: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.contract.Opcode/pc/accepts-at-minimum")
    void shouldAcceptPcAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final Opcode subject = new Opcode(depthValue(), gasValue(), gasCostValue(), memoryValue(), opValue(), value, reasonValue(), stackValue(), storageValue());
        assertValue(value, subject.pc());
    }

    @Test
    @DisplayName("pc: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.contract.Opcode/pc/accepts-at-maximum")
    void shouldAcceptPcAtMaximum() {
        final int value = 2147483647;
        final Opcode subject = new Opcode(depthValue(), gasValue(), gasCostValue(), memoryValue(), opValue(), value, reasonValue(), stackValue(), storageValue());
        assertValue(value, subject.pc());
    }

    @Test
    @DisplayName("rejects null for stack")
    @Tag("testId:mirrornode.contract.Opcode/stack/rejects-null")
    void shouldRejectNullStack() {
        assertThrows(NullPointerException.class, () -> new Opcode(depthValue(), gasValue(), gasCostValue(), memoryValue(), opValue(), pcValue(), reasonValue(), (List<String>) null, storageValue()));
    }

    @Test
    @DisplayName("the constructor copies stack; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.Opcode/stack/copies-in")
    void shouldCopyStackIn() {
        final List<String> value = new ArrayList<>(stackValue());
        final List<String> expected = List.copyOf(value);
        final Opcode subject = new Opcode(depthValue(), gasValue(), gasCostValue(), memoryValue(), opValue(), pcValue(), reasonValue(), value, storageValue());
        value.clear();
        assertValue(expected, subject.stack());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.stack()).clear());
    }

    @Test
    @DisplayName("rejects null for storage")
    @Tag("testId:mirrornode.contract.Opcode/storage/rejects-null")
    void shouldRejectNullStorage() {
        assertThrows(NullPointerException.class, () -> new Opcode(depthValue(), gasValue(), gasCostValue(), memoryValue(), opValue(), pcValue(), reasonValue(), stackValue(), (Map<String, String>) null));
    }

    @Test
    @DisplayName("the constructor copies storage; the accessor returns an unmodifiable map")
    @Tag("testId:mirrornode.contract.Opcode/storage/copies-in")
    void shouldCopyStorageIn() {
        final Map<String, String> value = new LinkedHashMap<>(storageValue());
        final Map<String, String> expected = Map.copyOf(value);
        final Opcode subject = new Opcode(depthValue(), gasValue(), gasCostValue(), memoryValue(), opValue(), pcValue(), reasonValue(), stackValue(), value);
        value.clear();
        assertValue(expected, subject.storage());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.storage()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.Opcode/value-semantics")
    void shouldCompareByValue() {
        final Opcode subject = create();
        final Opcode other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static int depthValue() {
        return 1;
    }

    private static long gasValue() {
        return 1L;
    }

    private static long gasCostValue() {
        return 1L;
    }

    private static List<String> memoryValue() {
        return List.of("value");
    }

    private static String opValue() {
        return "value";
    }

    private static int pcValue() {
        return 1;
    }

    private static String reasonValue() {
        return "value";
    }

    private static List<String> stackValue() {
        return List.of("value");
    }

    private static Map<String, String> storageValue() {
        return Map.ofEntries(Map.entry("value", "value"));
    }

    private static Opcode create() {
        return new Opcode(depthValue(), gasValue(), gasCostValue(), memoryValue(), opValue(), pcValue(), reasonValue(), stackValue(), storageValue());
    }
}
