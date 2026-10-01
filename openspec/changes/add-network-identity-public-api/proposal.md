## Why

SDK repositories need a unified, language-neutral representation of a Hedera network's identity before building addressing and configuration features on top of it. Providing a standard `Network<T>` abstraction ensures all SDKs share a consistent model for tracking a network's ledger ID, its human-readable name, and the specific unit of its native token.

## What Changes

- Define a `Network<$$Unit>` type that strongly associates a network identity with its native token unit.
- Expose the network's `id` as an immutable byte array.
- Expose the network's `name` as an optional human-readable string.
- Expose the network's `nativeTokenUnit` to provide a type-safe reference to the base currency (e.g. Hbar/tinybar) native to this network.

## Capabilities

### New Capabilities

- `network-identity`: Defines the common structure for representing a network's identity and linking it to its native token.

### Modified Capabilities

None.

## Impact

- Supplies the common source for corresponding Java and TypeScript OpenSpec changes regarding network identity.
- Derives the network identity delta from `spec/base/ledger.md` without modifying the source specification.
- Forms the prerequisite foundation for building features like checksum validation (which requires a `Network` reference) and `NetworkSetting` configurations.
