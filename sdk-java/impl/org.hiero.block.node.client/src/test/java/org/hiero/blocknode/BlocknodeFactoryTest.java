package org.hiero.blocknode;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.BlockNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlocknodeFactory`, generated from the specs.
@DisplayName("BlocknodeFactory")
class BlocknodeFactoryTest {

    @Test
    @DisplayName("createBlockNodeClient(blockNode) can be called and returns a value")
    @Tag("testId:blocknode/createBlockNodeClient/call")
    void shouldCallCreateBlockNodeClient() {
        assertNotNull(BlocknodeFactory.createBlockNodeClient(blockNodeValue()));
    }

    @Test
    @DisplayName("createBlockNodeClient rejects null for blockNode")
    @Tag("testId:blocknode/createBlockNodeClient/blockNode/rejects-null")
    void shouldRejectNullBlockNodeInCreateBlockNodeClient() {
        assertThrows(NullPointerException.class, () -> BlocknodeFactory.createBlockNodeClient((BlockNode) null));
    }

    private static BlockNode blockNodeValue() {
        return new BlockNode("127.0.0.1", 40840, false);
    }
}
