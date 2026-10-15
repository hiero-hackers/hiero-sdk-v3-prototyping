# The four `@@oneOf` sites, in code

Companion to [`oneof-collections.md`](oneof-collections.md), which compares the options in the abstract. This one
applies the chosen shapes to the **real four declarations** and shows what comes out: the spec before and after, the
generated API, and the caller code — all of it generated or compiled, not sketched.

## The criterion

Two shapes, and one rule that decides between them:

> **Does the API accept the variant type on its own?** If yes, the variants *are* the sum — the type above them is a
> sealed abstraction (**O6**). If a variant only ever exists as an element of the sum, the sum is a payload inside
> one type (**O2**).

A `TokenMintNftTransaction` is built, signed and submitted as itself — O6. An NFT grant never appears alone; it is
always a field of an allowance that also carries token, owner and spender — O2.

## 1. `TokenMintTransaction` → O6, with a receipt per variant

**Before** — one type, two nullable arms, a receipt whose `serials` is empty for half of all callers:

```
@@oneOf(amount, metadata)
@@finalType
TokenMintTransaction extends Transaction<TokenMintReceipt, TokenMintTransaction> {
    @@immutable tokenId: Address
    @@immutable @@nullable @@min(1) amount: int64
    @@immutable @@nullable @@maxSize(10) metadata: list<bytes>
}

@@finalType
TokenMintReceipt extends Receipt {
    @@immutable totalSupply: int64
    @@immutable @@default([]) serials: list<int64>   // empty for FUNGIBLE_COMMON
}
```

**After**:

```
@@sealed(TokenMintFungibleTransaction, TokenMintNftTransaction)
abstraction TokenMintTransaction<$$Receipt extends TokenMintReceipt, $$Self extends TokenMintTransaction<$$Receipt, $$Self>> extends Transaction<$$Receipt, $$Self> {
    @@immutable tokenId: Address
}

@@finalType
TokenMintFungibleTransaction extends TokenMintTransaction<TokenMintReceipt, TokenMintFungibleTransaction> {
    @@immutable @@min(1) amount: int64
}

@@finalType
TokenMintNftTransaction extends TokenMintTransaction<TokenMintNftReceipt, TokenMintNftTransaction> {
    @@immutable @@minSize(1) @@maxSize(10) metadata: list<bytes>
}

TokenMintReceipt extends Receipt {
    @@immutable totalSupply: int64
}

@@finalType
TokenMintNftReceipt extends TokenMintReceipt {
    @@immutable @@default([]) serials: list<int64>
}
```

**Generated Java** (from the real specs, unmodified output):

```java
public abstract sealed class TokenMintTransaction<ReceiptT extends TokenMintReceipt, Self extends TokenMintTransaction<ReceiptT, Self>>
        extends Transaction<ReceiptT, Self> permits TokenMintFungibleTransaction, TokenMintNftTransaction { … }

public final class TokenMintNftTransaction extends TokenMintTransaction<TokenMintNftReceipt, TokenMintNftTransaction> {
    private final List<byte[]> metadata;
    public TokenMintNftTransaction(final Address tokenId, final List<byte[]> metadata) { … }

    @Override
    public PackedTransaction<TokenMintNftReceipt, TokenMintNftTransaction> pack(final Account payer, final List<AccountId> nodes) { … }
}

public final class TokenMintNftReceipt extends TokenMintReceipt { … }   // adds serials
```

The receipt type travels through the self type into the whole machinery: `pack`, `sign` and
`signWithOperatorAndSubmit` of the NFT mint are typed on `TokenMintNftReceipt`, so `serials` exists exactly where it
is filled — and nowhere else.

**Caller**:

```java
// before
Response<TokenMintReceipt> r = new TokenMintTransaction().tokenId(tokenId).amount(500_000_000L)…
Response<TokenMintReceipt> r = new TokenMintTransaction().tokenId(nftId).metadata([…])…
r.queryReceipt().serials   // always there, empty in the first case

// after
Response<TokenMintReceipt>    r = new TokenMintFungibleTransaction().tokenId(tokenId).amount(500_000_000L)…
Response<TokenMintNftReceipt> r = new TokenMintNftTransaction().tokenId(nftId).metadata([…])…
r.queryReceipt().serials   // only on the NFT mint
```

And the generic access that the sealed abstraction is there for — compiled against the generated classes:

```java
static Address target(final TokenMintTransaction<?, ?> mint) {
    return mint.tokenId();
}
```

## 2. `TokenBurnTransaction` and `TokenWipeTransaction` → O6, one receipt

Structurally the same, but both variants share one receipt, so the abstraction needs only the self type:

```
@@sealed(TokenBurnFungibleTransaction, TokenBurnNftTransaction)
abstraction TokenBurnTransaction<$$Self extends TokenBurnTransaction<$$Self>> extends Transaction<TokenBurnReceipt, $$Self> {
    @@immutable tokenId: Address
}

@@finalType
TokenBurnFungibleTransaction extends TokenBurnTransaction<TokenBurnFungibleTransaction> {
    @@immutable @@min(1) amount: int64
}

@@finalType
TokenBurnNftTransaction extends TokenBurnTransaction<TokenBurnNftTransaction> {
    @@immutable @@minSize(1) @@maxSize(10) serials: list<int64>
}
```

`TokenWipeTransaction` is identical except that the abstraction carries two shared fields (`tokenId`, `accountId`).
Generated:

```java
public abstract sealed class TokenBurnTransaction<Self extends TokenBurnTransaction<Self>>
        extends Transaction<TokenBurnReceipt, Self> permits TokenBurnFungibleTransaction, TokenBurnNftTransaction { … }
public abstract sealed class TokenWipeTransaction<Self extends TokenWipeTransaction<Self>>
        extends Transaction<TokenWipeReceipt, Self> permits TokenWipeFungibleTransaction, TokenWipeNftTransaction { … }
```

