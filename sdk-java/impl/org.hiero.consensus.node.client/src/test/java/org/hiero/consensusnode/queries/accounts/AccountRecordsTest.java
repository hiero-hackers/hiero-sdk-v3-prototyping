package org.hiero.consensusnode.queries.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.transactions.BasicTransactionStatus;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Record;
import org.hiero.consensusnode.transactions.TransactionsFactory;
import org.hiero.consensusnode.transactions.accounts.AccountAllowanceApproveReceipt;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountRecords`, generated from the specs.
@DisplayName("AccountRecords")
class AccountRecordsTest {

    @Test
    @DisplayName("creates an AccountRecords and returns the values")
    @Tag("testId:consensusnode.queries.accounts.AccountRecords/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId accountId = accountIdValue();
        final List<Record<Receipt>> records = recordsValue();
        final AccountRecords subject = new AccountRecords(accountId, records);
        assertValue(accountId, subject.accountId());
        assertValue(records, subject.records());
    }

    @Test
    @DisplayName("creates an AccountRecords with the default values")
    @Tag("testId:consensusnode.queries.accounts.AccountRecords/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AccountRecords subject = new AccountRecords(accountIdValue());
        assertValue(List.of(), subject.records());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:consensusnode.queries.accounts.AccountRecords/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new AccountRecords((AccountId) null, recordsValue()));
    }

    @Test
    @DisplayName("rejects null for records")
    @Tag("testId:consensusnode.queries.accounts.AccountRecords/records/rejects-null")
    void shouldRejectNullRecords() {
        assertThrows(NullPointerException.class, () -> new AccountRecords(accountIdValue(), (List<Record<Receipt>>) null));
    }

    @Test
    @DisplayName("the constructor copies records; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.queries.accounts.AccountRecords/records/copies-in")
    void shouldCopyRecordsIn() {
        final List<Record<Receipt>> value = new ArrayList<>(recordsValue());
        final List<Record<Receipt>> expected = List.copyOf(value);
        final AccountRecords subject = new AccountRecords(accountIdValue(), value);
        value.clear();
        assertValue(expected, subject.records());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.records()).clear());
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static List<Record<Receipt>> recordsValue() {
        return List.of(new Record<>(TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L)), ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), new AccountAllowanceApproveReceipt(TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L)), BasicTransactionStatus.OK, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        }, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        }), null));
    }

    private static AccountRecords create() {
        return new AccountRecords(accountIdValue(), recordsValue());
    }
}
