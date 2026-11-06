package org.hiero.consensusnode.transactions;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractNonce`, generated from the specs.
@DisplayName("ContractNonce")
class ContractNonceTest {

    @Test
    @DisplayName("creates a ContractNonce and returns the values")
    @Tag("testId:consensusnode.transactions.ContractNonce/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final long nonce = nonceValue();
        final ContractNonce subject = new ContractNonce(contractId, nonce);
        assertValue(contractId, subject.contractId());
        assertValue(nonce, subject.nonce());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:consensusnode.transactions.ContractNonce/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractNonce((ContractId) null, nonceValue()));
    }

    @Test
    @DisplayName("nonce: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.ContractNonce/nonce/accepts-at-minimum")
    void shouldAcceptNonceAtMinimum() {
        final long value = -9223372036854775808L;
        final ContractNonce subject = new ContractNonce(contractIdValue(), value);
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("nonce: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.ContractNonce/nonce/accepts-at-maximum")
    void shouldAcceptNonceAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractNonce subject = new ContractNonce(contractIdValue(), value);
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.transactions.ContractNonce/value-semantics")
    void shouldCompareByValue() {
        final ContractNonce subject = create();
        final ContractNonce other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static long nonceValue() {
        return 1L;
    }

    private static ContractNonce create() {
        return new ContractNonce(contractIdValue(), nonceValue());
    }
}
