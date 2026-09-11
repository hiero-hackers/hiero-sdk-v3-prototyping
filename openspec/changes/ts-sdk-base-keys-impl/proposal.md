## Why

The Keys API in `src/base/Keys.ts` (and the `toBytes` / `toString` methods in the crypto classes) currently use mock or incomplete implementations for handling KeyFormats (DER/PEM encoding and PKCS8/SPKI containers). We need to complete this using the previous SDK's logic as a reference to properly support importing and exporting cryptographic keys across environments.

## What Changes

- Complete `createPrivateKey` and `createPublicKey` factories in `src/base/Keys.ts` to properly handle string and byte encodings.
- Complete the `toBytes` and `toString` methods in `Ed25519PrivateKey`, `Ed25519PublicKey`, `EcdsaPrivateKey`, and `EcdsaPublicKey`.
- Extract or replicate the DER and PEM encoding/decoding logic from the legacy `@hiero-ledger/cryptography` package to handle standard PKCS8 and SPKI wrapping without bringing in heavyweight external ASN.1 dependencies if we can use static prefixes.

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact

- Allows developers to seamlessly import and export keys using industry-standard PEM and DER formats, exactly as the V3 meta-language specification outlines.
