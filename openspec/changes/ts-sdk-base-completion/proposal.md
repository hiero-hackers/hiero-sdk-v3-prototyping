## Why

We laid the foundation for the new V3 TypeScript SDK in `hiero-sdk-v3-ts` by configuring the build tools and implementing foundational types (Addresses, Authority, StreamItem). To bring the SDK up to par with the `spec/base` metadata specifications, we need to implement the remaining core domain entities found in the `base` namespace.

## What Changes

- Implement the remaining types from `spec/base` in TypeScript:
  - `keys.md` (e.g. `PrivateKey`, `PublicKey` hierarchies)
  - `native-token.md` (e.g. native token and HBAR equivalents)
  - `common.md` (e.g. `Page`, `Timestamp`, common primitives)
  - `token.md` (e.g. Token classifications)
- Ensure all types follow the immutability (`readonly`) and strict typing patterns we established.

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact

- Completes the `base` layer of the V3 SDK in TypeScript, allowing us to build the `consensus-node-client` and `mirror-node-client` implementations on top of it.
