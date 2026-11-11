# EVM API

## Description

Types of the Ethereum Virtual Machine (EVM) that do not depend on a particular network: they mean the same on a Hiero
network as on any other EVM network.

`EvmAddress` is a 20-byte EVM address. It is read from 40 hex digits, with or without `0x`, and written in the
mixed-case checksum form of EIP-55 (`0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed`). Digits in mixed case must be the
EIP-55 form; a mistyped address is rejected instead of being read as a different one.

The contract ABI (application binary interface) defines how the arguments of a contract call, its results and the
logs of a contract are encoded. `AbiType` is the type of an ABI value — `uint256`, `int8`, `address`, `bool`,
`bytes32`, `bytes`, `string`, an array such as `uint256[]` or `bytes3[2]`, or a tuple such as `(address,uint256)` (a
struct in Solidity). `AbiValue` is a value of such a type, e.g. `AbiUint{value: 1000, bits: 256}` or
`AbiAddress{value: address}`; it knows its type, so a list of values can be encoded without a separate list of types.

`AbiFunction` describes a function of a contract. It computes the selector, encodes the call data of a call and
decodes its result. `AbiEvent` describes an event of a contract and decodes the logs the contract emits; `AbiTuple.encode()` encodes
values without a selector, e.g. the arguments of a constructor. Decoding is strict: data that is too short, an offset
that points outside the data, or a value with bits set that its type does not have (a `bool` other than 0 or 1, a
`uint8` above 255, an address with non-zero padding) is rejected rather than truncated.

## Design Notes

- **The namespace imports nothing.** `evm` holds only what is defined by the EVM and its standards (EIP-55, the
  contract ABI), never a Hiero type: no `ContractId` or `AccountId`, no long-zero address of an entity number, no
  system contract address. Those live in `ledger` and `consensusnode.*`, which may import `evm`; the reverse is not
  allowed, so that `evm` can later become a spec folder of its own without changes
  ([ADR-0015](../../docs/adr/0015-evm-generic-types-in-a-namespace-without-imports.md)). The tooling does not
  enforce the rule; a review does.
- **Scope.** What a Hiero SDK needs of the EVM, not an Ethereum library: no JSON-RPC, wallets or providers.
- **One type per kind, not per width.** The ABI has 32 widths of `uint<M>` and `int<M>` and 32 of `bytes<M>`. They are
  one type each with the width as an attribute (`AbiUintType{bits}`, `AbiFixedBytesType{size}`), validated by
  `@@min`, `@@max` and `@@multipleOf`, instead of 96 types. Types and values are separate sealed hierarchies because
  decoding needs the type before there is a value, and an empty array has no element to take the type from.
- **Constraints across attributes are checked on use.** That a value fits its width (`AbiUint{value: 256, bits: 8}`)
  and that the values of an array have its element type and length cannot be expressed by an annotation. A value is
  therefore created as given and checked when it is encoded (`invalid-argument-error`), like the threshold of an
  `AuthorityList` in [`authority.md`](authority.md).
- **Signatures, not JSON.** `parse` reads the canonical signature (`baz(uint32,bool)`), optionally with
  `returns (…)` for a function and `indexed` for an event parameter. The JSON ABI that the Solidity compiler writes is
  not read yet (see Questions & Comments).
- **Not supported:** the types `fixed<M>x<N>` / `ufixed<M>x<N>` (Solidity cannot use them) and `function` (a 24-byte
  address and selector, not used by the Hiero SDKs). `parse` rejects them.
- **Signatures are canonical.** `parse` accepts exactly the form that `signature()` writes, plus ` returns (…)` and
  ` indexed`: no other spaces, no parameter names, and `uint` / `int` as the only synonyms (of `uint256` / `int256`).
  A signature that a person may have typed differently (`f(uint256, bool)`) is rejected rather than guessed.
- **Limits.** The ABI limits neither the number of arguments, results or parameters nor how deeply tuples and arrays
  are nested, so `parse` does not either; the length of a fixed-size array is a `uint32`. A log has at most four
  topics, the first of which is the hash of the signature, so an event has at most three indexed parameters (Solidity
  documentation, "Events"); `parse` rejects more. An event created with more indexed parameters has no valid logs:
  `decodeLog` rejects every log of it, because the number of topics never matches.
- **Indexed dynamic values.** A log stores an indexed parameter of a dynamic type (`bytes`, `string`, an array, a
  tuple) only as the Keccak-256 hash of its value; the value cannot be recovered. `decodeLog` returns the hash as
  an `AbiFixedBytes` of 32 bytes in its place.

## API Schema

