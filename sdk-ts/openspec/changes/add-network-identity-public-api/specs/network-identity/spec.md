## Purpose

Define the TypeScript contract for network identity, preserving the common network identity behavior and adding only
the TypeScript language mapping.

## ADDED Requirements

### Requirement: Immutable network identity state

The SDK SHALL expose a `Network<$$Unit>` type containing immutable `id`, `name`, and `nativeTokenUnit` values. The
`$$Unit` generic parameter MUST extend `NativeTokenUnit` (as defined in the `native-token` capability).

#### Scenario: Network identity state is observed

- **WHEN** a caller reads the state of an instantiated network identity object
- **THEN** the caller receives the byte array `id`, the optional string `name`, and the `NativeTokenUnit` instance
  without being able to modify the network's stored state

### Requirement: TypeScript network mapping

The TypeScript SDK SHALL export `Network<Unit extends NativeTokenUnit>` as a class constructed with
`(id: Uint8Array, name: string | null, nativeTokenUnit: Unit)` and exposing read-only `id: Uint8Array`,
`name: string | null`, and `nativeTokenUnit: Unit` accessors.

#### Scenario: Network is constructed and read

- **WHEN** a network is constructed with id bytes, a name or `null`, and a native token unit
- **THEN** `id` returns the same bytes, `name` returns the same string or `null`, and `nativeTokenUnit` returns the same
  unit instance

### Requirement: TypeScript byte-array immutability

Because a `Uint8Array` cannot be frozen, the TypeScript SDK SHALL copy the id bytes at construction and SHALL return a
new copy from every read of `id`. Changes to the array passed at construction, or to an array returned by `id`, SHALL
NOT change the network's stored id.

#### Scenario: Id bytes are modified by a caller

- **WHEN** a caller modifies the array passed to the constructor, or modifies an array returned by `id`
- **THEN** a later read of `id` returns the original bytes

### Requirement: TypeScript input validation

Constructing a network SHALL throw a `TypeError` when `id` is not a `Uint8Array`, when `name` is neither a string nor
`null` (including `undefined`), or when `nativeTokenUnit` is not a `NativeTokenUnit`. No other constraints are added:
an empty id and an empty name are accepted.

#### Scenario: Invalid input is rejected

- **WHEN** a network is constructed with a non-`Uint8Array` id, an `undefined` name, or a value that is not a
  `NativeTokenUnit`
- **THEN** construction throws a `TypeError`

### Requirement: TypeScript wildcard mapping

A `Network<Unit>` for any concrete unit type SHALL be assignable to `Network<NativeTokenUnit>`, which is the TypeScript
mapping of the common `Network<ANY>` wildcard.

#### Scenario: A specific network is passed where any network is accepted

- **WHEN** a `Network` of a concrete unit type is passed to a parameter typed `Network<NativeTokenUnit>`
- **THEN** the code compiles without a cast
