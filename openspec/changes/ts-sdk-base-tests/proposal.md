## Why

The `base` SDK forms the foundational layer for all Hedera network interactions. To ensure absolute reliability and mathematical correctness, we must subject it to an exhaustive, vast variety of unit tests. We need to go beyond basic "happy path" testing and strictly assert every edge case, error state, and boundary condition in the networking and cryptography primitives.

## What Changes

- Add exhaustive unit tests for `DefaultHttpClient` covering all lifecycle states, timeouts, and header permutations.
- Add exhaustive unit tests for Cryptography (`Keys.ts`, `PemEncoding.ts`, `DerEncoding.ts`) covering all malformed inputs, mismatched formats, and invalid signatures.
- Add exhaustive unit tests for `WebChannel` covering exact binary byte-framing assertions and fetch failure scenarios.
- Add exhaustive unit tests for `NodeChannel` covering gRPC client initialization, deadline math, and callback wrapping.
- Add compilation tests to ensure pure metadata interfaces (`Address`, `Authority`, etc.) can be successfully extended.

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact

- Provides a completely bulletproof test harness that rigorously challenges every branch of the `base` SDK logic, cementing the stability of the foundation without relying on external network dependencies.