```
namespace evm

// A 20-byte EVM address: the address of an account or a contract on the EVM.
EvmAddress {
    @@immutable @@minSize(20) @@maxSize(20) bytes: bytes   // 20 raw bytes (network byte order)

    // The EIP-55 form: "0x" and the 40 hex digits, a letter in upper case where EIP-55's
    // checksum (the Keccak-256 hash of the lowercase digits) asks for it.
    string toString()

    // Parses an EvmAddress from its hex-string form. Accepts both "0xabc…" and bare "abc…"
    // (40 hex chars). Digits all in lower or all in upper case are accepted as they are; digits
    // in mixed case must be the EIP-55 form, otherwise the text is rejected as mistyped. Throws
    // on anything else (wrong length, non-hex characters).
    @@throws(illegal-format) @@static EvmAddress fromString(value: string)

    // Wraps raw bytes. `value.length` must equal 20; otherwise throws.
    @@throws(illegal-format) @@static EvmAddress fromBytes(value: bytes)
}

// --- ABI types ---------------------------------------------------------------------------------

// The type of an ABI value. Two types are equal if they have the same canonical name.
@@sealed(AbiBoolType, AbiAddressType, AbiUintType, AbiIntType, AbiFixedBytesType, AbiBytesType, AbiStringType,
        AbiArrayType, AbiTupleType)
abstraction AbiType {
    // The canonical name of the type, as used in a signature: "uint256", "bytes32", "(address,uint256)[]".
    string toString()

    // Reads a type from its name. "uint" and "int" are read as "uint256" and "int256"; spaces are not allowed. Throws
    // for a name that is no supported ABI type.
    @@throws(illegal-format) @@static AbiType parse(text: string)
}

// The type `bool`.
@@finalType
AbiBoolType extends AbiType {}

// The type `address`: a 20-byte EVM address.
@@finalType
AbiAddressType extends AbiType {}

// The type `uint<bits>`: an unsigned integer of the given width.
@@finalType
AbiUintType extends AbiType {
    @@immutable @@min(8) @@max(256) @@multipleOf(8) @@default(256) bits: uint16   // the width in bits
}

// The type `int<bits>`: a signed integer (two's complement) of the given width.
@@finalType
AbiIntType extends AbiType {
    @@immutable @@min(8) @@max(256) @@multipleOf(8) @@default(256) bits: uint16   // the width in bits
}

// The type `bytes<size>`: a fixed number of bytes.
@@finalType
AbiFixedBytesType extends AbiType {
    @@immutable @@min(1) @@max(32) size: uint8   // the number of bytes
}

// The type `bytes`: any number of bytes.
@@finalType
AbiBytesType extends AbiType {}

// The type `string`: text, encoded in UTF-8.
@@finalType
AbiStringType extends AbiType {}

// An array type: `T[]` of any length, or `T[length]` of a fixed length.
@@finalType
AbiArrayType extends AbiType {
    @@immutable elementType: AbiType                  // the type of the elements
    @@immutable @@nullable @@min(1) length: uint32    // the number of elements of a fixed-size array; absent for T[]
}

// A tuple type `(T1,…,Tn)`: a struct in Solidity, or the arguments or results of a function.
@@finalType
AbiTupleType extends AbiType {
    @@immutable components: list<AbiType>   // the types of the components, in their order

    // Decodes a value of this type. Throws if the data is no valid encoding of it (see the description).
    @@throws(illegal-format) AbiTuple decode(data: bytes)
}

// --- ABI values --------------------------------------------------------------------------------

// A value of an ABI type. Two values are equal if they have the same type and the same content.
@@sealed(AbiBool, AbiAddress, AbiUint, AbiInt, AbiFixedBytes, AbiBytes, AbiString, AbiArray, AbiTuple)
abstraction AbiValue {
    // The type of the value.
    AbiType abiType()
}

// A `bool`.
@@finalType
AbiBool extends AbiValue {
    @@immutable value: bool
}

// An `address`.
@@finalType
AbiAddress extends AbiValue {
    @@immutable value: EvmAddress
}

// A `uint<bits>`. The value must be below 2^bits; this is checked when the value is encoded.
@@finalType
AbiUint extends AbiValue {
    @@immutable value: uint256
    @@immutable @@min(8) @@max(256) @@multipleOf(8) @@default(256) bits: uint16   // the width of the type
}

// An `int<bits>`. The value must be between -2^(bits-1) and 2^(bits-1)-1; this is checked when the value is encoded.
@@finalType
AbiInt extends AbiValue {
    @@immutable value: int256
    @@immutable @@min(8) @@max(256) @@multipleOf(8) @@default(256) bits: uint16   // the width of the type
}

// A `bytes<n>`, where n is the number of bytes of the value.
@@finalType
AbiFixedBytes extends AbiValue {
    @@immutable @@minSize(1) @@maxSize(32) value: bytes
}

// A `bytes`.
@@finalType
AbiBytes extends AbiValue {
    @@immutable value: bytes
}

// A `string`.
@@finalType
AbiString extends AbiValue {
    @@immutable value: string
}

// An array. Every value has the element type of `arrayType`, and a fixed-size array has exactly `arrayType.length`
// values; both are checked when the array is encoded.
@@finalType
AbiArray extends AbiValue {
    @@immutable arrayType: AbiArrayType
    @@immutable values: list<AbiValue>
}

// A tuple of values: a struct, or the arguments or results of a function.
@@finalType
AbiTuple extends AbiValue {
    @@immutable values: list<AbiValue>

    // Encodes the values, e.g. the arguments of a constructor (the call data of a function also has its selector).
    // Throws if a value does not fit its type.
    @@throws(invalid-argument-error) bytes encode()
}

// --- functions and events ----------------------------------------------------------------------

// A function of a contract: its name and the types of its arguments and results.
@@finalType
AbiFunction {
    @@immutable @@pattern("[A-Za-z_$][A-Za-z0-9_$]*") name: string
    @@immutable @@default([]) inputs: list<AbiType>    // the types of the arguments
    @@immutable @@default([]) outputs: list<AbiType>   // the types of the results

    // The signature: the name and the canonical names of the argument types, e.g. "transfer(address,uint256)".
    string signature()

    // The selector: the first 4 bytes of the Keccak-256 hash of the signature.
    bytes selector()

    // The call data of a call: the selector, followed by the encoded arguments. Throws if the arguments do not have the
    // types of `inputs` or a value does not fit its type.
    @@throws(invalid-argument-error) bytes encodeCall(arguments: list<AbiValue>)

    // Decodes the arguments of call data. Throws if the data does not start with the selector or is no valid encoding
    // of the arguments.
    @@throws(illegal-format) list<AbiValue> decodeCall(data: bytes)

    // Decodes the result of a call, as values of the types of `outputs`. Throws if the data is no valid encoding of
    // them.
    @@throws(illegal-format) list<AbiValue> decodeResult(data: bytes)

    // Reads a function from its signature, optionally followed by " returns " and the output types in parentheses:
    // "balanceOf(address) returns (uint256)". The signature is canonical: no spaces except the two around "returns",
    // no parameter names. Throws for text that is no such signature.
    @@throws(illegal-format) @@static AbiFunction parse(signature: string)
}

// A parameter of an event.
@@finalType
AbiEventParameter {
    @@immutable abiType: AbiType
    @@immutable @@default(false) indexed: bool   // true: stored as a topic of the log instead of in its data
}

// An event of a contract: its name and parameters.
@@finalType
AbiEvent {
    @@immutable @@pattern("[A-Za-z_$][A-Za-z0-9_$]*") name: string
    @@immutable @@default([]) parameters: list<AbiEventParameter>
    @@immutable @@default(false) anonymous: bool   // true: the logs of the event have no topic with its signature

    // The signature: the name and the canonical names of the parameter types, without "indexed", e.g.
    // "Transfer(address,address,uint256)".
    string signature()

    // The Keccak-256 hash of the signature: the first topic of every log of the event, unless it is anonymous.
    bytes topic()

    // Decodes a log of the event: the values of all parameters, in their order. The topics are the hash of the
    // signature (unless the event is anonymous) and the indexed values; the data holds the values that are not indexed.
    // An indexed value of a dynamic type (bytes, string, array, tuple) is stored only as the Keccak-256 hash of its
    // value and is returned as that hash, an AbiFixedBytes of 32 bytes. Throws if the first topic is not the hash of
    // the signature, the number of topics does not match, or the data is no valid encoding.
    @@throws(illegal-format) list<AbiValue> decodeLog(topics: list<bytes>, data: bytes)

    // Reads an event from its signature; an indexed parameter has " indexed" after its type:
    // "Transfer(address indexed,address indexed,uint256)". The event is not anonymous, so at most three parameters
    // can be indexed. The signature is canonical: no spaces except the one before "indexed", no parameter names.
    // Throws for text that is no such signature.
    @@throws(illegal-format) @@static AbiEvent parse(signature: string)
}
```

## Default Instances for Testing

