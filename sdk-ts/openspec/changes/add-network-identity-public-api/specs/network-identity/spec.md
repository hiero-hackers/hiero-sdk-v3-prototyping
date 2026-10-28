## Purpose

Define the TypeScript contract for network identity, ensuring a strongly-typed and immutable representation of a network's ledger ID, name, and native token unit.

## ADDED Requirements

### Requirement: Immutable network identity state

The SDK SHALL expose a `Network<Unit extends NativeTokenUnit>` type containing immutable `id`, `name`, and `nativeTokenUnit` values. The `Unit` generic parameter MUST extend `NativeTokenUnit`.

#### Scenario: Network identity state is observed

- **WHEN** a caller reads the state of an instantiated `Network` object
- **THEN** the caller receives the `id` as `Uint8Array`, the optional `name` as `string | null`, and the `nativeTokenUnit` instance without being able to modify the network's stored state

### Requirement: Defensive copying of network identity bytes

The SDK SHALL protect the stored network `id` byte array from external modification through defensive copying.

#### Scenario: Caller mutates input or output byte array

- **WHEN** a caller modifies the `Uint8Array` passed into the network constructor or mutates the `Uint8Array` retrieved from the network
- **THEN** the internal state of the `Network` instance remains unchanged

## TypeScript API Mapping

| Common Meta-Language | TypeScript Contract |
| --- | --- |
| `Network<$$Unit extends NativeTokenUnit>` | `interface Network<Unit extends NativeTokenUnit>` |
| `id: bytes` (immutable) | `readonly id: Uint8Array` (defensively copied) |
| `name: string` (nullable, immutable) | `readonly name: string | null` |
| `nativeTokenUnit: $$Unit` (immutable) | `readonly nativeTokenUnit: Unit` |
| `NativeTokenUnit` abstraction | `interface NativeTokenUnit { readonly symbol: string; readonly baseUnitFactor: bigint | number; }` |
