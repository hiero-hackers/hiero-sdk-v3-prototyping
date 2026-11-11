## Purpose

Define the TypeScript contract for foundational numeric addressing, covering BaseAddress, Address, format parsing, string serialization, and checksum validation.

## ADDED Requirements

### Requirement: Immutable numeric address state

The SDK SHALL expose a `BaseAddress` abstraction and an `Address` concrete type containing immutable `shard`, `realm`, `num`, and `checksum` properties. On `Address`, `num` MUST NOT be null.

#### Scenario: Address state is observed

- **Given** an instantiated `Address` object.
- **When** a caller reads the address state.
- **Then** the caller receives the numeric `shard`, `realm`, and `num` alongside the optional string `checksum` without being able to modify the address's stored state.

### Requirement: Address parsing

The SDK SHALL expose a static `fromString()` factory capable of parsing string representations into `Address` objects.

#### Scenario: Address is parsed from string

- **Given** a string in the format "shard.realm.num" or "shard.realm.num-checksum".
- **When** a caller invokes `fromString()`.
- **Then** the operation returns a correctly populated `Address` object.

#### Scenario: Invalid address parsing fails

- **Given** an invalid string format.
- **When** a caller invokes `fromString()`.
- **Then** the operation terminates with an `illegal-format` error.

### Requirement: Checksum validation

The SDK SHALL expose a `validateChecksum()` method on the address.

#### Scenario: Checksum is validated

- **Given** an address with a checksum and a target `Network<ANY>`.
- **When** the caller invokes `validateChecksum(network)`.
- **Then** the operation calculates the expected checksum for that network and returns true if it matches the stored checksum.

### Requirement: Sentinel zero address

The SDK SHALL expose a `ZERO_ADDRESS` constant representing `0.0.0` with an empty checksum.

#### Scenario: Sentinel address is used

- **Given** the `ZERO_ADDRESS` constant.
- **When** referenced in code.
- **Then** it evaluates to a valid `Address` with `shard: 0`, `realm: 0`, and `num: 0`.

## TypeScript API Mapping

| Common Meta-Language | TypeScript Contract |
| --- | --- |
| `abstraction BaseAddress` | `abstract class BaseAddress` |
| `shard: uint64` (immutable) | `readonly shard: bigint` |
| `realm: uint64` (immutable) | `readonly realm: bigint` |
| `checksum: string` (immutable) | `readonly checksum: string` |
| `num: uint64` (nullable, immutable) | `readonly num: bigint \| null` |
| `bool validateChecksum(network)` | `validateChecksum(network: Network<any>): boolean` |
| `string toString()` | `toString(): string` |
| `string toStringWithChecksum()` | `toStringWithChecksum(): string` |
| `Address extends BaseAddress` | `class Address extends BaseAddress` |
| `@@override num: uint64` | `declare readonly num: bigint` (tightened to non-null) |
| `Address.fromString(address: string)` | `static fromString(address: string): Address` |
| `constant ZERO_ADDRESS: Address` | `const ZERO_ADDRESS: Address` |
| `@@throws(illegal-format)` | `throws IllegalFormatError` |
