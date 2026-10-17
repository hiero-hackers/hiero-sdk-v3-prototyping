package org.hiero.mirrornode.transaction;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.MirrorNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TransactionFactory`, generated from the specs.
@DisplayName("TransactionFactory")
class TransactionFactoryTest {

    @Test
    @DisplayName("createRepository(mirrorNode) can be called and returns a value")
    @Tag("testId:mirrornode.transaction/createRepository/call")
    void shouldCallCreateRepository() {
        assertNotNull(TransactionFactory.createRepository(mirrorNodeValue()));
    }

    @Test
    @DisplayName("createRepository rejects null for mirrorNode")
    @Tag("testId:mirrornode.transaction/createRepository/mirrorNode/rejects-null")
    void shouldRejectNullMirrorNodeInCreateRepository() {
        assertThrows(NullPointerException.class, () -> TransactionFactory.createRepository((MirrorNode) null));
    }

    private static MirrorNode mirrorNodeValue() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}
