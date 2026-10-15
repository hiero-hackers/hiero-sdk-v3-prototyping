# ADR-0011: Model a choice between variants as a sealed type

**Status:** Proposed
**Date:** 2026-10-15

## Context

### The deadlock this resolves

[ADR-0010](0010-collections-are-never-nullable.md) forbids `@@nullable` on a collection and deliberately left one
case open: the `@@oneOf` rules of `guidelines/api-guideline.md` open with "All listed fields must be declared with
`@@nullable`", so an arm that is a `list` cannot be written at all. Both rules are enforced at `ERROR`
(`collection.nullable`, `oneof.not-nullable`), and removing the annotation from such an arm only trades one finding
for the other — measured on `TokenMintTransaction.metadata`. Four declarations sat in that gap:
`TokenMintTransaction`, `TokenBurnTransaction`, `TokenWipeTransaction` and `NftAllowance`.

The specs use `@@oneOf` / `@@oneOrNoneOf` twelve times; the other eight pair scalars (`num`/`evmAddress`,
`ipAddress`/`domainName`, `prngNumber`/`prngBytes`, `fileId`/`contractId` twice, `accountId`/`contractId`,
`executionTime`/`deletionTime`) and are not affected.

### What the choice actually is

`@@oneOf` describes a **state** with two nullable fields and a rule attached. Nothing enforces that rule in any
generated language today, so "exactly one" is documentation, and the illegal combinations — both set, neither set —
remain constructible. The protocol does not help either: `TokenMintTransactionBody` has no protobuf `oneof`, just
`uint64 amount` and `repeated bytes metadata`, and protobuf tracks no presence for a repeated field. Whatever the
SDK guarantees here is an API-side guarantee, not a wire-level one.

This is the case sum types exist for, and the meta-language already has `@@sealed`, used by `Authority`
([ADR-0004](0004-authority-authorization-sum-type.md)), whose governing principle this follows: "No in-band
sentinels. Each distinct meaning is its own type or variant."

### What was measured

The options were worked through in `docs/oneof-collections.md` and applied to a copy of the whole `spec/` tree:
after the change `metalang validate` reports **0 errors** (before: 4), the Java generator produces 595 files with
nothing deferred, the generated Maven project builds with `-Xlint:all -Werror`, and caller code compiles against it,
including an exhaustive `switch` without a `default`. The decision was stress-tested in a `/grill-me` session that
produced the criterion below and the Rust mapping question.

## Decision

**A choice between variants is a sealed type.** Two shapes, picked by one question — *does the API accept the
variant on its own?*

- **Shape 1, the variants are the types.** A `@@sealed` abstraction carries the shared members; each variant is a
  `@@finalType` with only its own fields. Used where a caller builds and submits the variant itself:
  `TokenMintTransaction`, `TokenBurnTransaction`, `TokenWipeTransaction` each become an abstraction over a fungible
  and an NFT transaction.
- **Shape 2, the choice is a field.** The surrounding type keeps its shared fields and holds a `@@sealed`
  abstraction without own members. Used where the variant never travels alone: `NftAllowance` keeps token, owner and
  spender and gains `grant: NftGrant` with `NftSerialGrant` / `NftCollectionGrant`.

**`@@oneOf` keeps its place for scalar arms** that carry no further data; the eight existing sites stay as they are.
**A `list`, `set` or `map` is never an arm of `@@oneOf`** — that case is always one of the two shapes. This closes
the deadlock without amending either rule.

**`@@sealed` gets two language mappings** where a language distinguishes them — decided by whether the abstraction
extends another generated type. Members alone are no reason: every variant declares them, so they resolve on a union
or become inherent members of an enum. A supertype is, because neither a union nor an enum can carry one:

| | extends another generated type | extends nothing |
|---|---|---|
| Java | `abstract sealed class … permits` | `sealed interface … permits` |
| TypeScript | `interface` the variants implement, plus an `Any…` union | union type |
| Rust | **sealed trait** (public trait, private supertrait) | `enum` |
| Go | interface with an unexported marker method | interface with an unexported marker method |
| Python | union alias | union alias |

Rejected alternatives:

