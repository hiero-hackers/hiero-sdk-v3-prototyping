## Purpose

Define the shared behavior for constructing, retaining, parsing, and formatting standalone EVM address values across
supported SDKs. Derived from the `EvmAddress` definition in
[`spec/base/ledger.md`](../../../spec/base/ledger.md), this contract covers 20-byte values rather than ledger identifiers.

## ADDED Requirements

### Requirement: Immutable EVM address state

The SDK SHALL expose an `EvmAddress` value containing exactly 20 raw address bytes in network byte order. The stored
bytes SHALL be immutable and SHALL NOT be exposed in a way that permits modification of the address. Construction
SHALL preserve every byte and its position, including leading zero bytes. Mutating a supplied byte sequence after
construction SHALL NOT change the stored value. Reading the bytes SHALL return an immutable view or an independent
copy; a mutable reference to the stored bytes SHALL NOT be exposed.

The SDK SHALL expose the following language-neutral API, taken from the `EvmAddress` portion of
[`spec/base/ledger.md`](../../../spec/base/ledger.md):

```text
namespace ledger

type EvmAddress {
    @@immutable @@minLength(20) @@maxLength(20) bytes: bytes   // 20 raw bytes (network byte order)

    // Canonical EIP-55-style "0x<40 hex chars>" form.
    string toString()
}

// Parses an EvmAddress from its hex-string form. Accepts both "0xabc…" and bare "abc…"
// (40 hex chars). Throws illegal-format on anything else (wrong length, non-hex characters).
@@throws(illegal-format) @@static EvmAddress EvmAddress.fromString(value: string)

// Wraps raw bytes. `value.length` must equal 20; otherwise throws illegal-format.
@@throws(illegal-format) @@static EvmAddress EvmAddress.fromBytes(value: bytes)
```

#### Scenario: EVM address state is observed

- **WHEN** a caller reads the bytes of an EVM address created from 20 raw bytes
- **THEN** the caller receives the same ordered bytes without being able to modify the stored address

#### Scenario: Source bytes are changed after construction

- **WHEN** a caller creates an EVM address from a mutable 20-byte sequence and changes the source sequence afterward
- **THEN** subsequent byte reads and string formatting still represent the original 20 bytes

#### Scenario: Returned bytes cannot modify the address

- **WHEN** a caller reads an EVM address's bytes and attempts to change the returned bytes
- **THEN** the change is either rejected by an immutable view or affects only an independent copy, and the address
  remains unchanged

### Requirement: Construction from raw bytes

The SDK SHALL expose `EvmAddress.fromBytes(value)` to create an EVM address from exactly 20 raw bytes. The operation
SHALL report `illegal-format` when a non-null supplied byte sequence does not contain exactly 20 bytes. The operation
SHALL NOT truncate, pad, reverse, or otherwise normalize the bytes. Every 20-byte sequence, including all-zero bytes,
SHALL be accepted without checking whether a corresponding account or contract exists. Null input SHALL be rejected
before an address is returned; it SHALL NOT be treated as an empty or all-zero sequence.
SDK bindings SHALL document their language-appropriate mappings for the non-null input contract and the
`illegal-format` error identifier.

#### Scenario: EVM address is constructed from valid bytes

- **WHEN** a caller passes a sequence containing exactly 20 raw bytes to `EvmAddress.fromBytes(value)`
- **THEN** the operation returns an EVM address containing those bytes

#### Scenario: EVM address byte length is invalid

- **WHEN** a caller passes a non-null sequence whose length is not 20, including lengths 0, 19, and 21, to
  `EvmAddress.fromBytes(value)`
- **THEN** the operation reports `illegal-format` rather than padding or truncating the sequence

#### Scenario: Null byte input is rejected

- **WHEN** a caller passes null to `EvmAddress.fromBytes(value)`
- **THEN** the operation rejects the input using the SDK's documented null-input error mapping

#### Scenario: Zero bytes are valid address data

- **WHEN** a caller passes a sequence containing exactly 20 zero bytes to `EvmAddress.fromBytes(value)`
- **THEN** the operation returns an EVM address containing those 20 zero bytes without network validation

### Requirement: Parsing from hexadecimal text

The SDK SHALL expose `EvmAddress.fromString(value)` to parse exactly 40 ASCII hexadecimal characters with an optional
lowercase `0x` prefix. Valid address digits SHALL be `0-9`, `a-f`, and `A-F`; lowercase, uppercase, and mixed-case
digits SHALL be accepted and decoded identically. Two consecutive digits SHALL produce one byte, in left-to-right
order. Leading zero digits SHALL be retained.

