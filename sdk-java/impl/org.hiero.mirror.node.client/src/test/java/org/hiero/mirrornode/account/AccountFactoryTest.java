package org.hiero.mirrornode.account;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.MirrorNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountFactory`, generated from the specs.
@DisplayName("AccountFactory")
class AccountFactoryTest {

    @Test
    @DisplayName("createRepository(mirrorNode) can be called and returns a value")
    @Tag("testId:mirrornode.account/createRepository/call")
    void shouldCallCreateRepository() {
        assertNotNull(AccountFactory.createRepository(mirrorNodeValue()));
    }

    @Test
    @DisplayName("createRepository rejects null for mirrorNode")
    @Tag("testId:mirrornode.account/createRepository/mirrorNode/rejects-null")
    void shouldRejectNullMirrorNodeInCreateRepository() {
        assertThrows(NullPointerException.class, () -> AccountFactory.createRepository((MirrorNode) null));
    }

    private static MirrorNode mirrorNodeValue() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}
