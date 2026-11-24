package org.hiero.consensusnode.queries.contracts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractBytecode`, generated from the specs.
@DisplayName("ContractBytecode")
class ContractBytecodeTest {

    @Test
    @DisplayName("creates a ContractBytecode and returns the values")
    @Tag("testId:consensusnode.queries.contracts.ContractBytecode/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final byte[] bytecode = bytecodeValue();
        final ContractBytecode subject = new ContractBytecode(contractId, bytecode);
        assertValue(contractId, subject.contractId());
        assertValue(bytecode, subject.bytecode());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:consensusnode.queries.contracts.ContractBytecode/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractBytecode((ContractId) null, bytecodeValue()));
    }

    @Test
    @DisplayName("rejects null for bytecode")
    @Tag("testId:consensusnode.queries.contracts.ContractBytecode/bytecode/rejects-null")
    void shouldRejectNullBytecode() {
        assertThrows(NullPointerException.class, () -> new ContractBytecode(contractIdValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies bytecode; the accessor returns a copy")
    @Tag("testId:consensusnode.queries.contracts.ContractBytecode/bytecode/copies-in")
    void shouldCopyBytecodeIn() {
        final byte[] value = bytecodeValue();
        final byte[] expected = value.clone();
        final ContractBytecode subject = new ContractBytecode(contractIdValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bytecode());
        final byte[] returned = subject.bytecode();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bytecode());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.queries.contracts.ContractBytecode/value-semantics")
    void shouldCompareByValue() {
        final ContractBytecode subject = create();
        final ContractBytecode other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static byte[] bytecodeValue() {
        return new byte[] {1, 2, 3};
    }

    private static ContractBytecode create() {
        return new ContractBytecode(contractIdValue(), bytecodeValue());
    }
}
