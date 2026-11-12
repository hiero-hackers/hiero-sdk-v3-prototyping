package org.hiero.consensusnode.transactions.spi;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.Set;
import org.hiero.consensusnode.client.StatusConflictException;
import org.hiero.consensusnode.client.UnknownStatusException;
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

    @Test
    @DisplayName("getAllTransactionStatusSupports() can be called and returns a value (only StatusConflictException may occur)")
    @Tag("testId:consensusnode.transactions.spi/getAllTransactionStatusSupports/call")
    void shouldCallGetAllTransactionStatusSupports() throws Exception {
        try {
            assertNotNull(SpiFactory.getAllTransactionStatusSupports());
        } catch (final Exception e) {
            if (!(e instanceof StatusConflictException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getTransactionStatus(code) can be called and returns a value (only UnknownStatusException, StatusConflictException may occur)")
    @Tag("testId:consensusnode.transactions.spi/getTransactionStatus/call")
    void shouldCallGetTransactionStatus() throws Exception {
        try {
            assertNotNull(SpiFactory.getTransactionStatus(codeValue()));
        } catch (final Exception e) {
            if (!(e instanceof UnknownStatusException || e instanceof StatusConflictException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getTransactionStatus: code accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.transactions.spi/getTransactionStatus/code/accepts-at-minimum")
    void shouldAcceptCodeAtMinimumInGetTransactionStatus() throws Exception {
        try {
            assertNotNull(SpiFactory.getTransactionStatus(Integer.MIN_VALUE));
        } catch (final Exception e) {
            if (!(e instanceof UnknownStatusException || e instanceof StatusConflictException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getTransactionStatus: code accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.transactions.spi/getTransactionStatus/code/accepts-at-maximum")
    void shouldAcceptCodeAtMaximumInGetTransactionStatus() throws Exception {
        try {
            assertNotNull(SpiFactory.getTransactionStatus(2147483647));
        } catch (final Exception e) {
            if (!(e instanceof UnknownStatusException || e instanceof StatusConflictException)) {
                throw e;
            }
        }
    }

    private static Class<?> transactionTypeValue() {
        return String.class;
    }

    private static int codeValue() {
        return 1;
    }
}