## 3. `NftAllowance` → O2, a sealed grant

The variant never travels alone: it is always an element of `list<NftAllowance>`, next to token, owner and spender.
So the sum is a field.

**Before** — three nullable fields, two of which are the actual choice:

```
@@oneOf(serials, approveAll)
type NftAllowance {
    @@immutable tokenId: Address
    @@immutable ownerAccountId: AccountId
    @@immutable spenderAccountId: AccountId
    @@immutable @@nullable serials: list<int64>
    @@immutable @@nullable approveAll: bool
    @@immutable @@nullable delegatingSpender: AccountId
}
```

**After**:

```
@@sealed(NftSerialGrant, NftCollectionGrant) abstraction NftGrant {}

@@finalType
NftSerialGrant extends NftGrant {
    @@immutable @@minSize(1) serials: list<int64>
}

@@finalType
NftCollectionGrant extends NftGrant {
    @@immutable approved: bool        // false revokes an existing collection-wide grant
}

NftAllowance {
    @@immutable tokenId: Address
    @@immutable ownerAccountId: AccountId
    @@immutable spenderAccountId: AccountId
    @@immutable grant: NftGrant
    @@immutable @@nullable delegatingSpender: AccountId
}
```

**Generated** — this shape maps correctly in every language today:

```java
public sealed interface NftGrant permits NftSerialGrant, NftCollectionGrant {}
public record NftSerialGrant(List<Long> serials) implements NftGrant { … }
public record NftCollectionGrant(boolean approved) implements NftGrant { … }
```

```ts
export type NftGrant = NftSerialGrant | NftCollectionGrant;
```

```rust
pub enum NftGrant { NftSerialGrant(NftSerialGrant), NftCollectionGrant(NftCollectionGrant) }
```

**Caller** — the `null, null` tail disappears, and the choice is named:

```java
// before
new NftAllowance(nftCollectionId,   owner, spender, [1L, 2L, 3L], null, null)
new NftAllowance(otherCollectionId, owner, spender, null,         true, null)

// after
new NftAllowance(nftCollectionId,   owner, spender, new NftSerialGrant([1L, 2L, 3L]), null)
new NftAllowance(otherCollectionId, owner, spender, new NftCollectionGrant(true),     null)
```

Handling a grant is exhaustive without a default branch — compiled against the generated classes:

```java
static String describe(final NftGrant grant) {
    return switch (grant) {
        case NftSerialGrant serial -> serial.serials().size() + " serial(s)";
        case NftCollectionGrant collection -> collection.approved() ? "the whole collection" : "nothing (revoked)";
    };
}
```

## 4. What the generators do with this today

| Shape | Java | TypeScript | Rust |
|---|---|---|---|
| **O2** — sealed abstraction without members (`NftGrant`) | `sealed interface` + records | union type | `enum` + `From` impls |
| **O6** — sealed abstraction with members and a supertype (`TokenMintTransaction`) | `abstract sealed class … permits`, supertype kept | `export class TokenMintNftTransaction {` — **supertype dropped** | `pub enum TokenMintTransaction` and `pub struct TokenMintNftTransaction` with **no `impl Transaction`** |

So O2 is ready; O6 needs two generator changes, both of them described in
[`oneof-collections.md`](oneof-collections.md#o6--the-transaction-types-themselves-are-the-variants):

1. a variant must keep the supertypes of the sealed abstraction (today they are dropped silently — nothing is
   reported as deferred);
2. Rust needs the second `@@sealed` mapping: a sealed abstraction **with** members or supertypes is a *sealed trait*
   (public trait with a private supertrait), not an `enum`.

## 5. What was verified

The four changes were applied to a copy of the whole `spec/` tree and run through the real tooling:

| Check | Result |
|---|---|
| `metalang validate spec` | **0 errors**, 36 warnings (before: 4 errors) |
| `metalang generate --language=java` | 595 files, nothing deferred |
| Maven build of the generated project (`-Xlint:all -Werror`, doclint) | **BUILD SUCCESS**, 5 module JARs |
| Caller code above, compiled against those JARs | compiles, including the exhaustive `switch` and `TokenMintTransaction<?, ?>` |
| `metalang generate --language=ts\|rust` | succeeds, with the O6 gap of §4 |

Note what §5 means for the deadlock: the four sites become legal **without touching `@@oneOf` or
`collection.nullable`**. Neither rule has to be amended — the declarations simply stop needing a nullable collection.

## 6. Still open

- The examples and the prose of the three spec files still describe the old shapes; they were not rewritten for this
  experiment.
- `TokenMintReceipt` loses its `@@finalType` so that `TokenMintNftReceipt` can extend it. Whether the receipt should
  follow the variant at all is a decision of its own — §2 shows the alternative (one receipt, one type parameter).
- The SPI has to describe two API types mapping to one HAPI body (`TokenMintTransactionBody`).
- Names are a proposal, not a decision: `TokenMintFungibleTransaction` / `TokenMintNftTransaction`,
  `NftSerialGrant` / `NftCollectionGrant`.
- `NftCollectionGrant.approved: bool` keeps a boolean in the API; HAPI's `approved_for_all` is a `BoolValue`, so
  `false` is a meaningful revocation. Whether that deserves two variants instead of a flag is open.

---

**References:** [`oneof-collections.md`](oneof-collections.md), [ADR-0010](adr/0010-collections-are-never-nullable.md),
`spec/consensus-node-client/transactions-tokens.md`, `transactions-tokens-management.md`, `transactions-accounts.md`,
`tooling/protobuf/consensus-node/services/token_mint.proto`, `crypto_approve_allowance.proto`
