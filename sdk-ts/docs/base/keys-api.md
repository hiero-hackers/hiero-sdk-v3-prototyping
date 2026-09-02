# Keys API

The Keys API provides functionality to create and manage cryptographic keys in the Hiero SDK V3.
A cryptographic key is defined by a byte sequence and a cryptographic algorithm. 
Using the Keys API, you can generate keys, sign messages, verify signatures, and export/import keys across various standard formats like DER and PEM.

## Algorithms

The SDK supports the following key algorithms, accessible via the `KeyAlgorithm` enum:
- `KeyAlgorithm.ED25519` (Edwards-curve Digital Signature Algorithm)
- `KeyAlgorithm.ECDSA` (Elliptic Curve Digital Signature Algorithm, secp256k1)

## Generating Keys

You can randomly generate new private or public keys using the factory methods:

```typescript
import { KeyAlgorithm, generatePrivateKey, generatePublicKey } from '@hiero/sdk/base/keys';

// Generate a random ED25519 private key
const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);

// Generate a random ECDSA private key
const ecdsaPrivateKey = generatePrivateKey(KeyAlgorithm.ECDSA);

// Generate a random ED25519 public key independently
const publicKey = generatePublicKey(KeyAlgorithm.ED25519);
```

You can derive the corresponding public key from any private key using `createPublicKey()`:

```typescript
const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
const publicKey = privateKey.createPublicKey();
```

## Signing and Verifying Messages

Use `PrivateKey.sign()` to digitally sign a message (as a `Uint8Array`), and `PublicKey.verify()` to verify the signature.

```typescript
import { Buffer } from 'node:buffer';

const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
const publicKey = privateKey.createPublicKey();

const message = Buffer.from('hello hiero');

// Sign the message
const signature = privateKey.sign(message);

// Verify the signature
const isValid = publicKey.verify(message, signature); // returns true
```

## Exporting Keys

Keys can be exported into different formats depending on your needs. The API supports raw bytes, standard container formats (PKCS8 for private keys, SPKI for public keys), and standard encodings (DER for binary, PEM for string).

### Exporting as Raw Bytes

```typescript
const rawBytes = privateKey.toRawBytes();
```

### Exporting to Formatted Containers

Use `toBytes()` for binary formats (DER) and `toString()` for string formats (PEM). 
The `KeyFormat` enum combines the container structure and encoding rules:
- `KeyFormat.PKCS8_WITH_PEM` (Private Keys, String)
- `KeyFormat.PKCS8_WITH_DER` (Private Keys, Bytes)
- `KeyFormat.SPKI_WITH_PEM` (Public Keys, String)
- `KeyFormat.SPKI_WITH_DER` (Public Keys, Bytes)

```typescript
import { KeyFormat } from '@hiero/sdk/base/keys';

// Export Private Key to a PEM string
const pemString = privateKey.toString(KeyFormat.PKCS8_WITH_PEM);
// "-----BEGIN PRIVATE KEY-----
// ...
// -----END PRIVATE KEY-----"

// Export Public Key to a DER byte array
const derBytes = publicKey.toBytes(KeyFormat.SPKI_WITH_DER);
```

> **Note**: Trying to export a public key into a private key container (`PKCS8`) or vice versa will throw an `IllegalFormatError`.

## Importing Keys

You can restore keys from strings or bytes using the `createPrivateKey` and `createPublicKey` factory functions. 

### Importing from PEM Strings (Default)

If you simply pass a string, the SDK defaults to parsing it as a standard PEM block.

```typescript
const importedPriv = createPrivateKey("-----BEGIN PRIVATE KEY...-----");
const importedPub = createPublicKey("-----BEGIN PUBLIC KEY...-----");
```

### Importing from Explicit Container Formats

If you have raw DER bytes, provide the explicit container format:

```typescript
import { KeyFormat, createPublicKey } from '@hiero/sdk/base/keys';

// Import from SPKI DER bytes
const publicKey = createPublicKey(KeyFormat.SPKI_WITH_DER, derBytes);
```

### Importing from Raw Bytes

If you already have the raw bytes of the key (not enclosed in a DER container), you must specify the algorithm:

```typescript
import { KeyAlgorithm, createPrivateKey } from '@hiero/sdk/base/keys';

const privateKey = createPrivateKey(KeyAlgorithm.ED25519, rawBytes);
```

### Importing from Hex or Base64 Strings

You can import keys directly from Hex or Base64 encoded strings representing their raw bytes using `ByteImportEncoding`:

```typescript
import { KeyAlgorithm, ByteImportEncoding, createPrivateKey } from '@hiero/sdk/base/keys';

const hexString = "302e020100300506...";
const privateKey = createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, hexString);
```
