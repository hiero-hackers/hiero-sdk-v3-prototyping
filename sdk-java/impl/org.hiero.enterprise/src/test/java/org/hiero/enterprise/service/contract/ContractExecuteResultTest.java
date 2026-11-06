package org.hiero.enterprise.service.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.evm.AbiUint;
import org.hiero.evm.AbiValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractExecuteResult`, generated from the specs.
@DisplayName("ContractExecuteResult")
class ContractExecuteResultTest {

    @Test
    @DisplayName("creates a ContractExecuteResult and returns the values")
    @Tag("testId:enterprise.service.contract.ContractExecuteResult/create")
    void shouldCreateAndReturnTheValues() {
        final List<AbiValue> values = valuesValue();
        final byte[] result = resultValue();
        final long gasUsed = gasUsedValue();
        final ContractExecuteResult subject = new ContractExecuteResult(values, result, gasUsed);
        assertValue(values, subject.values());
        assertValue(result, subject.result());
        assertValue(gasUsed, subject.gasUsed());
    }

    @Test
    @DisplayName("creates a ContractExecuteResult with the default values")
    @Tag("testId:enterprise.service.contract.ContractExecuteResult/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractExecuteResult subject = new ContractExecuteResult(resultValue(), gasUsedValue());
        assertValue(List.of(), subject.values());
    }

    @Test
    @DisplayName("rejects null for values")
    @Tag("testId:enterprise.service.contract.ContractExecuteResult/values/rejects-null")
    void shouldRejectNullValues() {
        assertThrows(NullPointerException.class, () -> new ContractExecuteResult((List<AbiValue>) null, resultValue(), gasUsedValue()));
    }

    @Test
    @DisplayName("the constructor copies values; the accessor returns an unmodifiable collection")
    @Tag("testId:enterprise.service.contract.ContractExecuteResult/values/copies-in")
    void shouldCopyValuesIn() {
        final List<AbiValue> value = new ArrayList<>(valuesValue());
        final List<AbiValue> expected = List.copyOf(value);
        final ContractExecuteResult subject = new ContractExecuteResult(value, resultValue(), gasUsedValue());
        value.clear();
        assertValue(expected, subject.values());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.values()).clear());
    }

    @Test
    @DisplayName("rejects null for result")
    @Tag("testId:enterprise.service.contract.ContractExecuteResult/result/rejects-null")
    void shouldRejectNullResult() {
        assertThrows(NullPointerException.class, () -> new ContractExecuteResult(valuesValue(), (byte[]) null, gasUsedValue()));
    }

    @Test
    @DisplayName("the constructor copies result; the accessor returns a copy")
    @Tag("testId:enterprise.service.contract.ContractExecuteResult/result/copies-in")
    void shouldCopyResultIn() {
        final byte[] value = resultValue();
        final byte[] expected = value.clone();
        final ContractExecuteResult subject = new ContractExecuteResult(valuesValue(), value, gasUsedValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.result());
        final byte[] returned = subject.result();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.result());
    }

    @Test
    @DisplayName("gasUsed: accepts the minimum (0)")
    @Tag("testId:enterprise.service.contract.ContractExecuteResult/gasUsed/accepts-at-minimum")
    void shouldAcceptGasUsedAtMinimum() {
        final long value = 0L;
        final ContractExecuteResult subject = new ContractExecuteResult(valuesValue(), resultValue(), value);
        assertValue(value, subject.gasUsed());
    }

    @Test
    @DisplayName("gasUsed: accepts the maximum (18446744073709551615)")
    @Tag("testId:enterprise.service.contract.ContractExecuteResult/gasUsed/accepts-at-maximum")
    void shouldAcceptGasUsedAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final ContractExecuteResult subject = new ContractExecuteResult(valuesValue(), resultValue(), value);
        assertValue(value, subject.gasUsed());
    }

    private static List<AbiValue> valuesValue() {
        return List.of(new AbiUint(new BigInteger("1"), 256));
    }

    private static byte[] resultValue() {
        return new byte[] {1, 2, 3};
    }

    private static long gasUsedValue() {
        return 1L;
    }

    private static ContractExecuteResult create() {
        return new ContractExecuteResult(valuesValue(), resultValue(), gasUsedValue());
    }
}
