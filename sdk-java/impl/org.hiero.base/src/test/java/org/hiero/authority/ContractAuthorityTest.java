package org.hiero.authority;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractAuthority`, generated from the specs.
@DisplayName("ContractAuthority")
class ContractAuthorityTest {

    @Test
    @DisplayName("creates a ContractAuthority and returns the values")
    @Tag("testId:authority.ContractAuthority/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final boolean delegatable = delegatableValue();
        final ContractAuthority subject = new ContractAuthority(contractId, delegatable);
        assertValue(contractId, subject.contractId());
        assertValue(delegatable, subject.delegatable());
    }

    @Test
    @DisplayName("creates a ContractAuthority with the default values")
    @Tag("testId:authority.ContractAuthority/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractAuthority subject = new ContractAuthority(contractIdValue());
        assertValue(false, subject.delegatable());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:authority.ContractAuthority/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractAuthority((ContractId) null, delegatableValue()));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:authority.ContractAuthority/value-semantics")
    void shouldCompareByValue() {
        final ContractAuthority subject = create();
        final ContractAuthority other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static boolean delegatableValue() {
        return true;
    }

    private static ContractAuthority create() {
        return new ContractAuthority(contractIdValue(), delegatableValue());
    }
}
