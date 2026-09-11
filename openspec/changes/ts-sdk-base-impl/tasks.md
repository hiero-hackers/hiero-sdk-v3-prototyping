## 1. HTTP Implementation

- [x] 1.1 Add logic to `createHttpClient` in `src/base/Http.ts` to return an object implementing `HttpClient`. Use `fetch` and `AbortController` to handle timeouts, returning an `HttpResponse`.
- [x] 1.2 Write a Vitest test mocking `fetch` to verify that `HttpClient.execute()` correctly handles successful responses and timeout errors.

## 2. Cryptography Implementation

- [x] 2.1 Install `@noble/curves` as a dependency.
- [x] 2.2 Create `src/base/crypto/Ed25519PrivateKey.ts` and `Ed25519PublicKey.ts` implementing the `PrivateKey` and `PublicKey` interfaces using `@noble/curves/ed25519`.
- [x] 2.3 Create `src/base/crypto/EcdsaPrivateKey.ts` and `EcdsaPublicKey.ts` implementing the same interfaces using `@noble/curves/secp256k1`.
- [x] 2.4 Update `generatePrivateKey` and `generatePublicKey` in `src/base/Keys.ts` to instantiate these concrete classes based on the `KeyAlgorithm` enum.
- [x] 2.5 Write tests verifying that generated keys can successfully sign and verify payloads.

## 3. Integration & Code Quality

- [x] 3.1 Run `npm run test` (Vitest) to verify HTTP and Crypto implementations.
- [x] 3.2 Run `npx tsc` and ESLint to ensure strict type safety and encapsulation are maintained.
