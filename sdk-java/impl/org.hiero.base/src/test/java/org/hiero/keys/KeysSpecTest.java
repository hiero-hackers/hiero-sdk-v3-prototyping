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

    /// `keys/the-ed25519-key-db48-derives-the-public-key-e0c8ec`
    ///
    /// Source: the Java, Go and JavaScript SDKs (the key of their Ed25519 serialization tests and its public key).
    @Test
    @DisplayName("the Ed25519 key db48… derives the public key e0c8ec…")
    void theEd25519KeyDb48DerivesThePublicKeyE0c8ec() {
        // given
        final PrivateKey privateKey = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

        // when
        final PublicKey publicKey = privateKey.createPublicKey();

        // then
        assertEquals(hex("e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7"), publicKey.bytes());
    }

    /// `keys/the-ed25519-signatures-of-other-sdks-verify`
    ///
    /// Source: the known answers of the Rust, Swift and JavaScript SDKs ("hello, world") and of the Java SDK ("This is a
    /// message about the world."); checked by verification, since a deterministic Ed25519 nonce is not required.
    @Test
    @DisplayName("the Ed25519 signatures of other SDKs verify")
    void theEd25519SignaturesOfOtherSDKsVerify() {
        // given
        final PublicKey publicKey = KeysFactory.createPublicKey(KeyAlgorithm.ED25519, hex("e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7"));
        final byte[] first = hex("9d04bfed7baa97c80d29a6ae48c0d896ce8463a7ea0c16197d55a563c73996ef" + "062b2adf507f416c108422c0310fc6fb21886e11ce3de3e951d7a56049743f07");
        final byte[] second = hex("73bea53f31ca9c42a422ecb7516ec08d0bbd1a6bfd630ccf10ec1872454814d2" + "9f4a8011129cd007eab544af01a75f508285b591e5bed24b68f927751e49e30e");

        // when / then
        assertTrue(publicKey.verify(utf8("hello, world"), first));
        assertTrue(publicKey.verify(utf8("This is a message about the world."), second));
    }

    /// `keys/the-rfc-8032-signatures-of-test-1-and-test-3-verify`
    ///
    /// Source: RFC 8032, section 7.1, TEST 1 (the empty message) and TEST 3; the C++ SDK runs these vectors.
    @Test
    @DisplayName("the RFC 8032 signatures of TEST 1 and TEST 3 verify")
    void theRFC8032SignaturesOfTEST1AndTEST3Verify() {
        // given
        final PublicKey test1 = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, hex("9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60")).createPublicKey();
        final PublicKey test3 = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, hex("c5aa8df43f9f837bedb7442f31dcb7b166d38535076f094b85ce3a2e0b4458f7")).createPublicKey();

        // when / then
        assertEquals(hex("fc51cd8e6218a1a38da47ed00230f0580816ed13ba3303ac5deb911548908025"), test3.bytes());
        assertTrue(test1.verify(hex(""), hex("e5564300c360ac729086e2cc806e828a84877f1eb8e5d974d873e065224901555fb8821590a33bacc61e39701cf9b46bd2" + "5bf5f0595bbe24655141438e7a100b")));
        assertTrue(test3.verify(hex("af82"), hex("6291d657deec24024827e69c3abe01a30ce548a284743a445e3680d7db5ac3ac18ff9b538d16f290ae67f760984dc6594a" + "7c15e9716ed28dc027beceea1ec40a")));
    }

    /// `keys/a-secp256k1-signature-of-a-transaction-body-is-the-rfc-6979-low-s-signature`
    ///
    /// Source: the known answer of the JavaScript SDK for a transaction body (Keccak-256, RFC 6979, low-s).
    @Test
    @DisplayName("a secp256k1 signature of a transaction body is the RFC 6979 low-s signature")
    void aSecp256k1SignatureOfATransactionBodyIsTheRFC6979LowSSignature() {
        // given
        final PrivateKey privateKey = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");
        final byte[] body = hex("0a0e0a0408011001120608001000180412060800100018031880c2d72f220208783200721a0a180a0a0a0608001000180410130a0a0a060800100018051014");

        // when
        final byte[] signature = privateKey.sign(body);

        // then
        assertEquals(hex("63201532040178a60e2738bdaaa00d628004b15d109162fa42e066fcb67201" + "90438473bbf155fd7ff6bfb2a94141157f1e1a080aa84473d7f4c68f8025275a0a"), signature);
    }

    /// `keys/a-signature-with-a-changed-byte-does-not-verify`
    ///
    /// Source: the Java, Go, Python and Rust SDKs (a signature with one changed byte no longer verifies). The signatures are
    /// the known answers above with byte 5 increased by one.
    @Test
    @DisplayName("a signature with a changed byte does not verify")
    void aSignatureWithAChangedByteDoesNotVerify() {
        // given
        final PublicKey ed25519 = KeysFactory.createPublicKey(KeyAlgorithm.ED25519, hex("e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7"));
        final PublicKey ecdsa = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048").createPublicKey();

        // when / then
        assertFalse(ed25519.verify(utf8("hello, world"), hex("9d04bfed7bab97c80d29a6ae48c0d896ce8463a7ea0c16197d55a563c73996ef" + "062b2adf507f416c108422c0310fc6fb21886e11ce3de3e951d7a56049743f07")));
        assertFalse(ecdsa.verify(utf8("hello world"), hex("f3a13a555f208cd6532716b8f388bd4e9d8ed0b252743e923114c0c6cbfe414c" + "086e3717a6502c3edff6130d34df252fb94b6f662d0cd27e2110903320563851")));
    }

    /// `keys/a-signature-of-the-wrong-length-does-not-verify`
    ///
    /// Source: the C++ and Go SDKs (a signature of the wrong length is invalid, not an error).
    @Test
    @DisplayName("a signature of the wrong length does not verify")
    void aSignatureOfTheWrongLengthDoesNotVerify() {
        // given
        final PublicKey publicKey = defaultPublicKey();
        for (final byte[] signature : List.of(hex("01020304"), hex(""), repeat(hex("00"), 65L))) {
            // when / then
            assertFalse(publicKey.verify(utf8("hello"), signature));
        }
    }

    /// `keys/the-empty-message-can-be-signed-and-verified`
    ///
    /// Source: the C++ SDK (sign and verify the empty message).
    @Test
    @DisplayName("the empty message can be signed and verified")
    void theEmptyMessageCanBeSignedAndVerified() {
        for (final PrivateKey privateKey : List.of(defaultPrivateKey(), KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048"))) {
            // when
            final byte[] signature = privateKey.sign(hex(""));

            // then
            assertTrue(privateKey.createPublicKey().verify(hex(""), signature));
        }
    }

    /// `keys/a-signature-does-not-verify-with-a-key-of-the-other-algorithm`
    ///
    /// Source: the Rust, Swift and Python SDKs (a signature of one algorithm does not verify with a key of the other).
    @Test
    @DisplayName("a signature does not verify with a key of the other algorithm")
    void aSignatureDoesNotVerifyWithAKeyOfTheOtherAlgorithm() {
        // given
        final PublicKey ed25519 = KeysFactory.createPublicKey(KeyAlgorithm.ED25519, hex("e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7"));
        final PublicKey ecdsa = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048").createPublicKey();
        final byte[] ed25519Signature = hex("9d04bfed7baa97c80d29a6ae48c0d896ce8463a7ea0c16197d55a563c73996ef" + "062b2adf507f416c108422c0310fc6fb21886e11ce3de3e951d7a56049743f07");
        final byte[] ecdsaSignature = hex("f3a13a555f1f8cd6532716b8f388bd4e9d8ed0b252743e923114c0c6cbfe414c" + "086e3717a6502c3edff6130d34df252fb94b6f662d0cd27e2110903320563851");

        // when / then
        assertFalse(ecdsa.verify(utf8("hello, world"), ed25519Signature));
        assertFalse(ed25519.verify(utf8("hello world"), ecdsaSignature));
    }

    /// `keys/a-pkcs-8-version-2-key-with-its-public-key-is-read-and-written-as-version-1`
    ///
    /// Source: RFC 5958 (OneAsymmetricKey, version 1, with the public key); the Swift SDK reads it and writes version 0.
    @Test
    @DisplayName("a PKCS#8 version 2 key with its public key is read and written as version 1")
    void aPKCS8Version2KeyWithItsPublicKeyIsReadAndWrittenAsVersion1() {
        // given
        final byte[] der = hex("3053020101300506032b65700422042092a736a341feb460a3906dec5b7eeaaf64111e6d9769e06b543986968465802f" + "a123032100b8485a5584725595e5b856f8b361ed84571ccb58e00447b307f9df608acef1f7");

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, der);

        // then
        assertEquals(hex("92a736a341feb460a3906dec5b7eeaaf64111e6d9769e06b543986968465802f"), key.bytes());
        assertEquals(hex("302e020100300506032b65700422042092a736a341feb460a3906dec5b7eeaaf64111e6d9769e06b543986968465802f"), key.toBytes(KeyFormat.PKCS8_WITH_DER));
    }

    /// `keys/hex-digits-may-be-upper-case`
    ///
    /// Source: the C++, Go and Swift SDKs (hex digits in upper case).
    @Test
    @DisplayName("hex digits may be upper case")
    void hexDigitsMayBeUpperCase() {
        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "D3671A1E98BB22F011C0E4BCF5125590E15D8F21A7017309BB558852039BC75C");

        // then
        assertEquals(defaultPrivateKey(), key);
    }

    /// `keys/a-secp256k1-private-key-with-leading-zero-bytes-keeps-32-bytes`
    ///
    /// Source: the JavaScript and Go SDKs (a secp256k1 scalar with leading zero bytes keeps its 32 bytes).
    @Test
    @DisplayName("a secp256k1 private key with leading zero bytes keeps 32 bytes")
    void aSecp256k1PrivateKeyWithLeadingZeroBytesKeeps32Bytes() {
        // given
        final String hexText = "0000000000000000fd2fe3d732d3412140accab21b4b7303ff05f9c9127542cd";

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, hexText);

        // then
        assertEquals(hex(hexText), key.bytes());
        assertEquals(key, KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, key.toBytes(KeyFormat.PKCS8_WITH_DER)));
    }

    /// `keys/a-secp256k1-private-key-is-checked-against-the-order-of-secp256k1`
    ///
    /// Source: the Go SDK (the range check uses the order of secp256k1: the order of P-256 plus one is a valid secp256k1
    /// scalar, because it is below the order of secp256k1).
    @Test
    @DisplayName("a secp256k1 private key is checked against the order of secp256k1")
    void aSecp256k1PrivateKeyIsCheckedAgainstTheOrderOfSecp256k1() {
        // given
        final String hexText = "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632552";

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, hexText);

        // then
        assertEquals(hex(hexText), key.bytes());
    }

    /// `keys/a-generated-key-of-each-algorithm-signs-and-verifies`
    ///
    /// Source: the Go, Java, Python and C++ SDKs (a generated key has its algorithm and 32 bytes, and signs).
    @Test
    @DisplayName("a generated key of each algorithm signs and verifies")
    void aGeneratedKeyOfEachAlgorithmSignsAndVerifies() {
        for (final KeyAlgorithm algorithm : List.of(KeyAlgorithm.ED25519, KeyAlgorithm.ECDSA)) {
            // when
            final PrivateKey key = KeysFactory.generatePrivateKey(algorithm);
            final byte[] signature = key.sign(utf8("hello"));

            // then
            assertEquals(algorithm, key.algorithm());
            assertEquals(KeyType.PRIVATE, key.type());
            assertEquals(32L, length(key.bytes()));
            assertTrue(key.createPublicKey().verify(utf8("hello"), signature));
        }
    }

    /// `keys/an-spki-key-of-another-curve-or-with-trailing-bytes-is-rejected`
    ///
    /// Source: the Go and Python SDKs (an EC key of another curve), the Go SDK (bytes after the DER structure).
    @Test
    @DisplayName("an SPKI key of another curve or with trailing bytes is rejected")
    void anSPKIKeyOfAnotherCurveOrWithTrailingBytesIsRejected() {
        // given: the RFC 5480 SPKI of the test "a secp256k1 public key is written as RFC 5480 SPKI with the compressed point"
        // with the curve 1.3.132.0.11 instead of 1.3.132.0.10, and the same SPKI followed by one byte
        final byte[] otherCurve = hex("3036301006072a8648ce3d020106052b8104000b032200" + "02703a9370b0443be6ae7c507b0aec81a55e94e4a863b9655360bd65358caa6588");
        final byte[] trailing = hex("3036301006072a8648ce3d020106052b8104000a032200" + "02703a9370b0443be6ae7c507b0aec81a55e94e4a863b9655360bd65358caa658800");
        for (final byte[] der : List.of(otherCurve, trailing)) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPublicKey(KeyFormat.SPKI_WITH_DER, der));
        }
    }

    /// `keys/raw-key-bytes-of-the-wrong-length-or-with-an-unknown-point-prefix-are-rejected`
    ///
    /// Source: the Go, Java and Python SDKs (raw keys of the wrong length), the Python SDK (an unknown point prefix).
    @Test
    @DisplayName("raw key bytes of the wrong length or with an unknown point prefix are rejected")
    void rawKeyBytesOfTheWrongLengthOrWithAnUnknownPointPrefixAreRejected() {
        // when / then: an Ed25519 public key has 32 bytes
        assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPublicKey(KeyAlgorithm.ED25519, repeat(hex("01"), 33L)));

        // when / then: a secp256k1 private key has 32 bytes
        assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, repeat(hex("01"), 31L)));

        // when / then: a compressed SEC 1 point starts with 02 or 03
        assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPublicKey(KeyAlgorithm.ECDSA, hex("05703a9370b0443be6ae7c507b0aec81a55e94e4a863b9655360bd65358caa6588")));
    }

    /// `keys/a-pem-key-whose-end-label-differs-from-its-begin-label-is-rejected`
    ///
    /// Source: RFC 7468 (the labels of BEGIN and END must match); rejected by the Swift SDK.
    @Test
    @DisplayName("a PEM key whose END label differs from its BEGIN label is rejected")
    void aPEMKeyWhoseENDLabelDiffersFromItsBEGINLabelIsRejected() {
        // given
        final String pem = "-----BEGIN PRIVATE KEY-----\nMC4CAQAwBQYDK2VwBCIEINNnGh6YuyLwEcDkvPUSVZDhXY8hpwFzCbtViFIDm8dc\n-----END PUBLIC KEY-----\n";

        // when / then
        assertThrows(IllegalArgumentException.class, () -> KeysFactory.createPrivateKey(pem));
    }

    /// `keys/a-pem-key-with-extra-whitespace-and-blank-lines-is-read`
    ///
    /// Source: RFC 7468, section 2 (parsers ignore whitespace around the lines); the JavaScript SDK.
    @Test
    @DisplayName("a PEM key with extra whitespace and blank lines is read")
    void aPEMKeyWithExtraWhitespaceAndBlankLinesIsRead() {
        // given: the default key in PEM (see "Key examples") with tabs, spaces and blank lines
        final String pem = "\n-----BEGIN PRIVATE KEY-----  \n\t MC4CAQAwBQYDK2VwBCIEINNnGh6YuyLwEcDkvPUSVZDhXY8hpwFzCbtViFIDm8dc \t\n\n-----END PRIVATE KEY-----\n\n";

        // when
        final PrivateKey key = KeysFactory.createPrivateKey(pem);

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
