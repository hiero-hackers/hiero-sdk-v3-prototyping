# Gemini vs. Claude: TypeScript SDK

**Keep Claude's version as the base.** Gemini's version (PR #28) and Claude's version of `sdk-ts` implement the same three common changes (native token, network identity, numeric address) on the same core ideas: `bigint`, private fields, a protected `create()` hook and HIP-15 checksums. They split on seven behaviors. In each, Gemini picks the permissive option: inexact conversions truncate silently, an address with no checksum validates as `true`, values outside the uint64 range and non-canonical strings parse, and units are plain objects with no type check.

Claude rejects every one of those, and also ships the pagination contract, type-level tests and a consumer compile check that Gemini does not have. Worth carrying over from Gemini: its `./base` subpath export and its testnet, previewnet and custom-ledger checksum fixtures, which match Claude's implementation exactly.

Scope note: Gemini's version was compared at head commit `3b4d7fe8` by reading its source. Installing and running Gemini's code was blocked by this session's permission policy, so its behavior below comes from reading the code. Claude's code was built and its 185 tests were run.

## Eagle-eye view

Gemini is the PR #28 version; Claude is the current code on branch claude-ts-implementation.

| Aspect | Gemini | Claude |
| --- | --- | --- |
| Verdict | Source of a few fixtures and ideas | Recommended base |
| Common changes implemented | 3 (native token, network identity, numeric address) | 4 (the same three plus pagination) |
| Source code | 6 files, 457 lines | 11 files, 643 lines (incl. pagination) |
| Test code | 6 files, 524 lines, 63 runtime cases | 15 files, 1,101 lines, 185 runtime + type tests |
| Public exports | 6 runtime + 2 type-only | 9 runtime |
| Design stance | Permissive: accepts `number`, truncates conversions, structural units | Strict: `bigint` only, exact or throw, nominal classes |
| Common-spec checks (10) | 3 meet, 6 partial, 1 not implemented | 9 meet, 1 partial |
| HIP-15 checksum | Correct; 1 of 18 official vectors tested | Correct; all 18 tested |
| Compile-time verification | None (tests not type-checked) | Type-level tests + consumer compile against `dist/` |
| Verified for this comparison | Source read only | Built, type-checked and tested |

## At a glance

| Dimension | Gemini | Claude |
| --- | --- | --- |
| Common changes covered | Native token, network identity, numeric address | The same three plus common pagination (`Page`, `MirrorNodeError`) |
| Package | One package `@hiero/sdk-v3`, exports `.` and `./base` | npm workspace; package `@hiero-ledger/sdk-base-api`, exports `.` only |
| Toolchain | TypeScript ^5.6.3, Vitest ^2.1.3 | TypeScript 7.0.2, Vitest 5.1.0 |
| `NativeTokenUnit` | `interface`; `baseUnitFactor: bigint \| number` | Abstract class; `baseUnitFactor: bigint`, validated > 0 and int64 |
| Token amounts | Accepts `bigint \| number`; no int64 range check | `bigint` only; int64 range enforced |
| Inexact `to()` | Truncates toward zero (250 tinybar → 2 of a 100-tinybar unit) | Throws `RangeError` |
| `Network` | `interface` + `createNetwork()` factory + internal `NetworkImpl` | Exported concrete class `Network` |
| `Network` unit check | Not null only | `instanceof NativeTokenUnit` |
| `Address` construction | Public constructor; accepts `number`; not final | Private constructor + `Address.of()`; `bigint` only; final at compile time |
| uint64 upper bound | Not enforced (2^64 and above accepted) | Enforced in `of()` and `fromString()` |
| `fromString()` grammar | `\d+` per part: leading zeros accepted | Canonical HIP-15 grammar: leading zeros rejected |
| `validateChecksum()` with no checksum | Returns `true` | Returns `false` |
| `validateChecksum()` parameter | `Network<any>` | `Network<NativeTokenUnit>` |
| Error mapping | `IllegalFormatError(message)` for format and some type/range cases | `IllegalFormatError(message, options)` for format only; `TypeError`/`RangeError` otherwise |
| Immutability | `Object.freeze(this)` + private fields | Private fields behind getters; no freeze |
| Tests | 6 files, 41 test blocks (63 cases); runtime only; test files not type-checked | 9 runtime + type files, 185 tests, plus a consumer compile check |
| OpenSpec changes | 3, each linking its common source and revision | 4, same links and revisions, plus recorded TypeScript decisions and open questions |

## Structure and tooling

Gemini's version is one flat package that groups types by common namespace. Claude's is a workspace with one base package, one file per type, and separate build and test configs.

| Aspect | Gemini | Claude |
| --- | --- | --- |
| Layout | `src/base/{nativeToken,ledger}.ts` + `src/base/internal/*-impl.ts` | `hiero-sdk-base-api/src/` with one file per type (`page.ts`, `native-token.ts`, `network.ts`, `address.ts`, …) |
| Internal helpers | `NetworkImpl`, `GenericNativeToken`, `calculateHip15Checksum` in `internal/`, not re-exported | `requireInt64`, `requireUint64`, `hip15Checksum` marked `@internal`; `stripInternal` removes them from the published `.d.ts` |
| Export style | `export *` from `src/index.ts`; `NativeTokenUnit` and `Network` are type-only exports | Explicit named exports; every export has a runtime value |
| `exports` conditions | `types` + `import` | `types` + `default` |
| `tsconfig` | `strict`; tests outside `rootDir`, so `tsc` never checks them | `strict` + `exactOptionalPropertyTypes`, `noUncheckedIndexedAccess`, `noImplicitOverride`, `verbatimModuleSyntax`, `isolatedDeclarations`; tests type-checked by their own config |
| Vitest | `globals: true`; runtime tests only | Runtime tests plus `typecheck` mode for `*.test-d.ts` |
| Scripts | `build`, `typecheck`, `test` | `build`, `typecheck`, `test`, `test:consumer`, `verify` (all four in order) |

Why the test type-checking gap matters: Gemini's tests write `// @ts-expect-error` above expressions like `(addr as any).num = 456n`. Once cast to `any` they cannot error, so those directives would fail type-checking as unused. Because `tsc` never sees the tests, none of their compile-time assertions are actually checked.

## Native token

Both define `NativeToken<Self, Unit>` as an abstract class with private `#amount`/`#unit`, a base-class `toBaseUnits()`, a base-class `to()` and a `protected abstract create(amount, unit): Self` hook. They differ in what a unit is and how strictly values are checked.

| Aspect | Gemini | Claude |
| --- | --- | --- |
| `NativeTokenUnit` | `interface { readonly symbol: string; readonly baseUnitFactor: bigint \| number }` | Abstract class with private fields; `protected constructor(symbol, baseUnitFactor)` |
| Unit validation | Factor not null and > 0, checked when a token is constructed and in `to()`; `symbol` never checked | Checked once, in the unit's constructor: `symbol` is a string, factor is an int64 `bigint` > 0 |
| Unit immutability | `readonly` is compile-time only; a caller-built object can be changed later | Getter-only accessors over private fields |
| Mixing units of different tokens | Allowed: any `{ symbol, baseUnitFactor }` object fits every `Unit` | Rejected at compile time when concrete units declare a private brand (documented) |
| `amount` input | `bigint \| number`; `number` must be a safe integer | `bigint` only (`TypeError` otherwise) |
| int64 range | Not checked; `2n ** 100n` is accepted | `RangeError` outside −2^63 … 2^63−1, including results of `to()` |
| Inexact `to()` | Integer division, truncating toward zero; the remainder is silently lost | `RangeError`; never rounds or truncates |
| `toBaseUnits()` overflow | Unbounded `bigint` | Unbounded `bigint` (open question recorded in the design) |
| Freezing | `Object.freeze(this)` in the base constructor | No freeze; private fields make state read-only |

Two Gemini behaviors are worth flagging:

- **Freezing in a base constructor breaks subclasses.** `NativeToken`'s constructor freezes `this` before the subclass constructor finishes. A concrete token that declares any public field (for example `readonly code = "HBAR"`) or assigns `this.x` after `super()` will throw `TypeError` on construction. Gemini's test subclass has no fields, so this goes unnoticed.
- **A token's value can change after construction.** `toBaseUnits()` reads `unit.baseUnitFactor` on every call. If the caller mutates the unit object (it is a plain object behind an interface), every token built on it changes value. Gemini's tests avoid this only by calling `Object.freeze` on their own unit fixtures.

The truncation is a deliberate Gemini decision (its design: “Integer division naturally performs floor division for positive values”), and its tests assert it (250 tinybar → 2 microbar). The common spec says `to()` returns the amount “mathematically converted”, which an integer result can only satisfy when the division is exact.

## Network identity

This is the closest match. Both copy the id bytes on the way in and on every read, accept `string | null` for the name, and reject `undefined`. The real difference is the shape of the public type.

| Aspect | Gemini | Claude |
| --- | --- | --- |
| Public type | `interface Network<Unit extends NativeTokenUnit = NativeTokenUnit>` | `class Network<Unit extends NativeTokenUnit>` |
| Creation | `createNetwork(id, name, unit)` returns an internal `NetworkImpl` | `new Network(id, name, unit)` |
| Runtime type check by consumers | Not possible: `instanceof Network` does not exist; any object with the three properties satisfies the interface | `instanceof Network`; `validateChecksum()` uses it to reject non-networks |
| `id` validation | `Uint8Array` required | `Uint8Array` required; subclasses such as `Buffer` normalised to a plain `Uint8Array` |
| `nativeTokenUnit` validation | Not null/undefined | `instanceof NativeTokenUnit` |
| Default type argument | `NativeTokenUnit` | None; the wildcard is written `Network<NativeTokenUnit>` |
| Freeze | `Object.freeze(this)` | None |

Gemini's interface-plus-factory gives it room to swap implementations later, as its design argues. The cost is that nothing ties a value to the SDK: a hand-written `{ id, name, nativeTokenUnit }` object passes as a `Network` and skips the defensive copy. Its `id` would then be a mutable array the caller still holds.

## Numeric address and checksum

Both model `BaseAddress` as an abstract class and `Address` as a subclass that narrows `num` to `bigint`, and both export `ZERO_ADDRESS` and `IllegalFormatError`. They differ in where the behavior lives, how strict construction and parsing are, and what an empty checksum means.

### Hierarchy

| Aspect | Gemini | Claude |
| --- | --- | --- |
| `num` storage | Stored in `BaseAddress` as `bigint \| null`; `Address` re-reads it with `super.num as bigint` | Abstract getter on `BaseAddress`; `Address` stores its own `#num: bigint` |
| `toString()` | Implemented in `BaseAddress`; prints `0.0.?` when `num` is null | Abstract; each subtype defines its canonical form |
| `validateChecksum()` | Implemented in `BaseAddress`; returns `true` when `num` is null | Abstract; `Address` implements HIP-15 |
| `@@finalType` | Not enforced: public constructor, `extends Address` compiles | Private constructor: `extends Address` and `new Address()` fail to compile |

Putting `toString()` and the checksum in the base is convenient now, but the later EVM-form `ContractId`/`AccountId` have no numeric form. They would have to override both, or inherit a `0.0.?` string and an always-`true` validation.

### Construction and parsing

| Input | Gemini | Claude |
| --- | --- | --- |
| `new Address(0, 0, 123)` / `Address.of(0n, 0n, 123n)` | Accepted (`number` allowed) | Constructor private; `of()` requires `bigint` |
| Negative part | `IllegalFormatError` | `RangeError` from `of()`; `IllegalFormatError` from `fromString()` |
| Part above 2^64−1 | Accepted | Rejected |
| Checksum `"ABC"` passed to the constructor / `of()` | Accepted, so `toStringWithChecksum()` yields `0.0.1-ABC`, which `fromString()` rejects | `IllegalFormatError` |
| `fromString("0.0.0123")` | Accepted, normalised to `0.0.123` | `IllegalFormatError` (HIP-15 lists it as invalid) |
| `fromString("00.0.1")` | Accepted | `IllegalFormatError` |
| `fromString(123)` | `IllegalFormatError` | `TypeError` |
| Round trip `fromString(s).toStringWithChecksum() === s` | Not guaranteed (leading zeros) | Guaranteed |

### Checksum

The two HIP-15 implementations are the same algorithm. Gemini uses `bigint` arithmetic throughout; Claude uses `number`, with every intermediate value shown to stay below 2^53. Running Claude's code on Gemini's five fixtures reproduces every expected value: `0.0.123` on ledgers `0x00`/`0x01`/`0x02` → `vfmkw`/`esxsf`/`ogizo`, `0.0.0` on `0x00` → `uvnqa`, and `0.0.255` on `0x0c1f0002` → `wypmn`.

The semantics differ in three ways:

- **Empty checksum:** Gemini returns `true` (“nothing to contradict”); Claude returns `false`, so `true` always means a checksum matched. You chose `false` in this session.
- **Parameter type:** Gemini takes `Network<any>`; Claude takes `Network<NativeTokenUnit>`. CLAUDE.md asks specs and code to avoid `ANY`.
- **Non-network argument:** Gemini rejects only `null`/`undefined`; Claude requires `instanceof Network`.

Gemini's `IllegalFormatError` takes only a message. Claude's also accepts `ErrorOptions`, so a `cause` can be kept.

## Conformance to the common specs

Both fall short on one requirement, the unbounded toBaseUnits() result. Claude meets every other requirement below; Gemini falls short on five more. “Partial” means the requirement holds for well-behaved input but a case the requirement's type or wording covers breaks it.

| Common requirement | Gemini | Claude | Why Gemini falls short |
| --- | --- | --- | --- |
| Native token: unit state immutable, `baseUnitFactor` is int64 | Partial | Meets | Unit is a caller-owned object; factor may be any `number` or unbounded `bigint` |
| Native token: amount immutable, int64 | Partial | Meets | Amount is read-only, but values beyond int64 are accepted, and the value moves if the unit object is mutated |
| Native token: `to()` returns `Self`, mathematically converted | Partial | Meets | Inexact conversions are truncated instead of converted |
| Native token: `toBaseUnits()` returns a 64-bit integer | Partial | Partial | Neither bounds the result; recorded as an open question in Claude's design |
| Network: immutable `id`, nullable `name`, `nativeTokenUnit` | Meets | Meets | — |
| Address: immutable state, uint64 parts, `num` non-null on `Address` | Partial | Meets | No upper bound on parts |
| Address: `fromString()` parses `shard.realm.num[-checksum]`, invalid input → `illegal-format` | Partial | Meets | Accepts leading zeros (HIP-15 invalid) and out-of-range parts |
| Address: `validateChecksum(network)` true when the checksum matches | Meets | Meets | — (the two differ only when there is no checksum, which the common spec does not cover) |
| `ZERO_ADDRESS` is `0.0.0` with empty checksum | Meets | Meets | — |
| Common pagination (`Page<T>`, `mirror-node-error`) | Not implemented | Meets | Out of Gemini's scope |

## Tests and verification

Claude's suite is about three times larger and checks three things Gemini's cannot: compile-time types, the published entry point, and whether its own tests fail when the code is broken.

| Check | Gemini | Claude |
| --- | --- | --- |
| Runtime contract tests | 6 files, 41 test blocks; one `it.each` over 23 invalid strings → 63 cases | 185 tests across 9 files (runtime + type-level), all passing on this machine |
| Type-level tests | None; `@ts-expect-error` lines exist but `tsc` never sees the test files | `expectTypeOf` + `@ts-expect-error`, run by Vitest typecheck |
| Consumer compilation | “PublicApiShape” tests import from `src/`, at runtime | Four consumer files compiled against the built `dist/` through the package name |
| Package boundary | Checks that exports are defined | Asserts the exact export list, no runtime/peer/optional dependencies, `files: ["dist"]` |
| HIP-15 vectors | 1 of the 18 official vectors (`0.0.123-vfmkw`) + 4 additional fixtures | All 18 official vectors + every invalid example HIP-15 lists |
| Mutation checks | Not recorded | Recorded per change: breaking the code on purpose failed 2–20 tests each time |
| Verified run for this comparison | Not run (blocked by permission policy) | `npm run verify` passed: type check, build, 185 tests, consumer compile |

Some Gemini assertions accept more than they claim. For example, the immutability tests use `expect(() => (x as any).num = 456n).toThrow()`. This passes on any throw, including the frozen-object error from `Object.freeze`, and does not check that the value is unchanged.

## OpenSpec artifacts

For the three changes both implement, they record the same common source revisions, so traceability is equal. Claude's artifacts also state, as numbered requirements, every TypeScript decision the common spec leaves open, plus the open questions. Gemini's artifacts state most of them only in prose, and some not at all.

| Change | Common source revision | Gemini artifacts | Claude artifacts |
| --- | --- | --- | --- |
| Common pagination | `1d0e9989` | — | Proposal, spec (7 requirements), design, tasks 14/14 |
| Native token | `65dded26` | Spec repeats the 4 common requirements + a mapping table; truncation stated only in the design | 4 common + 4 TypeScript requirements (int64 mapping, unit, token, exact conversion); open questions on overflow and unit branding |
| Network identity | `bd1c1601` | 2 requirements (state, defensive copying) + mapping table | 1 common + 4 TypeScript requirements (mapping, byte copying, validation, wildcard) |
| Numeric address | `f27ddd82` | Spec repeats the common requirements; empty-checksum behavior and parsing strictness not specified | 4 common + 6 TypeScript requirements (mapping, construction, error mapping, canonical parsing, formatting, HIP-15 validation) |

Two artifact details stand out in Gemini's artifacts:

- **Scenario format.** Its native token and numeric address specs keep the common **Given/When/Then** bullets, while its network spec uses **WHEN/THEN**. Claude's artifacts use WHEN/THEN throughout and pass `openspec validate --strict`.
- **Mapping mismatch.** The numeric address mapping table says `Address` narrows `num` with `declare readonly num: bigint`. The code uses an `override get num()` that casts `super.num as bigint`.

## Recommendation

Commit and merge Claude's version (it is still uncommitted on branch claude-ts-implementation), and close Gemini's PR #28 or rebase it as a small follow-up that adds only what Claude's version lacks.

Carry over from Gemini:

- [ ] The four extra checksum fixtures (testnet `0x01`, previewnet `0x02`, `0.0.0` on mainnet, the `0x0c1f0002` custom ledger). They already match Claude's implementation.
- [ ] A `./base` subpath export, if the team wants per-namespace entry points as more namespaces land.
- [ ] A default type argument `Network<Unit extends NativeTokenUnit = NativeTokenUnit>`, so the wildcard can be written `Network`.

Decide as a team before either is final:

- [ ] Empty-checksum semantics (`true` in Gemini, `false` in Claude). Raise this in the common spec so Java and TypeScript agree.
- [ ] Whether `toBaseUnits()` should reject results outside int64. Both implementations leave it unbounded.
- [ ] Interface + factory (Gemini) or exported class (Claude) for value types such as `Network`. This decision repeats for every later value type.

## Sources

- [PR #28 “type script implementation (Gemini)”](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/pull/28) by oGranny: open, head `3b4d7fe8`, 33 files, +2,963/−1, no reviews yet
- Claude's version: `sdk-ts/` on branch `claude-ts-implementation` (uncommitted working tree)
- Common changes: `openspec/changes/1-add-common-pagination-public-api`, `add-native-token-public-api`, `add-network-identity-public-api`, `add-numeric-address-public-api`
- [HIP-15: Address Checksum](https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-15.md)
- `guidelines/api-best-practices-js.md` and `guidelines/api-guideline.md` (nullability, `bytes`, narrowing, `@@finalType`)
