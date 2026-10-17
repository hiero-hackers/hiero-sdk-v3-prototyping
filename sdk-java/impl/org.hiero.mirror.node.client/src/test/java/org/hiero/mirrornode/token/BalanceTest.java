package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Balance`, generated from the specs.
@DisplayName("Balance")
class BalanceTest {

    @Test
    @DisplayName("creates a Balance and returns the values")
    @Tag("testId:mirrornode.token.Balance/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId accountId = accountIdValue();
        final long balance = balanceValue();
        final long decimals = decimalsValue();
        final Balance subject = new Balance(accountId, balance, decimals);
        assertValue(accountId, subject.accountId());
        assertValue(balance, subject.balance());
        assertValue(decimals, subject.decimals());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:mirrornode.token.Balance/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new Balance((AccountId) null, balanceValue(), decimalsValue()));
    }

    @Test
    @DisplayName("balance: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.Balance/balance/accepts-at-minimum")
    void shouldAcceptBalanceAtMinimum() {
        final long value = -9223372036854775808L;
        final Balance subject = new Balance(accountIdValue(), value, decimalsValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("balance: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.Balance/balance/accepts-at-maximum")
    void shouldAcceptBalanceAtMaximum() {
        final long value = 9223372036854775807L;
        final Balance subject = new Balance(accountIdValue(), value, decimalsValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("decimals: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.Balance/decimals/accepts-at-minimum")
    void shouldAcceptDecimalsAtMinimum() {
        final long value = -9223372036854775808L;
        final Balance subject = new Balance(accountIdValue(), balanceValue(), value);
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("decimals: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.Balance/decimals/accepts-at-maximum")
    void shouldAcceptDecimalsAtMaximum() {
        final long value = 9223372036854775807L;
        final Balance subject = new Balance(accountIdValue(), balanceValue(), value);
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.token.Balance/value-semantics")
    void shouldCompareByValue() {
        final Balance subject = create();
        final Balance other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long balanceValue() {
        return 1L;
    }

    private static long decimalsValue() {
        return 1L;
    }

    private static Balance create() {
        return new Balance(accountIdValue(), balanceValue(), decimalsValue());
    }
}
