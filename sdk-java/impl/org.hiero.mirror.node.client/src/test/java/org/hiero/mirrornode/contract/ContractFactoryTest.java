package org.hiero.mirrornode.contract;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.MirrorNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractFactory`, generated from the specs.
@DisplayName("ContractFactory")
class ContractFactoryTest {

    @Test
    @DisplayName("createRepository(mirrorNode) can be called and returns a value")
    @Tag("testId:mirrornode.contract/createRepository/call")
    void shouldCallCreateRepository() {
        assertNotNull(ContractFactory.createRepository(mirrorNodeValue()));
    }

    @Test
    @DisplayName("createRepository rejects null for mirrorNode")
    @Tag("testId:mirrornode.contract/createRepository/mirrorNode/rejects-null")
    void shouldRejectNullMirrorNodeInCreateRepository() {
        assertThrows(NullPointerException.class, () -> ContractFactory.createRepository((MirrorNode) null));
    }

    private static MirrorNode mirrorNodeValue() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}
