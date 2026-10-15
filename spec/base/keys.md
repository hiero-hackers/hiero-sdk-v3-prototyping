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

    bytes decode(keyType : KeyType, value : string)
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
    
    bytes decode(value : string)
}

// combined container format and encoding
// container: the container format; encoding: the encoding
enum KeyFormat(container: KeyContainer, encoding: KeyEncoding) {
    PKCS8_WITH_DER(PKCS8, DER),
    SPKI_WITH_DER(SPKI, DER),
    PKCS8_WITH_PEM(PKCS8, PEM),
    SPKI_WITH_PEM(SPKI, PEM)
    
    bool supportsType(type: KeyType) // returns true if the internal container format supports the given key type
    bytes decode(keyType : KeyType, value : string) // decodes the given string value into raw bytes for the given key type
  
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

Known-answer tests of the default key, written as given / when / then: the expected values are the raw bytes of the
default instance and the PKCS#8 encoding listed under "Key examples". What the spec does not state as a value (the
derived public key, a signature) is checked by the properties the API promises.

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
```

## Questions & Comments

- **No EVM address of an ECDSA public key.** The EVM address of an account with an ECDSA (secp256k1)
  key is derived from the public key (the last 20 bytes of the Keccak-256 hash of the uncompressed
  key), e.g. to create an account by its EVM-address alias. The API has no such derivation, so the
  TCK's `generateKey` with type `evmAddress` cannot be answered (see
  [`tck-binding.md`](../../docs/tck-binding.md)). Should `PublicKey` get an `EvmAddress toEvmAddress()`
  that throws for ED25519 keys, or should it live on `EvmAddress`?
