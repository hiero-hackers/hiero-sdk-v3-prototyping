package org.hiero.consensusnode.transactions.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TokenAllowance`, generated from the specs.
@DisplayName("TokenAllowance")
class TokenAllowanceTest {

    @Test
    @DisplayName("creates a TokenAllowance and returns the values")
    @Tag("testId:consensusnode.transactions.accounts.TokenAllowance/create")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final AccountId ownerAccountId = ownerAccountIdValue();
        final AccountId spenderAccountId = spenderAccountIdValue();
        final long amount = amountValue();
        final TokenAllowance subject = new TokenAllowance(tokenId, ownerAccountId, spenderAccountId, amount);
        assertValue(tokenId, subject.tokenId());
        assertValue(ownerAccountId, subject.ownerAccountId());
        assertValue(spenderAccountId, subject.spenderAccountId());
        assertValue(amount, subject.amount());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:consensusnode.transactions.accounts.TokenAllowance/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenAllowance((Address) null, ownerAccountIdValue(), spenderAccountIdValue(), amountValue()));
    }

    @Test
    @DisplayName("rejects null for ownerAccountId")
    @Tag("testId:consensusnode.transactions.accounts.TokenAllowance/ownerAccountId/rejects-null")
    void shouldRejectNullOwnerAccountId() {
        assertThrows(NullPointerException.class, () -> new TokenAllowance(tokenIdValue(), (AccountId) null, spenderAccountIdValue(), amountValue()));
    }

    @Test
    @DisplayName("rejects null for spenderAccountId")
    @Tag("testId:consensusnode.transactions.accounts.TokenAllowance/spenderAccountId/rejects-null")
    void shouldRejectNullSpenderAccountId() {
        assertThrows(NullPointerException.class, () -> new TokenAllowance(tokenIdValue(), ownerAccountIdValue(), (AccountId) null, amountValue()));
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.accounts.TokenAllowance/amount/accepts-at-minimum")
    void shouldAcceptAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenAllowance subject = new TokenAllowance(tokenIdValue(), ownerAccountIdValue(), spenderAccountIdValue(), value);
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.accounts.TokenAllowance/amount/accepts-at-maximum")
    void shouldAcceptAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenAllowance subject = new TokenAllowance(tokenIdValue(), ownerAccountIdValue(), spenderAccountIdValue(), value);
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.transactions.accounts.TokenAllowance/value-semantics")
    void shouldCompareByValue() {
        final TokenAllowance subject = create();
        final TokenAllowance other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static AccountId ownerAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId spenderAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long amountValue() {
        return 1L;
    }

    private static TokenAllowance create() {
        return new TokenAllowance(tokenIdValue(), ownerAccountIdValue(), spenderAccountIdValue(), amountValue());
    }
}
