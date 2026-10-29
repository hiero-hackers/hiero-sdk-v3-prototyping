package org.hiero.consensusnode.transactions;

import org.hiero.consensusnode.status.BasicTransactionStatus;
import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.transactions.accounts.AccountAllowanceApproveReceipt;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Record`, generated from the specs.
@DisplayName("Record")
class RecordTest {

    @Test
    @DisplayName("creates a Record and returns the values")
    @Tag("testId:consensusnode.transactions.Record/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final Receipt receipt = receiptValue();
        final @Nullable ZonedDateTime parentConsensusTimestamp = parentConsensusTimestampValue();
        final Record<Receipt> subject = new Record<>(transactionId, consensusTimestamp, receipt, parentConsensusTimestamp);
        assertValue(transactionId, subject.transactionId());
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(receipt, subject.receipt());
        assertValue(parentConsensusTimestamp, subject.parentConsensusTimestamp());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.transactions.Record/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new Record<>((TransactionId) null, consensusTimestampValue(), receiptValue(), parentConsensusTimestampValue()));
    }

    @Test
    @DisplayName("rejects null for consensusTimestamp")
    @Tag("testId:consensusnode.transactions.Record/consensusTimestamp/rejects-null")
    void shouldRejectNullConsensusTimestamp() {
        assertThrows(NullPointerException.class, () -> new Record<>(transactionIdValue(), (ZonedDateTime) null, receiptValue(), parentConsensusTimestampValue()));
    }

    @Test
    @DisplayName("rejects null for receipt")
    @Tag("testId:consensusnode.transactions.Record/receipt/rejects-null")
    void shouldRejectNullReceipt() {
        assertThrows(NullPointerException.class, () -> new Record<>(transactionIdValue(), consensusTimestampValue(), (Receipt) null, parentConsensusTimestampValue()));
    }

    private static TransactionId transactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static ZonedDateTime consensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static Receipt receiptValue() {
        return new AccountAllowanceApproveReceipt(TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L)), BasicTransactionStatus.OK, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        }, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        });
    }

    private static ZonedDateTime parentConsensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static Record<Receipt> create() {
        return new Record<>(transactionIdValue(), consensusTimestampValue(), receiptValue(), parentConsensusTimestampValue());
    }
}