- **Amend `@@oneOf` so a collection arm counts as absent when empty.** The smallest change, but it keeps the
  guarantee a runtime check, leaves the illegal states constructible, makes one arm use `null` and the other
  emptiness, and changes the contract of all twelve sites.
- **Mirror the wire: two always-present fields with defaults.** `amount == 0` is an in-band sentinel, which ADR-0004
  rejects; the generated convenience constructor would let a caller build a mint that mints nothing.
- **Relax `collection.nullable`.** Supersedes ADR-0010, and Go still cannot express the distinction — `list<T>` and
  `@@nullable list<T>` are both `[]T`.
- **Shape 2 everywhere** (a payload object inside every transaction). Costs the caller a nesting level and a type
  that is meaningless outside its transaction, for no gain where the variant is submitted on its own.
- **Separate types without a sealed abstraction.** Loses the shared fields, the common supertype and any generic
  access over the variants.

## Consequences

### Positive

- At the four sites the illegal states stop being representable; every field becomes required, and `amount` is a
  primitive instead of a boxed nullable.
- `collection.nullable` and the `@@oneOf` rules both stay as they are; the four declarations simply stop needing a
  nullable collection. `metalang validate spec` goes from 4 errors to 0, which unblocks ADR-0010.
- Shape 1 lets the receipt follow the variant: `TokenMintNftReceipt` carries the assigned serials, the fungible
  receipt does not — today `serials` is documented as "empty for FUNGIBLE_COMMON".
- The pattern is written down once, with domain-free examples, in `guidelines/api-guideline.md`.

### Negative

- `@@sealed` now has two mappings in TypeScript and Rust, decided by whether the abstraction extends another
  generated type. That is one more rule to know when reading generated code: the mapping of a spec type depends on
  its supertype, not on the annotation alone.
- The type count grows: three transactions become three abstractions plus six final types, and `NftAllowance` gains
  three types.
- Exhaustive handling is not available everywhere: Rust (sealed trait) and Go (interface) cannot offer it; Java,
  TypeScript and Python can.
- Callers of the current draft API rename (`TokenMintTransaction` → `TokenMintFungibleTransaction` /
  `TokenMintNftTransaction`). V3 is unreleased, so nothing outside this repository breaks.

### Follow-ups

- Apply the pattern to the four sites, including their examples and prose — done in this change.
- ~~Teach the generators shape 1~~ — done in the same change: a sealed abstraction that extends another generated
  type becomes an interface plus an `Any…` union in TypeScript and a sealed trait (public trait, private
  `crate::sealed::Sealed` supertrait) in Rust, and Python emits the union alias even for a generic abstraction.
  Java and Go already produced the right shapes.
- ~~Record the two mappings in the language guides~~ — done for Rust, TypeScript and Python; the Java and Go
  guides already describe what their generators emit.
- Describe in the SPI how two API types map to one HAPI body (`TokenMintTransactionBody`).
- Fold the working papers `docs/oneof-collections.md` and `docs/oneof-collections-in-code.md` into this ADR and the
  guideline, then delete them.
- **Revisit when** a language is added whose type system cannot express a closed set, or when `@@oneOf` turns out to
  be unnecessary for the remaining scalar sites as well.

---

**References:**

- [ADR-0010](0010-collections-are-never-nullable.md) (the deferred follow-up this answers),
  [ADR-0004](0004-authority-authorization-sum-type.md) ("No in-band sentinels")
- `guidelines/api-guideline.md` — *Model a choice as a type, not as a set of nullable fields*;
  *Rules and recommendations for `@@oneOf`*
- `tooling/metalang/metalang-core/src/main/java/org/hiero/sdk/v3/metalang/diagnostic/Rule.java` —
  `collection.nullable`, `oneof.not-nullable`
- Affected declarations: `spec/consensus-node-client/transactions-tokens.md`,
  `transactions-tokens-management.md`, `transactions-accounts.md`
- `tooling/protobuf/consensus-node/services/token_mint.proto`, `crypto_approve_allowance.proto` — no protobuf
  `oneof`; `repeated` fields have no presence
- [protobuf.dev — Field presence](https://protobuf.dev/programming-guides/field_presence/) (accessed 2026-10-15)
- Working papers with the full comparison and the compiled per-language examples:
  `docs/oneof-collections.md`, `docs/oneof-collections-in-code.md`
