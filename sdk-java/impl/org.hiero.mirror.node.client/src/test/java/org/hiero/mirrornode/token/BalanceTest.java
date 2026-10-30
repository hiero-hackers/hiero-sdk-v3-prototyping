package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;
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
        final AccountId account = accountValue();
        final long balance = balanceValue();
        final @Nullable Long decimals = decimalsValue();
        final Balance subject = new Balance(account, balance, decimals);
        assertValue(account, subject.account());
        assertValue(balance, subject.balance());
        assertValue(decimals, subject.decimals());
    }

    @Test
    @DisplayName("rejects null for account")
    @Tag("testId:mirrornode.token.Balance/account/rejects-null")
    void shouldRejectNullAccount() {
        assertThrows(NullPointerException.class, () -> new Balance((AccountId) null, balanceValue(), decimalsValue()));
    }

    @Test
    @DisplayName("balance: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.Balance/balance/accepts-at-minimum")
    void shouldAcceptBalanceAtMinimum() {
        final long value = -9223372036854775808L;
        final Balance subject = new Balance(accountValue(), value, decimalsValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("balance: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.Balance/balance/accepts-at-maximum")
    void shouldAcceptBalanceAtMaximum() {
        final long value = 9223372036854775807L;
        final Balance subject = new Balance(accountValue(), value, decimalsValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("decimals: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.Balance/decimals/accepts-at-minimum")
    void shouldAcceptDecimalsAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final Balance subject = new Balance(accountValue(), balanceValue(), value);
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("decimals: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.Balance/decimals/accepts-at-maximum")
    void shouldAcceptDecimalsAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final Balance subject = new Balance(accountValue(), balanceValue(), value);
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

    private static AccountId accountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long balanceValue() {
        return 1L;
    }

    private static long decimalsValue() {
        return 1L;
    }

    private static Balance create() {
        return new Balance(accountValue(), balanceValue(), decimalsValue());
    }
}
