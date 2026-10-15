# Nullable collections: the problem and the options

Research note on the six `collection.nullable` findings in the V3 specs. It collects what the protocol does, what
each target language can express, what the industry does, and which options exist — so that the decision can be made
once and recorded as an ADR.

## 1. The finding

`guidelines/api-guideline.md`, *Never define nullable collections*: "The data types `list`, `set`, and `map` should
never be nullable. It is best practice to return an empty collection instead of `null`." The validator enforces it as
`collection.nullable` at severity `ERROR`, which means nothing is generated while a spec violates it.

Six declarations violate it today:

| Spec | Field | Shape |
|---|---|---|
| `consensus-node-admin-client/transactions-nodes.md:133` | `NodeUpdateTransaction.gossipEndpoints` | `@@nullable list<ServiceEndpoint>` |
| `consensus-node-admin-client/transactions-nodes.md:134` | `NodeUpdateTransaction.serviceEndpoints` | `@@nullable list<ServiceEndpoint>` |
| `consensus-node-client/transactions-accounts.md:193` | `NftAllowance.serials` | `@@oneOf(serials, approveAll)` |
| `consensus-node-client/transactions-tokens.md:268` | `TokenMintTransaction.metadata` | `@@oneOf(amount, metadata)` |
| `consensus-node-client/transactions-tokens.md:292` | `TokenBurnTransaction.serials` | `@@oneOf(amount, serials)` |
| `consensus-node-client/transactions-tokens-management.md:71` | `TokenWipeTransaction.serials` | `@@oneOf(amount, serials)` |

## 2. One rule, two unrelated problems

**(A) "Leave unchanged" in a partial update** — two sites. `NodeUpdateTransaction` updates only the fields that are
set; `null` means *do not touch this list*.

**(B) A variant that is not the active one** — four sites. All four carry `@@oneOf`: a mint is either fungible
(`amount`) or non-fungible (`metadata`), never both. `null` here means *this arm is not in use*.

The two need different answers, and (B) is not a spec mistake at all: **the meta-language mandates the violation.**
The rules for `@@oneOf` in the guideline begin with "All listed fields must be declared with `@@nullable`". A
`@@oneOf` arm that is a collection therefore *has* to be written as `@@nullable list<…>`, which the
`collection.nullable` rule then rejects. Two guideline rules contradict each other, and four of the six findings are
that contradiction.

## 3. What the protocol actually does

Primary evidence from the vendored definitions in `tooling/protobuf`:

