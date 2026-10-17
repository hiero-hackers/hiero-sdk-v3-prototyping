package org.hiero.consensusnode.queries.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountBalance`, generated from the specs.
@DisplayName("AccountBalance")
class AccountBalanceTest {

    @Test
    @DisplayName("creates an AccountBalance and returns the values")
    @Tag("testId:consensusnode.queries.accounts.AccountBalance/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId accountId = accountIdValue();
        final NativeToken<?, ?> balance = balanceValue();
        final Map<Address, Long> tokenBalances = tokenBalancesValue();
        final AccountBalance subject = new AccountBalance(accountId, balance, tokenBalances);
        assertValue(accountId, subject.accountId());
        assertValue(balance, subject.balance());
        assertValue(tokenBalances, subject.tokenBalances());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:consensusnode.queries.accounts.AccountBalance/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new AccountBalance((AccountId) null, balanceValue(), tokenBalancesValue()));
    }

    @Test
    @DisplayName("rejects null for balance")
    @Tag("testId:consensusnode.queries.accounts.AccountBalance/balance/rejects-null")
    void shouldRejectNullBalance() {
        assertThrows(NullPointerException.class, () -> new AccountBalance(accountIdValue(), (NativeToken<?, ?>) null, tokenBalancesValue()));
    }

    @Test
    @DisplayName("rejects null for tokenBalances")
    @Tag("testId:consensusnode.queries.accounts.AccountBalance/tokenBalances/rejects-null")
    void shouldRejectNullTokenBalances() {
        assertThrows(NullPointerException.class, () -> new AccountBalance(accountIdValue(), balanceValue(), (Map<Address, Long>) null));
    }

    @Test
    @DisplayName("the constructor copies tokenBalances; the accessor returns an unmodifiable map")
    @Tag("testId:consensusnode.queries.accounts.AccountBalance/tokenBalances/copies-in")
    void shouldCopyTokenBalancesIn() {
        final Map<Address, Long> value = new LinkedHashMap<>(tokenBalancesValue());
        final Map<Address, Long> expected = Map.copyOf(value);
        final AccountBalance subject = new AccountBalance(accountIdValue(), balanceValue(), value);
        value.clear();
        assertValue(expected, subject.tokenBalances());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.tokenBalances()).clear());
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static NativeToken<?, ?> balanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static Map<Address, Long> tokenBalancesValue() {
        return Map.ofEntries(Map.entry(new Address(0L, 0L, "", 1001L), 1L));
    }

    private static AccountBalance create() {
        return new AccountBalance(accountIdValue(), balanceValue(), tokenBalancesValue());
    }
}
