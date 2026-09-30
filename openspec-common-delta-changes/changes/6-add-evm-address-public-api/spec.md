## Purpose

Define the shared behavior for constructing, retaining, parsing, and formatting standalone EVM address values across
supported SDKs.

## ADDED Requirements

### Requirement: Immutable EVM address state

The SDK SHALL expose an `EvmAddress` value containing exactly 20 raw address bytes in network byte order. The stored
bytes SHALL be immutable and SHALL NOT be exposed in a way that permits modification of the address.

#### Scenario: EVM address state is observed

- **GIVEN** an EVM address created from 20 raw bytes
- **WHEN** a caller reads its bytes
- **THEN** the caller receives the same ordered bytes without being able to modify the stored address

### Requirement: Construction from raw bytes

The SDK SHALL expose `EvmAddress.fromBytes(value)` to create an EVM address from exactly 20 raw bytes. The operation
SHALL report `illegal-format` when the supplied byte sequence does not contain exactly 20 bytes.

#### Scenario: EVM address is constructed from valid bytes

- **GIVEN** a sequence containing exactly 20 raw bytes
- **WHEN** a caller passes the sequence to `EvmAddress.fromBytes(value)`
- **THEN** the operation returns an EVM address containing those bytes

#### Scenario: EVM address byte length is invalid

- **GIVEN** a byte sequence whose length is not 20
- **WHEN** a caller passes the sequence to `EvmAddress.fromBytes(value)`
- **THEN** the operation reports `illegal-format`

### Requirement: Parsing from hexadecimal text

The SDK SHALL expose `EvmAddress.fromString(value)` to parse exactly 40 hexadecimal characters with an optional `0x`
prefix. The operation SHALL report `illegal-format` for any other input, including text with the wrong length or a
non-hexadecimal character.

#### Scenario: Prefixed EVM address is parsed

- **GIVEN** 40 valid hexadecimal characters prefixed by `0x`
- **WHEN** a caller passes the text to `EvmAddress.fromString(value)`
- **THEN** the operation returns the corresponding EVM address

#### Scenario: Unprefixed EVM address is parsed

- **GIVEN** 40 valid hexadecimal characters without a prefix
- **WHEN** a caller passes the text to `EvmAddress.fromString(value)`
- **THEN** the operation returns the corresponding EVM address

#### Scenario: EVM address text is invalid

- **GIVEN** text that does not contain exactly 40 hexadecimal characters after its optional `0x` prefix
- **WHEN** a caller passes the text to `EvmAddress.fromString(value)`
- **THEN** the operation reports `illegal-format`

### Requirement: Canonical string form

The SDK SHALL expose `toString()` on `EvmAddress`. The operation SHALL return the canonical EIP-55-style form as `0x`
followed by 40 hexadecimal characters.

#### Scenario: EVM address is formatted

- **GIVEN** an EVM address
- **WHEN** a caller invokes `toString()`
- **THEN** the operation returns its canonical EIP-55-style `0x`-prefixed 40-character hexadecimal form
