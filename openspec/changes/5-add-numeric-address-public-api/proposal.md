## Why

SDK repositories need a unified, language-neutral representation of network addresses. Defining the foundational `BaseAddress` and concrete numeric `Address` provides a strict type hierarchy for entity addressing (e.g. for topics, tokens, and schedules) before building specialized types like EVM or Account aliases on top of them.

## What Changes

- Define `BaseAddress` as an abstract type containing `shard`, `realm`, optional `num`, and `checksum` alongside checksum validation and string representation methods.
- Define `Address` as a concrete subtype of `BaseAddress` that mandates a non-null `num`.
- Define a strict parsing method `fromString` capable of parsing "shard.realm.num" strings with optional checksums.
- Expose the constant `ZERO_ADDRESS` (0.0.0) serving as a network-recognized clear-sentinel for address-typed fields.

## Capabilities

### New Capabilities

- `numeric-address`: Defines the foundational and concrete numeric addressing primitives for Hedera networks, handling parsing, formatting, and checksum validation.

### Modified Capabilities

None.

## Impact

- Supplies the common source for corresponding Java and TypeScript OpenSpec changes regarding addressing.
- Derives the numeric address delta from `spec/base/ledger.md` without modifying the source specification.
- Forms the prerequisite foundation for building more complex addressing forms (like EVM capable addresses and Account IDs).
