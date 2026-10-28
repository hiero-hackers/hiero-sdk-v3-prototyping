## Why

SDKs need a language-idiomatic way to represent a network identity — its ledger ID, optional
human-readable name, and native token unit — grounded in the language-neutral contract defined
in the common `add-network-identity-public-api` change. This TypeScript change derives the type
shapes and behavioral requirements from that common specification and adds the TypeScript-specific
contract mappings needed to ship a correct, idiomatic public API.

## What Changes

- Expose a `Network<Unit extends NativeTokenUnit>` interface with immutable `id`, `name`, and `nativeTokenUnit`.
- `id` is a `Uint8Array` (defensive copy on both construction and read).
- `name` maps the nullable `string` to `string | null` (never `undefined`).
- Provide a `createNetwork` factory function as the sole public construction path.
- Keep concrete implementation internal; export only the public contract.

## Capabilities

### New Capabilities

- `network-identity`: TypeScript public interface for network identity, linked to `NativeTokenUnit`.

### Modified Capabilities

None.

## Impact

- Derived from the common `add-network-identity-public-api` proposal and specification.
- Requires `NativeTokenUnit` from `add-native-token-public-api` (already complete).
- Does not introduce concrete network instances (e.g., Mainnet, Testnet) — those belong to network-specific changes.
