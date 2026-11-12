package org.hiero.consensusnode.clpr.transactions;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClprChannelOwnershipProof`, generated from the specs.
@DisplayName("ClprChannelOwnershipProof")
class ClprChannelOwnershipProofTest {

    @Test
    @DisplayName("creates a ClprChannelOwnershipProof and returns the values")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final byte[] channelId = channelIdValue();
        final PublicKey publicKey = publicKeyValue();
        final byte[] signature = signatureValue();
        final ClprChannelOwnershipProof subject = new ClprChannelOwnershipProof(channelId, publicKey, signature);
        assertValue(channelId, subject.channelId());
        assertValue(publicKey, subject.publicKey());
        assertValue(signature, subject.signature());
    }

    @Test
    @DisplayName("rejects null for channelId")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/channelId/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullChannelId() {
        assertThrows(NullPointerException.class, () -> new ClprChannelOwnershipProof((byte[]) null, publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("channelId: accepts the minimum number of elements")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/channelId/accepts-at-minimum-size")
    @Tag("hip:1535")
    void shouldAcceptChannelIdAtMinimumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        final ClprChannelOwnershipProof subject = new ClprChannelOwnershipProof(value, publicKeyValue(), signatureValue());
        assertValue(value, subject.channelId());
    }

    @Test
    @DisplayName("channelId: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/channelId/rejects-below-minimum-size")
    @Tag("hip:1535")
    void shouldRejectChannelIdBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprChannelOwnershipProof(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}, publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("channelId: rejects more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/channelId/rejects-above-maximum-size")
    @Tag("hip:1535")
    void shouldRejectChannelIdAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprChannelOwnershipProof(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33}, publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("the constructor copies channelId; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/channelId/copies-in")
    @Tag("hip:1535")
    void shouldCopyChannelIdIn() {
        final byte[] value = channelIdValue();
        final byte[] expected = value.clone();
        final ClprChannelOwnershipProof subject = new ClprChannelOwnershipProof(value, publicKeyValue(), signatureValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.channelId());
        final byte[] returned = subject.channelId();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.channelId());
    }

    @Test
    @DisplayName("rejects null for publicKey")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/publicKey/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullPublicKey() {
        assertThrows(NullPointerException.class, () -> new ClprChannelOwnershipProof(channelIdValue(), (PublicKey) null, signatureValue()));
    }

    @Test
    @DisplayName("rejects null for signature")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/signature/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullSignature() {
        assertThrows(NullPointerException.class, () -> new ClprChannelOwnershipProof(channelIdValue(), publicKeyValue(), (byte[]) null));
    }

    @Test
    @DisplayName("signature: accepts the minimum number of elements")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/signature/accepts-at-minimum-size")
    @Tag("hip:1535")
    void shouldAcceptSignatureAtMinimumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64};
        final ClprChannelOwnershipProof subject = new ClprChannelOwnershipProof(channelIdValue(), publicKeyValue(), value);
        assertValue(value, subject.signature());
    }

    @Test
    @DisplayName("signature: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/signature/rejects-below-minimum-size")
    @Tag("hip:1535")
    void shouldRejectSignatureBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprChannelOwnershipProof(channelIdValue(), publicKeyValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63}));
    }

    @Test
    @DisplayName("signature: rejects more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/signature/rejects-above-maximum-size")
    @Tag("hip:1535")
    void shouldRejectSignatureAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprChannelOwnershipProof(channelIdValue(), publicKeyValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65}));
    }

    @Test
    @DisplayName("the constructor copies signature; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/signature/copies-in")
    @Tag("hip:1535")
    void shouldCopySignatureIn() {
        final byte[] value = signatureValue();
        final byte[] expected = value.clone();
        final ClprChannelOwnershipProof subject = new ClprChannelOwnershipProof(channelIdValue(), publicKeyValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.signature());
        final byte[] returned = subject.signature();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.signature());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/value-semantics")
    @Tag("hip:1535")
    void shouldCompareByValue() {
        final ClprChannelOwnershipProof subject = create();
        final ClprChannelOwnershipProof other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("create(channelId, privateKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprChannelOwnershipProof/create/call")
    @Tag("hip:1535")
    void shouldCallCreate() {
        assertNotNull(ClprChannelOwnershipProof.create(channelIdValue(), privateKeyValue()));
    }

    private static byte[] channelIdValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
    }

    private static PublicKey publicKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey();
    }

    private static byte[] signatureValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64};
    }

    private static ClprChannelOwnershipProof create() {
        return new ClprChannelOwnershipProof(channelIdValue(), publicKeyValue(), signatureValue());
    }

    private static ClprChannelOwnershipProof defaultSubject() {
        return ClprChannelOwnershipProof.create(new byte[] {(byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31}, KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static PrivateKey privateKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }
}
