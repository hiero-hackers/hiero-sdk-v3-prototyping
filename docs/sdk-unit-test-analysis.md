# Unit tests of the current SDKs as tests of the specs: analysis

Which unit tests of the seven Hiero SDKs describe behaviour that every SDK must show, so that they belong into the
`## Testing` sections of the specs (see [`guidelines/test-language.md`](../guidelines/test-language.md))? This
document collects the candidates, the values they use, where the SDKs disagree, and what the specs lack to express
them. It is an analysis, not a decision; the decisions it asks for are listed at the end.

## Method

- **Sources.** The unit tests of `hiero-ledger/hiero-sdk-{java,js,go,rust,python,cpp,swift}` at the state of
  2026-10-15 (Java `f6087fd`, JS `5e7eac1`, Go `ea08f5e`, Python `5c00486`, C++ `f38e648`, Rust `3b0696e`, Swift
  `d09f5eb`): about 1,250 test files. Integration tests against a network were left out.
- **Criteria.** A test is a candidate if it is (1) offline, (2) about behaviour of the protocol, a HIP or a published
  standard rather than of one SDK's implementation, and (3) ideally tested by two or more SDKs with the same values.
  The test language also requires that an expected value comes from the spec itself or from a published test vector.
- **Verification.** The values below that are marked ✔ were recomputed independently, not taken from the SDKs: key
  derivations and RFC 8032 signatures with OpenSSL 3.6, HIP-15 checksums with an implementation of the published
  algorithm, ABI selectors with Keccak-256, status codes against the vendored `response_code.proto`.

## Summary

