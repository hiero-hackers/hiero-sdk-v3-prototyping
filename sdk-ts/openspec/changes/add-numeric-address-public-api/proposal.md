## Source Reference

- Common proposal: [`openspec/changes/add-numeric-address-public-api/proposal.md`](../../../../openspec/changes/add-numeric-address-public-api/proposal.md)
- Common specification: [`openspec/changes/add-numeric-address-public-api/spec.md`](../../../../openspec/changes/add-numeric-address-public-api/spec.md)
- Source revision: `f27ddd82aa60479f4850dde4957ec9ac86e8fc88`
- Depends on: `add-network-identity-public-api` (TypeScript `Network`)
- External reference: [HIP-15 Address Checksum](https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-15.md)

## Why

SDK repositories need a unified, language-neutral representation of network addresses. The TypeScript SDK adopts the
foundational `BaseAddress` and concrete numeric `Address` so tokens, topics, files, and schedules share one exported,
immutable, parseable, and checksum-aware identifier before EVM-capable and account addresses build on it.

## What Changes

- Define `BaseAddress` as an abstract type containing `shard`, `realm`, optional `num`, and `checksum` alongside checksum
  validation and string representation methods.
- Define `Address` as a concrete subtype of `BaseAddress` that mandates a non-null `num`.
- Define a strict parsing method `fromString` for "shard.realm.num" strings with optional checksums.
- Expose the constant `ZERO_ADDRESS` (0.0.0).
- Map these to exported TypeScript `BaseAddress` and `Address` classes, a `ZERO_ADDRESS` constant, and an
  `IllegalFormatError` class for the common `illegal-format` error. All uint64 values are `bigint`.
- Define the TypeScript behavior the common specification leaves open:
  - Checksums follow HIP-15, calculated from the network's ledger `id` bytes.
  - `validateChecksum()` returns `false` for an address without a checksum.
  - `Address` is created through a static `Address.of()` factory; its constructor is private, which enforces
    `@@finalType` at compile time.
  - `fromString()` accepts only the canonical HIP-15 form.

## Capabilities

### New Capabilities

- `numeric-address`: Defines the foundational and concrete numeric addressing primitives, handling parsing, formatting,
  and checksum validation.

### Modified Capabilities

None.

## Impact

- Adds `BaseAddress`, `Address`, `ZERO_ADDRESS`, and `IllegalFormatError` to `@hiero-ledger/sdk-base-api`. No new
  runtime dependencies.
- `Address.of()` is a TypeScript addition not named in the common schema; it is the mapping of the
  `Address{shard, realm, num, checksum}` construction used by `ZERO_ADDRESS`.
- Does not modify `spec/base/ledger.md` or the common change.

### Intentionally Deferred

- `EvmCapableAddress`, `ContractId`, `AccountId`, and their sentinels (common deltas `add-evm-address-public-api`,
  `add-contract-id-public-api`, `add-account-id-public-api`).
- Producing a checksum for a given network (for example `toStringWithChecksum(network)`); not part of the common
  contract. Checksums are only parsed, stored, and validated.
- Equality, hashing, and comparison of addresses.
