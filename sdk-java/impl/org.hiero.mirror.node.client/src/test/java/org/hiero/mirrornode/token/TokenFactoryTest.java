package org.hiero.mirrornode.token;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.MirrorNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `TokenFactory`, generated from the specs.
@DisplayName("TokenFactory")
class TokenFactoryTest {

    @Test
    @DisplayName("createRepository(mirrorNode) can be called and returns a value")
    void shouldCallCreateRepository() {
        assertNotNull(TokenFactory.createRepository(mirrorNodeValue()));
    }

    @Test
    @DisplayName("createRepository rejects null for mirrorNode")
    void shouldRejectNullMirrorNodeInCreateRepository() {
        assertThrows(NullPointerException.class, () -> TokenFactory.createRepository((MirrorNode) null));
    }

    private static MirrorNode mirrorNodeValue() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}
