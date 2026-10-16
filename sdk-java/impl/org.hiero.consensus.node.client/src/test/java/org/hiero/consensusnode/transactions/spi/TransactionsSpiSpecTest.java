package org.hiero.consensusnode.transactions.spi;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.hiero.consensusnode.transactions.Receipt;
import org.junit.jupiter.api.DisplayName;
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
    void aTypeWithoutARegisteredTransactionSupportIsRejected() {
        // when / then
        assertThrows(NoSuchElementException.class, () -> SpiFactory.getTransactionSupport(Receipt.class));
    }
}
