## 1. Refactor Cryptography Implementations

- [x] 1.1 Move the `src/base/crypto/` directory to `src/base/internal/crypto/`.
- [x] 1.2 Update imports in `src/base/Keys.ts` to point to `src/base/internal/crypto/Ed25519.js` and `src/base/internal/crypto/Ecdsa.js`.

## 2. Refactor HTTP Implementations

- [x] 2.1 Create `src/base/internal/http/DefaultHttpClient.ts`.
- [x] 2.2 Extract the `DefaultHttpClient` class from `src/base/Http.ts` into the new file.
- [x] 2.3 Update `src/base/Http.ts` to import `DefaultHttpClient` from `src/base/internal/http/DefaultHttpClient.js` for use in the `createHttpClient` factory.

## 3. Testing & Verification

- [x] 3.1 Update any test imports if they were importing directly from `crypto/` (none expected, but verify).
- [x] 3.2 Run `npm run test` to ensure tests still pass.
- [x] 3.3 Run `npx tsc` to verify no compilation errors.
