## 1. Exhaustive HTTP Unit Tests

## 1. Exhaustive HTTP Unit Tests

- [x] 1.1 Update `tests/Http.test.ts` to test GET/POST behavior with and without body payloads.
- [x] 1.2 Test default headers blending with request-specific headers in `DefaultHttpClient`.
- [x] 1.3 Assert `client-closed-error` is thrown when executing a request on a closed client.
- [x] 1.4 Assert `client-closed-error` correctly aborts all in-flight requests immediately upon calling `close()`.
- [x] 1.5 Assert `timeout-error` is properly mapped when the AbortController fires via the timeout parameter.
- [x] 1.6 Assert `connection-error` is mapped for generic `fetch` rejections.

## 2. Exhaustive Cryptography Unit Tests

- [x] 2.1 Update `tests/Keys.test.ts` to test `illegal-format` errors for completely invalid hex, malformed Base64, and garbage text strings.
- [x] 2.2 Test mismatched formats: e.g., attempting to parse a Public Key using a `PKCS8` container, or parsing a Private Key using `SPKI`.
- [x] 2.3 Test signature tampering: manually modify a byte in a valid signature and assert `verify()` returns false.
- [x] 2.4 Test explicit failures in `DerEncoding.ts`: wrapping/unwrapping buffers that are too short to contain the required prefixes.
- [x] 2.5 Test explicit failures in `PemEncoding.ts`: missing `-----BEGIN...` boundaries or mismatched headers.

## 3. Exhaustive WebChannel Unit Tests

- [x] 3.1 Create `tests/WebChannel.test.ts`.
- [x] 3.2 Test `encodeRequest` by asserting the returned buffer starts exactly with `0x00` and the correct 4-byte length integer.
- [x] 3.3 Test `decodeUnaryResponse` with a valid mock gRPC-Web frame.
- [x] 3.4 Test `decodeUnaryResponse` with an invalid frame (e.g. throwing "No grpc-web data frame found").
- [x] 3.5 Test `_createUnaryClient` correctly parses `fetch` responses and routes to the callback.
- [x] 3.6 Test `_createUnaryClient` correctly routes HTTP `!response.ok` (like 404/500) into a callback error.
- [x] 3.7 Test `_createUnaryClient` correctly routes AbortController timeouts to a callback timeout error.
- [x] 3.8 Assert all lazy getters (`crypto`, `smartContract`, etc.) correctly instantiate and cache their respective `proto` services.

## 4. Exhaustive NodeChannel Unit Tests

- [x] 4.1 Create `tests/NodeChannel.test.ts`.
- [x] 4.2 Test lazy client initialization (verifying `@grpc/grpc-js` `Client` is only instantiated once).
- [x] 4.3 Test that deadline math correctly adds `grpcDeadline` milliseconds to the current time.
- [x] 4.4 Test that `makeUnaryRequest` errors trigger the callback with an error.
- [x] 4.5 Test that `makeUnaryRequest` successes correctly parse the buffer and trigger the callback with data.
- [x] 4.6 Test `close()` method terminates the grpc client cleanly.

## 5. Verification

- [x] 5.1 Run `npm run test` to verify all exhaustive unit tests pass instantaneously.
