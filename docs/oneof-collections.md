# `@@oneOf` with a collection arm: the options

Working paper for the decision [ADR-0010](adr/0010-collections-are-never-nullable.md) deferred. It is not an ADR:
it lays out the four sites, what the protocol does, and each option worked through with **real generator output**,
so the decision can be made on what the SDKs would actually look like.

## 1. What has to be decided

`@@oneOf` demands that every listed field is `@@nullable`; ADR-0010 forbids `@@nullable` on a collection. Four
declarations sit in that gap and have no legal spelling:

| Spec | Type | Arms |
|---|---|---|
| `transactions-tokens.md:263` | `TokenMintTransaction` | `amount: int64` / `metadata: list<bytes>` |
| `transactions-tokens.md:287` | `TokenBurnTransaction` | `amount: int64` / `serials: list<int64>` |
| `transactions-tokens-management.md:65` | `TokenWipeTransaction` | `amount: int64` / `serials: list<int64>` |
| `transactions-accounts.md:188` | `NftAllowance` | `serials: list<int64>` / `approveAll: bool` |

The deadlock is mechanical, not editorial. Both rules are `ERROR`, and dropping `@@nullable` from an arm only
trades one finding for the other — measured on `TokenMintTransaction.metadata`:

```
consensus-node-client/transactions-tokens.md:268:45: ERROR [oneof.not-nullable] Field 'metadata' listed in @@oneOf must be @@nullable
```

The specs use `@@oneOf` / `@@oneOrNoneOf` **twelve** times; the eight others pair scalars (`fileId`/`contractId`,
`ipAddress`/`domainName`, `prngNumber`/`prngBytes`, `num`/`evmAddress`, `accountId`/`contractId`,
`executionTime`/`deletionTime`) and are unaffected by the collection question — but an option that changes the
`@@oneOf` rule itself changes the contract for all twelve.

None of the four transactions is bound in the TCK (`tooling/tck/bindings` names none of them), so no option here
has a TCK cost today.

## 2. What the protocol does

`TokenMintTransactionBody` is three plain fields — **no protobuf `oneof`**:

```protobuf
message TokenMintTransactionBody {
    TokenID token = 1;
    uint64 amount = 2;
    repeated bytes metadata = 3;
}
```

`token_burn.proto` and `token_wipe_account.proto` have the same shape. "Exactly one of" is a convention of the
consensus node, expressed on the wire by `amount == 0` and an empty list; protobuf tracks no presence for a repeated
field, so the SDK cannot send a distinction the wire does not have. Whatever is decided here is an **API-side**
guarantee — the packer maps it onto the same two fields either way.

## 3. The options

