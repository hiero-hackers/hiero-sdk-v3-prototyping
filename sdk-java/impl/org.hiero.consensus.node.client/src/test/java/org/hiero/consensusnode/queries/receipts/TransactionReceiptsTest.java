package org.hiero.consensusnode.queries.receipts;

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
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TransactionReceipts`, generated from the specs.
@DisplayName("TransactionReceipts")
class TransactionReceiptsTest {

    @Test
    @DisplayName("creates a TransactionReceipts and returns the values")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/create")
    void shouldCreateAndReturnTheValues() {
        final UntypedReceipt receipt = receiptValue();
        final List<UntypedReceipt> duplicates = duplicatesValue();
        final List<UntypedReceipt> children = childrenValue();
        final TransactionReceipts subject = new TransactionReceipts(receipt, duplicates, children);
        assertValue(receipt, subject.receipt());
        assertValue(duplicates, subject.duplicates());
        assertValue(children, subject.children());
    }

    @Test
    @DisplayName("creates a TransactionReceipts with the default values")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TransactionReceipts subject = new TransactionReceipts(receiptValue());
        assertValue(List.of(), subject.duplicates());
        assertValue(List.of(), subject.children());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final UntypedReceipt receipt = receiptValue();
        final List<UntypedReceipt> duplicates = duplicatesValue();
        final List<UntypedReceipt> children = childrenValue();
        final TransactionReceipts subject = TransactionReceipts.builder()
                .receipt(receipt)
                .duplicates(duplicates)
                .children(children)
                .build();
        assertValue(receipt, subject.receipt());
        assertValue(duplicates, subject.duplicates());
        assertValue(children, subject.children());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TransactionReceipts subject = TransactionReceipts.builder()
                .receipt(receiptValue())
                .build();
        assertValue(List.of(), subject.duplicates());
        assertValue(List.of(), subject.children());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TransactionReceipts.builder().build());
    }

    @Test
    @DisplayName("rejects null for receipt")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/receipt/rejects-null")
    void shouldRejectNullReceipt() {
        assertThrows(NullPointerException.class, () -> new TransactionReceipts((UntypedReceipt) null, duplicatesValue(), childrenValue()));
    }

    @Test
    @DisplayName("rejects null for duplicates")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/duplicates/rejects-null")
    void shouldRejectNullDuplicates() {
        assertThrows(NullPointerException.class, () -> new TransactionReceipts(receiptValue(), (List<UntypedReceipt>) null, childrenValue()));
    }

    @Test
    @DisplayName("the constructor copies duplicates; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/duplicates/copies-in")
    void shouldCopyDuplicatesIn() {
        final List<UntypedReceipt> value = new ArrayList<>(duplicatesValue());
        final List<UntypedReceipt> expected = List.copyOf(value);
        final TransactionReceipts subject = new TransactionReceipts(receiptValue(), value, childrenValue());
        value.clear();
        assertValue(expected, subject.duplicates());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.duplicates()).clear());
    }

    @Test
    @DisplayName("rejects null for children")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/children/rejects-null")
    void shouldRejectNullChildren() {
        assertThrows(NullPointerException.class, () -> new TransactionReceipts(receiptValue(), duplicatesValue(), (List<UntypedReceipt>) null));
    }

    @Test
    @DisplayName("the constructor copies children; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceipts/children/copies-in")
    void shouldCopyChildrenIn() {
        final List<UntypedReceipt> value = new ArrayList<>(childrenValue());
        final List<UntypedReceipt> expected = List.copyOf(value);
        final TransactionReceipts subject = new TransactionReceipts(receiptValue(), duplicatesValue(), value);
        value.clear();
        assertValue(expected, subject.children());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.children()).clear());
    }

    private static UntypedReceipt receiptValue() {
        return new UntypedReceipt(TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L)), BasicTransactionStatus.OK, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        }, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        }, null, null, null, null, null, null, null, null, null, null, null, List.of(), null, null, null);
    }

    private static List<UntypedReceipt> duplicatesValue() {
        return List.of(new UntypedReceipt(TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L)), BasicTransactionStatus.OK, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        }, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        }, null, null, null, null, null, null, null, null, null, null, null, List.of(), null, null, null));
    }

    private static List<UntypedReceipt> childrenValue() {
        return List.of(new UntypedReceipt(TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L)), BasicTransactionStatus.OK, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        }, new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        }, null, null, null, null, null, null, null, null, null, null, null, List.of(), null, null, null));
    }

    private static TransactionReceipts create() {
        return new TransactionReceipts(receiptValue(), duplicatesValue(), childrenValue());
    }
}
