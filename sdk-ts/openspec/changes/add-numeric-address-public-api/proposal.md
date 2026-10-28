## Why

SDK repositories need a unified, language-neutral representation of network addresses. Defining the foundational `BaseAddress` and concrete numeric `Address` provides a strict type hierarchy for entity addressing (e.g. for topics, tokens, and schedules) before building specialized types like EVM or Account aliases on top of them. This proposal adapts the common `add-numeric-address-public-api` change for TypeScript.

## Source Reference

- Common proposal: `../../openspec/changes/add-numeric-address-public-api/proposal.md`
- Common spec: `../../openspec/changes/add-numeric-address-public-api/spec.md`
- Source revision: `f27ddd82aa60479f4850dde4957ec9ac86e8fc88`

## What Changes

- Define `BaseAddress` abstract base class with immutable `shard` (`bigint`), `realm` (`bigint`), optional `num` (`bigint | null`), and `checksum` (`string`).
- Define `Address` concrete class tightening `num` to non-nullable `bigint`.
- Implement `Address.fromString(address: string)` parsing `shard.realm.num` and optional `-<checksum>`.
- Implement `validateChecksum(network: Network<any>): boolean` computing HIP-15 checksum over `shard.realm.num` against `network.id`.
- Implement `toString(): string` and `toStringWithChecksum(): string`.
- Define `IllegalFormatError` for format and parsing failures.
- Expose the constant `ZERO_ADDRESS` (0.0.0) as a frozen clear-sentinel.
- Export addressing contracts and types from `src/base/index.ts` and `src/index.ts`.

## Capabilities

### New Capabilities

- `numeric-address`: Defines the TypeScript public API contracts for foundational and numeric addressing, string parsing, formatting, and checksum validation.

### Modified Capabilities

None.

## Impact

- Exports `BaseAddress`, `Address`, `ZERO_ADDRESS`, and `IllegalFormatError` from `src/base/ledger.ts`.
- Defers EVM addresses (`EvmAddress`), EVM capable addresses (`EvmCapableAddress`), `ContractId`, and `AccountId` to subsequent changes.