```
// The ABI type and value used where any of them will do.
instance AbiType = AbiUintType{bits: 256}
instance AbiValue = AbiUint{value: 1, bits: 256}

// EIP-55, section "Test Cases": addresses in their EIP-55 form (all caps, all lower and mixed case).
values EIP55_ADDRESSES: list<string> = ["0x52908400098527886E0F7030069857D2E4169EE7",
        "0x8617E340B3D01FA5F11F306F4090FD50E238070D", "0xde709f2102306220921060314715629080e2fb77",
        "0x27b1fdb04752bbc536007a920d24acb045561c26", "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed",
        "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359", "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB",
        "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb"]

// Names that are no supported ABI type: widths that are no multiple of 8 or out of range, the unsupported types,
// malformed arrays and tuples.
values NO_ABI_TYPES: list<string> = ["", "uint7", "uint0", "uint264", "int12", "bytes0", "bytes33", "uint256[0]",
        "uint256[", "uint256]", "(uint256", "(uint256,)", "fixed128x18", "ufixed128x18", "function", "address payable",
        "uint 256", "Uint256", "struct"]

// Function signatures that parse rejects: incomplete or misplaced parentheses, names that are no identifiers, spaces
// other than the two around "returns", parameter names, "indexed", empty arguments, unsupported types, widths with a
// leading zero, array lengths that are 0, negative, beyond uint32 or have a leading zero, and anything after the end.
values MALFORMED_FUNCTION_SIGNATURES: list<string> = ["", "f", "f(", "f)", "()", "(uint256)", "1f()", "f-g()",
        "f.g()", "Fü()", "f (uint256)", "f( uint256)", "f(uint256 )", "f(uint256, bool)", "f(,)", "f(uint256,)",
        "f(,uint256)", "f(uint256))", "f()x", "f();", "f() returns", "f() returns ", "f()returns (bool)",
        "f() returns(bool)", "f() returns  (bool)", "f()  returns (bool)", "f() returns bool", "f() RETURNS (bool)",
        "f() returns (bool) returns (bool)", "f(address to)", "f(uint256 indexed)", "f(uint7)", "f(byte)", "f(uint08)",
        "f(bytes01)", "f(uint256[01])", "f(uint256[0])", "f(uint256[-1])", "f(uint256[4294967296])",
        "f(uint256[99999999999])", "f(uint256[)", "f((uint256)", "f(uint256))", "f(fixed128x18)", "f(function)"]

// Event signatures that parse rejects: the cases of a function that apply to events, "indexed" misspelt, doubled,
// without its space or before the type, and "returns".
values MALFORMED_EVENT_SIGNATURES: list<string> = ["", "E", "E(", "1E()", "E (uint256)", "E( uint256)",
        "E(uint256 indexed, bool)", "E(uint256 indexed,)", "E(uint256 indexed indexed)", "E(uint256  indexed)",
        "E(uint256indexed)", "E(uint256 Indexed)", "E(indexed uint256)", "E(address indexed from)", "E(address from)",
        "E(uint256) returns (bool)", "E() anonymous", "E(uint7 indexed)", "E()x"]
```

## Examples

### Call a function and read its result

```
AbiFunction transfer = AbiFunction.parse("transfer(address,uint256) returns (bool)");
bytes callData = transfer.encodeCall(List.of(new AbiAddress(recipient), new AbiUint(1000)));
// ... execute the call with callData ...
List<AbiValue> result = transfer.decodeResult(resultData);   // [AbiBool[value=true]]
```

### Read the logs of a transfer

```
AbiEvent transferEvent = AbiEvent.parse("Transfer(address indexed,address indexed,uint256)");
List<AbiValue> values = transferEvent.decodeLog(log.topics(), log.data());
EvmAddress from = ((AbiAddress) values.get(0)).value();
```

## Testing

The expected values come from the examples of the contract ABI specification of the Solidity documentation
("Contract ABI Specification", sections "Examples" and "Use of Dynamic Types"), from the tests of the Java SDK, and
from this spec where it defines a behaviour of its own (strict decoding, the checks on use).