All snippets below are real: the options were written as a scratch spec and generated with the current
generators (see [§6](#6-how-these-examples-were-produced)). They are reduced to the payload shape — `tokenId: string`
instead of `Address`, no `extends Transaction<...>` — so the difference is visible without the transaction
machinery.

### O1 — One transaction type per variant

```
@@finalType
TokenMintFungible {
    @@immutable tokenId: string
    @@immutable @@min(1) amount: int64
}

@@finalType
TokenMintNft {
    @@immutable tokenId: string
    @@immutable @@minSize(1) @@maxSize(10) metadata: list<bytes>
}
```

Generated:

```java
public record TokenMintFungible(String tokenId, long amount) { … }
public record TokenMintNft(String tokenId, List<byte[]> metadata) { … }
```

```rust
pub struct TokenMintFungible { token_id: String, amount: i64 }
pub struct TokenMintNft { token_id: String, metadata: Vec<Vec<u8>> }
```

```ts
export class TokenMintFungible { readonly #tokenId: string; readonly #amount: bigint; … }
export class TokenMintNft { readonly #tokenId: string; readonly #metadata: ReadonlyArray<Uint8Array>; … }
```

- Every field is required; `amount` is a Java **primitive `long`**, not a boxed `@Nullable Long`.
- No illegal state is representable, and no runtime `@@oneOf` check is needed anywhere.
- The receipts can follow the split: `TokenMintNftReceipt` carries `serials`, the fungible one does not — today
  `TokenMintReceipt.serials` is documented as "empty for FUNGIBLE_COMMON", an empty-list-as-absent of its own.
- Cost: 3 transactions become 6 (plus `NftAllowance` → 2 types). The shared fields (`tokenId`, and in `TokenWipe`
  also `accountId`) are repeated in each type or pulled into an abstraction — and `Transaction<$$Receipt, $$Self>`
  is self-typed, so an intermediate abstraction has to be generic too.
- The SPI has to tolerate several API types mapping to one HAPI body.

### O2 — One transaction type with a sealed payload

```
@@sealed(FungibleAmount, NftMetadata) abstraction MintPayload {}

@@finalType
FungibleAmount extends MintPayload { @@immutable @@min(1) amount: int64 }

@@finalType
NftMetadata extends MintPayload { @@immutable @@minSize(1) @@maxSize(10) metadata: list<bytes> }

@@finalType
TokenMint {
    @@immutable tokenId: string
    @@immutable payload: MintPayload
}
```

Generated — all five generators already support `@@sealed` (`Authority` in `spec/base/authority.md:71` is the live
precedent):

```java
public sealed interface MintPayload permits FungibleAmount, NftMetadata {}
public record TokenMint(String tokenId, MintPayload payload) { … }
```

```rust
pub enum MintPayload {
    FungibleAmount(FungibleAmount),
    NftMetadata(NftMetadata),
}
impl From<FungibleAmount> for MintPayload { … }   // so `payload.into()` works
```

```ts
export type MintPayload = FungibleAmount | NftMetadata;
export class TokenMint { readonly #tokenId: string; readonly #payload: MintPayload; … }
```

Caller:

```java
new TokenMint("0.0.5", new NftMetadata(List.of(blob)));
```

```rust
TokenMint::new("0.0.5".into(), NftMetadata::new(vec![blob])?.into())?;
```

- Illegal states unrepresentable, exhaustive matching in Java, Rust and TypeScript, and the Rust mapping is a real
  `enum` rather than two structs.
- One transaction type per HAPI body is kept, so the SPI is unaffected.
- Cost: one extra nesting level for the caller, three new names per transaction, and the variant types are
  meaningless outside their transaction.

### O3 — The empty collection is the arm that is not chosen

```
@@oneOf(amount, metadata)
@@finalType
TokenMint {
    @@immutable tokenId: string
    @@immutable @@nullable @@min(1) amount: int64
    @@immutable @@default([]) @@maxSize(10) metadata: list<bytes>
}
```

This is **rejected by the validator today** — `oneof.not-nullable` fires on `metadata`, which is exactly the rule
this option would amend ("a collection arm counts as present when it is non-empty").

```java
public record TokenMint(String tokenId, @Nullable Long amount, List<byte[]> metadata) {
    …
    /// Creates a new `TokenMint` with the default value of `metadata`.
    public TokenMint(final String tokenId, final @Nullable Long amount) { this(tokenId, amount, List.of()); }
}
```

```rust
pub struct TokenMint { token_id: String, amount: Option<i64>, metadata: Vec<Vec<u8>> }
```

- Smallest change: one guideline rule, one validator rule, four annotations. No API restructuring.
- The generator already emits a convenience constructor that defaults `metadata` — the fungible case reads
  `new TokenMint(tokenId, 10L)`.
- But the guarantee stays a runtime check: `TokenMint(id, null, List.of())` and `TokenMint(id, 5L, List.of(blob))`
  are both constructible. `@@oneOf` has no generated enforcement today in any language, so "exactly one" is
  documentation either way.
- Asymmetric: one arm uses `null` for absence, the other emptiness. A reader has to know which.

### O4 — Wire-faithful: no `@@oneOf` at all

```
@@finalType
TokenMint {
    @@immutable tokenId: string
    @@immutable @@default(0) amount: int64
    @@immutable @@default([]) @@maxSize(10) metadata: list<bytes>
}
```

```java
public record TokenMint(String tokenId, long amount, List<byte[]> metadata) {
    …
    public TokenMint(final String tokenId) { … }   // mints nothing
}
```

- Mirrors `TokenMintTransactionBody` one to one, no nullable anywhere, no new rule.
- But `amount == 0` is an in-band sentinel, which [ADR-0004](adr/0004-authority-authorization-sum-type.md)
  explicitly rejects, and the generated convenience constructor lets a caller build a mint that mints nothing.
  The API would be weaker than v2, not stronger.

### O5 — Allow `@@nullable` on a collection after all

Keep the four declarations as they are and relax `collection.nullable` (or exempt `@@oneOf` arms from it). This
requires superseding ADR-0010, and the reason it was decided against stands: Go maps `list<T>` and
`@@nullable list<T>` to the same `[]T`, so the distinction is not expressible in one of the five targets. Listed
for completeness.

### O6 — The transaction types themselves are the variants

Instead of putting the difference into a payload object (O2), the two transaction types *are* the closed set, and
what they share sits in the abstraction above them:

```
@@sealed(TokenMintFungible, TokenMintNft)
abstraction TokenMint<$$Self extends TokenMint<$$Self>> extends Transaction<MintReceipt, $$Self> {
    @@immutable tokenId: string
}

@@finalType
TokenMintFungible extends TokenMint<TokenMintFungible> {
    @@immutable @@min(1) amount: int64
}

@@finalType
TokenMintNft extends TokenMint<TokenMintNft> {
    @@immutable @@minSize(1) @@maxSize(10) metadata: list<bytes>
}
```

The caller writes what they mean, with no wrapper object and `amount` as a primitive:

```java
new TokenMintNft("0.0.5", List.of(blob))                   // O6
new TokenMint("0.0.5", new NftMetadata(List.of(blob)))     // O2
```

#### Why `enum` is the wrong Rust mapping here

`@@sealed` does two different jobs in the specs, and only one of them is an enum:

- **A sum of values.** The abstraction has no members and exists only to say "one of these" — `Authority`
  (`spec/base/authority.md:71`), or `MintPayload` in O2. The variants share nothing but membership, a caller always
  matches to do anything, and the sum is what gets passed around. A Rust `enum` is exactly that.
- **A closed supertype.** The abstraction carries shared state (`tokenId`) and a shared contract (`Transaction`),
  and the variants are used *as themselves*; the sum is only occasionally needed for generic access. This is O6 —
  and here an enum is the wrong shape:
  - an enum cannot be a supertype, so the variants lose `impl Transaction` entirely. Measured: with the sealed
    intermediate the generator emits no `impl Transaction<MintReceipt> for TokenMintFungible`, without it, it does —
    and nothing is reported as deferred;
  - every shared access needs a wrap and an unwrap (`TokenMint::from(tx)`, then `match`), where the point was
    `tx.token_id()`;
  - the builder chain breaks: `TokenMintNft::new(…)` yields the struct, not the sum.

  The Rust idiom for a closed supertype is the **sealed trait** — a public trait with a private supertrait, so only
  the defining crate can implement it. The standard library seals this way (`SliceIndex`, `Pattern`), it keeps
  `&dyn TokenMint` and the shared accessors, and it needs no wrapping. The price is that Rust has no exhaustive
  `match` over a trait; exhaustiveness there comes only from enums.

So the mapping rule a generator would need: **a `@@sealed` abstraction without own members and without supertypes
becomes an enum; one that has members or supertypes becomes a sealed trait.** O2 and `Authority` keep the enum, O6
gets the trait.

#### What O6 looks like in each language

All five versions below were written out in full and verified — `javac -Xlint:all -Werror`, `rustc --edition 2024
--deny warnings` plus its test, `go vet` + `gofmt -l` + `go test`, `tsc --strict --exactOptionalPropertyTypes
--noUncheckedIndexedAccess --noImplicitOverride`, and `mypy --strict`. The snippets are shortened to the declarations
that carry the design.

**Java** — a sealed abstract class holds the shared state and keeps the `Transaction` contract; the `switch` needs
no `default`:

```java
public abstract sealed class TokenMint<S extends TokenMint<S>> extends Transaction<MintReceipt, S>
        permits TokenMintFungible, TokenMintNft {
    private final String tokenId;
    public String tokenId() { return tokenId; }
}

public final class TokenMintFungible extends TokenMint<TokenMintFungible> {
    private final long amount;                     // primitive, not @Nullable Long
}

public static String describe(final TokenMint<?> transaction) {
    return switch (transaction) {                  // exhaustive: a third variant breaks the build
        case TokenMintFungible fungible -> "mint " + fungible.amount();
        case TokenMintNft nft -> "mint " + nft.metadata().size() + " NFT(s)";
    };
}
```

**Rust** — a sealed trait, closed by a private supertrait:

```rust
mod sealed {
    /// Private supertrait: only this crate can name it, so only this crate can implement `TokenMint`.
    pub trait Sealed {}
}

pub trait TokenMint: Transaction<MintReceipt> + sealed::Sealed {
    fn token_id(&self) -> &str;
}

pub struct TokenMintFungible { memo: Option<String>, token_id: String, amount: i64 }

impl Transaction<MintReceipt> for TokenMintFungible { /* … */ }
impl sealed::Sealed for TokenMintFungible {}
impl TokenMint for TokenMintFungible {
    fn token_id(&self) -> &str { &self.token_id }
}

/// The generic access, without unwrapping anything.
pub fn target_token(tx: &dyn TokenMint) -> &str { tx.token_id() }
```

**Go** — an interface with an unexported method is the closed set; Go inherits no state, so each struct carries its
own fields:

```go
type TokenMint interface {
    Transaction
    TokenID() string
    isTokenMint() // unexported: no type outside this package can implement TokenMint
}

type TokenMintFungible struct {
    memo    *string
    tokenID string
    amount  int64
}

func (t TokenMintFungible) TokenID() string { return t.tokenID }
func (TokenMintFungible) isTokenMint()      {}

func TargetToken(t TokenMint) string { return t.TokenID() }
```

**TypeScript** — an abstract class for what is shared, a union alias for the closed set; a foreign subclass of the
abstract class is simply not a member of the union:

```ts
export abstract class TokenMint<S extends TokenMint<S>> extends Transaction<MintReceipt, S> {
    readonly #tokenId: string;
    get tokenId(): string { return this.#tokenId; }
}

export class TokenMintFungible extends TokenMint<TokenMintFungible> { readonly #amount: bigint; }

export type AnyTokenMint = TokenMintFungible | TokenMintNft;

export function describe(transaction: AnyTokenMint): string {
    if (transaction instanceof TokenMintFungible) { return `mint ${transaction.amount}`; }
    if (transaction instanceof TokenMintNft) { return `mint ${transaction.metadata.length} NFT(s)`; }
    return assertNever(transaction);               // exhaustive via `never`
}
```

**Python** — a `Protocol` for the shared access, a union alias for exhaustiveness (as the Python mapping already
does for `@@sealed`):

```python
class MintTransaction(Transaction, Protocol):
    @property
    def token_id(self) -> str: ...

@dataclass(frozen=True, kw_only=True, slots=True)
class TokenMintFungible:
    memo: str | None = None
    token_id: str
    amount: int

type TokenMint = TokenMintFungible | TokenMintNft

def describe(transaction: TokenMint) -> str:
    match transaction:
        case TokenMintFungible():
            return f"mint {transaction.amount}"
        case TokenMintNft():
            return f"mint {len(transaction.metadata)} NFT(s)"
        case _ as unreachable:
            assert_never(unreachable)
```

What each language can actually guarantee — measured on the versions above:

| | closed set enforced | exhaustive check | shared state inherited |
|---|---|---|---|
| Java | yes (`permits`) | yes (`switch`) | yes |
| Rust (sealed trait) | yes (private supertrait) | **no** | no (per struct) |
| TypeScript | the union is the gate | yes (`never`) | yes (abstract class) |
| Go | yes (unexported method) | **no** (type switch needs `default`) | no (per struct) |
| Python | the union alias | yes (`assert_never`) | no (per dataclass) |

- Everything O2 gives, without the payload object: no illegal state, primitives, one name per concept.
- Only option besides O1 where the receipt can follow the variant.
- Cost: **it does not work with the generators of today.** A sealed abstraction that itself extends another
  abstraction drops that supertype in Rust and TypeScript (silently — nothing is deferred), and Rust needs the
  second `@@sealed` mapping described above. Java already generates O6 correctly.

## 4. Comparison

| | O1 separate types | O2 sealed payload | O6 sealed transactions | O3 empty arm | O4 wire-faithful | O5 relax |
|---|---|---|---|---|---|---|
| Illegal state representable | no | no | no | yes (runtime check) | yes | yes |
| Nullable collection | no | no | no | no | no | **yes** |
| New names per transaction | +1 | +3 | +2 | 0 | 0 | 0 |
| Caller nesting | none | one level | none | none | none | none |
| Java `amount` | `long` | `long` | `long` | `@Nullable Long` | `long` | `@Nullable Long` |
| Rust shape | two structs | `enum` | sealed trait + two structs | `Option` + `Vec` | two plain fields | `Option` + `Option` |
| Shared fields in one place | no | yes | yes | yes | yes | yes |
| Generic access over the variants | no | via the payload | yes | n/a | n/a | n/a |
| Needs a tooling change | no | no | **yes** (supertype + Rust mapping) | `oneof.not-nullable` | no | `collection.nullable` |
| Touches the other 8 `@@oneOf` sites | no | no | no | yes (rule contract) | no | yes |
| Receipt can follow the variant | yes | no | yes | no | no | no |
| Supersedes an ADR | no | no | no | no | conflicts with 0004 | supersedes 0010 |

## 5. What to settle before the ADR

- **Is the `@@oneOf` annotation meant to be a type-level guarantee or a documented contract?** Nothing enforces it
  at runtime in any generated language today. If it stays documentation, O3 costs little; if it should become a
  guarantee, only O1 and O2 deliver it.
- **Do the four sites have to share one answer?** `NftAllowance` is a value type, not a transaction — O1 reads
  differently there (`NftSerialAllowance` / `NftApproveAllAllowance`) than for the three transactions.
- **Is a variant-specific receipt wanted** (`serials` only on the NFT mint)? That is an argument only O1 can cash.
- **How does the SPI describe several API types mapping to one HAPI body** — does `TransactionSupport` need to say
  anything, or is that purely the packer's business?
- **Does `Transaction<$$Receipt, $$Self>` tolerate an intermediate abstraction** for the shared fields of O1, given
  the self type and ADR-0006 (generic methods only `@@finalMethod` or `@@static`)? For O6 the answer is measured:
  Java generates it correctly today, Rust and TypeScript drop the grandparent.
- **Does `@@sealed` need two mappings** — enum for a pure sum, sealed trait / interface for a closed supertype? That
  is a meta-language decision beyond these four sites, and O6 depends on it.
- **Is exhaustiveness part of the promise?** Rust (sealed trait) and Go (interface) cannot give it; Java,
  TypeScript and Python can. If the specs promise exhaustive handling, two of five languages cannot keep it.

## 6. How these examples were produced

A scratch spec with one namespace per option, generated with the current CLI:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=java|ts|rust \
    --fail-on=never --config=sdk-<lang>/generator.properties --output=<throwaway> <scratch spec>
```

O1, O2, O4 and O6 validate clean; O3 reports `oneof.not-nullable`, which is the point of that option. The snippets
are copied from the generated files, shortened to the declaration lines.

The per-language versions of O6 are **not** generator output — the generators cannot produce them yet. They were
written by hand as the target shape and checked with the real toolchains: `javac -Xlint:all -Werror` (JDK 25),
`rustc --edition 2024 --deny warnings` plus `--test`, `go vet` / `gofmt -l` / `go test` (Go 1.27),
`tsc --strict --exactOptionalPropertyTypes --noUncheckedIndexedAccess --noImplicitOverride`, and `mypy --strict`
(Python 3.14). All five compile clean, and the Java, Go, Rust and Python ones run.

---

**References:** [`docs/nullable-collections.md`](nullable-collections.md) (the research behind ADR-0010),
[ADR-0010](adr/0010-collections-are-never-nullable.md), [ADR-0004](adr/0004-authority-authorization-sum-type.md),
[ADR-0006](adr/0006-generic-methods-final-or-static.md),
`spec/consensus-node-client/transactions-tokens.md`, `transactions-tokens-management.md`,
`transactions-accounts.md`, `spec/base/authority.md:71`,
`tooling/protobuf/consensus-node/services/token_mint.proto`, `token_burn.proto`, `token_wipe_account.proto`,
`guidelines/api-guideline.md` (*Rules and recommendations for `@@oneOf`*)
