# Keys API

This section defines the API for keys.

## Description

The keys API provides functionality to create and manage cryptographic keys.
A cryptographic key is defined by a byte sequence and a cryptographic algorithm.
Here it is independent if the key is a public or private key.
A private key can be used to sign messages and a public key can be used to verify signatures.
A private key is normally generated as a random key for a specific algorithm.
A public key can be derived from a private key.

To read or write keys different formats are supported.
The easiest way is to read or write the raw bytes of the key.
To do so, the algorithm must be known.

To make import and export of keys more convenient, so-called key containers exist.
Like algorithms those containers are well defined and standardized.
A container normally contains the raw bytes of the key and the algorithm.
To import and export a key in a container an encoding must be specified.
Not all container formats support all encodings according to their specification, and the encoding can end in a byte array result or a
string result.

## API Schema

```
namespace keys

// all key types
enum KeyType {
    PUBLIC, // a public key
    PRIVATE // a private key
}

//all supported algorithms
enum KeyAlgorithm {
    ED25519, // Edwards-curve Digital Signature Algorithm 
    ECDSA // Elliptic Curve Digital Signature Algorithm (secp256k1 curve)
}

// abstract key definition
abstraction Key {
    @@immutable bytes: bytes //the raw bytes of the key
    @@immutable algorithm: KeyAlgorithm //the algorithm of the key
    @@immutable type: KeyType //the type of the key
    
    // returns the key in the RAW encoding
    bytes toRawBytes() 
    
    // Convert to bytes using specified container format
    // Throws illegal-format if container.format is not BYTES or doesn't support this key type
    @@throws(illegal-format) bytes toBytes(container: KeyFormat) 
    
    // Convert to string using specified container format
    // Throws illegal-format if container.format is not STRING or doesn't support this key type
    @@throws(illegal-format) string toString(container: KeyFormat) 
}

// a key pair
KeyPair {
    @@immutable publicKey: PublicKey // the public key of the key pair
    @@immutable privateKey: PrivateKey // the private key of the key pair
}

// public key definition
PublicKey extends Key {
    
    // Verify a signature using this public key
    // returns true if the signature is valid for the message and the public key
    bool verify(message: bytes, signature: bytes)
}

// private key definition
PrivateKey extends Key {
    
    // Sign a message with this private key
    // returns the signature for the message
    bytes sign(message: bytes) 
    
    // Derive the corresponding public key
    // always returns a new PublicKey instance
    PublicKey createPublicKey() 
}

// factory methods of keys that should be added to the namespace in the best language dependent way

//Generate a new key based on a specific algorithm
@@static PrivateKey generatePrivateKey(algorithm: KeyAlgorithm)
@@static PublicKey generatePublicKey(algorithm: KeyAlgorithm)

//Read a key based on a specific algorithm from a string
@@throws(illegal-format) @@static PrivateKey createPrivateKey(algorithm: KeyAlgorithm, encoding: ByteImportEncoding, value: string) // calls createPrivateKey(algorithm: KeyAlgorithm, rawBytes: bytes)
@@throws(illegal-format) @@static PublicKey createPublicKey(algorithm: KeyAlgorithm, encoding: ByteImportEncoding, value: string) // calls createPublicKey(algorithm: KeyAlgorithm, rawBytes: bytes)

//Read a key based on a specific algorithm from a byte array
@@throws(illegal-format) @@static PrivateKey createPrivateKey(algorithm: KeyAlgorithm, rawBytes: bytes) // reads bytes as raw bytes for the given algorithm
@@throws(illegal-format) @@static PublicKey createPublicKey(algorithm: KeyAlgorithm, rawBytes: bytes) // reads bytes as raw bytes for the given algorithm

//Read a key based on a specific format (container & encoding) from a string
@@throws(illegal-format) @@static PrivateKey createPrivateKey(container: KeyFormat, value: string) // if container.format is not STRING an illegal format error is thrown
@@throws(illegal-format) @@static PublicKey createPublicKey(container: KeyFormat, value: string) // if container.format is not STRING an illegal format error is thrown

//Read a key based on a specific format (container & encoding) from a byte array
@@throws(illegal-format) @@static PrivateKey createPrivateKey(container: KeyFormat, value: bytes) // if container.format is not BYTES an illegal format error is thrown
@@throws(illegal-format) @@static PublicKey createPublicKey(container: KeyFormat, value: bytes) // if container.format is not BYTES an illegal format error is thrown

//Read a key based on our preferred format (container & encoding) from a string
@@throws(illegal-format) @@static PrivateKey createPrivateKey(value: string) // reads string as PKCS#8 PEM
@@throws(illegal-format) @@static PublicKey createPublicKey(value: string) // reads string as SPKI PEM

// --- key import/export formats (formerly the keys.io sub-namespace) ---

enum RawFormat {
    STRING, // string representation of the bytes in the specified encoding
    BYTES // raw bytes
}

// all supported encodings that can be used to import/export a container format
// rawFormat: the raw format of the import / export
enum KeyEncoding(rawFormat: RawFormat) {
    DER(BYTES), // Distinguished Encoding Rules (binary)
    PEM(STRING) // Privacy Enhanced Mail (Base64 text)

    // decodes the given text into the DER bytes of a key of the given type; fails for a binary encoding and for text
    // that is not in this encoding
    @@throws(illegal-format) bytes decode(keyType : KeyType, value : string)
}

// all supported container formats
enum KeyContainer {
    PKCS8, // PKCS#8 Private Key Specification
    SPKI // Subject Public Key Info
    
    bool supportsType(type: KeyType) // returns true if the container format supports the given key type
}

// encoding information for import / export
enum ByteImportEncoding {
    HEX, // hex string representation of the bytes
    BASE64 // base64 string representation of the bytes
    
    @@throws(illegal-format) bytes decode(value : string) // fails for text that is not in this encoding
}

// combined container format and encoding
// container: the container format; encoding: the encoding
enum KeyFormat(container: KeyContainer, encoding: KeyEncoding) {
    PKCS8_WITH_DER(PKCS8, DER),
    SPKI_WITH_DER(SPKI, DER),
    PKCS8_WITH_PEM(PKCS8, PEM),
    SPKI_WITH_PEM(SPKI, PEM)
    
    bool supportsType(type: KeyType) // returns true if the internal container format supports the given key type
    // decodes the given string value into raw bytes for the given key type; fails if the container cannot hold the key
    // type or the text is not in the encoding
    @@throws(illegal-format) bytes decode(keyType : KeyType, value : string)
  
}

```

