## Source Reference

- [Common proposal](../../../../openspec/changes/add-numeric-address-public-api/proposal.md)
- [Common specification](../../../../openspec/changes/add-numeric-address-public-api/spec.md)
- Source revision: `f27ddd82aa60479f4850dde4957ec9ac86e8fc88`
- [HIP-15 Address Checksum](https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-15.md)

## Context

`@hiero-ledger/sdk-base-api` already exports `Network` and `NativeTokenUnit`. This change adds the numeric address types
to the same package and no dependencies. Mappings follow `guidelines/api-best-practices-js.md` and the *Narrowing
inherited nullability* rule of `guidelines/api-guideline.md`.

## Decisions

### Numeric representation

`uint64` values are `bigint`, consistent with the `int64` mapping of `add-native-token-public-api`. An internal
`requireUint64` helper (next to `requireInt64`) throws `TypeError` for non-`bigint` input and `RangeError` outside
`0`..`2^64 − 1`.

### `BaseAddress` abstract class

- `protected constructor(shard: bigint, realm: bigint, checksum: string)`; state in ECMAScript private fields behind
  getter-only accessors. It validates `shard` and `realm` as uint64 and `checksum` as a string. The HIP-15 checksum
  format is checked by `Address`, because later subtypes (EVM-form ids) may carry no checksum.
- `abstract get num(): bigint | null` — the nullable slot.
- `abstract toString(): string` and `abstract validateChecksum(network: Network<NativeTokenUnit>): boolean`, because
  the canonical form and checksum depend on the selector of each subtype.
- `toStringWithChecksum()` is implemented once in the base: `toString()` plus `-<checksum>` when non-empty.
- `BaseAddress` stays extensible (protected constructor) for the later `EvmCapableAddress`.

### `Address` concrete class

- `@@finalType` is mapped to a **private** constructor: TypeScript rejects `extends Address` and `new Address(...)` at
  compile time. Instances are created through `Address.of(shard, realm, num, checksum = "")`, `Address.fromString()`,
  and `ZERO_ADDRESS`.
- `override get num(): bigint` narrows the inherited nullable accessor, which TypeScript accepts as a covariant getter.
- The checksum must be `""` or exactly five lowercase ASCII letters; otherwise `IllegalFormatError`.

### `IllegalFormatError`

Defined like `MirrorNodeError`: `extends Error`, `name = "IllegalFormatError"`, `(message: string, options?: ErrorOptions)`.
Format problems throw it; wrong argument types throw `TypeError` and out-of-range numbers passed to `Address.of()` throw
`RangeError`, consistent with the other types in the package. Out-of-range numbers inside a string passed to
`fromString()` are a format problem and throw `IllegalFormatError`.

### Parsing

`fromString()` uses the HIP-15 input regex
`^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-([a-z]{5}))?$`, then converts each part with `BigInt` and rejects values
above `2^64 − 1`. No whitespace trimming or leading zeros are accepted, so parsing followed by
`toStringWithChecksum()` round-trips exactly. The checksum is stored, not validated, because no network is given.

### HIP-15 checksum

An internal `hip15Checksum(ledgerId: Uint8Array, address: string): string` implements the HIP-15 pseudocode: digits with
`.` mapped to 10, the ledger id followed by six zero bytes, weights of 31, `p3 = 26^3`, `p5 = 26^5`, the final multiply
by 1 000 003, and five base-26 letters `a`–`z` with the most significant first. Every intermediate value stays below
`2^53` (the largest is `(p5 − 1) * 1 000 003 ≈ 1.2 × 10^13`), so `number` arithmetic is exact. The letter order is
confirmed against the HIP-15 reference vectors.

`validateChecksum(network)` checks `network instanceof Network` (`TypeError`), returns `false` for an empty checksum, and
otherwise compares the stored checksum with `hip15Checksum(network.id, toString())`.

### `ZERO_ADDRESS`

An exported `const ZERO_ADDRESS: Address = Address.of(0n, 0n, 0n)`. Instances are immutable, so sharing one instance is
safe.

### Exported contract

`src/index.ts` adds `Address`, `BaseAddress`, `IllegalFormatError`, and `ZERO_ADDRESS`. `requireUint64` and
`hip15Checksum` stay internal. The package-boundary test's expected export list is extended accordingly.

## Verification Approach

- Runtime tests: construction and validation through `Address.of()`, immutability, parsing of valid and every HIP-15
  rejected input, uint64 boundaries, formatting, round-trips, `ZERO_ADDRESS`, and all eighteen HIP-15 reference vectors
  for ledger ids `0x00` and `0xa1ff01` (both `hip15Checksum` directly and `validateChecksum`).
- Type-level tests: `num` narrowing, `bigint` accessors, read-only state, the private constructor, and that
  `validateChecksum` accepts a `Network` of any unit type.
- The consumer-compilation check parses, formats, and validates an address through the published entry point and
  asserts that `Address` cannot be extended.
- The package-boundary test asserts the extended export list and no runtime dependencies.
