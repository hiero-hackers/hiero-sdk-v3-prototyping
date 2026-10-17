package org.hiero.enterprise.service.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Contract`, generated from the specs.
@DisplayName("Contract")
class ContractTest {

    @Test
    @DisplayName("creates a Contract and returns the values")
    @Tag("testId:enterprise.service.contract.Contract/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final Contract subject = new Contract(contractId);
        assertValue(contractId, subject.contractId());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:enterprise.service.contract.Contract/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new Contract((ContractId) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:enterprise.service.contract.Contract/value-semantics")
    void shouldCompareByValue() {
        final Contract subject = create();
        final Contract other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static Contract create() {
        return new Contract(contractIdValue());
    }

    private static Contract defaultSubject() {
        return new Contract(new ContractId(0L, 0L, "", 1001L, null));
    }
}
