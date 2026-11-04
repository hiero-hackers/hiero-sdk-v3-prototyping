# EVM API

## Description

Types of the Ethereum Virtual Machine (EVM) that do not depend on a particular network: they mean the same on a Hiero
network as on any other EVM network.

`EvmAddress` is a 20-byte EVM address. It is read from 40 hex digits, with or without `0x`, and written in the
mixed-case checksum form of EIP-55 (`0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed`). Digits in mixed case must be the
EIP-55 form; a mistyped address is rejected instead of being read as a different one.

## Design Notes

- **The namespace imports nothing.** `evm` holds only what is defined by the EVM and its standards (EIP-55, the
  contract ABI), never a Hiero type: no `ContractId` or `AccountId`, no long-zero address of an entity number, no
  system contract address. Those live in `ledger` and `consensusnode.*`, which may import `evm`; the reverse is not
  allowed, so that `evm` can later become a spec folder of its own without changes
  ([ADR-0015](../../docs/adr/0015-evm-generic-types-in-a-namespace-without-imports.md)). The tooling does not
  enforce the rule; a review does.
- **Scope.** What a Hiero SDK needs of the EVM, not an Ethereum library: no JSON-RPC, wallets or providers.

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
```

## Default Instances for Testing

```
// EIP-55, section "Test Cases": addresses in their EIP-55 form (all caps, all lower and mixed case).
values EIP55_ADDRESSES: list<string> = ["0x52908400098527886E0F7030069857D2E4169EE7",
        "0x8617E340B3D01FA5F11F306F4090FD50E238070D", "0xde709f2102306220921060314715629080e2fb77",
        "0x27b1fdb04752bbc536007a920d24acb045561c26", "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed",
        "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359", "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB",
        "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb"]
```

## Testing

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
```

## Questions & Comments
