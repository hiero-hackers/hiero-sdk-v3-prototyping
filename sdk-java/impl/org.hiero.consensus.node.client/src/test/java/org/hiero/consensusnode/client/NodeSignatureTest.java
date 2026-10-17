package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NodeSignature`, generated from the specs.
@DisplayName("NodeSignature")
class NodeSignatureTest {

    @Test
    @DisplayName("creates a NodeSignature and returns the values")
    @Tag("testId:consensusnode.client.NodeSignature/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId node = nodeValue();
        final PublicKey publicKey = publicKeyValue();
        final byte[] signature = signatureValue();
        final NodeSignature subject = new NodeSignature(node, publicKey, signature);
        assertValue(node, subject.node());
        assertValue(publicKey, subject.publicKey());
        assertValue(signature, subject.signature());
    }

    @Test
    @DisplayName("rejects null for node")
    @Tag("testId:consensusnode.client.NodeSignature/node/rejects-null")
    void shouldRejectNullNode() {
        assertThrows(NullPointerException.class, () -> new NodeSignature((AccountId) null, publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("rejects null for publicKey")
    @Tag("testId:consensusnode.client.NodeSignature/publicKey/rejects-null")
    void shouldRejectNullPublicKey() {
        assertThrows(NullPointerException.class, () -> new NodeSignature(nodeValue(), (PublicKey) null, signatureValue()));
    }

    @Test
    @DisplayName("rejects null for signature")
    @Tag("testId:consensusnode.client.NodeSignature/signature/rejects-null")
    void shouldRejectNullSignature() {
        assertThrows(NullPointerException.class, () -> new NodeSignature(nodeValue(), publicKeyValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies signature; the accessor returns a copy")
    @Tag("testId:consensusnode.client.NodeSignature/signature/copies-in")
    void shouldCopySignatureIn() {
        final byte[] value = signatureValue();
        final byte[] expected = value.clone();
        final NodeSignature subject = new NodeSignature(nodeValue(), publicKeyValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.signature());
        final byte[] returned = subject.signature();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.signature());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.client.NodeSignature/value-semantics")
    void shouldCompareByValue() {
        final NodeSignature subject = create();
        final NodeSignature other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId nodeValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static PublicKey publicKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey();
    }

    private static byte[] signatureValue() {
        return new byte[] {1, 2, 3};
    }

    private static NodeSignature create() {
        return new NodeSignature(nodeValue(), publicKeyValue(), signatureValue());
    }
}
