package org.hiero.consensusnode.transactions.spi;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;
import org.hiero.consensusnode.client.UnknownStatusException;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.Receipt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-spi`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-spi")
class TransactionsSpiSpecTest {

    /// `consensusnode.transactions.spi/every-registered-transaction-support-is-found-by-its-transaction-type`
    ///
    /// Source: this spec (getAllTransactionSupports returns every registered support, getTransactionSupport the one of a
    /// type).
    @Test
    @DisplayName("every registered transaction support is found by its transaction type")
    @Tag("testId:consensusnode.transactions.spi/every-registered-transaction-support-is-found-by-its-transaction-type")
    void everyRegisteredTransactionSupportIsFoundByItsTransactionType() {
        for (final TransactionSupport<?, ?> support : SpiFactory.getAllTransactionSupports()) {
            // when
            final TransactionSupport<?, ?> found = SpiFactory.getTransactionSupport(support.getTransactionType());

            // then
            assertEquals(support.getTransactionType(), found.getTransactionType());
        }
    }

    /// `consensusnode.transactions.spi/a-type-without-a-registered-transaction-support-is-rejected`
    ///
    /// Source: this spec (getTransactionSupport throws if no support is registered for the type). A receipt is no
    /// transaction, so no support is registered for it.
    @Test
    @DisplayName("a type without a registered transaction support is rejected")
    @Tag("testId:consensusnode.transactions.spi/a-type-without-a-registered-transaction-support-is-rejected")
    void aTypeWithoutARegisteredTransactionSupportIsRejected() {
        // when / then
        assertThrows(NoSuchElementException.class, () -> SpiFactory.getTransactionSupport(Receipt.class));
    }

    /// `consensusnode.transactions.spi/a-code-of-the-protocol-is-its-basic-transaction-status`
    ///
    /// Source: this spec (the SDK registers the support of BasicTransactionStatus, so getTransactionStatus returns the
    /// BasicTransactionStatus of a code of the protocol); HAPI response_code.proto
    /// (SUCCESS is 22, INVALID_SIGNATURE 7).
    @Test
    @DisplayName("a code of the protocol is its basic transaction status")
    @Tag("testId:consensusnode.transactions.spi/a-code-of-the-protocol-is-its-basic-transaction-status")
    void aCodeOfTheProtocolIsItsBasicTransactionStatus() {
        // given
        final TransactionStatus success = BasicTransactionStatus.SUCCESS;
        final TransactionStatus invalidSignature = BasicTransactionStatus.INVALID_SIGNATURE;

        // when / then
        assertEquals(success, SpiFactory.getTransactionStatus(22));
        assertEquals(invalidSignature, SpiFactory.getTransactionStatus(7));
    }

    /// `consensusnode.transactions.spi/a-code-no-one-names-is-rejected`
    ///
    /// Source: this spec (a code that no registered TransactionStatusSupport names is an
    /// error); HAPI response_code.proto (no code is negative, none is above 1,000).
    @Test
    @DisplayName("a code no one names is rejected")
    @Tag("testId:consensusnode.transactions.spi/a-code-no-one-names-is-rejected")
    void aCodeNoOneNamesIsRejected() {
        for (final long code : List.of(-1L, 1000000L)) {
            // when / then
            assertThrows(UnknownStatusException.class, () -> SpiFactory.getTransactionStatus(checked(integer(code), "int32").intValueExact()));
        }
    }

    /// `consensusnode.transactions.spi/every-status-of-a-registered-support-is-found-by-its-code`
    ///
    /// Source: this spec (every status of a registered support is found by its code).
    @Test
    @DisplayName("every status of a registered support is found by its code")
    @Tag("testId:consensusnode.transactions.spi/every-status-of-a-registered-support-is-found-by-its-code")
    void everyStatusOfARegisteredSupportIsFoundByItsCode() {
        for (final TransactionStatusSupport support : SpiFactory.getAllTransactionStatusSupports()) {
            for (final TransactionStatus status : support.getStatuses()) {
                // when / then
                assertEquals(status, SpiFactory.getTransactionStatus(status.code()));
            }
        }
    }
}
