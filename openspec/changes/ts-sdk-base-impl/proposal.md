## Why

With the abstract type hierarchies from `spec/base` complete, we now need to supply the concrete implementations for the factory methods defined by the spec. This means turning our abstract `HttpClient` and `Key` interfaces into functional code that can actually perform network requests and cryptographic signing.

## What Changes

- Concrete implementations for HTTP transport using standard Web APIs (`fetch`).
- Concrete implementations for Cryptographic Keys (ED25519 and ECDSA secp256k1) using standard isomorphic cryptography libraries.
- Implementation of the factory methods `createHttpClient`, `generatePrivateKey`, and `createPrivateKey`.

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact

- Provides the actual runtime behavior for the SDK's transport and cryptography layers, allowing higher-level namespaces to perform network calls and sign transactions.
