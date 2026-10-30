package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.common.NftTransfer;
import org.hiero.mirrornode.common.NftTransferParties;
import org.hiero.mirrornode.common.StakingRewardTransfer;
import org.hiero.mirrornode.common.TokenTransfer;
import org.hiero.mirrornode.common.Transfer;
import org.hiero.mirrornode.transaction.AssessedCustomFee;
import org.hiero.mirrornode.transaction.BasicTransactionType;
import org.hiero.mirrornode.transaction.CustomFeeLimit;
import org.hiero.mirrornode.transaction.TransactionInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountBalanceTransactions`, generated from the specs.
@DisplayName("AccountBalanceTransactions")
class AccountBalanceTransactionsTest {

    @Test
    @DisplayName("creates an AccountBalanceTransactions and returns the values")
    @Tag("testId:mirrornode.account.AccountBalanceTransactions/create")
    void shouldCreateAndReturnTheValues() {
        final AccountInfo account = accountValue();
        final List<TransactionInfo> transactions = transactionsValue();
        final AccountBalanceTransactions subject = new AccountBalanceTransactions(account, transactions);
        assertValue(account, subject.account());
        assertValue(transactions, subject.transactions());
    }

    @Test
    @DisplayName("creates an AccountBalanceTransactions with the default values")
    @Tag("testId:mirrornode.account.AccountBalanceTransactions/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AccountBalanceTransactions subject = new AccountBalanceTransactions(accountValue());
        assertValue(List.of(), subject.transactions());
    }

    @Test
    @DisplayName("rejects null for account")
    @Tag("testId:mirrornode.account.AccountBalanceTransactions/account/rejects-null")
    void shouldRejectNullAccount() {
        assertThrows(NullPointerException.class, () -> new AccountBalanceTransactions((AccountInfo) null, transactionsValue()));
    }

    @Test
    @DisplayName("rejects null for transactions")
    @Tag("testId:mirrornode.account.AccountBalanceTransactions/transactions/rejects-null")
    void shouldRejectNullTransactions() {
        assertThrows(NullPointerException.class, () -> new AccountBalanceTransactions(accountValue(), (List<TransactionInfo>) null));
    }

    @Test
    @DisplayName("the constructor copies transactions; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.account.AccountBalanceTransactions/transactions/copies-in")
    void shouldCopyTransactionsIn() {
        final List<TransactionInfo> value = new ArrayList<>(transactionsValue());
        final List<TransactionInfo> expected = List.copyOf(value);
        final AccountBalanceTransactions subject = new AccountBalanceTransactions(accountValue(), value);
        value.clear();
        assertValue(expected, subject.transactions());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.transactions()).clear());
    }

    private static AccountInfo accountValue() {
        return new AccountInfo(new AccountId(0L, 0L, "", 1001L, null, null), null, 1L, null, List.of(new TokenBalance(new Address(0L, 0L, "", 1001L), 1L)), 1L, 1L, null, null, null, null, null, null, null, null, true, true, null, null, null, null);
    }

    private static List<TransactionInfo> transactionsValue() {
        return List.of(new TransactionInfo(TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L)), new byte[] {1, 2, 3}, null, null, 1L, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null, null, null, List.of(new CustomFeeLimit(null, 1L, null)), 1L, new byte[] {1, 2, 3}, BasicTransactionType.ATOMIC_BATCH, 1, null, null, "value", true, Duration.ofSeconds(1L), ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), List.of(new Transfer(new AccountId(0L, 0L, "", 1001L, null, null), 1L, true)), List.of(new TokenTransfer(new Address(0L, 0L, "", 1001L), new AccountId(0L, 0L, "", 1001L, null, null), 1L, true)), List.of(new NftTransfer(true, new NftTransferParties(null, null, new Address(0L, 0L, "", 1001L)), 1L)), List.of(new StakingRewardTransfer(new AccountId(0L, 0L, "", 1001L, null, null), 1L)), List.of(new AssessedCustomFee(1L, null, List.of(new AccountId(0L, 0L, "", 1001L, null, null)), null))));
    }

    private static AccountBalanceTransactions create() {
        return new AccountBalanceTransactions(accountValue(), transactionsValue());
    }
}
