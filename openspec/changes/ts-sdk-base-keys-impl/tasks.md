## 1. Implement Key Format Utilities

## 1. Implement Key Format Utilities

- [x] 1.1 Create `src/base/internal/crypto/DerEncoding.ts` to handle static DER prefix wrapping and unwrapping for PKCS8 and SPKI, mimicking the legacy SDK's byte-parsing logic.
- [x] 1.2 Create `src/base/internal/crypto/PemEncoding.ts` to handle translating DER bytes to/from Base64 PEM strings with appropriate headers.

## 2. Update Cryptography Classes

- [x] 2.1 Update `Ed25519PrivateKey` and `Ed25519PublicKey` to implement `toBytes` and `toString` using `DerEncoding` and `PemEncoding` based on the requested `KeyFormat`.
- [x] 2.2 Update `EcdsaPrivateKey` and `EcdsaPublicKey` to implement `toBytes` and `toString` similarly.

## 3. Update Public API Factories

- [x] 3.1 Update `src/base/Keys.ts` to implement the `createPrivateKey` and `createPublicKey` factory methods, detecting formats and parsing via the new utilities.

## 4. Integration

- [x] 4.1 Write comprehensive tests in `tests/Keys.test.ts` to verify importing and exporting in DER and PEM formats.
- [x] 4.2 Run `npm run test` and `npx tsc` to verify functionality.
