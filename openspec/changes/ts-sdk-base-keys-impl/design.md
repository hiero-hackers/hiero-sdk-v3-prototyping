## Context

The previous SDK relied on complex third-party ASN.1 parsers (`asn1js`) and `forge-light` for handling PEMs, primarily because it supported encrypted private keys (which are out of scope for the new `spec/base/keys.md`). Since we only need to support standard PKCS8 for Private Keys and SPKI for Public Keys (for Ed25519 and ECDSA secp256k1), we can dramatically simplify the encoding/decoding logic using static DER prefixes.

## Goals / Non-Goals

**Goals:**
- Implement DER and PEM parsing exactly matching the behavior expected by `KeyFormat.PKCS8_WITH_DER` / `PEM` and `SPKI_WITH_DER` / `PEM`.
- Throw `illegal-format` errors for mismatches (e.g., trying to read a Public Key from a PKCS8 container).

**Non-Goals:**
- Implementing password-protected (encrypted) PEM decryption, as this is not in the `spec/base/keys.md` API schema.

## Decisions

### 1. Simplify DER Parsing
**Decision:** We will use static prefix matching for Ed25519 and ECDSA DER headers to extract the raw key bytes, similar to how `temp/hiero-sdk-js/packages/cryptography/src/Ed25519PrivateKey.js` manually validates the 16-byte Ed25519 prefix. This avoids heavy ASN.1 dependencies while ensuring full spec compliance.

### 2. PEM Formatting
**Decision:** PEM formatting will simply be Base64-encoding the DER bytes and wrapping them in standard `-----BEGIN PRIVATE KEY-----` / `-----END PRIVATE KEY-----` headers (or `PUBLIC KEY`).