- **protobuf has no presence for lists.** "Repeated fields and maps do not track presence: there is no distinction
  between an *empty* and a *not-present* repeated field."
  ([protobuf.dev](https://protobuf.dev/programming-guides/field_presence/)) Whatever the SDK models, the wire format
  carries two states, not three.
- **`node_update.proto` says so explicitly.** `gossip_endpoint` is a plain `repeated proto.ServiceEndpoint` with the
  comment "If set, the new list SHALL replace the existing list." and, two lines above, "This list MUST NOT be
  empty." The scalar fields of the same message use wrapper types (`StringValue`, `BytesValue`, `BoolValue`) to get
  presence; the two list fields deliberately do not.
  → For (A), *empty* and *unchanged* are the same thing on the wire, and clearing the list is impossible by
  protocol. The third state the spec models does not exist.
- **HAPI's own escape hatch, when it does want presence for a list,** is a wrapper message:
  `AssociatedRegisteredNodeList` (field 11 of the same `NodeUpdateTransactionBody`) exists only to wrap
  `repeated uint64` so that the field can be absent.
- **The token bodies have no protobuf `oneof`.** `TokenMintTransactionBody` is `TokenID token`, `uint64 amount`,
  `repeated bytes metadata`; `token_burn.proto` and `token_wipe_account.proto` are the same shape. "Exactly one of"
  is a protocol convention expressed by `amount == 0` / empty list — not by the wire format.

## 4. What a nullable list costs per language

| Language | `list<T>` | `@@nullable list<T>` | Consequence |
|---|---|---|---|
| Java | `List<T>` | `@Nullable List<T>` | Contradicts `api-best-practices-java.md`: "The public API must never return `null` for collections. Instead, an empty collection must be returned." Same as *Effective Java* item 54 and SEI CERT **MET55-J**. |
| TypeScript | `readonly T[]` | `readonly T[] \| null` | Works, but adds a second absent value next to `undefined`; the TS guide already has to pick one of the two. |
| Rust | `Vec<T>` | `Option<Vec<T>>` | Legitimate *when the distinction is real* — and free (`Vec` is non-null, so `Option<Vec<T>>` has the same size). The community rule is exactly that: use `Option` only if "empty" and "absent" differ semantically. |
| Go | `[]T` | `[]T` | **The distinction is lost.** `api-best-practices-go.md` maps `@@nullable T` to `*T` "except for interfaces, slices and maps, whose zero value is already `nil`". The Go wiki asks for precisely that: "When designing interfaces, avoid making a distinction between a nil slice and a non-nil, zero-length slice, as this can lead to subtle programming errors." |
| Python | `list[T]` | `list[T] \| None` | Works; `None` is a third state the caller must handle. |

So the one language that silently drops the semantics is Go — and it does so because its ecosystem rule says the
distinction should not be made in the first place. Any solution that relies on "null list ≠ empty list" is
**not portable across the five targets**, which is the whole point of a language-agnostic spec.

## 5. What the industry does

- **Google AIP-134 (standard `Update`)**: "the service **must** treat an omitted field mask as an implied field mask
  equivalent to all fields that are populated (have a non-empty value)". In the largest public API style guide,
  *empty list = leave unchanged* is the default semantic — no nullable list needed. Full replacement is opted into
  with the mask value `*`.
- **Google AIP-144 (repeated fields)**: a standard update "has one key limitation: the user is only able to update
  *the entire* list"; when per-entry control is needed, the answer is **custom `Add`/`Remove` methods**, not a
  tri-state list.
- **RFC 7396 (JSON Merge Patch)** takes the opposite route for documents: absent = unchanged, `null` = delete. It
  needs three states because a JSON document has no schema-level way to say "clear" — and it pays for it with the
  well-known inability to patch *into* a null value.
- **Tri-state wrappers are an established idiom** where the distinction is unavoidable: `Patch<T>` =
  `Unchanged | Set(T) | Clear` (e.g. the Rust crate `value-extra`), Pydantic's `MISSING`, `dataclasses.MISSING`,
  PEP 661 sentinels (accepted, Python 3.15), Speakeasy's `OptionalNullable<T>`. The pattern is to make the third
  state a **type**, never a `null`.
- **Mutually exclusive fields** are the textbook case for sum types: "make illegal states unrepresentable"
  (Yaron Minsky). Java's data-oriented-programming guidance says it directly: "In either/or situations, avoid
  multiple fields with mutually exclusive or conditional requirements and instead create a sealed interface to model
  the alternatives." TypeScript discriminated unions and Rust enums are the same tool. The meta-language already has
  `@@sealed(A, B)` for exactly this.

## 6. Options

### For (A) — the two `NodeUpdateTransaction` lists

**A1 — Drop `@@nullable`; empty means "unchanged".** `@@immutable @@default([]) gossipEndpoints: list<ServiceEndpoint>`.
*Pro:* identical to the wire, identical to AIP-134, nothing is lost (the protocol forbids an empty list anyway),
portable to all five languages, rule stays `ERROR`. *Contra:* the semantics live in a doc comment, not in the type;
if HAPI ever allows clearing the list, the spec needs a real third state.

**A2 — A tri-state wrapper type in `base/common`,** e.g. `@@sealed(Unchanged, Replace) abstraction ListUpdate<$$T>`
with `Replace { values: list<$$T> }` (and a `Clear` variant if the protocol ever supports it).
*Pro:* the three states are explicit and exhaustively matchable in every language; the established industry idiom;
survives a protocol change without a breaking field type change. *Contra:* new vocabulary in every update
transaction, more ceremony for a distinction HAPI does not currently have, and a generic sum type is the most
expensive construct to map across Java/Go/Python.

**A3 — An explicit `clearGossipEndpoints: bool` attribute.** *Rejected:* a boolean trap (two fields that must be
read together to understand one value), it cannot be enforced by the type system, and the protocol has nothing to
map `true` onto.

### For (B) — the four `@@oneOf` arms

**B1 — A sealed payload type.** `TokenMintTransaction` keeps one type and carries
`@@sealed(FungibleMint, NftMint) abstraction MintPayload`, with `FungibleMint { amount: int64 }` and
`NftMint { metadata: list<bytes> }`. *Pro:* illegal states unrepresentable, no nullable anywhere, exhaustive
matching, one transaction type per HAPI body. *Contra:* one extra nesting level for the caller
(`.payload(new NftMint(...))`); the builder ergonomics of the v2 SDK are gone.

**B2 — Separate transaction types per variant** — the idea raised in the request.
`TokenMintFungibleTransaction { tokenId, amount }` and `TokenMintNftTransaction { tokenId, metadata }`, both final,
both packing into the same `TokenMintTransactionBody`. Same for burn and wipe; `NftAllowance` splits into
`NftSerialAllowance` and `NftApproveAllAllowance`. *Pro:* every field is required and non-nullable, the `@@oneOf`
runtime check disappears, each type is discoverable by name and by IDE completion, and the receipts can differ per
variant. It is also the straightest answer to what v2 got wrong: v2 has one `TokenMintTransaction` with both
`setAmount()` and `setMetadata()`, and the Hedera docs have to warn "when minting NFTs, you should not set the
amount". *Contra:* the type count grows (3 transactions → 6), the common fields are repeated or need a shared
abstraction, and the SPI must still describe them as one HAPI transaction type.

**B3 — Amend the `@@oneOf` rule instead of the specs:** allow a collection arm to be non-nullable, with *empty* as
its absent value (`@@oneOf` then reads "exactly one of: `amount != null`, `metadata` non-empty"). The existing
`@@min(1)` / `@@minSize(1)` constraints already make an empty arm invalid. *Pro:* the smallest change — one
guideline rule, four annotations, no API change at all, and it resolves the rule contradiction at its root.
*Contra:* keeps a validation-based (not type-based) guarantee, so illegal states remain representable.

**B4 — Relax `collection.nullable` to `WARNING`, or add a suppression annotation** (`@@nullableCollection("reason")`).
*Pro:* unblocks everything immediately. *Contra:* it does not answer the question, and the Go generator still cannot
express what the spec then claims.

## 7. Recommendation

The general rule, which both halves are instances of:

> A collection is never nullable. Absence is the empty collection. When absence must be distinguished from empty,
> that is a **state**, not a value — model it with a type (sum type or wrapper), never with `null`.

Concretely:

1. **(A) → A1.** The protocol has two states, so the API should have two. Record the semantics in the field comment
   and keep A2 in the drawer for the day HAPI gains a way to clear a list — introducing `ListUpdate<$$T>` later is a
   breaking change of two fields, not of the whole model.
2. **(B) → B2, with B1 as the runner-up.** This is the case the V3 generation exists to fix, and it is the only
   option that removes the `@@oneOf` runtime check entirely. If the extra transaction types are judged too costly,
   B1 gives most of the benefit at one nesting level; B3 is the minimal change that at least makes the guideline
   self-consistent.
3. **Either way, fix the rule contradiction**: `@@oneOf` must stop requiring `@@nullable` for collection arms, or the
   two rules keep producing errors that no spec author can avoid.

A decision here is architectural and touches four spec files plus the guideline — it belongs in an ADR (the
`adr-create` skill), with this note as its evidence base.

## 8. Open questions

- Does any Hiero network allow *clearing* a node's endpoint list by other means? If yes, (A) needs a third state
  after all and A2 becomes the answer.
- Should `TokenMintReceipt` / `TokenBurnReceipt` differ per variant under B2 (the NFT mint returns serials, the
  fungible mint does not — today `serials` is an empty list for fungible mints)?
- Does the SPI (`consensusnode.transactions.spi`) need to express "several API transaction types map to one HAPI
  body", and does that change `TransactionSupport`?

## Sources

- [protobuf.dev — Field presence](https://protobuf.dev/programming-guides/field_presence/)
- [Google AIP-134 — Standard methods: Update](https://google.aip.dev/134)
- [Google AIP-144 — Repeated fields](https://google.aip.dev/144)
- [RFC 7396 — JSON Merge Patch](https://datatracker.ietf.org/doc/html/rfc7396)
- [RFC 6902 — JSON Patch](https://datatracker.ietf.org/doc/html/rfc6902)
- [Go wiki — Code Review Comments, "Declaring Empty Slices"](https://go.dev/wiki/CodeReviewComments)
- [SEI CERT MET55-J — Return an empty array or collection instead of a null value](https://wiki.sei.cmu.edu/confluence/display/java/MET55-J.+Return+an+empty+array+or+collection+instead+of+a+null+value+for+methods+that+return+an+array+or+collection)
- [Effective Java item 54 — Return empty collections or arrays, not nulls](https://github.com/AlphaWang/alpha-effective-java-3e/blob/master/8_methods/item_54_return_empty_collections_or_arrays,_not_nulls.md)
- [Effective Java item 55 — Return optionals judiciously (do not wrap containers)](https://github.com/clxering/Effective-Java-3rd-edition-Chinese-English-bilingual/blob/dev/Chapter-8/Chapter-8-Item-55-Return-optionals-judiciously.md)
- [inside.java — Data-Oriented Programming: Make illegal states unrepresentable](https://inside.java/2024/06/03/dop-v1-1-illegal-states/)
- [functional-architecture.org — Make illegal states unrepresentable](https://functional-architecture.org/make_illegal_states_unrepresentable/)
- [Rust users forum — `Option<Vec<T>>` vs `Vec<T>` (semantics and size)](https://users.rust-lang.org/t/space-time-usage-to-construct-vec-t-vs-option-vec-t/35596)
- [`value-extra` crate — `Patch<T>`: `Some` / `None` / `Empty`](https://docs.rs/value-extra/latest/value_extra/)
- [PEP 661 — Sentinel values](https://peps.python.org/pep-0661/)
- [TypeScript — `exactOptionalPropertyTypes`](https://www.typescriptlang.org/play/4-4/new-ts-features/exact-optional-properties.ts.html)
- [tkdodo.eu — optional vs. undefined](https://tkdodo.eu/blog/optional-vs-undefined)
- [Hedera docs — Mint a token (`setAmount` / `setMetadata` on one v2 class)](https://docs.hedera.com/hedera/sdks-and-apis/sdks/token-service/mint-a-token)
- In-repo evidence: `tooling/protobuf/consensus-node/services/node_update.proto`,
  `token_mint.proto`, `token_burn.proto`, `token_wipe_account.proto`;
  `guidelines/api-guideline.md` (`@@oneOf` rules, *Never define nullable collections*);
  `guidelines/api-best-practices-{java,go,rust,ts,python}.md`