Parsing SHALL NOT require or validate an EIP-55 letter-case checksum on the supplied digits. A mixed-case input that
does not match the canonical EIP-55 casing SHALL still be accepted if its digits and length are valid. Canonical
checksum casing is an output-formatting requirement, separate from input parsing.

The operation SHALL report `illegal-format` for any non-null text outside this grammar. It SHALL NOT trim whitespace,
accept an uppercase `0X` prefix, accept separators or signs, remove extra prefixes, or pad shortened values.
Null input SHALL be rejected using the SDK's documented null-input error mapping.

#### Scenario: Prefixed EVM address is parsed

- **WHEN** a caller passes 40 valid hexadecimal characters prefixed by `0x` to `EvmAddress.fromString(value)`
- **THEN** the operation returns the corresponding EVM address

#### Scenario: Unprefixed EVM address is parsed

- **WHEN** a caller passes 40 valid hexadecimal characters without a prefix to `EvmAddress.fromString(value)`
- **THEN** the operation returns the corresponding EVM address

#### Scenario: Letter case does not change the parsed address

- **WHEN** a caller passes lowercase, uppercase, and mixed-case forms of the same 40 hexadecimal digits to
  `EvmAddress.fromString(value)`, including a mixed-case form that does not match EIP-55 casing
- **THEN** every form is accepted and produces the same ordered 20 bytes

#### Scenario: EVM address text length is invalid

- **WHEN** a caller passes an empty string, `0x` alone, or text containing 39 or 41 digits after an optional `0x`
  prefix to `EvmAddress.fromString(value)`
- **THEN** the operation reports `illegal-format`

#### Scenario: EVM address text contains invalid characters

- **WHEN** a caller passes a 40-character body containing a character outside ASCII `0-9`, `a-f`, and `A-F` to
  `EvmAddress.fromString(value)`, including `g`, a full-width digit, or whitespace within the body
- **THEN** the operation reports `illegal-format`

#### Scenario: EVM address text contains unsupported decoration

- **WHEN** a caller passes otherwise valid address digits with leading or trailing whitespace, an uppercase `0X`
  prefix, a repeated prefix, a sign, separators, or an appended ledger-checksum suffix to `EvmAddress.fromString(value)`
- **THEN** the operation reports `illegal-format` without normalizing the input into an accepted form

#### Scenario: Null text input is rejected

- **WHEN** a caller passes null to `EvmAddress.fromString(value)`
- **THEN** the operation rejects the input using the SDK's documented null-input error mapping

### Requirement: Canonical string form

The SDK SHALL expose `toString()` on `EvmAddress`. The operation SHALL return exactly 42 ASCII characters: lowercase
`0x` followed by 40 hexadecimal digits encoding the stored bytes. Leading zero digits SHALL NOT be omitted.

Letter casing SHALL follow [EIP-55](https://eips.ethereum.org/EIPS/eip-55). Formatting SHALL be deterministic and
SHALL NOT depend on the original input casing, locale, network, or chain identifier. Output SHALL
contain no whitespace, separators, or appended ledger-checksum suffix. All-uppercase or all-lowercase bodies SHALL
be allowed when they are the canonical EIP-55 result.

#### Scenario: EVM address is formatted

- **WHEN** a caller invokes `toString()` on an EVM address parsed from `0xfb6916095ca1df60bb79ce92ce3ea74c37c5d359`
- **THEN** the operation returns `0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359`, matching the EIP-55 reference vector

#### Scenario: Leading zeros are preserved in formatted text

- **WHEN** a caller invokes `toString()` on an EVM address constructed from exactly 20 zero bytes
- **THEN** the operation returns `0x0000000000000000000000000000000000000000`, with all 40 digits retained

### Requirement: Address representation round trips

The SDK SHALL preserve the original 20-byte value when formatting an address and parsing the resulting text.
Formatting a parsed canonical string SHALL reproduce that same canonical string. Equivalent accepted input
spellings SHALL produce identical canonical output. These operations SHALL NOT alter the stored bytes or require
network access.

#### Scenario: Raw address bytes survive a text round trip

- **WHEN** a caller formats an EVM address created from any valid 20-byte sequence, including one beginning with
  zero bytes, and parses the resulting string
- **THEN** the new address exposes exactly the original ordered 20 bytes and formats to the same canonical string

#### Scenario: Accepted spellings normalize to one canonical form

- **WHEN** a caller parses equivalent prefixed and unprefixed inputs with different valid hexadecimal letter casing
  and formats each resulting address
- **THEN** every output is the same EIP-55 canonical string, and formatting and reparsing it remains stable
