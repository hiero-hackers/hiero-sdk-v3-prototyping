package org.hiero.consensusnode.transactions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `PackedTransaction`, generated from the specs.
@DisplayName("PackedTransaction")
class PackedTransactionTest {

    @Test
    @DisplayName("fromBytes(bytes) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:consensusnode.transactions.PackedTransaction/fromBytes/call")
    void shouldCallFromBytes() throws Exception {
        try {
            assertNotNull(PackedTransaction.fromBytes(bytesValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static byte[] bytesValue() {
        return new byte[] {1, 2, 3};
    }
}
