package org.hiero.keys;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `keys`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("keys")
class KeysSpecTest {

    /// `keys/a-private-key-imported-from-hex-has-these-raw-bytes`
    @Test
    @DisplayName("a private key imported from hex has these raw bytes")
    void aPrivateKeyImportedFromHexHasTheseRawBytes() {
        // given
        final String hexText = "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c";

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, hexText);

        // then
        assertEquals(hex(hexText), key.bytes());
        assertEquals(KeyAlgorithm.ED25519, key.algorithm());
        assertEquals(KeyType.PRIVATE, key.type());
    }

    /// `keys/the-default-private-key-encodes-as-pkcs-8-der`
    @Test
    @DisplayName("the default private key encodes as PKCS#8 DER")
    void theDefaultPrivateKeyEncodesAsPKCS8DER() {
        // given
        final PrivateKey key = defaultPrivateKey();

        // when
        final byte[] der = key.toBytes(KeyFormat.PKCS8_WITH_DER);

        // then
        assertEquals(hex("302e020100300506032b657004220420d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"), der);
    }

    /// `keys/the-public-key-of-an-ed25519-key-is-a-public-ed25519-key-of-32-bytes`
    @Test
    @DisplayName("the public key of an Ed25519 key is a public Ed25519 key of 32 bytes")
    void thePublicKeyOfAnEd25519KeyIsAPublicEd25519KeyOf32Bytes() {
        // given
        final PrivateKey privateKey = defaultPrivateKey();

        // when
        final PublicKey publicKey = privateKey.createPublicKey();

        // then
        assertEquals(KeyType.PUBLIC, publicKey.type());
        assertEquals(KeyAlgorithm.ED25519, publicKey.algorithm());
        assertEquals(32L, length(publicKey.bytes()));
        assertEquals(publicKey, KeysFactory.createPublicKey(KeyAlgorithm.ED25519, publicKey.bytes()));
    }

    /// `keys/a-signature-verifies-with-the-public-key-and-only-for-its-message`
    @Test
    @DisplayName("a signature verifies with the public key and only for its message")
    void aSignatureVerifiesWithThePublicKeyAndOnlyForItsMessage() {
        // given
        final byte[] message = utf8("hello");

        // when
        final byte[] signature = defaultPrivateKey().sign(message);

        // then: RFC 8032 signatures may be randomised by the platform (Apple CryptoKit does), so only verification is checked
        assertEquals(64L, length(signature));
        assertTrue(defaultPublicKey().verify(message, signature));
        assertFalse(defaultPublicKey().verify(utf8("hello!"), signature));
    }

    /// `keys/a-key-survives-every-format-that-supports-its-type`
    @Test
    @DisplayName("a key survives every format that supports its type")
    void aKeySurvivesEveryFormatThatSupportsItsType() {
        // given
        final PrivateKey key = defaultPrivateKey();
        for (final KeyFormat format : List.of(KeyFormat.PKCS8_WITH_DER, KeyFormat.SPKI_WITH_DER, KeyFormat.PKCS8_WITH_PEM, KeyFormat.SPKI_WITH_PEM)) {
            if (!format.supportsType(KeyType.PRIVATE)) {
                // when / then: a format for public keys rejects a private key
                assertThrows(IllegalArgumentException.class, () -> key.toBytes(format));
            } else if (format.encoding() == KeyEncoding.DER) {
                // when
                final byte[] der = key.toBytes(format);

                // then
                assertEquals(key, KeysFactory.createPrivateKey(format, der));
            } else {
                // when
                final String pem = key.toString(format);

                // then
                assertTrue(startsWith(pem, "-----BEGIN PRIVATE KEY-----"));
                assertEquals(key, KeysFactory.createPrivateKey(format, pem));
            }
        }
    }

    /// `keys/a-hex-key-of-the-wrong-length-is-rejected`
    @Test
    @DisplayName("a hex key of the wrong length is rejected")
    void aHexKeyOfTheWrongLengthIsRejected() {
        // given
        final String tooShort = "d3671a1e";
        final String tooLong = repeat("ab", 33L);
        for (final String hexText : List.of(tooShort, tooLong)) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, hexText));
        }
    }

    /// `keys/an-ed25519-pkcs-8-der-key-derives-the-reference-public-key`
    ///
    /// Source: the reference vectors of hashgraph/hedera-sdk-reference#93, read with the same results by the
    /// Java, JavaScript, Rust, C++ and Swift SDKs; verified with OpenSSL.
    @Test
    @DisplayName("an Ed25519 PKCS#8 DER key derives the reference public key")
    void anEd25519PKCS8DERKeyDerivesTheReferencePublicKey() {
        // given
        final byte[] der = hex("302e020100300506032b657004220420feb858a4a69600a5eef2d9c76f7fb84fc0b6627f29e0ab17e160f640c267d404");

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, der);

        // then
        assertEquals(KeyAlgorithm.ED25519, key.algorithm());
        assertEquals(hex("feb858a4a69600a5eef2d9c76f7fb84fc0b6627f29e0ab17e160f640c267d404"), key.bytes());
        assertEquals(hex("8ccd31b53d1835b467aac795dab19b274dd3b37e3daf12fcec6bc02bac87b53d"), key.createPublicKey().bytes());
    }

    /// `keys/an-ed25519-pkcs-8-pem-key-derives-the-reference-public-key`
    ///
    /// Source: hedera-sdk-reference#93 (Java, JavaScript, Rust, C++, Swift); verified with OpenSSL.
    @Test
    @DisplayName("an Ed25519 PKCS#8 PEM key derives the reference public key")
    void anEd25519PKCS8PEMKeyDerivesTheReferencePublicKey() {
        // given
        final String pem = "-----BEGIN PRIVATE KEY-----\nMC4CAQAwBQYDK2VwBCIEIOgbjaHgEqF7PY0t2dUf2VU0u1MRoKii/fywDlze4lvl\n-----END PRIVATE KEY-----\n";

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_PEM, pem);

        // then
        assertEquals(hex("e81b8da1e012a17b3d8d2dd9d51fd95534bb5311a0a8a2fdfcb00e5cdee25be5"), key.bytes());
        assertEquals(hex("f7b9aa4a8e4eee94e4277dfe757d8d7cde027e7cd5349b7d8e6ee21c9b9395be"), key.createPublicKey().bytes());
    }

    /// `keys/an-ed25519-public-key-is-read-from-and-written-as-spki-der`
    ///
    /// Source: RFC 8410 (SPKI of Ed25519); the key is the one of the Java, Go, Rust and Swift tests.
    @Test
    @DisplayName("an Ed25519 public key is read from and written as SPKI DER")
    void anEd25519PublicKeyIsReadFromAndWrittenAsSPKIDER() {
        // given
        final byte[] raw = hex("e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7");
        final byte[] der = concat(hex("302a300506032b6570032100"), raw);

        // when
        final PublicKey read = KeysFactory.createPublicKey(KeyFormat.SPKI_WITH_DER, der);
        final byte[] written = KeysFactory.createPublicKey(KeyAlgorithm.ED25519, raw).toBytes(KeyFormat.SPKI_WITH_DER);

        // then
        assertEquals(raw, read.bytes());
        assertEquals(der, written);
    }

    /// `keys/the-rfc-8032-secret-keys-derive-the-rfc-8032-public-keys`
    ///
    /// Source: RFC 8032, section 7.1, TEST 1 and TEST 2 (published test vectors).
    @Test
    @DisplayName("the RFC 8032 secret keys derive the RFC 8032 public keys")
    void theRFC8032SecretKeysDeriveTheRFC8032PublicKeys() {
        // given
        final byte[] secret1 = hex("9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60");
        final byte[] secret2 = hex("4ccd089b28ff96da9db6c346ec114e0f5b8a319f35aba624da8cf6ed4fb8a6fb");

        // when
        final PublicKey public1 = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, secret1).createPublicKey();
        final PublicKey public2 = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, secret2).createPublicKey();

        // then
        assertEquals(hex("d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a"), public1.bytes());
        assertEquals(hex("3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c"), public2.bytes());
    }

    /// `keys/an-rfc-8032-signature-verifies-only-for-its-message`
    ///
    /// Source: RFC 8032, section 7.1, TEST 2.
    @Test
    @DisplayName("an RFC 8032 signature verifies only for its message")
    void anRFC8032SignatureVerifiesOnlyForItsMessage() {
        // given
        final PublicKey publicKey = KeysFactory.createPublicKey(KeyAlgorithm.ED25519, hex("3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c"));
        final byte[] signature = hex("92a009a9f0d4cab8720e820b5f642540a2b27b5416503f8fb3762223ebdb69da" + "085ac1e43e15996e458f3613d0f11d8c387b2eaeb4302aeeb00d291612bb0c00");

        // when
        final boolean valid = publicKey.verify(hex("72"), signature);
        final boolean otherMessage = publicKey.verify(hex("73"), signature);

        // then
        assertTrue(valid);
        assertFalse(otherMessage);
    }

    /// `keys/a-secp256k1-private-key-derives-its-compressed-public-key`
    ///
    /// Source: secp256k1 (SEC 2); the vector of the Go, JavaScript, Rust and Swift tests, verified with OpenSSL. The public
    /// key of an ECDSA key is the compressed SEC 1 point (33 bytes) in every SDK.
    @Test
    @DisplayName("a secp256k1 private key derives its compressed public key")
    void aSecp256k1PrivateKeyDerivesItsCompressedPublicKey() {
        // given
        final String hexText = "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048";

        // when
        final PublicKey publicKey = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, hexText).createPublicKey();

        // then
        assertEquals(KeyAlgorithm.ECDSA, publicKey.algorithm());
        assertEquals(hex("02703a9370b0443be6ae7c507b0aec81a55e94e4a863b9655360bd65358caa6588"), publicKey.bytes());
    }

    /// `keys/a-secp256k1-spki-der-key-with-a-compressed-or-uncompressed-point-is-read-as-the-compressed-point`
    ///
    /// Source: RFC 5480 (SPKI of an EC key); the vectors of hedera-sdk-reference#93 (Java, JavaScript, Rust, C++, Swift),
    /// verified with OpenSSL.
    @Test
    @DisplayName("a secp256k1 SPKI DER key with a compressed or uncompressed point is read as the compressed point")
    void aSecp256k1SPKIDERKeyWithACompressedOrUncompressedPointIsReadAsTheCompressedPoint() {
        // given
        final byte[] compressed = hex("3036301006072a8648ce3d020106052b8104000a032200036843f5cb338bbb4cdb21b0da4ea739d910951d6e8a5f703d313efe31afe788f4");
        final byte[] uncompressed = hex("3056301006072a8648ce3d020106052b8104000a03420004aaac1c3ac1bea0245b8e00ce1e2018f9eab61b6331fbef7266f2287750a6597795f855ddcad2377e22259d1fcb4e0f1d35e8f2056300c15070bcbfce3759cc9d");

        // when
        final PublicKey first = KeysFactory.createPublicKey(KeyFormat.SPKI_WITH_DER, compressed);
        final PublicKey second = KeysFactory.createPublicKey(KeyFormat.SPKI_WITH_DER, uncompressed);

        // then
        assertEquals(hex("036843f5cb338bbb4cdb21b0da4ea739d910951d6e8a5f703d313efe31afe788f4"), first.bytes());
        assertEquals(hex("03aaac1c3ac1bea0245b8e00ce1e2018f9eab61b6331fbef7266f2287750a65977"), second.bytes());
    }

    /// `keys/a-secp256k1-private-key-of-zero-or-of-the-group-order-is-rejected`
    ///
    /// Source: SEC 1, section 3.2 (a private key is in [1, n - 1]); rejected by the Go and Python SDKs.
    @Test
    @DisplayName("a secp256k1 private key of zero or of the group order is rejected")
    void aSecp256k1PrivateKeyOfZeroOrOfTheGroupOrderIsRejected() {
        // given
        final byte[] zero = repeat(hex("00"), 32L);
        final byte[] order = hex("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141");
        for (final byte[] rawBytes : List.of(zero, order)) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, rawBytes));
        }
    }

    /// `keys/33-zero-bytes-are-no-secp256k1-public-key`
    ///
    /// Source: SEC 1, section 2.3.4 (a point must be on the curve); rejected by the Go SDK (the Java SDK accepts it).
    @Test
    @DisplayName("33 zero bytes are no secp256k1 public key")
    void test33ZeroBytesAreNoSecp256k1PublicKey() {
        // given
        final byte[] rawBytes = repeat(hex("00"), 33L);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPublicKey(KeyAlgorithm.ECDSA, rawBytes));
    }

    /// `keys/text-that-is-no-hex-is-rejected`
    ///
    /// Source: the C++ and Go SDKs.
    @Test
    @DisplayName("text that is no hex is rejected")
    void textThatIsNoHexIsRejected() {
        // given
        final String noHex = "fdsakfdsalf";
        final String oddLength = "303";
        for (final String value : List.of(noHex, oddLength)) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, value));
        }
    }

    /// `keys/a-pem-key-with-an-unknown-label-is-rejected`
    ///
    /// Source: RFC 7468 (PEM labels); rejected by the Rust and Swift SDKs.
    @Test
    @DisplayName("a PEM key with an unknown label is rejected")
    void aPEMKeyWithAnUnknownLabelIsRejected() {
        // given
        final String pem = "-----BEGIN PRIVATE KEYS-----\nMC4CAQAwBQYDK2VwBCIEIOgbjaHgEqF7PY0t2dUf2VU0u1MRoKii/fywDlze4lvl\n-----END PRIVATE KEYS-----\n";

        // when / then
        assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPrivateKey(pem));
    }

    /// `keys/the-same-bytes-as-ed25519-and-as-secp256k1-key-are-different-keys`
    ///
    /// Source: the Python SDK; a key is its algorithm and its bytes.
    @Test
    @DisplayName("the same bytes as Ed25519 and as secp256k1 key are different keys")
    void theSameBytesAsEd25519AndAsSecp256k1KeyAreDifferentKeys() {
        // given
        final byte[] rawBytes = repeat(hex("01"), 32L);

        // when
        final PrivateKey ed25519 = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, rawBytes);
        final PrivateKey ecdsa = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, rawBytes);

        // then
        assertNotEquals(ed25519, ecdsa);
    }

    /// `keys/a-secp256k1-signature-verifies-with-the-public-key-and-only-for-its-message`
    ///
    /// Source: the Go, C++ and Python SDKs (sign, verify, and a signature that no longer verifies for another message).
    @Test
    @DisplayName("a secp256k1 signature verifies with the public key and only for its message")
    void aSecp256k1SignatureVerifiesWithThePublicKeyAndOnlyForItsMessage() {
        // given
        final PrivateKey privateKey = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");
        final byte[] message = utf8("hello world");

        // when
        final byte[] signature = privateKey.sign(message);

        // then
        assertEquals(64L, length(signature));
        assertTrue(privateKey.createPublicKey().verify(message, signature));
        assertFalse(privateKey.createPublicKey().verify(utf8("hello world!"), signature));
    }

    /// `keys/a-secp256k1-signature-is-the-rfc-6979-low-s-signature-of-the-keccak-256-hash`
    ///
    /// Source: the known answer of the Go, Rust and Swift SDKs (Keccak-256, RFC 6979, low-s); the Java SDK computes the same r
    /// and the s of the upper half, n - s.
    @Test
    @DisplayName("a secp256k1 signature is the RFC 6979 low-s signature of the Keccak-256 hash")
    void aSecp256k1SignatureIsTheRFC6979LowSSignatureOfTheKeccak256Hash() {
        // given
        final PrivateKey privateKey = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");

        // when
        final byte[] signature = privateKey.sign(utf8("hello world"));

        // then
        assertEquals(hex("f3a13a555f1f8cd6532716b8f388bd4e9d8ed0b252743e923114c0c6cbfe414c" + "086e3717a6502c3edff6130d34df252fb94b6f662d0cd27e2110903320563851"), signature);
    }

    /// `keys/a-secp256k1-signature-with-a-high-s-verifies`
    ///
    /// Source: the known answer of the Java SDK, which does not normalise s; the signature differs from the low-s one only
    /// in s (n - s).
    @Test
    @DisplayName("a secp256k1 signature with a high s verifies")
    void aSecp256k1SignatureWithAHighSVerifies() {
        // given
        final PublicKey publicKey = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048").createPublicKey();
        final byte[] highS = hex("f3a13a555f1f8cd6532716b8f388bd4e9d8ed0b252743e923114c0c6cbfe414c" + "f791c8e859afd3c12009ecf2cb20dacf01636d80823bcdbd9ec1ce59afe008f0");

        // when / then
        assertTrue(publicKey.verify(utf8("hello world"), highS));
    }

    /// `keys/a-secp256k1-key-survives-every-format-that-supports-its-type`
    ///
    /// Source: the Java, Go and Python SDKs (an ECDSA key read back from its own DER and PEM encoding).
    @Test
    @DisplayName("a secp256k1 key survives every format that supports its type")
    void aSecp256k1KeySurvivesEveryFormatThatSupportsItsType() {
        // given
        final PrivateKey privateKey = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");
        final PublicKey publicKey = privateKey.createPublicKey();

        // when / then: PKCS#8 holds the private key, SPKI the public key, each in DER and in PEM
        assertEquals(privateKey, KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, privateKey.toBytes(KeyFormat.PKCS8_WITH_DER)));
        assertEquals(privateKey, KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_PEM, privateKey.toString(KeyFormat.PKCS8_WITH_PEM)));
        assertEquals(publicKey, KeysFactory.createPublicKey(KeyFormat.SPKI_WITH_DER, publicKey.toBytes(KeyFormat.SPKI_WITH_DER)));
        assertEquals(publicKey, KeysFactory.createPublicKey(KeyFormat.SPKI_WITH_PEM, publicKey.toString(KeyFormat.SPKI_WITH_PEM)));
    }

    /// `keys/a-private-key-imported-from-base64-is-the-key-of-the-same-bytes-in-hex`
    ///
    /// Source: this spec (ByteImportEncoding.BASE64, RFC 4648); no SDK imports raw key bytes from Base64. The text is the
    /// default key in Base64.
    @Test
    @DisplayName("a private key imported from Base64 is the key of the same bytes in hex")
    void aPrivateKeyImportedFromBase64IsTheKeyOfTheSameBytesInHex() {
        // given
        final String base64Text = "02caHpi7IvARwOS89RJVkOFdjyGnAXMJu1WIUgObx1w=";

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.BASE64, base64Text);

        // then
        assertEquals(defaultPrivateKey(), key);
    }

    /// `keys/a-secp256k1-public-key-is-written-as-rfc-5480-spki-with-the-compressed-point`
    ///
    /// Source: RFC 5480 (an EC key in an SPKI: id-ecPublicKey, the named curve secp256k1, the compressed point); the key
    /// of the test "a secp256k1 private key derives its compressed public key". OpenSSL writes exactly these bytes.
    @Test
    @DisplayName("a secp256k1 public key is written as RFC 5480 SPKI with the compressed point")
    void aSecp256k1PublicKeyIsWrittenAsRFC5480SPKIWithTheCompressedPoint() {
        // given
        final PublicKey publicKey = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048").createPublicKey();

        // when
        final byte[] der = publicKey.toBytes(KeyFormat.SPKI_WITH_DER);

        // then
        assertEquals(hex("3036301006072a8648ce3d020106052b8104000a032200" + "02703a9370b0443be6ae7c507b0aec81a55e94e4a863b9655360bd65358caa6588"), der);
    }

    /// `keys/a-secp256k1-private-key-is-read-from-a-standard-pkcs-8-structure`
    ///
    /// Source: RFC 5958 and RFC 5915 (the smallest PKCS#8 of an EC key: no curve and no public key inside the
    /// ECPrivateKey); read by OpenSSL.
    @Test
    @DisplayName("a secp256k1 private key is read from a standard PKCS#8 structure")
    void aSecp256k1PrivateKeyIsReadFromAStandardPKCS8Structure() {
        // given
        final byte[] der = hex("303e020100301006072a8648ce3d020106052b8104000a042730250201010420" + "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, der);

        // then
        assertEquals(KeyAlgorithm.ECDSA, key.algorithm());
        assertEquals(hex("8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048"), key.bytes());
    }

    /// `keys/the-legacy-ecdsa-key-form-of-earlier-sdks-is-no-pkcs-8-and-is-rejected`
    ///
    /// Source: the form that most current Hiero SDKs write and read (see "The standards of the containers"); it is no PKCS#8
    /// structure, and OpenSSL rejects it as well.
    @Test
    @DisplayName("the legacy ECDSA key form of earlier SDKs is no PKCS#8 and is rejected")
    void theLegacyECDSAKeyFormOfEarlierSDKsIsNoPKCS8AndIsRejected() {
        // given
        final byte[] der = hex("3030020100300706052b8104000a04220420" + "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, der));
    }

    /// `keys/a-hex-key-may-have-a-0x-prefix`
    ///
    /// Source: the Java and Go SDKs accept hex keys with a 0x prefix.
    @Test
    @DisplayName("a hex key may have a 0x prefix")
    void aHexKeyMayHaveA0xPrefix() {
        // given
        final String hexText = "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c";

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "0x" + hexText);

        // then
        assertEquals(defaultPrivateKey(), key);
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }
}
