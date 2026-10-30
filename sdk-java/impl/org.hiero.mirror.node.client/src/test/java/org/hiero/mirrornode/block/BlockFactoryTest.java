package org.hiero.mirrornode.block;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.MirrorNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlockFactory`, generated from the specs.
@DisplayName("BlockFactory")
class BlockFactoryTest {

    @Test
    @DisplayName("createRepository(mirrorNode) can be called and returns a value")
    @Tag("testId:mirrornode.block/createRepository/call")
    void shouldCallCreateRepository() {
        assertNotNull(BlockFactory.createRepository(mirrorNodeValue()));
    }

    @Test
    @DisplayName("createRepository rejects null for mirrorNode")
    @Tag("testId:mirrornode.block/createRepository/mirrorNode/rejects-null")
    void shouldRejectNullMirrorNodeInCreateRepository() {
        assertThrows(NullPointerException.class, () -> BlockFactory.createRepository((MirrorNode) null));
    }

    private static MirrorNode mirrorNodeValue() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}
