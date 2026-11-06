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

/// Tests of `ContractCallResult`, generated from the specs.
@DisplayName("ContractCallResult")
class ContractCallResultTest {

    @Test
    @DisplayName("creates a ContractCallResult and returns the values")
    @Tag("testId:enterprise.service.contract.ContractCallResult/create")
    void shouldCreateAndReturnTheValues() {
        final List<AbiValue> values = valuesValue();
        final byte[] result = resultValue();
        final ContractCallResult subject = new ContractCallResult(values, result);
        assertValue(values, subject.values());
        assertValue(result, subject.result());
    }

    @Test
    @DisplayName("creates a ContractCallResult with the default values")
    @Tag("testId:enterprise.service.contract.ContractCallResult/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractCallResult subject = new ContractCallResult(resultValue());
        assertValue(List.of(), subject.values());
    }

    @Test
    @DisplayName("rejects null for values")
    @Tag("testId:enterprise.service.contract.ContractCallResult/values/rejects-null")
    void shouldRejectNullValues() {
        assertThrows(NullPointerException.class, () -> new ContractCallResult((List<AbiValue>) null, resultValue()));
    }

    @Test
    @DisplayName("the constructor copies values; the accessor returns an unmodifiable collection")
    @Tag("testId:enterprise.service.contract.ContractCallResult/values/copies-in")
    void shouldCopyValuesIn() {
        final List<AbiValue> value = new ArrayList<>(valuesValue());
        final List<AbiValue> expected = List.copyOf(value);
        final ContractCallResult subject = new ContractCallResult(value, resultValue());
        value.clear();
        assertValue(expected, subject.values());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.values()).clear());
    }

    @Test
    @DisplayName("rejects null for result")
    @Tag("testId:enterprise.service.contract.ContractCallResult/result/rejects-null")
    void shouldRejectNullResult() {
        assertThrows(NullPointerException.class, () -> new ContractCallResult(valuesValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies result; the accessor returns a copy")
    @Tag("testId:enterprise.service.contract.ContractCallResult/result/copies-in")
    void shouldCopyResultIn() {
        final byte[] value = resultValue();
        final byte[] expected = value.clone();
        final ContractCallResult subject = new ContractCallResult(valuesValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.result());
        final byte[] returned = subject.result();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.result());
    }

    private static List<AbiValue> valuesValue() {
        return List.of(new AbiUint(new BigInteger("1"), 256));
    }

    private static byte[] resultValue() {
        return new byte[] {1, 2, 3};
    }

    private static ContractCallResult create() {
        return new ContractCallResult(valuesValue(), resultValue());
    }
}
