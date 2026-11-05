## Source Reference

- Common proposal: [`openspec/changes/add-network-identity-public-api/proposal.md`](../../../../openspec/changes/add-network-identity-public-api/proposal.md)
- Common specification: [`openspec/changes/add-network-identity-public-api/spec.md`](../../../../openspec/changes/add-network-identity-public-api/spec.md)
- Source revision: `bd1c16017837047432bf7cf2aa44789769a14e29`
- Depends on: `add-native-token-public-api` (TypeScript `NativeTokenUnit`)

## Why

SDK repositories need a unified, language-neutral representation of a network's identity before building addressing
and configuration features on top of it. The TypeScript SDK adopts the common `Network<T>` contract so checksum
validation and network settings can rely on one exported, immutable network identity type.

## What Changes

- Define a `Network<$$Unit>` type that strongly associates a network identity with its native token unit.
- Expose the network's `id` as an immutable byte array.
- Expose the network's `name` as an optional human-readable string.
- Expose the network's `nativeTokenUnit` to provide a type-safe reference to the network's native token unit.
- Map these to an exported TypeScript `Network<Unit extends NativeTokenUnit>` class with a `Uint8Array` id that is
  defensively copied, a `string | null` name, and a `Unit` native token unit, following
  `guidelines/api-best-practices-js.md`.

## Capabilities

### New Capabilities

- `network-identity`: Defines the common structure for representing a network's identity and linking it to its native
  token.

### Modified Capabilities

None.

## Impact

- Adds `Network` to the existing `@hiero-ledger/sdk-base-api` package. No new runtime dependencies.
- Does not modify `spec/base/ledger.md` or the common change.

### Intentionally Deferred

- Predefined networks (mainnet, testnet, Solo) and their ledger ID bytes (common deltas `add-hedera-network-settings-public-api`
  and `add-solo-network-setting-public-api`).
- Rules for assigning ledger ID bytes; still an open question in `spec/base/ledger.md`.
- Equality, hashing, and string formatting of networks; not part of the common contract.
