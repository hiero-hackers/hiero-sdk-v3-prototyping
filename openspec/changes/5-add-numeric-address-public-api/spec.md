## Purpose

Define the language-neutral contract for foundational numeric addressing, covering `BaseAddress`, `Address`, format parsing, string serialization, and checksum validation. SDK-specific OpenSpec changes use this document as their behavioral starting point and add only language mapping requirements.

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

## Language-Neutral API Schema

```text
namespace ledger
requires {Network} from ledger

// Abstract base for every entity identifier that lives in the (shard, realm)-space of a Hiero ledger.
abstraction BaseAddress {
    @@immutable shard: uint64                                 // shard number
    @@immutable realm: uint64                                 // realm number
    @@immutable checksum: string                              // protocol-side checksum over the (shard, realm, selector) form; empty string when no checksum applies
    @@immutable @@nullable num: uint64                        // Hiero entity number.

    // Validates the checksum against the given network's checksum scheme.
    bool validateChecksum(network: Network<ANY>)

    // Canonical string form.
    string toString()

    // toString() with an appended "-<checksum>" suffix when a non-empty checksum is set.
    string toStringWithChecksum()
}

// Concrete address with a single numeric selector.
@@finalType
Address extends BaseAddress {
    @@immutable @@override num: uint64                        // tightening: on Address, num is always set
}

// Parses Address from string format: "shard.realm.num" or "shard.realm.num-checksum"
// @@throws(illegal-format) if format is invalid, values are negative, or parsing fails
@@throws(illegal-format) @@static Address Address.fromString(address: string)

// The zero address (0.0.0). Callers should not use this value as an actual entity id.
constant ZERO_ADDRESS: Address = Address{shard: 0, realm: 0, num: 0, checksum: ""}
```
