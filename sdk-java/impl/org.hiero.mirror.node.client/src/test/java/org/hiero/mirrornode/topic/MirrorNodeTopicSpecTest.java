package org.hiero.mirrornode.topic;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-topic`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-topic")
class MirrorNodeTopicSpecTest {

    /// `mirrornode.topic/chunk-info-keeps-the-initial-transaction-id-the-chunk-number-and-the-total`
    ///
    /// Source: this spec (a message submitted in several chunks carries the chunk's number and the total number of
    /// chunks, and the id of the transaction of its first chunk).
    @Test
    @DisplayName("chunk info keeps the initial transaction id, the chunk number and the total")
    void chunkInfoKeepsTheInitialTransactionIdTheChunkNumberAndTheTotal() {
        // given
        final TransactionId initial = defaultTransactionId();

        // when
        final ChunkInfo chunk = new ChunkInfo(initial, 0, 2, 3, false);

        // then
        assertEquals(initial, chunk.initialTransactionId());
        assertEquals(2, chunk.number());
        assertEquals(3, chunk.total());
        assertFalse(chunk.scheduled());
    }

    private static TransactionId defaultTransactionId() {
        return TransactionId.generateTransactionId(defaultAddress());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }
}
