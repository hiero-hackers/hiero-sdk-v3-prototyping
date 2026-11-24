package org.hiero.consensusnode.client;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TransactionSigner`, generated from the specs.
@DisplayName("TransactionSigner")
class TransactionSignerTest {

    @Test
    @DisplayName("of(privateKey) can be called and returns a value")
    @Tag("testId:consensusnode.client.TransactionSigner/of/call")
    void shouldCallOf() {
        assertNotNull(TransactionSigner.of(privateKeyValue()));
    }

    private static PrivateKey privateKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }
}
