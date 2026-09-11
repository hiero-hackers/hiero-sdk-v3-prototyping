## Context

We need concrete implementations of `HttpClient` and cryptographic `Key`s (ED25519, ECDSA) that work seamlessly in both Node.js and browser environments (isomorphic).

## Goals / Non-Goals

**Goals:**
- Implement `createHttpClient` using the native `fetch` API, which is standard in modern browsers and Node 18+.
- Implement cryptographic signing and verification using `@noble/curves` or standard WebCrypto/Node crypto, ensuring it is purely isomorphic.

**Non-Goals:**
- Writing our own cryptographic primitives from scratch (we will use established, audited packages).

## Decisions

### 1. HTTP Transport
**Decision:** We will use the built-in `fetch` API. It satisfies the need for an isomorphic transport layer without adding heavy dependencies like `axios`. We will map HTTP status codes and `AbortController` timeouts to the SDK's `HttpResponse` and `timeout-error`.

### 2. Cryptography
**Decision:** We will rely on `@noble/curves` (specifically `ed25519` and `secp256k1`) as our core dependency for key generation, signing, and verification because it is dependency-free, audited, and strictly isomorphic.

## Risks / Trade-offs
- `fetch` in Node.js might have slight behavioral differences compared to browsers (e.g., handling of connection limits). We will rely on standard `AbortController` for handling the `connectTimeout` and `requestTimeout`.