## Default Instances for Testing

```
// An ED25519 key with known bytes (the key of the examples below), so that tests can check signatures and
// encodings against fixed values.
instance PrivateKey = createPrivateKey(algorithm: KeyAlgorithm.ED25519, encoding: ByteImportEncoding.HEX,
        value: "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")

// The public key of the default private key.
instance PublicKey = DEFAULT(PrivateKey).createPublicKey()
```

## KeyContainer rules

Not all combinations of container and encoding are valid.
For example, a PKCS8 container format can only be used with DER encoding.
Here you can find the complete list of rules as a basic implementation of the enum:

### Key examples

The following examples show the different key formats as string representations.

#### PKCS#8 + DER (Private Key)

The string is a hex dump of the DER bytes.

```
30 2E 02 01 00 30 05 06 03 2B 65 70 04 22 04 20D3 67 1A 1E 98 BB 22 F0 11 C0 E4 BC F5 12 55 90
E1 5D 8F 21 A7 01 73 09 BB 55 88 52 03 9B C7 5C
```

#### PKCS#8 + PEM (Private Key)

```
-----BEGIN PRIVATE KEY-----
MC4CAQAwBQYDK2VwBCIEINNnGh6YuyLwEcDkvPUSVZDhXY8hpwFzCbtViFIDm8dc
-----END PRIVATE KEY-----
```

#### SPKI + DER (Public Key)

The string is a hex dump of the DER bytes.

```
30 2A 30 05 06 03 2B 65 70 03 21 00
1D 0F 36 11 67 7D 11 EC 59 85 55 9B
0A 7C 22 7A D9 88 CB 0B E6 C8 27 67
E8 96 F6 9E E3
```

#### SPKI + PEM (Public Key)

```
-----BEGIN PUBLIC KEY-----
MCowBQYDK2VwAyEAHQ82EWd9EexZhVWbCnwie
tmIywvmyCdn6Jb2nuM=
-----END PUBLIC KEY-----
```

## Testing

