package org.hiero.consensusnode.transactions.spi;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `SpiFactory`, generated from the specs.
@DisplayName("SpiFactory")
class SpiFactoryTest {

    @Test
    @DisplayName("getTransactionSupport(transactionType) can be called and returns a value (only NoSuchElementException may occur)")
    @Tag("testId:consensusnode.transactions.spi/getTransactionSupport/call")
    void shouldCallGetTransactionSupport() throws Exception {
        try {
            assertNotNull(SpiFactory.getTransactionSupport(transactionTypeValue()));
        } catch (final Exception e) {
            if (!(e instanceof NoSuchElementException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getTransactionSupport rejects null for transactionType")
    @Tag("testId:consensusnode.transactions.spi/getTransactionSupport/transactionType/rejects-null")
    void shouldRejectNullTransactionTypeInGetTransactionSupport() {
        assertThrows(NullPointerException.class, () -> SpiFactory.getTransactionSupport((Class<?>) null));
    }

    @Test
    @DisplayName("getAllTransactionSupports() can be called and returns a value")
    @Tag("testId:consensusnode.transactions.spi/getAllTransactionSupports/call")
    void shouldCallGetAllTransactionSupports() {
        assertNotNull(SpiFactory.getAllTransactionSupports());
    }

    private static Class<?> transactionTypeValue() {
        return String.class;
    }
}
