package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountBalance`, generated from the specs.
@DisplayName("AccountBalance")
class AccountBalanceTest {

    @Test
    @DisplayName("creates an AccountBalance and returns the values")
    @Tag("testId:mirrornode.account.AccountBalance/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId account = accountValue();
        final long balance = balanceValue();
        final List<TokenBalance> tokens = tokensValue();
        final AccountBalance subject = new AccountBalance(account, balance, tokens);
        assertValue(account, subject.account());
        assertValue(balance, subject.balance());
        assertValue(tokens, subject.tokens());
    }

    @Test
    @DisplayName("creates an AccountBalance with the default values")
    @Tag("testId:mirrornode.account.AccountBalance/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AccountBalance subject = new AccountBalance(accountValue(), balanceValue());
        assertValue(List.of(), subject.tokens());
    }

    @Test
    @DisplayName("rejects null for account")
    @Tag("testId:mirrornode.account.AccountBalance/account/rejects-null")
    void shouldRejectNullAccount() {
        assertThrows(NullPointerException.class, () -> new AccountBalance((AccountId) null, balanceValue(), tokensValue()));
    }

    @Test
    @DisplayName("balance: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.AccountBalance/balance/accepts-at-minimum")
    void shouldAcceptBalanceAtMinimum() {
        final long value = -9223372036854775808L;
        final AccountBalance subject = new AccountBalance(accountValue(), value, tokensValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("balance: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.AccountBalance/balance/accepts-at-maximum")
    void shouldAcceptBalanceAtMaximum() {
        final long value = 9223372036854775807L;
        final AccountBalance subject = new AccountBalance(accountValue(), value, tokensValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("rejects null for tokens")
    @Tag("testId:mirrornode.account.AccountBalance/tokens/rejects-null")
    void shouldRejectNullTokens() {
        assertThrows(NullPointerException.class, () -> new AccountBalance(accountValue(), balanceValue(), (List<TokenBalance>) null));
    }

    @Test
    @DisplayName("the constructor copies tokens; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.account.AccountBalance/tokens/copies-in")
    void shouldCopyTokensIn() {
        final List<TokenBalance> value = new ArrayList<>(tokensValue());
        final List<TokenBalance> expected = List.copyOf(value);
        final AccountBalance subject = new AccountBalance(accountValue(), balanceValue(), value);
        value.clear();
        assertValue(expected, subject.tokens());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.tokens()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.account.AccountBalance/value-semantics")
    void shouldCompareByValue() {
        final AccountBalance subject = create();
        final AccountBalance other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId accountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long balanceValue() {
        return 1L;
    }

    private static List<TokenBalance> tokensValue() {
        return List.of(new TokenBalance(new Address(0L, 0L, "", 1001L), 1L));
    }

    private static AccountBalance create() {
        return new AccountBalance(accountValue(), balanceValue(), tokensValue());
    }
}