Known-answer tests, written as given / when / then. The expected values come from this spec (the default key and its
PKCS#8 encoding under "Key examples"), from published standards (RFC 8032, RFC 8410, RFC 5480, SEC 1) or from the
reference vectors that the Hiero SDKs share (hedera-sdk-reference#93); the comment above each test names its source.
What no source states as a value (a signature of the default key) is checked by the properties the API promises.
See `docs/sdk-unit-test-analysis.md` for where the SDKs test the same.

```tests
test "a private key imported from hex has these raw bytes" {
    // given
    let hexText = "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"

    // when
    let key = createPrivateKey(algorithm: KeyAlgorithm.ED25519, encoding: ByteImportEncoding.HEX, value: hexText)

    // then
    assertEquals(expected: hex(text: hexText), actual: key.bytes)
    assertEquals(expected: KeyAlgorithm.ED25519, actual: key.algorithm)
    assertEquals(expected: KeyType.PRIVATE, actual: key.type)
}

test "the default private key encodes as PKCS#8 DER" {
    // given
    let key = DEFAULT(PrivateKey)

    // when
    let der = key.toBytes(container: KeyFormat.PKCS8_WITH_DER)

    // then
    assertEquals(
            expected: hex(text: "302e020100300506032b657004220420d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"),
            actual: der)
}

test "the public key of an Ed25519 key is a public Ed25519 key of 32 bytes" {
    // given
    let privateKey = DEFAULT(PrivateKey)

    // when
    let publicKey = privateKey.createPublicKey()

    // then
    assertEquals(expected: KeyType.PUBLIC, actual: publicKey.type)
    assertEquals(expected: KeyAlgorithm.ED25519, actual: publicKey.algorithm)
    assertEquals(expected: 32, actual: length(value: publicKey.bytes))
    assertEquals(expected: publicKey, actual: createPublicKey(algorithm: KeyAlgorithm.ED25519, rawBytes: publicKey.bytes))
}

test "a signature verifies with the public key and only for its message" {
    // given
    let message = utf8(text: "hello")

    // when
    let signature = DEFAULT(PrivateKey).sign(message: message)

    // then
    assertEquals(expected: 64, actual: length(value: signature))
    assertEquals(expected: signature, actual: DEFAULT(PrivateKey).sign(message: message),
            message: "Ed25519 signatures are deterministic")
    assertTrue(condition: DEFAULT(PublicKey).verify(message: message, signature: signature))
    assertFalse(condition: DEFAULT(PublicKey).verify(message: utf8(text: "hello!"), signature: signature))
}

test "a key survives every format that supports its type" {
    // given
    let key = DEFAULT(PrivateKey)

    for format in [KeyFormat.PKCS8_WITH_DER, KeyFormat.SPKI_WITH_DER, KeyFormat.PKCS8_WITH_PEM,
            KeyFormat.SPKI_WITH_PEM] {
        if not(value: format.supportsType(type: KeyType.PRIVATE)) {
            // when / then: a format for public keys rejects a private key
            @@expectThrows(illegal-format)
            key.toBytes(container: format)
        } else if format.encoding == KeyEncoding.DER {
            // when
            let der = key.toBytes(container: format)

            // then
            assertEquals(expected: key, actual: createPrivateKey(container: format, value: der))
        } else {
            // when
            let pem = key.toString(container: format)

            // then
            assertTrue(condition: startsWith(value: pem, prefix: "-----BEGIN PRIVATE KEY-----"))
            assertEquals(expected: key, actual: createPrivateKey(container: format, value: pem))
        }
    }
}

test "a hex key of the wrong length is rejected" {
    // given
    let tooShort = "d3671a1e"
    let tooLong = repeat(value: "ab", count: 33)

    for hexText in [tooShort, tooLong] {
        // when / then
        @@expectThrows(illegal-format)
        createPrivateKey(algorithm: KeyAlgorithm.ED25519, encoding: ByteImportEncoding.HEX, value: hexText)
    }
}

// Source: the reference vectors of hashgraph/hedera-sdk-reference#93, read with the same results by the
// Java, JavaScript, Rust, C++ and Swift SDKs; verified with OpenSSL.
test "an Ed25519 PKCS#8 DER key derives the reference public key" {
    // given
    let der = hex(text: "302e020100300506032b657004220420feb858a4a69600a5eef2d9c76f7fb84fc0b6627f29e0ab17e160f640c267d404")

    // when
    let key = createPrivateKey(container: KeyFormat.PKCS8_WITH_DER, value: der)

    // then
    assertEquals(expected: KeyAlgorithm.ED25519, actual: key.algorithm)
    assertEquals(expected: hex(text: "feb858a4a69600a5eef2d9c76f7fb84fc0b6627f29e0ab17e160f640c267d404"), actual: key.bytes)
    assertEquals(expected: hex(text: "8ccd31b53d1835b467aac795dab19b274dd3b37e3daf12fcec6bc02bac87b53d"),
            actual: key.createPublicKey().bytes)
}

// Source: hedera-sdk-reference#93 (Java, JavaScript, Rust, C++, Swift); verified with OpenSSL.
test "an Ed25519 PKCS#8 PEM key derives the reference public key" {
    // given
    let pem = "-----BEGIN PRIVATE KEY-----\nMC4CAQAwBQYDK2VwBCIEIOgbjaHgEqF7PY0t2dUf2VU0u1MRoKii/fywDlze4lvl\n-----END PRIVATE KEY-----\n"

    // when
    let key = createPrivateKey(container: KeyFormat.PKCS8_WITH_PEM, value: pem)

    // then
    assertEquals(expected: hex(text: "e81b8da1e012a17b3d8d2dd9d51fd95534bb5311a0a8a2fdfcb00e5cdee25be5"), actual: key.bytes)
    assertEquals(expected: hex(text: "f7b9aa4a8e4eee94e4277dfe757d8d7cde027e7cd5349b7d8e6ee21c9b9395be"),
            actual: key.createPublicKey().bytes)
}

// Source: RFC 8410 (SPKI of Ed25519); the key is the one of the Java, Go, Rust and Swift tests.
test "an Ed25519 public key is read from and written as SPKI DER" {
    // given
    let raw = hex(text: "e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7")
    let der = hex(text: "302a300506032b6570032100") + raw

    // when
    let read = createPublicKey(container: KeyFormat.SPKI_WITH_DER, value: der)
    let written = createPublicKey(algorithm: KeyAlgorithm.ED25519, rawBytes: raw).toBytes(container: KeyFormat.SPKI_WITH_DER)

    // then
    assertEquals(expected: raw, actual: read.bytes)
    assertEquals(expected: der, actual: written)
}

// Source: RFC 8032, section 7.1, TEST 1 and TEST 2 (published test vectors).
test "the RFC 8032 secret keys derive the RFC 8032 public keys" {
    // given
    let secret1 = hex(text: "9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60")
    let secret2 = hex(text: "4ccd089b28ff96da9db6c346ec114e0f5b8a319f35aba624da8cf6ed4fb8a6fb")

    // when
    let public1 = createPrivateKey(algorithm: KeyAlgorithm.ED25519, rawBytes: secret1).createPublicKey()
    let public2 = createPrivateKey(algorithm: KeyAlgorithm.ED25519, rawBytes: secret2).createPublicKey()

    // then
    assertEquals(expected: hex(text: "d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a"), actual: public1.bytes)
    assertEquals(expected: hex(text: "3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c"), actual: public2.bytes)
}

// Source: RFC 8032, section 7.1, TEST 2.
test "an RFC 8032 signature verifies only for its message" {
    // given
    let publicKey = createPublicKey(algorithm: KeyAlgorithm.ED25519,
            rawBytes: hex(text: "3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c"))
    let signature = hex(text: "92a009a9f0d4cab8720e820b5f642540a2b27b5416503f8fb3762223ebdb69da"
            + "085ac1e43e15996e458f3613d0f11d8c387b2eaeb4302aeeb00d291612bb0c00")

    // when
    let valid = publicKey.verify(message: hex(text: "72"), signature: signature)
    let otherMessage = publicKey.verify(message: hex(text: "73"), signature: signature)

    // then
    assertTrue(condition: valid)
    assertFalse(condition: otherMessage)
}

// Source: secp256k1 (SEC 2); the vector of the Go, JavaScript, Rust and Swift tests, verified with OpenSSL. The public
// key of an ECDSA key is the compressed SEC 1 point (33 bytes) in every SDK.
test "a secp256k1 private key derives its compressed public key" {
    // given
    let hexText = "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048"

    // when
    let publicKey = createPrivateKey(algorithm: KeyAlgorithm.ECDSA, encoding: ByteImportEncoding.HEX, value: hexText)
            .createPublicKey()

    // then
    assertEquals(expected: KeyAlgorithm.ECDSA, actual: publicKey.algorithm)
    assertEquals(expected: hex(text: "02703a9370b0443be6ae7c507b0aec81a55e94e4a863b9655360bd65358caa6588"),
            actual: publicKey.bytes)
}

// Source: RFC 5480 (SPKI of an EC key); the vectors of hedera-sdk-reference#93 (Java, JavaScript, Rust, C++, Swift),
// verified with OpenSSL.
test "a secp256k1 SPKI DER key with a compressed or uncompressed point is read as the compressed point" {
    // given
    let compressed = hex(text: "3036301006072a8648ce3d020106052b8104000a032200036843f5cb338bbb4cdb21b0da4ea739d910951d6e8a5f703d313efe31afe788f4")
    let uncompressed = hex(text: "3056301006072a8648ce3d020106052b8104000a03420004aaac1c3ac1bea0245b8e00ce1e2018f9eab61b6331fbef7266f2287750a6597795f855ddcad2377e22259d1fcb4e0f1d35e8f2056300c15070bcbfce3759cc9d")

    // when
    let first = createPublicKey(container: KeyFormat.SPKI_WITH_DER, value: compressed)
    let second = createPublicKey(container: KeyFormat.SPKI_WITH_DER, value: uncompressed)

    // then
    assertEquals(expected: hex(text: "036843f5cb338bbb4cdb21b0da4ea739d910951d6e8a5f703d313efe31afe788f4"), actual: first.bytes)
    assertEquals(expected: hex(text: "03aaac1c3ac1bea0245b8e00ce1e2018f9eab61b6331fbef7266f2287750a65977"), actual: second.bytes)
}

// Source: SEC 1, section 3.2 (a private key is in [1, n - 1]); rejected by the Go and Python SDKs.
test "a secp256k1 private key of zero or of the group order is rejected" {
    // given
    let zero = repeat(value: hex(text: "00"), count: 32)
    let order = hex(text: "fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141")

    for rawBytes in [zero, order] {
        // when / then
        @@expectThrows(illegal-format)
        createPrivateKey(algorithm: KeyAlgorithm.ECDSA, rawBytes: rawBytes)
    }
}

// Source: SEC 1, section 2.3.4 (a point must be on the curve); rejected by the Go SDK (the Java SDK accepts it).
test "33 zero bytes are no secp256k1 public key" {
    // given
    let rawBytes = repeat(value: hex(text: "00"), count: 33)

    // when / then
    @@expectThrows(illegal-format)
    createPublicKey(algorithm: KeyAlgorithm.ECDSA, rawBytes: rawBytes)
}

// Source: the C++ and Go SDKs.
test "text that is no hex is rejected" {
    // given
    let noHex = "fdsakfdsalf"
    let oddLength = "303"

    for value in [noHex, oddLength] {
        // when / then
        @@expectThrows(illegal-format)
        createPrivateKey(algorithm: KeyAlgorithm.ED25519, encoding: ByteImportEncoding.HEX, value: value)
    }
}

// Source: RFC 7468 (PEM labels); rejected by the Rust and Swift SDKs.
test "a PEM key with an unknown label is rejected" {
    // given
    let pem = "-----BEGIN PRIVATE KEYS-----\nMC4CAQAwBQYDK2VwBCIEIOgbjaHgEqF7PY0t2dUf2VU0u1MRoKii/fywDlze4lvl\n-----END PRIVATE KEYS-----\n"

    // when / then
    @@expectThrows(illegal-format)
    createPrivateKey(value: pem)
}

// Source: the Python SDK; a key is its algorithm and its bytes.
test "the same bytes as Ed25519 and as secp256k1 key are different keys" {
    // given
    let rawBytes = repeat(value: hex(text: "01"), count: 32)

    // when
    let ed25519 = createPrivateKey(algorithm: KeyAlgorithm.ED25519, rawBytes: rawBytes)
    let ecdsa = createPrivateKey(algorithm: KeyAlgorithm.ECDSA, rawBytes: rawBytes)

    // then
    assertNotEquals(unexpected: ed25519, actual: ecdsa)
}
```

## Questions & Comments

- **No EVM address of an ECDSA public key.** The EVM address of an account with an ECDSA (secp256k1)
  key is derived from the public key (the last 20 bytes of the Keccak-256 hash of the uncompressed
  key), e.g. to create an account by its EVM-address alias. The API has no such derivation, so the
  TCK's `generateKey` with type `evmAddress` cannot be answered (see
  [`tck-binding.md`](../../docs/tck-binding.md)). Should `PublicKey` get an `EvmAddress toEvmAddress()`
  that throws for ED25519 keys, or should it live on `EvmAddress`?