```tests
// Source: the Swift, Rust, Python and C++ SDKs.
@@testId("an-evm-address-is-read-from-40-hex-digits-with-or-without-0x")
test "an EVM address is read from 40 hex digits with or without 0x" {
    // given
    let bytes = hex(text: "1234567890abcdef1234567890abcdef12345678")

    // when
    let withPrefix = EvmAddress.fromString(value: "0x1234567890abcdef1234567890abcdef12345678")
    let withoutPrefix = EvmAddress.fromString(value: "1234567890abcdef1234567890abcdef12345678")

    // then
    assertEquals(expected: bytes, actual: withPrefix.bytes)
    assertEquals(expected: bytes, actual: withoutPrefix.bytes)
    assertEquals(expected: withPrefix, actual: EvmAddress.fromBytes(value: bytes))
}

// Source: the Swift, Rust, Python and C++ SDKs.
@@testId("an-evm-address-of-the-wrong-length-or-with-other-characters-is-rejected")
test "an EVM address of the wrong length or with other characters is rejected" {
    for text in ["", "0x1234", "0x0f0e0d0c0b0a09080706050403020100", "0xGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG"] {
        // when / then
        @@expectThrows(illegal-format)
        EvmAddress.fromString(value: text)
    }
    for size in [19, 21] {
        // when / then
        @@expectThrows(illegal-format)
        EvmAddress.fromBytes(value: repeat(value: hex(text: "00"), count: size))
    }
}

// Source: the Swift, Java, Rust and Go SDKs (hex digits in either case denote the same address).
@@testId("an-evm-address-is-the-same-in-upper-and-lower-case")
test "an EVM address is the same in upper and lower case" {
    // when
    let upper = EvmAddress.fromString(value: "0x1234567890ABCDEF1234567890ABCDEF12345678")
    let lower = EvmAddress.fromString(value: "1234567890abcdef1234567890abcdef12345678")

    // then
    assertEquals(expected: lower, actual: upper)
}

// Source: the C++ SDK (39 digits, 0x inside the digits, 22 bytes) and the Python SDK (42 digits).
@@testId("an-evm-address-of-39-or-42-digits-with-a-misplaced-or-doubled-0x-or-of-22-bytes-is-rejected")
test "an EVM address of 39 or 42 digits, with a misplaced or doubled 0x, or of 22 bytes is rejected" {
    for text in ["303132333435363738396162636465666768696", "1234567890abcdef1234567890abcdef1234567890",
            "3031323334353637383961626364650x66676869", "0x0x1234567890abcdef1234567890abcdef12345678"] {
        // when / then
        @@expectThrows(illegal-format)
        EvmAddress.fromString(value: text)
    }

    // when / then
    @@expectThrows(illegal-format)
    EvmAddress.fromBytes(value: repeat(value: hex(text: "00"), count: 22))
}

// Source: EIP-55, section "Test Cases", written from the bytes of the address.
@@testId("an-evm-address-is-written-in-its-eip-55-form")
test "an EVM address is written in its EIP-55 form" {
    for checksummed in EIP55_ADDRESSES {
        // when: built from the bytes, so the case of the text plays no part
        let evm = EvmAddress.fromBytes(value: hex(text: substring(value: checksummed, start: 2, end: 42)))

        // then
        assertEquals(expected: checksummed, actual: evm.toString())
        assertEquals(expected: evm, actual: EvmAddress.fromString(value: checksummed))
    }
}

// Source: EIP-55 (mixed case carries a checksum); rejected by ethers.js, web3.js and viem. The second address is the
// one of the Go SDK's mirror node tests, which is not in EIP-55 form.
@@testId("an-evm-address-in-mixed-case-that-is-not-its-eip-55-form-is-rejected")
test "an EVM address in mixed case that is not its EIP-55 form is rejected" {
    for text in ["0x5aaeb6053F3E94C9b9A09f33669435E7Ef1BeAed", "7F2c5e0f4a3b9E8d1C6A5b4E3f2D1c0B9a8E7F6d"] {
        // when / then
        @@expectThrows(illegal-format)
        EvmAddress.fromString(value: text)
    }
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (baz).
@@testId("the-call-of-baz-uint32-bool-with-69-and-true-is-the-one-of-the-abi-specification")
test "the call of baz(uint32,bool) with 69 and true is the one of the ABI specification" {
    // given
    let baz = AbiFunction.parse(signature: "baz(uint32,bool) returns (bool)")

    // when
    let data = baz.encodeCall(arguments: [AbiUint{value: 69, bits: 32}, AbiBool{value: true}])

    // then
    assertEquals(expected: "baz(uint32,bool)", actual: baz.signature())
    assertEquals(expected: hex(text: "cdcd77c0"), actual: baz.selector())
    assertEquals(expected: hex(text: "cdcd77c0"
            + "0000000000000000000000000000000000000000000000000000000000000045"
            + "0000000000000000000000000000000000000000000000000000000000000001"), actual: data)
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (bar).
@@testId("the-call-of-bar-bytes3-2-is-the-one-of-the-abi-specification")
test "the call of bar(bytes3[2]) is the one of the ABI specification" {
    // given
    let bar = AbiFunction.parse(signature: "bar(bytes3[2])")
    let pair = AbiArray{arrayType: AbiArrayType{elementType: AbiFixedBytesType{size: 3}, length: 2},
            values: [AbiFixedBytes{value: utf8(text: "abc")}, AbiFixedBytes{value: utf8(text: "def")}]}

    // when
    let data = bar.encodeCall(arguments: [pair])

    // then
    assertEquals(expected: hex(text: "fce353f6"
            + "6162630000000000000000000000000000000000000000000000000000000000"
            + "6465660000000000000000000000000000000000000000000000000000000000"), actual: data)
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (sam).
@@testId("the-call-of-sam-bytes-bool-uint256-array-is-the-one-of-the-abi-specification")
test "the call of sam(bytes,bool,uint256[]) is the one of the ABI specification" {
    // given
    let sam = AbiFunction.parse(signature: "sam(bytes,bool,uint256[])")
    let numbers = AbiArray{arrayType: AbiArrayType{elementType: AbiUintType{bits: 256}, length: null},
            values: [AbiUint{value: 1, bits: 256}, AbiUint{value: 2, bits: 256}, AbiUint{value: 3, bits: 256}]}
    let arguments: list<AbiValue> = [AbiBytes{value: utf8(text: "dave")}, AbiBool{value: true}, numbers]

    // when
    let data = sam.encodeCall(arguments: arguments)

    // then
    assertEquals(expected: hex(text: "a5643bf2"
            + "0000000000000000000000000000000000000000000000000000000000000060"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "00000000000000000000000000000000000000000000000000000000000000a0"
            + "0000000000000000000000000000000000000000000000000000000000000004"
            + "6461766500000000000000000000000000000000000000000000000000000000"
            + "0000000000000000000000000000000000000000000000000000000000000003"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "0000000000000000000000000000000000000000000000000000000000000002"
            + "0000000000000000000000000000000000000000000000000000000000000003"), actual: data)
    assertEquals(expected: arguments, actual: sam.decodeCall(data: data))
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Use of Dynamic Types" (f).
@@testId("the-call-of-f-with-static-and-dynamic-arguments-is-the-one-of-the-abi-specification")
test "the call of f with static and dynamic arguments is the one of the ABI specification" {
    // given
    let f = AbiFunction.parse(signature: "f(uint256,uint32[],bytes10,bytes)")
    let numbers = AbiArray{arrayType: AbiArrayType{elementType: AbiUintType{bits: 32}, length: null},
            values: [AbiUint{value: 1110, bits: 32}, AbiUint{value: 1929, bits: 32}]}

    // when
    let data = f.encodeCall(arguments: [AbiUint{value: 291, bits: 256}, numbers,
            AbiFixedBytes{value: utf8(text: "1234567890")}, AbiBytes{value: utf8(text: "Hello, world!")}])

    // then
    assertEquals(expected: hex(text: "8be65246"
            + "0000000000000000000000000000000000000000000000000000000000000123"
            + "0000000000000000000000000000000000000000000000000000000000000080"
            + "3132333435363738393000000000000000000000000000000000000000000000"
            + "00000000000000000000000000000000000000000000000000000000000000e0"
            + "0000000000000000000000000000000000000000000000000000000000000002"
            + "0000000000000000000000000000000000000000000000000000000000000456"
            + "0000000000000000000000000000000000000000000000000000000000000789"
            + "000000000000000000000000000000000000000000000000000000000000000d"
            + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"), actual: data)
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Use of Dynamic Types" (g).
@@testId("the-call-of-g-with-nested-arrays-is-the-one-of-the-abi-specification")
test "the call of g with nested arrays is the one of the ABI specification" {
    // given
    let g = AbiFunction.parse(signature: "g(uint256[][],string[])")
    let numbersType = AbiArrayType{elementType: AbiUintType{bits: 256}, length: null}
    let numbers = AbiArray{arrayType: AbiArrayType{elementType: numbersType, length: null}, values: [
            AbiArray{arrayType: numbersType, values: [AbiUint{value: 1, bits: 256}, AbiUint{value: 2, bits: 256}]},
            AbiArray{arrayType: numbersType, values: [AbiUint{value: 3, bits: 256}]}]}
    let words = AbiArray{arrayType: AbiArrayType{elementType: AbiStringType{}, length: null},
            values: [AbiString{value: "one"}, AbiString{value: "two"}, AbiString{value: "three"}]}

    // when
    let data = g.encodeCall(arguments: [numbers, words])

    // then
    assertEquals(expected: hex(text: "2289b18c"
            + "0000000000000000000000000000000000000000000000000000000000000040"
            + "0000000000000000000000000000000000000000000000000000000000000140"
            + "0000000000000000000000000000000000000000000000000000000000000002"
            + "0000000000000000000000000000000000000000000000000000000000000040"
            + "00000000000000000000000000000000000000000000000000000000000000a0"
            + "0000000000000000000000000000000000000000000000000000000000000002"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "0000000000000000000000000000000000000000000000000000000000000002"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "0000000000000000000000000000000000000000000000000000000000000003"
            + "0000000000000000000000000000000000000000000000000000000000000003"
            + "0000000000000000000000000000000000000000000000000000000000000060"
            + "00000000000000000000000000000000000000000000000000000000000000a0"
            + "00000000000000000000000000000000000000000000000000000000000000e0"
            + "0000000000000000000000000000000000000000000000000000000000000003"
            + "6f6e650000000000000000000000000000000000000000000000000000000000"
            + "0000000000000000000000000000000000000000000000000000000000000003"
            + "74776f0000000000000000000000000000000000000000000000000000000000"
            + "0000000000000000000000000000000000000000000000000000000000000005"
            + "7468726565000000000000000000000000000000000000000000000000000000"), actual: data)
}

// Source: the Java SDK (ContractFunctionResultTest: a result of integers, an address and two strings).
@@testId("a-result-of-integers-an-address-and-strings-is-decoded")
test "a result of integers, an address and strings is decoded" {
    // given
    let decoder = AbiFunction.parse(signature: "f() returns (uint256,int256,address,uint256,string,string)")

    // when
    let values = decoder.decodeResult(data: hex(text: "00000000000000000000000000000000000000000000000000000000ffffffff"
            + "7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
            + "00000000000000000000000011223344556677889900aabbccddeeff00112233"
            + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
            + "00000000000000000000000000000000000000000000000000000000000000c0"
            + "0000000000000000000000000000000000000000000000000000000000000100"
            + "000000000000000000000000000000000000000000000000000000000000000d"
            + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
            + "0000000000000000000000000000000000000000000000000000000000000014"
            + "48656c6c6f2c20776f726c642c20616761696e21000000000000000000000000"))

    // then
    let expected: list<AbiValue> = [AbiUint{value: 4294967295, bits: 256},
            AbiInt{value: 57896044618658097711785492504343953926634992332820282019728792003956564819967, bits: 256},
            AbiAddress{value: EvmAddress.fromString(value: "11223344556677889900aabbccddeeff00112233")},
            AbiUint{value: 115792089237316195423570985008687907853269984665640564039457584007913129639935, bits: 256},
            AbiString{value: "Hello, world!"}, AbiString{value: "Hello, world, again!"}]
    assertEquals(expected: expected, actual: values)
}

// Source: the Java SDK (ContractFunctionResultTest: a string array).
@@testId("a-result-of-a-string-array-is-decoded")
test "a result of a string array is decoded" {
    // given
    let decoder = AbiFunction.parse(signature: "f() returns (string[])")

    // when
    let values = decoder.decodeResult(data: hex(text: "0000000000000000000000000000000000000000000000000000000000000020"
            + "0000000000000000000000000000000000000000000000000000000000000002"
            + "0000000000000000000000000000000000000000000000000000000000000040"
            + "0000000000000000000000000000000000000000000000000000000000000080"
            + "000000000000000000000000000000000000000000000000000000000000000C"
            + "72616E646F6D2062797465730000000000000000000000000000000000000000"
            + "000000000000000000000000000000000000000000000000000000000000000C"
            + "72616E646F6D2062797465730000000000000000000000000000000000000000"))

    // then
    let expected: list<AbiValue> = [AbiArray{arrayType: AbiArrayType{elementType: AbiStringType{}, length: null},
            values: [AbiString{value: "random bytes"}, AbiString{value: "random bytes"}]}]
    assertEquals(expected: expected, actual: values)
}

// Source: this spec (decoding is strict: a value with bits set that its type does not have is rejected). A word of
// 256 one bits is no bool, no uint32, no address and no bytes1 (its padding is not zero); 2 is no bool either.
@@testId("a-value-with-bits-its-type-does-not-have-is-rejected-when-decoded")
test "a value with bits its type does not have is rejected when decoded" {
    // given
    let word = hex(text: "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")

    for signature in ["f() returns (bool)", "f() returns (uint32)", "f() returns (address)", "f() returns (bytes1)"] {
        // when / then
        @@expectThrows(illegal-format)
        AbiFunction.parse(signature: signature).decodeResult(data: word)
    }

    // given
    let boolResult = AbiFunction.parse(signature: "f() returns (bool)")

    // when / then
    @@expectThrows(illegal-format)
    boolResult.decodeResult(data: hex(text: "0000000000000000000000000000000000000000000000000000000000000002"))
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (the arguments of baz and sam
// without their selector are the encoding of the tuples (uint32,bool) and (bytes,bool,uint256[])).
@@testId("a-tuple-type-decodes-the-static-and-dynamic-tuples-of-the-abi-specification")
test "a tuple type decodes the static and dynamic tuples of the ABI specification" {
    // given
    let bazArguments = AbiTupleType{components: [AbiUintType{bits: 32}, AbiBoolType{}]}
    let bazType: AbiType = bazArguments
    let samArguments = AbiTupleType{components: [AbiBytesType{}, AbiBoolType{},
            AbiArrayType{elementType: AbiUintType{bits: 256}, length: null}]}
    let numbers = AbiArray{arrayType: AbiArrayType{elementType: AbiUintType{bits: 256}, length: null},
            values: [AbiUint{value: 1, bits: 256}, AbiUint{value: 2, bits: 256}, AbiUint{value: 3, bits: 256}]}

    // when
    let baz = bazArguments.decode(data: hex(text: "0000000000000000000000000000000000000000000000000000000000000045"
            + "0000000000000000000000000000000000000000000000000000000000000001"))
    let sam = samArguments.decode(data: hex(text: "0000000000000000000000000000000000000000000000000000000000000060"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "00000000000000000000000000000000000000000000000000000000000000a0"
            + "0000000000000000000000000000000000000000000000000000000000000004"
            + "6461766500000000000000000000000000000000000000000000000000000000"
            + "0000000000000000000000000000000000000000000000000000000000000003"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "0000000000000000000000000000000000000000000000000000000000000002"
            + "0000000000000000000000000000000000000000000000000000000000000003"))

    // then
    assertEquals(expected: AbiTuple{values: [AbiUint{value: 69, bits: 32}, AbiBool{value: true}]}, actual: baz)
    assertEquals(expected: AbiTuple{values: [AbiBytes{value: utf8(text: "dave")}, AbiBool{value: true}, numbers]},
            actual: sam)
    assertEquals(expected: bazType, actual: baz.abiType())
}

// Source: this spec (decoding is strict: data that is too short for the tuple is rejected).
@@testId("a-tuple-type-rejects-data-that-is-too-short-for-it")
test "a tuple type rejects data that is too short for it" {
    // given
    let bazArguments = AbiTupleType{components: [AbiUintType{bits: 32}, AbiBoolType{}]}

    // when / then
    @@expectThrows(illegal-format)
    bazArguments.decode(data: hex(text: "0000000000000000000000000000000000000000000000000000000000000045"))
}

// Source: this spec (decoding is strict: data that is too short or an offset outside the data is rejected).
@@testId("data-that-is-too-short-or-points-outside-itself-is-rejected-when-decoded")
test "data that is too short or points outside itself is rejected when decoded" {
    // given
    let uintResult = AbiFunction.parse(signature: "f() returns (uint256)")
    let stringResult = AbiFunction.parse(signature: "f() returns (string)")

    // when / then
    @@expectThrows(illegal-format)
    uintResult.decodeResult(data: hex(text: "00"))
    @@expectThrows(illegal-format)
    stringResult.decodeResult(data: hex(text: "0000000000000000000000000000000000000000000000000000000000000040"))
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Function Selector" (the call data starts
// with the selector); this spec (call data with another selector is rejected).
@@testId("call-data-of-another-function-is-rejected")
test "call data of another function is rejected" {
    // given
    let baz = AbiFunction.parse(signature: "baz(uint32,bool)")
    let data = AbiFunction.parse(signature: "bar(bytes3[2])").encodeCall(arguments: [
            AbiArray{arrayType: AbiArrayType{elementType: AbiFixedBytesType{size: 3}, length: 2},
            values: [AbiFixedBytes{value: utf8(text: "abc")}, AbiFixedBytes{value: utf8(text: "def")}]}])

    // when / then
    @@expectThrows(illegal-format)
    baz.decodeCall(data: data)
}

// Source: this spec (a value that does not fit its width is rejected when it is encoded).
@@testId("an-integer-that-does-not-fit-its-width-is-rejected-when-encoded")
test "an integer that does not fit its width is rejected when encoded" {
    let tooLarge: list<AbiValue> = [AbiUint{value: 256, bits: 8}, AbiInt{value: 128, bits: 8},
            AbiInt{value: -129, bits: 8}]
    for value in tooLarge {
        // when / then
        @@expectThrows(invalid-argument-error)
        AbiTuple{values: [value]}.encode()
    }

    // then: the limits themselves fit
    let limits = AbiTuple{values: [AbiUint{value: 255, bits: 8}, AbiInt{value: 127, bits: 8},
            AbiInt{value: -128, bits: 8}]}
    assertEquals(expected: hex(text: "00000000000000000000000000000000000000000000000000000000000000ff"
            + "000000000000000000000000000000000000000000000000000000000000007f"
            + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff80"), actual: limits.encode())
}

// Source: this spec (arguments of other types than the inputs, and an array whose values do not match its type, are
// rejected when they are encoded).
@@testId("arguments-of-other-types-and-a-malformed-array-are-rejected-when-encoded")
test "arguments of other types and a malformed array are rejected when encoded" {
    // given
    let baz = AbiFunction.parse(signature: "baz(uint32,bool)")
    let pairType = AbiArrayType{elementType: AbiFixedBytesType{size: 3}, length: 2}

    // when / then: a uint256 instead of a uint32, a missing argument
    @@expectThrows(invalid-argument-error)
    baz.encodeCall(arguments: [AbiUint{value: 69, bits: 256}, AbiBool{value: true}])
    @@expectThrows(invalid-argument-error)
    baz.encodeCall(arguments: [AbiUint{value: 69, bits: 32}])

    // when / then: a fixed-size array with one value, an array with a value of another type
    @@expectThrows(invalid-argument-error)
    AbiTuple{values: [AbiArray{arrayType: pairType, values: [AbiFixedBytes{value: utf8(text: "abc")}]}]}.encode()
    @@expectThrows(invalid-argument-error)
    AbiTuple{values: [AbiArray{arrayType: pairType,
            values: [AbiFixedBytes{value: utf8(text: "abc")}, AbiFixedBytes{value: utf8(text: "de")}]}]}.encode()
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Types" (uint and int are synonyms for
// uint256 and int256; the canonical names are used in signatures).
@@testId("an-abi-type-is-read-from-and-written-as-its-canonical-name")
test "an ABI type is read from and written as its canonical name" {
    for name in ["uint8", "uint256", "int16", "int256", "address", "bool", "bytes1", "bytes32", "bytes", "string",
            "uint256[]", "bytes3[2]", "uint256[][3]", "(address,uint256)", "(address,uint256)[]",
            "(uint256,(bool,string[]))", "()"] {
        // when / then
        assertEquals(expected: name, actual: AbiType.parse(text: name).toString())
    }

    // given
    let uint256: AbiType = AbiUintType{bits: 256}
    let int256: AbiType = AbiIntType{bits: 256}
    let pair: AbiType = AbiArrayType{elementType: AbiFixedBytesType{size: 3}, length: 2}

    // when / then
    assertEquals(expected: uint256, actual: AbiType.parse(text: "uint"))
    assertEquals(expected: int256, actual: AbiType.parse(text: "int"))
    assertEquals(expected: pair, actual: AbiType.parse(text: "bytes3[2]"))
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Types" (uint<M> with 0 < M <= 256 and
// M % 8 == 0, bytes<M> with 0 < M <= 32); this spec (fixed, ufixed and function are not supported).
@@testId("a-name-that-is-no-supported-abi-type-is-rejected")
test "a name that is no supported ABI type is rejected" {
    for name in NO_ABI_TYPES {
        // when / then
        @@expectThrows(illegal-format)
        AbiType.parse(text: name)
    }
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Types" (uint<M> with 0 < M <= 256 and
// M % 8 == 0, bytes<M> with 0 < M <= 32).
@@testId("a-width-that-the-abi-does-not-have-is-rejected")
test "a width that the ABI does not have is rejected" {
    for bits in [0, 7, 12, 264] {
        // when / then
        @@expectThrows(invalid-argument-error)
        AbiUintType{bits: bits}
        @@expectThrows(invalid-argument-error)
        AbiInt{value: 0, bits: bits}
    }
    for size in [0, 33] {
        // when / then
        @@expectThrows(invalid-argument-error)
        AbiFixedBytesType{size: size}
    }
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (baz(uint32,bool) with the
// selector cdcd77c0); this spec (a function and an event are values of their name and types, parse only reads them
// from a signature).
@@testId("a-function-and-an-event-built-from-their-types-equal-the-parsed-ones")
test "a function and an event built from their types equal the parsed ones" {
    // when
    let baz = AbiFunction{name: "baz", inputs: [AbiUintType{bits: 32}, AbiBoolType{}], outputs: [AbiBoolType{}]}
    let bazEvent = AbiEvent{name: "baz", parameters: [AbiEventParameter{abiType: AbiUintType{bits: 32}, indexed: true},
            AbiEventParameter{abiType: AbiBoolType{}}]}

    // then
    assertEquals(expected: AbiFunction.parse(signature: "baz(uint32,bool) returns (bool)"), actual: baz)
    assertEquals(expected: "baz(uint32,bool)", actual: baz.signature())
    assertEquals(expected: hex(text: "cdcd77c0"), actual: baz.selector())
    assertEquals(expected: AbiEvent.parse(signature: "baz(uint32 indexed,bool)"), actual: bazEvent)
    assertEquals(expected: hex(text: "cdcd77c0"), actual: slice(value: bazEvent.topic(), start: 0, end: 4))
}

// Source: the Solidity documentation, "Contract ABI Specification", sections "Function Selector" and "Types" (the
// signature is the name and the canonical types of the arguments, separated by commas without spaces; tuples are in
// parentheses); this spec (" returns " and the result types).
@@testId("a-function-signature-is-read-with-its-name-arguments-and-results")
test "a function signature is read with its name, arguments and results" {
    for text in ["f()", "_()", "$()", "f_1$()", "transfer(address,uint256)", "f((uint256,address)[],bool)",
            "f(uint256[][3],bytes32[2][])", "f(())", "f(bytes,string,bool,address,int8,uint8,bytes1)"] {
        // when / then
        assertEquals(expected: text, actual: AbiFunction.parse(signature: text).signature())
    }

    // when
    let transfer = AbiFunction.parse(signature: "transfer(address,uint256) returns (bool)")
    let noResults = AbiFunction.parse(signature: "f() returns ()")

    // then
    assertEquals(expected: "transfer", actual: transfer.name)
    assertEquals(expected: [AbiAddressType{}, AbiUintType{bits: 256}], actual: transfer.inputs)
    assertEquals(expected: [AbiBoolType{}], actual: transfer.outputs)
    assertEquals(expected: AbiFunction.parse(signature: "f()"), actual: noResults)
}

// Source: EIP-165, section "How a Contract will Publish the Interfaces it Implements" (the interface identifier of
// ERC165, the selector of supportsInterface(bytes4), is 0x01ffc9a7).
@@testId("the-selector-of-supportsinterface-is-the-one-of-eip-165")
test "the selector of supportsInterface is the one of EIP-165" {
    // when
    let supportsInterface = AbiFunction.parse(signature: "supportsInterface(bytes4) returns (bool)")

    // then
    assertEquals(expected: hex(text: "01ffc9a7"), actual: supportsInterface.selector())
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Function Selector" (uint and int are
// written as uint256 and int256 in the signature from which the selector is computed).
@@testId("uint-and-int-in-a-signature-are-read-as-uint256-and-int256")
test "uint and int in a signature are read as uint256 and int256" {
    // when
    let short = AbiFunction.parse(signature: "f(uint,int[]) returns (uint)")

    // then
    assertEquals(expected: "f(uint256,int256[])", actual: short.signature())
    assertEquals(expected: AbiFunction.parse(signature: "f(uint256,int256[]) returns (uint256)"), actual: short)
    assertEquals(expected: AbiFunction.parse(signature: "f(uint256,int256[])").selector(), actual: short.selector())
}

// Source: this spec (the ABI limits neither the number of arguments and results nor the nesting of tuples and arrays,
// so parse does not either).
@@testId("a-function-with-100-arguments-and-results-and-deeply-nested-types-is-read")
test "a function with 100 arguments and results and deeply nested types is read" {
    // given
    let hundred = repeat(value: "uint256,", count: 99) + "uint256"
    let nestedTuple = repeat(value: "(", count: 32) + "uint256" + repeat(value: ")", count: 32)
    let nestedArray = "uint256" + repeat(value: "[]", count: 32)

    // when
    let wide = AbiFunction.parse(signature: "f(" + hundred + ") returns (" + hundred + ")")
    let deep = AbiFunction.parse(signature: "f(" + nestedTuple + "," + nestedArray + ")")

    // then
    assertEquals(expected: 100, actual: length(value: wide.inputs))
    assertEquals(expected: 100, actual: length(value: wide.outputs))
    assertEquals(expected: "f(" + hundred + ")", actual: wide.signature())
    assertEquals(expected: "f(" + nestedTuple + "," + nestedArray + ")", actual: deep.signature())
}

// Source: this spec (the length of a fixed-size array is a uint32 of at least 1, written without leading zeros).
@@testId("an-array-length-from-1-to-the-largest-uint32-is-read")
test "an array length from 1 to the largest uint32 is read" {
    for text in ["f(uint256[1])", "f(uint256[10])", "f(uint256[4294967295])"] {
        // when / then
        assertEquals(expected: text, actual: AbiFunction.parse(signature: text).signature())
    }

    // when / then
    @@expectThrows(illegal-format)
    AbiFunction.parse(signature: "f(uint256[4294967296])")
}

// Source: this spec (parse reads only the canonical signature: no spaces except around "returns", no parameter names,
// no unsupported types, array lengths of a uint32 of at least 1).
@@testId("a-malformed-function-signature-is-rejected")
test "a malformed function signature is rejected" {
    for text in MALFORMED_FUNCTION_SIGNATURES {
        // when / then
        @@expectThrows(illegal-format)
        AbiFunction.parse(signature: text)
    }
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Events" (the signature of an event does
// not contain "indexed"); this spec (" indexed" after the type of an indexed parameter).
@@testId("an-event-signature-is-read-with-its-name-and-indexed-parameters")
test "an event signature is read with its name and indexed parameters" {
    // when
    let empty = AbiEvent.parse(signature: "E()")
    let mixed = AbiEvent.parse(signature: "E((uint256,address) indexed,string,bytes32[2] indexed)")
    let wide = AbiEvent.parse(signature: "E(" + repeat(value: "uint256,", count: 99) + "uint256)")

    // then
    assertEquals(expected: "E()", actual: empty.signature())
    assertEquals(expected: 0, actual: length(value: empty.parameters))
    assertFalse(condition: empty.anonymous)
    assertEquals(expected: "E((uint256,address),string,bytes32[2])", actual: mixed.signature())
    assertEquals(expected: [true, false, true], actual: [mixed.parameters[0].indexed, mixed.parameters[1].indexed,
            mixed.parameters[2].indexed])
    assertEquals(expected: 100, actual: length(value: wide.parameters))
}

// Source: the Solidity documentation, "Events" (up to three parameters of an event can be indexed; a log has at most
// four topics, the first of which is the hash of the signature).
@@testId("an-event-with-three-indexed-parameters-is-read-and-one-with-four-is-rejected")
test "an event with three indexed parameters is read and one with four is rejected" {
    // when
    let three = AbiEvent.parse(signature: "E(uint256 indexed,address indexed,bool indexed,string)")

    // then
    assertEquals(expected: "E(uint256,address,bool,string)", actual: three.signature())

    // when / then
    @@expectThrows(illegal-format)
    AbiEvent.parse(signature: "E(uint256 indexed,address indexed,bool indexed,string indexed)")
}

// Source: this spec (parse reads only the canonical signature: " indexed" after the type, no other spaces, no
// parameter names, no "returns").
@@testId("a-malformed-event-signature-is-rejected")
test "a malformed event signature is rejected" {
    for text in MALFORMED_EVENT_SIGNATURES {
        // when / then
        @@expectThrows(illegal-format)
        AbiEvent.parse(signature: text)
    }
}

// Source: the Solidity documentation, "Contract ABI Specification", sections "Function Selector" and "Events" (the
// selector is the first 4 bytes of the Keccak-256 hash of the signature, the topic of an event the whole hash); the
// selector of baz(uint32,bool) of section "Examples".
@@testId("the-topic-of-an-event-is-the-hash-whose-first-4-bytes-are-the-selector")
test "the topic of an event is the hash whose first 4 bytes are the selector" {
    // given
    let bazEvent = AbiEvent.parse(signature: "baz(uint32 indexed,bool)")

    // when
    let topic = bazEvent.topic()

    // then
    assertEquals(expected: "baz(uint32,bool)", actual: bazEvent.signature())
    assertEquals(expected: 32, actual: length(value: topic))
    assertEquals(expected: hex(text: "cdcd77c0"), actual: slice(value: topic, start: 0, end: 4))
    assertEquals(expected: [AbiEventParameter{abiType: AbiUintType{bits: 32}, indexed: true},
            AbiEventParameter{abiType: AbiBoolType{}, indexed: false}], actual: bazEvent.parameters)
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Events" (the first topic is the hash of
// the signature, an indexed value of at most 32 bytes is a topic of its own padded to 32 bytes, the other values are
// encoded in the data); the addresses of EIP55_ADDRESSES.
@@testId("a-log-of-a-transfer-is-decoded")
test "a log of a transfer is decoded" {
    // given
    let transfer = AbiEvent.parse(signature: "Transfer(address indexed,address indexed,uint256)")
    let from = EvmAddress.fromString(value: EIP55_ADDRESSES[4])
    let to = EvmAddress.fromString(value: EIP55_ADDRESSES[5])
    let topics = [transfer.topic(), hex(text: "000000000000000000000000") + from.bytes,
            hex(text: "000000000000000000000000") + to.bytes]

    // when
    let values = transfer.decodeLog(topics: topics,
            data: hex(text: "00000000000000000000000000000000000000000000000000000000000003e8"))

    // then
    let expected: list<AbiValue> = [AbiAddress{value: from}, AbiAddress{value: to}, AbiUint{value: 1000, bits: 256}]
    assertEquals(expected: expected, actual: values)
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Events" (an indexed value of a dynamic
// type is stored as the Keccak-256 hash of its encoding); this spec (the hash is returned as an AbiFixedBytes).
@@testId("an-indexed-string-of-a-log-is-returned-as-its-hash")
test "an indexed string of a log is returned as its hash" {
    // given
    let named = AbiEvent.parse(signature: "Named(string indexed,uint256)")
    let hash = hex(text: repeat(value: "ab", count: 32))

    // when
    let values = named.decodeLog(topics: [named.topic(), hash],
            data: hex(text: "0000000000000000000000000000000000000000000000000000000000000007"))

    // then
    let expected: list<AbiValue> = [AbiFixedBytes{value: hash}, AbiUint{value: 7, bits: 256}]
    assertEquals(expected: expected, actual: values)
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Events" (an anonymous event has no topic
// with its signature); this spec (a log of another event or with a missing topic is rejected).
@@testId("a-log-of-another-event-or-with-other-topics-is-rejected-and-an-anonymous-one-is-read-without-its-topic")
test "a log of another event or with other topics is rejected, and an anonymous one is read without its topic" {
    // given
    let transfer = AbiEvent.parse(signature: "Transfer(address indexed,address indexed,uint256)")
    let from = hex(text: "000000000000000000000000") + EvmAddress.fromString(value: EIP55_ADDRESSES[4]).bytes
    let amount = hex(text: "00000000000000000000000000000000000000000000000000000000000003e8")
    let other = AbiEvent.parse(signature: "Approval(address indexed,address indexed,uint256)")

    // when / then: the topic of another event, a missing topic
    @@expectThrows(illegal-format)
    transfer.decodeLog(topics: [other.topic(), from, from], data: amount)
    @@expectThrows(illegal-format)
    transfer.decodeLog(topics: [transfer.topic(), from], data: amount)

    // when
    let anonymous = AbiEvent{name: "Moved", parameters: [AbiEventParameter{abiType: AbiAddressType{}, indexed: true},
            AbiEventParameter{abiType: AbiUintType{bits: 256}, indexed: false}], anonymous: true}
    let values = anonymous.decodeLog(topics: [from], data: amount)

    // then
    let expected: list<AbiValue> = [AbiAddress{value: EvmAddress.fromString(value: EIP55_ADDRESSES[4])},
            AbiUint{value: 1000, bits: 256}]
    assertEquals(expected: expected, actual: values)
}

// Source: this spec (a value knows its type).
@@testId("a-value-knows-its-type")
test "a value knows its type" {
    // given
    let uint32: AbiType = AbiUintType{bits: 32}
    let bytes3: AbiType = AbiFixedBytesType{size: 3}

    // when / then
    assertEquals(expected: uint32, actual: AbiUint{value: 1, bits: 32}.abiType())
    assertEquals(expected: bytes3, actual: AbiFixedBytes{value: utf8(text: "abc")}.abiType())
    assertEquals(expected: AbiType.parse(text: "(bool,string)"),
            actual: AbiTuple{values: [AbiBool{value: true}, AbiString{value: "x"}]}.abiType())
}

// Source: the Solidity documentation, "Expressions and Control Structures", section "Error handling: Assert, Require,
// Revert and Exceptions": a revert with a reason is the call data of Error(string) (selector 0x08c379a0), the example
// "Not enough Ether provided."; a panic is Panic(uint256) (selector 0x4e487b71), code 0x11 for an arithmetic overflow.
@@testId("the-revert-data-of-error-string-and-panic-uint256-is-decoded")
test "the revert data of Error(string) and Panic(uint256) is decoded" {
    // given
    let error = AbiFunction.parse(signature: "Error(string)")
    let panic = AbiFunction.parse(signature: "Panic(uint256)")
    let revert = hex(text: "08c379a0"
            + "0000000000000000000000000000000000000000000000000000000000000020"
            + "000000000000000000000000000000000000000000000000000000000000001a"
            + "4e6f7420656e6f7567682045746865722070726f76696465642e000000000000")
    let overflow = hex(text: "4e487b71" + "0000000000000000000000000000000000000000000000000000000000000011")

    // when
    let reason = error.decodeCall(data: revert)
    let code = panic.decodeCall(data: overflow)

    // then
    assertEquals(expected: hex(text: "08c379a0"), actual: error.selector())
    assertEquals(expected: hex(text: "4e487b71"), actual: panic.selector())
    assertEquals(expected: [AbiString{value: "Not enough Ether provided."}], actual: reason)
    assertEquals(expected: [AbiUint{value: 17, bits: 256}], actual: code)

    // when / then: revert data of the other one is rejected
    @@expectThrows(illegal-format)
    error.decodeCall(data: overflow)
}
```

## Questions & Comments

- **JSON ABI.** The Solidity compiler and every Ethereum tool describe a contract as a JSON ABI (a list of functions,
  events and errors with parameter names). Reading it (`AbiContract.parse(json)` or similar) is what a generator of
  contract interfaces needs. It is not specified yet: which JSON library a language may use is open, and the
  signatures cover the calls the SDK makes.
- **Errors of a reverted call.** A reverted call returns `Error(string)` (selector `0x08c379a0`), `Panic(uint256)` or
  a custom error of the contract. An `AbiError` that decodes them would be the counterpart of `AbiEvent`; it is needed
  once `ContractFunctionResult` exposes the revert data.
- **Parameter names.** Human-readable signatures with names (`transfer(address to, uint256 amount)`) are common in
  other libraries. `parse` rejects them; values are addressed by position.
