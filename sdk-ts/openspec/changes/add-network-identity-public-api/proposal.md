## Why

SDK repositories need a unified, language-neutral representation of a Hedera network's identity before building addressing and configuration features on top of it. This proposal adapts the common `add-network-identity-public-api` change to provide an idiomatic, strongly-typed TypeScript representation for `Network<T>` that binds a network's ledger ID, optional human-readable name, and native token unit.

## Source Reference

- Common proposal: `../../openspec/changes/add-network-identity-public-api/proposal.md`
- Common spec: `../../openspec/changes/add-network-identity-public-api/spec.md`
- Source revision: `bd1c16017837047432bf7cf2aa44789769a14e29`

## What Changes

- Define `NativeTokenUnit` interface representing a unit of a native token with `symbol` and `baseUnitFactor`.
- Define `Network<Unit extends NativeTokenUnit>` interface exposing immutable `id` (`Uint8Array`), optional `name` (`string | null`), and `nativeTokenUnit` (`Unit`).
- Provide an immutable factory or implementation class ensuring defensive copying of `id` on construction and access so stored network state cannot be mutated.
- Export public contracts from `src/base/index.ts` while keeping concrete implementation internal.
- Establish the minimal TypeScript build and testing setup for the `sdk-ts` workspace.

## Capabilities

### New Capabilities

- `network-identity`: Defines the TypeScript public API contracts for network identity and native token unit association.

### Modified Capabilities

None.

## Impact

- Initializes the `sdk-ts` workspace with package, compilation, and testing configuration.
- Introduces public exports for `Network` and `NativeTokenUnit`.
- Defers checksum calculation, address validation, network settings, endpoints, and node channels to subsequent delta changes.
