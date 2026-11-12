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

/// Tests of `ClprConnectorOwnershipProof`, generated from the specs.
@DisplayName("ClprConnectorOwnershipProof")
class ClprConnectorOwnershipProofTest {

    @Test
    @DisplayName("creates a ClprConnectorOwnershipProof and returns the values")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final byte[] channelId = channelIdValue();
        final byte[] salt = saltValue();
        final byte[] connectorId = connectorIdValue();
        final PublicKey publicKey = publicKeyValue();
        final byte[] signature = signatureValue();
        final ClprConnectorOwnershipProof subject = new ClprConnectorOwnershipProof(channelId, salt, connectorId, publicKey, signature);
        assertValue(channelId, subject.channelId());
        assertValue(salt, subject.salt());
        assertValue(connectorId, subject.connectorId());
        assertValue(publicKey, subject.publicKey());
        assertValue(signature, subject.signature());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/builder/builds")
    @Tag("hip:1535")
    void shouldBuildTheValuesOfTheConstructor() {
        final byte[] channelId = channelIdValue();
        final byte[] salt = saltValue();
        final byte[] connectorId = connectorIdValue();
        final PublicKey publicKey = publicKeyValue();
        final byte[] signature = signatureValue();
        final ClprConnectorOwnershipProof subject = ClprConnectorOwnershipProof.builder()
                .channelId(channelId)
                .salt(salt)
                .connectorId(connectorId)
                .publicKey(publicKey)
                .signature(signature)
                .build();
        assertValue(channelId, subject.channelId());
        assertValue(salt, subject.salt());
        assertValue(connectorId, subject.connectorId());
        assertValue(publicKey, subject.publicKey());
        assertValue(signature, subject.signature());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/builder/optional-values")
    @Tag("hip:1535")
    void shouldBuildWithoutTheOptionalValues() {
        final ClprConnectorOwnershipProof subject = ClprConnectorOwnershipProof.builder()
                .channelId(channelIdValue())
                .salt(saltValue())
                .connectorId(connectorIdValue())
                .publicKey(publicKeyValue())
                .signature(signatureValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/builder/rejects-missing-value")
    @Tag("hip:1535")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ClprConnectorOwnershipProof.builder().build());
    }

    @Test
    @DisplayName("rejects null for channelId")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/channelId/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullChannelId() {
        assertThrows(NullPointerException.class, () -> new ClprConnectorOwnershipProof((byte[]) null, saltValue(), connectorIdValue(), publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("channelId: accepts the minimum number of elements")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/channelId/accepts-at-minimum-size")
    @Tag("hip:1535")
    void shouldAcceptChannelIdAtMinimumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        final ClprConnectorOwnershipProof subject = new ClprConnectorOwnershipProof(value, saltValue(), connectorIdValue(), publicKeyValue(), signatureValue());
        assertValue(value, subject.channelId());
    }

    @Test
    @DisplayName("channelId: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/channelId/rejects-below-minimum-size")
    @Tag("hip:1535")
    void shouldRejectChannelIdBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprConnectorOwnershipProof(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}, saltValue(), connectorIdValue(), publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("channelId: rejects more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/channelId/rejects-above-maximum-size")
    @Tag("hip:1535")
    void shouldRejectChannelIdAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprConnectorOwnershipProof(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33}, saltValue(), connectorIdValue(), publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("the constructor copies channelId; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/channelId/copies-in")
    @Tag("hip:1535")
    void shouldCopyChannelIdIn() {
        final byte[] value = channelIdValue();
        final byte[] expected = value.clone();
        final ClprConnectorOwnershipProof subject = new ClprConnectorOwnershipProof(value, saltValue(), connectorIdValue(), publicKeyValue(), signatureValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.channelId());
        final byte[] returned = subject.channelId();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.channelId());
    }

    @Test
    @DisplayName("rejects null for salt")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/salt/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullSalt() {
        assertThrows(NullPointerException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), (byte[]) null, connectorIdValue(), publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("salt: accepts the minimum number of elements")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/salt/accepts-at-minimum-size")
    @Tag("hip:1535")
    void shouldAcceptSaltAtMinimumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        final ClprConnectorOwnershipProof subject = new ClprConnectorOwnershipProof(channelIdValue(), value, connectorIdValue(), publicKeyValue(), signatureValue());
        assertValue(value, subject.salt());
    }

    @Test
    @DisplayName("salt: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/salt/rejects-below-minimum-size")
    @Tag("hip:1535")
    void shouldRejectSaltBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}, connectorIdValue(), publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("salt: rejects more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/salt/rejects-above-maximum-size")
    @Tag("hip:1535")
    void shouldRejectSaltAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33}, connectorIdValue(), publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("the constructor copies salt; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/salt/copies-in")
    @Tag("hip:1535")
    void shouldCopySaltIn() {
        final byte[] value = saltValue();
        final byte[] expected = value.clone();
        final ClprConnectorOwnershipProof subject = new ClprConnectorOwnershipProof(channelIdValue(), value, connectorIdValue(), publicKeyValue(), signatureValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.salt());
        final byte[] returned = subject.salt();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.salt());
    }

    @Test
    @DisplayName("rejects null for connectorId")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/connectorId/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullConnectorId() {
        assertThrows(NullPointerException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), (byte[]) null, publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("connectorId: accepts the minimum number of elements")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/connectorId/accepts-at-minimum-size")
    @Tag("hip:1535")
    void shouldAcceptConnectorIdAtMinimumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        final ClprConnectorOwnershipProof subject = new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), value, publicKeyValue(), signatureValue());
        assertValue(value, subject.connectorId());
    }

    @Test
    @DisplayName("connectorId: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/connectorId/rejects-below-minimum-size")
    @Tag("hip:1535")
    void shouldRejectConnectorIdBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}, publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("connectorId: rejects more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/connectorId/rejects-above-maximum-size")
    @Tag("hip:1535")
    void shouldRejectConnectorIdAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33}, publicKeyValue(), signatureValue()));
    }

    @Test
    @DisplayName("the constructor copies connectorId; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/connectorId/copies-in")
    @Tag("hip:1535")
    void shouldCopyConnectorIdIn() {
        final byte[] value = connectorIdValue();
        final byte[] expected = value.clone();
        final ClprConnectorOwnershipProof subject = new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), value, publicKeyValue(), signatureValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.connectorId());
        final byte[] returned = subject.connectorId();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.connectorId());
    }

    @Test
    @DisplayName("rejects null for publicKey")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/publicKey/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullPublicKey() {
        assertThrows(NullPointerException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), connectorIdValue(), (PublicKey) null, signatureValue()));
    }

    @Test
    @DisplayName("rejects null for signature")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/signature/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullSignature() {
        assertThrows(NullPointerException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), connectorIdValue(), publicKeyValue(), (byte[]) null));
    }

    @Test
    @DisplayName("signature: accepts the minimum number of elements")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/signature/accepts-at-minimum-size")
    @Tag("hip:1535")
    void shouldAcceptSignatureAtMinimumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64};
        final ClprConnectorOwnershipProof subject = new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), connectorIdValue(), publicKeyValue(), value);
        assertValue(value, subject.signature());
    }

    @Test
    @DisplayName("signature: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/signature/rejects-below-minimum-size")
    @Tag("hip:1535")
    void shouldRejectSignatureBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), connectorIdValue(), publicKeyValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63}));
    }

    @Test
    @DisplayName("signature: rejects more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/signature/rejects-above-maximum-size")
    @Tag("hip:1535")
    void shouldRejectSignatureAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), connectorIdValue(), publicKeyValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65}));
    }

    @Test
    @DisplayName("the constructor copies signature; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/signature/copies-in")
    @Tag("hip:1535")
    void shouldCopySignatureIn() {
        final byte[] value = signatureValue();
        final byte[] expected = value.clone();
        final ClprConnectorOwnershipProof subject = new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), connectorIdValue(), publicKeyValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.signature());
        final byte[] returned = subject.signature();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.signature());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/value-semantics")
    @Tag("hip:1535")
    void shouldCompareByValue() {
        final ClprConnectorOwnershipProof subject = create();
        final ClprConnectorOwnershipProof other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("create(channelId, salt, privateKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprConnectorOwnershipProof/create/call")
    @Tag("hip:1535")
    void shouldCallCreate() {
        assertNotNull(ClprConnectorOwnershipProof.create(channelIdValue(), saltValue(), privateKeyValue()));
    }

    private static byte[] channelIdValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
    }

    private static byte[] saltValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
    }

    private static byte[] connectorIdValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
    }

    private static PublicKey publicKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey();
    }

    private static byte[] signatureValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64};
    }

    private static ClprConnectorOwnershipProof create() {
        return new ClprConnectorOwnershipProof(channelIdValue(), saltValue(), connectorIdValue(), publicKeyValue(), signatureValue());
    }

    private static ClprConnectorOwnershipProof defaultSubject() {
        return ClprConnectorOwnershipProof.create(new byte[] {(byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31}, new byte[] {(byte) 32, (byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, (byte) 61, (byte) 62, (byte) 63}, KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static PrivateKey privateKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }
}