- **About a tenth of the unit tests are candidates.** Most SDK tests check V2 builders (set/get, "immutable after
  freeze"), protobuf round trips and snapshots, retry and channel logic. In V3 these are immutable records, internal
  protobuf and implementation details; there is nothing to copy.
- **The candidates are dense where standards are involved:** keys (shared reference vectors of
  hedera-sdk-reference#93 in six SDKs, RFC 8032), HIP-15 checksums (six SDKs, full tables in two), entity-id
  parsing, Hbar units and parsing, TransactionId strings, Solidity ABI encoding and selectors (four SDKs), Ethereum
  RLP data (six SDKs), semantic versions, Mirror Node path rules.
- **Only some of them can be written today.** The specs lack the API for many (Hbar parsing and formatting,
  checksum computation, long-zero EVM addresses, NftId, ABI encoding, Ethereum data), and several behaviours differ
  between the SDKs, so a spec has to decide before a test can say what is right.
- **The analysis found problems in the specs:** the SPKI examples of `keys.md` are truncated, the existing test
  "Ed25519 signatures are deterministic" fails on Swift (CryptoKit randomises), `EvmAddress.toString()` promises
  EIP-55 and key aliases promise base32, which no SDK does, equality of ids and Hbar amounts is field-wise in the test
  language but value-wise in every SDK, and the ECDSA "PKCS#8" form that six SDKs use is no valid PKCS#8.

## Category 1: adopt now

These map onto the current specs and use values that the SDKs agree on and that were verified.

### Keys (`spec/base/keys.md`)

| Test | Input → expected | SDKs | |
|---|---|---|---|
| Ed25519 PKCS#8 DER import | `302e020100300506032b657004220420feb858a4a69600a5eef2d9c76f7fb84fc0b6627f29e0ab17e160f640c267d404` → private `feb858a4…c267d404`, public `8ccd31b53d1835b467aac795dab19b274dd3b37e3daf12fcec6bc02bac87b53d` | Java, JS, Rust, C++, Swift | ✔ |
| Ed25519 PKCS#8 PEM import | `MC4CAQAwBQYDK2VwBCIEIOgbjaHgEqF7PY0t2dUf2VU0u1MRoKii/fywDlze4lvl` → private `e81b8da1…ee25be5`, public `f7b9aa4a8e4eee94e4277dfe757d8d7cde027e7cd5349b7d8e6ee21c9b9395be` | Java, JS, Rust, C++, Swift | ✔ |
| Ed25519 SPKI import/export | `302a300506032b6570032100` + raw key ↔ raw key | Java, JS, Go, Rust, C++, Swift | |
| RFC 8032 §7.1: public key and `verify` | TEST 1 secret `9d61b19d…7f60` → public `d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a`; TEST 2 message `72` → signature `92a009a9…bb0c00` verifies | C++ (published vector) | ✔ |
| secp256k1 public key derivation | raw `8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048` → `02703a9370b0443be6ae7c507b0aec81a55e94e4a863b9655360bd65358caa6588` | Go, JS, Rust, Swift | ✔ |
| secp256k1 SPKI import (RFC 5480) | `3036301006072a8648ce3d020106052b8104000a03220003…88f4` → `036843f5…88f4`; uncompressed `3056…` → compressed `03aaac1c…a65977` | Java, JS, Rust, C++, Swift | |
| rejection | secp256k1 scalar 0 and the curve order `ffff…364141`; 33 zero bytes as ECDSA public key; malformed hex (`fdsakfdsalf`, odd length); PEM label `BEGIN PRIVATE KEYS` | Go, Python, C++, Rust, Swift | |
| equality | the same 32 bytes as Ed25519 and as ECDSA key are not equal | Python | |

Written in the test language:

```tests
test "the private key of a PKCS#8 DER Ed25519 key derives the reference public key" {
    // given
    let der = hex(text: "302e020100300506032b657004220420feb858a4a69600a5eef2d9c76f7fb84fc0b6627f29e0ab17e160f640c267d404")

    // when
    let key = createPrivateKey(container: KeyFormat.PKCS8_WITH_DER, value: der)

    // then
    assertEquals(expected: hex(text: "feb858a4a69600a5eef2d9c76f7fb84fc0b6627f29e0ab17e160f640c267d404"), actual: key.bytes)
    assertEquals(expected: hex(text: "8ccd31b53d1835b467aac795dab19b274dd3b37e3daf12fcec6bc02bac87b53d"),
            actual: key.createPublicKey().bytes)
}

test "an RFC 8032 signature verifies with the RFC 8032 public key" {
    // given
    let publicKey = createPublicKey(algorithm: KeyAlgorithm.ED25519,
            rawBytes: hex(text: "3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c"))
    let signature = hex(text: "92a009a9f0d4cab8720e820b5f642540a2b27b5416503f8fb3762223ebdb69da"
            + "085ac1e43e15996e458f3613d0f11d8c387b2eaeb4302aeeb00d291612bb0c00")

    // when / then
    assertTrue(condition: publicKey.verify(message: hex(text: "72"), signature: signature))
    assertFalse(condition: publicKey.verify(message: hex(text: "73"), signature: signature))
}
```

### Entity ids (`spec/base/ledger.md`)

| Test | Input → expected | SDKs | |
|---|---|---|---|
| HIP-15 checksum is parsed and valid on its ledger | `0.0.123-vfmkw` (mainnet `[0]`), `0.0.123-esxsf` (testnet `[1]`), `0.0.123-ogizo` (previewnet `[2]`); `0.0.1` → `dfkxr` / `mswfa` / `wghmj`, ledger `[3]` → `ftsts` | Java, JS, Go, Rust, Python, Swift | ✔ |
| a checksum of another ledger is invalid | `0.0.123-ntjli` on previewnet; `0.0.123-vfmkw` on testnet | Java, Rust, Swift | ✔ |
| multi-byte ledger id | `0.0.0` on ledger `[12, 31, 0, 2]` → `kenvf` | Rust, Swift (full 256-entry tables) | ✔ |
| malformed checksum rejected | `0.0.123-ntjl`, `0.0.123-ntjl1`, `0.0.123-` | Java, Go, Rust, Swift | |
| malformed id rejected | `0.0.`, `0.0`, `a.b.c`, `0.0.a`, `0.0.-1`, `-1.-1.-1`, `1.2.3.4`, `""` | JS, Python, C++, Java, Rust, Swift | |
| round trip | `0.0.5005`, `1.2.3`, `10.50.25050` ↔ shard, realm, num | all | |
| `EvmAddress` parsing | 40 hex digits with or without `0x` accepted; `0x1234`, `""`, 16 bytes, non-hex rejected; `fromBytes` with 19 or 21 bytes rejected | Swift, Rust, Python, C++ | |
| `ContractId.fromEvmAddress` | `(1, 2, 98329e006610472e6B372C080833f6D79ED833cf)` keeps shard 1, realm 2 and the address | Java, Rust, Go, JS | |
| IPv4 bytes | `[192, 168, 1, 1]` → `"192.168.1.1"`; 3 or 5 bytes rejected | C++ (dotted quad is a standard) | |

```tests
test "an account id checksum is valid on the ledger it was computed for" {
    // given
    let id = AccountId.fromString(value: "0.0.123-esxsf")

    // when
    let valid = id.validateChecksum(network: DEFAULT(Network))

    // then
    assertEquals(expected: "esxsf", actual: id.checksum)
    assertTrue(condition: valid)
}
```

(`DEFAULT(Network)` is the testnet, ledger id `[1]`.) Note for the `ContractId` case: the spec's `@@oneOf(num,
evmAddress)` makes `num` null, while Java and Go return 0 — the test follows the spec.

### Hbar units and TransactionId strings

| Test | Input → expected | SDKs |
|---|---|---|
| unit factors | TINYBAR 1 … GIGABAR 10^17 | Python, C++ (normative, equals `hedera.md`) |
| construction → base units | 50 ℏ → 5,000,000,000 tℏ; 50 μℏ → 5,000; 50 kℏ → 5,000,000,000,000; negative amounts | Java, Python, C++, Swift |
| TransactionId round trip | `0.0.23847@1588539964.632521325` and `…/4` (nonce 4) unchanged by `fromString` → `toString`; `accountId` = `0.0.23847` | Java, Rust, Swift, Python |
| TransactionId rejection | `0.0.31415?1641088801.2`, `0.0.31415/1641088801.2`, `0.0.31415?scheduled/1412@1641088801.2`, `0.0.23847.1588539964.632521325/4`, `0.0.23847@1588539964/4`, `0.0.123@abc.def` | Rust, Swift, Java, Python |

### Elsewhere

- **SemanticVersion.toString** (`consensus-node-admin-client/queries-network.md`): `(1,2,3)` → `"1.2.3"`, `(3,1,4,
  pre "15.92")` → `"3.1.4-15.92"`, `(1,41,1, build "6535asd")` → `"1.41.1+6535asd"`, `(0,1,4,"0.9a2","sha.25531c")`
  → `"0.1.4-0.9a2+sha.25531c"` (Rust, Swift; semver.org).
- **Mirror Node REST paths** (`mirror-node-client/mirror-node-http.md`): rejected `"https://evil.example/api/v1/accounts"`,
  `"accounts/0.0.3"`, `""`, `"/accounts\n/0.0.1"`; accepted `"/"`, `"/accounts/0.0.1/tokens?token.id=gt:0.0.5&order=asc"`
  (Java, JS, Go). Expressible as far as the `@@pattern` of `MirrorNodeHttpRequest.path` reaches; see category 2.
- **ServiceEndpoint** is `@@oneOf(ipAddress, domainName)`: an endpoint with both is rejected (Java, Python).

### Transactions (`spec/consensus-node-client/transactions.md`)

The generic transaction behaviour that the V3 API can express offline, tested on `PrngTransaction` with the default
account as payer (the tests are in the `## Testing` section of `transactions.md`):

| Test | Protocol / source | SDKs |
|---|---|---|
| one body per target node, in the order of the nodes, each naming its node; the payer is the account of the id | HAPI | Java, Go, JS, Python |
| signing adds one 64-byte signature per node that verifies over the body of that node | HAPI (signature over `bodyBytes`) | Java, Python, C++, Go |
| signing again with the same key adds nothing | a key appears once in a `SignatureMap` | Python, Java, Go |
| an Ed25519 and a secp256k1 key both sign every body | `SignaturePair` | JS, Python |
| `toBytes`/`fromBytes` keeps the bytes, the id, the bodies and the signatures | body bytes are kept byte-exact | Rust, Python, JS, Go, Swift |
| a restored transaction is signed by another party | multi-party signing | Python |
| signatures made outside the SDK are attached (`sign(signatures)`) | out-of-process signing | Rust, JS |
| `packForBatch`: one body for node `0.0.0` | HIP-551 | Python |
| a `TransactionList` of another SDK with a valid start in 2125 is read (✔ decoded independently) | 64-bit seconds | JS fixture |
| `BasicTransactionStatus` codes 0, 1, 2, 399 (✔) | `response_code.proto` | Java, Swift, JS, Go, Python |

Two sentences were added to the API documentation because every SDK behaves that way: `signableBodies()` keeps the
order of the nodes, and `PackedTransaction.sign(...)` does not add a key that has already signed.

Not expressible yet (category 2): the transaction hash (SHA-384 of the signed transaction bytes; a JS fixture pins
`c0a57957…`), reading memo, fee and valid duration back from a `PackedTransaction`, the 120-second default valid
duration, the 100-byte memo limit, `@@throws` ids for `sign(signatures)` (wrong or missing node) and `fromBytes`
(empty, malformed, smuggled bodies), a fixed `TransactionId` for byte-exact fixtures, a default `TransactionSigner`
for `sign(payerId, signer, nodes)`, `highVolume` (HIP-1313, tested by six SDKs), and the remaining status codes.
Not adopted: the default max fees per type (SDK conventions), and everything about V2 freezing.

## Category 2: adopt after a spec change

The behaviour is general and the SDKs agree, but the spec cannot express it yet.

| Area | What the spec needs | Candidate tests |
|---|---|---|
| **Hbar** (`hedera.md`, `native-token.md`) | `Hbar.fromString` with `@@throws(illegal-format)`; `toString`; `negated`; a decimal amount or `toDecimal(unit)` (50 ℏ in kℏ is 0.05) | Java and Python share one parse table (`"1"`, `"1 ℏ"`, `"1.5 mℏ"` → 150,000, `"+3"`, `"-3"`); JS and Python share one reject list (`"1 "`, `"1."`, `".1"`, `"1.151 uℏ"`, `"1.151 h"`, …); fractional tinybars (`0.1 tℏ`) rejected by Java, Swift, Python |
| **Hbar equality** | equality by value (tinybars), not by the attributes `amount` and `unit` | 50 ℏ equals 5,000,000,000 tℏ in every SDK |
| **Checksum computation** (`ledger.md`) | `toStringWithChecksum(network)` — today it can only repeat a parsed checksum | `0.0.123` on testnet → `"0.0.123-esxsf"` (Rust, Swift, Go) ✔ |
| **Checksum and equality** | decide whether `checksum` takes part in `==`; in the test language it does, in Go it does not | `{1,2,3}` equals `{1,2,3, checksum "abcde"}` (Go) |
| **Long-zero EVM addresses** | `Address`/`AccountId`/`ContractId` ↔ `EvmAddress`, and whether shard and realm are encoded | `0.0.5005` ↔ `000000000000000000000000000000000000138d` (Java, Rust, Swift, Go, JS) |
| **NftId** | the type | `"0.0.5005@1234"` and `"0.0.5005/1234"` → `"0.0.5005/1234"`; with checksum; eight reject cases (five SDKs) |
| **`PublicKey.toEvmAddress()`** | the method (already an open question in `keys.md`) | `DEBAE3CA62AB3157110DBA79C8DE26540DC320EE9BE73A77D70BA175643A3500` → `d8eb8db03c699faa3f47adcdcd2ae91773b10f8b` (Java, Rust, C++, Swift); Ed25519 rejected |
| **SEC1 / `EC PRIVATE KEY`** | a container for it (six SDKs read it) | six shared reference vectors of hedera-sdk-reference#93 |
| **Encrypted PEM** | a passphrase parameter | Ed25519 and secp256k1 vectors with passphrase `asdasd123` (Java, JS, Rust, Swift, Go) |
| **TransactionId.scheduled** | the attribute | `…?scheduled` and `…?scheduled/3` round trips (Java, Rust, Swift, Go, Python) |
| **Solidity ABI** | a contract-call encoding API: `encode(function, params)`, selector, result getters | selectors ✔ `foo(address,address,address[])` → `7d48c86d`, `foo(bool,bool)` → `b3cedfcf`, `set_message(string)` → `2e982602`, `set_message(bytes)` → `010473a7`, the 26-type `testFunction` → `4438e4ce` (Java, Rust, Swift, C++); full encodings and a result vector in four to five SDKs |
| **Ethereum data** | an RLP / Ethereum transaction data type | one legacy and one EIP-1559 vector with field-level expectations in six SDKs |
| **Node transactions** | annotations: `@@maxSize(10)` gossip endpoints, `@@maxSize(8)` service endpoints, 48-byte certificate hash, `nodeId ≥ 0`, description length | Java and Python enforce 100 / 10 / 8 / 48; description counts **UTF-8 bytes** — `@@maxLength` counts code points, so a byte-length annotation or a decision is needed |
| **Status codes** | complete `BasicTransactionStatus` (at least `SUCCESS(22)`) | ✔ `SUCCESS = 22`, `RECEIVER_SIG_REQUIRED = 113`, `INVALID_GOSSIP_CA_CERTIFICATE = 344`, `SERVICE_ENDPOINTS_EXCEEDED_LIMIT = 356`, `INVALID_IPV4_ADDRESS = 357`, `PENDING_AIRDROP_ID_LIST_TOO_LONG = 363`, `INVALID_TOKEN_IN_PENDING_AIRDROP = 369` |
| **Ledger ids** | constants for mainnet `[0]`, testnet `[1]`, previewnet `[2]`, local `[3]` | JS, Swift |
| **Mirror Node paths** | a pattern that rejects `//host` and `..` segments | `"//evil.example/steal"`, `"/accounts/../../etc/passwd"` (Java, JS, Go) |
| **Validation errors in tests** | an error id for a violated validation annotation, so that `@@expectThrows` can name it | every `@@maxSize` / `@@oneOf` violation in `Type{…}` |

## Category 3: do not adopt

- **V2 builder mechanics**: setters and getters, "transaction is immutable after freeze", `toBytes`/`fromBytes`.
  V3 transactions are immutable records.
- **Protobuf snapshots and model mappings** (Java `.snap`, Swift `__Snapshots__`, `fromProtobuf`). Protobuf is internal
  in V3 except for the SPI (ADR-0009).
- **Network, retry, channels, TLS, node health, logging, mocked gRPC and Mirror Node responses.** These are
  implementation details.
- **Batch restrictions** ("FreezeTransaction is not allowed in a batch"). V3 rejects them by design; see
  `transactions-batch.md`.
- **Mnemonics, SLIP-10/BIP-39 derivation, recovery ids.** These have published vectors, but they are not part of
  the specs.
- **Language artefacts**: `hashCode` values, NaN and type checks, operator overloading, signed readings of unsigned
  values (Java `getUint32(3) == -1`).
- **Custom fees, KeyList, enum wire codes.** No SDK validates custom fees or KeyList thresholds on the client. The
  enums carry no wire codes in V3.

The `@@maxLength` and `@@maxSize` annotations of the specs have **no V2 test to copy**. No SDK checks memo lengths,
list caps or token name lengths on the client, except for node transactions. Tests for these annotations come from
the specs, not from the SDKs; the generated contract tests already cover their boundaries.

## Where the SDKs disagree — decisions for the specs

| Question | SDKs | Recommendation |
|---|---|---|
| Must Ed25519 signing be deterministic (RFC 8032)? | Swift/CryptoKit randomises; all others are deterministic | **Decided:** not required; `sign` must produce a signature that verifies (RFC 8032), the test checks only that. |
| ECDSA signature: low-s or not; which hash | Go, Rust and Swift normalise to low-s (`…086e3717…`); Java does not (`…f791c8e8…`); the hash (Keccak-256) is stated nowhere | **Decided:** Keccak-256, RFC 6979, low-s, 64-byte r‖s when signing; `verify` accepts a high s. Both known answers are tests in `keys.md`. |
| ECDSA private key "PKCS#8" | Six SDKs use `3030020100300706052b8104000a04220420<scalar>`. This is **no valid PKCS#8**: OpenSSL rejects it; the standard form is `303e020100301006072a8648ce3d020106052b8104000a0427302502010104…` (✔). Go exports SEC1. | **Decided:** standard PKCS#8 (RFC 5958, RFC 5915) only; the legacy form is rejected (test in `keys.md`). |
| ECDSA SPKI export | Go and Python: RFC 5480 (`3036…`); Rust, Swift, JS, C++: legacy curve-OID-only `302d…` | **Decided:** RFC 5480 with the compressed point (test in `keys.md`). |
| ECDSA public key `bytes` | All return the 33-byte compressed point | Specify it in `keys.md`. |
| TransactionId nanos | Java, JS, Go, Python pad to 9 digits (`.000000005`); Rust, Swift, C++ do not (`.5` means 5 ns there) | Specify 9 zero-padded digits. Then adopt `0.0.3@1614997926.5` → `0.0.3@1614997926.000000005`. |
| Hbar `toString` | Java, JS, Rust, Swift: `tℏ` below 10,000 tinybars, else decimal ℏ (`9999 tℏ`, `0.0001 ℏ`); Python: always 8 decimals | Add `toString` with the majority format; Rust's boundary values are the tests. |
| μ in `μℏ` | Spec and five SDKs: U+03BC; Swift: U+00B5 (✔) | Keep U+03BC and say so; strings compare by code point. |
| `EvmAddress.toString` | Lowercase with `0x` (Swift, Rust), without (Java, Python), uppercase (C++); the spec says EIP-55, which no SDK does | Decide; EIP-55 vectors would have to come from EIP-55 itself. |
| Key-alias text form | The spec says `shard.realm.<base32>`; the SDKs print hex DER (`0.0.302a3005…`) | Decide. |
| Long-zero address of a non-zero shard/realm | Java, Go, JS encode num only; Python packs shard and realm; Rust reads them back | Decide before adding the conversion. |
| Bare forms | JS accepts `"25050"` as `0.0.25050`; Java accepts a bare EVM hex; leading zeros (`0.00.123`) only rejected by Python | Decide which strings are ids. |
| `num` range | The spec: `uint64`; HAPI: `int64` | Narrow to the `int64` range, or document it. |
| Hbar MIN/MAX | Python, Java: ±50 billion ℏ; Go: `int64` range | Out of scope for now; open question in `hedera.md`. |

## Problems found in the specs

- **`keys.md`: the SPKI examples are truncated.** The DER hex has 29 of 32 key bytes, and the PEM decodes to
  42 bytes. They belong to no known key; the test suite must not use them. This was also noted in the Q&C of
  `guidelines/test-language.md`.
- **(resolved) `keys.md`: "Ed25519 signatures are deterministic"** was asserted by a test, but CryptoKit does not guarantee it
  (decision above).
- **`ledger.md`:** `EvmAddress.toString()` says EIP-55 and the key alias says base32, but no SDK does either.
  `toStringWithChecksum()` cannot compute a checksum, because it has no ledger.
- **The test language: equality of value types.** "Same concrete type, all attributes equal" makes 50 ℏ ≠
  5,000,000,000 tℏ and two ids that differ only in the checksum unequal. Every SDK decides otherwise for Hbar, and
  Go does for ids. Types with value semantics need a way to say how they compare (an `equals` declared in the spec, or
  a normalised representation).
- **`spec/enterprise/service-contract.md`:** `ofBytes23` looks like a typo for `ofBytes32`.

## Next steps

1. Write the tests of category 1 into the `## Testing` sections of `keys.md` and `ledger.md`, and the Hbar and
   TransactionId parts that are expressible. They are values verified here and accepted by most SDKs.
2. Settle the decisions above, starting with those that make existing tests wrong or untestable: Ed25519
   determinism, the ECDSA PKCS#8 form, value equality of `Hbar` and ids, TransactionId nanos.
3. Add the API that unlocks most cross-SDK vectors: `Hbar.fromString`/`toString`, `toStringWithChecksum(network)`,
   `PublicKey.toEvmAddress()`, `TransactionId.scheduled`, a low-level ABI encoding.
4. Every test taken over records its source in a comment (standard, HIP, or "hedera-sdk-reference#93"), so that a
   reviewer can check the value.
