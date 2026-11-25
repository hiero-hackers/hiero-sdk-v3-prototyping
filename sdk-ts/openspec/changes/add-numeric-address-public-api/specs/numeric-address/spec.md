## Purpose

Define the TypeScript contract for foundational numeric addressing, preserving the common `BaseAddress`, `Address`,
parsing, formatting, and checksum behavior and adding only the TypeScript language mapping.

## ADDED Requirements

### Requirement: Immutable numeric address state

The SDK SHALL expose a `BaseAddress` abstraction and an `Address` concrete type containing immutable `shard`, `realm`,
`num`, and `checksum` properties. On `Address`, `num` MUST NOT be null.

#### Scenario: Address state is observed

- **WHEN** a caller reads the state of an instantiated `Address` object
- **THEN** the caller receives the numeric `shard`, `realm`, and `num` alongside the optional string `checksum` without
  being able to modify the address's stored state

### Requirement: Address parsing

The SDK SHALL expose a static `fromString()` factory capable of parsing string representations into `Address` objects.

#### Scenario: Address is parsed from string

- **WHEN** a caller invokes `fromString()` with a string in the format "shard.realm.num" or "shard.realm.num-checksum"
- **THEN** the operation returns a correctly populated `Address` object

#### Scenario: Invalid address parsing fails

- **WHEN** a caller invokes `fromString()` with an invalid string format
- **THEN** the operation terminates with an `illegal-format` error

### Requirement: Checksum validation

The SDK SHALL expose a `validateChecksum()` method on the address.

#### Scenario: Checksum is validated

- **WHEN** the caller invokes `validateChecksum(network)` on an address with a checksum and a target `Network<ANY>`
- **THEN** the operation calculates the expected checksum for that network and returns true if it matches the stored
  checksum

### Requirement: Sentinel zero address

The SDK SHALL expose a `ZERO_ADDRESS` constant representing `0.0.0` with an empty checksum.

#### Scenario: Sentinel address is used

- **WHEN** the `ZERO_ADDRESS` constant is referenced in code
- **THEN** it evaluates to a valid `Address` with `shard: 0`, `realm: 0`, and `num: 0`

### Requirement: TypeScript address mapping

The TypeScript SDK SHALL export `BaseAddress` as an abstract class with read-only `shard: bigint`, `realm: bigint`,
`checksum: string`, and `num: bigint | null` accessors, and abstract `toString()` and
`validateChecksum(network: Network<NativeTokenUnit>)` methods. It SHALL export `Address` extending `BaseAddress` with
`num` narrowed to `bigint`. `shard`, `realm`, and `num` SHALL be in the uint64 range `0` to `2^64 - 1`.

#### Scenario: Address num is narrowed

- **WHEN** a consumer reads `num` from a value typed `Address`
- **THEN** the value is typed `bigint` without `null`, while the same read through `BaseAddress` is typed
  `bigint | null`

### Requirement: TypeScript address construction

The TypeScript SDK SHALL expose `Address.of(shard: bigint, realm: bigint, num: bigint, checksum: string = "")`. The
`Address` constructor SHALL be private so that `Address` cannot be extended or constructed directly. `Address.of()` SHALL
throw a `TypeError` for a value that is not a `bigint` or a checksum that is not a string, a `RangeError` for a value
outside the uint64 range, and an `IllegalFormatError` for a checksum that is neither empty nor exactly five lowercase
ASCII letters.

#### Scenario: Address is constructed directly

- **WHEN** a consumer calls `Address.of(0n, 0n, 123n, "vfmkw")`
- **THEN** the result is an `Address` with shard `0n`, realm `0n`, num `123n`, and checksum `"vfmkw"`

#### Scenario: Invalid construction is rejected

- **WHEN** a consumer calls `Address.of()` with `-1n`, `2n ** 64n`, a `number`, or the checksum `"VFMKW"`
- **THEN** the call throws a `RangeError`, `RangeError`, `TypeError`, or `IllegalFormatError` respectively

### Requirement: TypeScript illegal-format mapping

The TypeScript SDK SHALL map `illegal-format` to an exported `IllegalFormatError` class that extends `Error`, has the
`name` `"IllegalFormatError"`, and preserves a message and an optional `cause`.

#### Scenario: Parsing fails

- **WHEN** `Address.fromString()` cannot parse its input
- **THEN** it throws an `IllegalFormatError` detectable with `instanceof`

### Requirement: Canonical parsing

`Address.fromString()` SHALL accept exactly the strings matching
`^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-[a-z]{5})?$` whose three numbers are each at most `2^64 - 1`, and
SHALL throw an `IllegalFormatError` for every other string. It SHALL throw a `TypeError` when its argument is not a
string. Parsing SHALL NOT validate the checksum against any network.

#### Scenario: Non-canonical input is rejected

- **WHEN** `fromString()` is called with `"0.00.123"`, `"0.0.0123"`, `"0.0.123-VFMKW"`, `"0.0.123 - vfmkw"`, `"0.123"`,
  `"0.0.123-vf"`, `"-1.0.0"`, or `"0.0.18446744073709551616"`
- **THEN** it throws an `IllegalFormatError`

#### Scenario: Parsing and formatting round-trip

- **WHEN** a canonical string is parsed and then formatted with `toStringWithChecksum()`
- **THEN** the result equals the original string

### Requirement: String formatting

`toString()` SHALL return `"<shard>.<realm>.<num>"` in decimal. `toStringWithChecksum()` SHALL return `toString()`
followed by `"-<checksum>"` when the checksum is non-empty, and `toString()` alone when it is empty.

#### Scenario: Address without checksum is formatted

- **WHEN** `toStringWithChecksum()` is called on `ZERO_ADDRESS`
- **THEN** it returns `"0.0.0"`

### Requirement: HIP-15 checksum validation

`Address.validateChecksum(network)` SHALL compute the HIP-15 checksum of `toString()` using `network.id` as the ledger
id, and SHALL return `true` only when the address's checksum is non-empty and equal to that computed checksum. It SHALL
return `false` when the address has no checksum. It SHALL throw a `TypeError` when `network` is not a `Network`.

#### Scenario: HIP-15 reference checksums are validated

- **WHEN** `validateChecksum()` is called for the HIP-15 reference vectors, for example `0.0.123-vfmkw` with ledger id
  `0x00` and `0.0.123-pzmtv` with ledger id `0xa1ff01`
- **THEN** it returns `true` for the matching ledger id and `false` for the other

#### Scenario: Address without checksum is validated

- **WHEN** `validateChecksum()` is called on an address whose checksum is empty
- **THEN** it returns `false`
