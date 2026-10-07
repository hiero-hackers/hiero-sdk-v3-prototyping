# TODO

Open follow-up tasks for the V3 SDK prototyping effort. See [`CLAUDE.md`](../CLAUDE.md) for project orientation and
[`guidelines/api-guideline.md`](../guidelines/api-guideline.md) for the meta-language.

## Best-practice guides

- [ ] **Document the `$$Self` self-type mapping in every language best-practice guide.**
  The native-token abstraction uses an F-bounded self-type
  (`NativeToken<$$Self extends NativeToken<$$Self, $$Unit>, $$Unit extends NativeTokenUnit>`, see
  [`spec/base/native-token.md`](../spec/base/native-token.md)) so that `to(...)` returns the concrete type (e.g. `Hbar`)
  instead of the abstraction. Each guide must describe the idiomatic mapping:
  - Swift: `Self` (protocol) — no explicit self parameter needed
  - Rust: `-> Self` on the trait, with `$$Unit` modeled as an associated type
  - TypeScript: polymorphic `this` return type
  - Java: explicit F-bound (`<SELF extends NativeToken<SELF, U>, U extends NativeTokenUnit>`); wildcard usages become
    `NativeToken<?, ?>`
  - C++: CRTP (`template<class Self, class Unit>`)
  - Go: interface-based mapping (no generics-based self-type)
  - Python: `Self` (PEP 673) / `TypeVar`
  - Also consider adding a dedicated "self type" section to `guidelines/api-guideline.md` so the convention is defined
    once for all languages.

- [ ] **Document the implicit enum `values()` mapping in every language best-practice guide.**
  Every enum implicitly provides `@@static list<EnumName> values()` (now defined in
  [`guidelines/api-guideline.md`](../guidelines/api-guideline.md) → Enumerations) and must not declare it explicitly.
  Each guide must describe how this is realized:
  - Java: built-in `values()`
  - Swift: `CaseIterable.allCases`
  - Python: `list(MyEnum)`
  - TypeScript / JavaScript: `Object.values(MyEnum)`
  - Rust: `strum::IntoEnumIterator` or a manually maintained `const` array
  - Go: a maintained slice of the constant values
  - C++: a manually maintained array / generated table

## Meta-language

- [ ] **Decide how optional collections ("not set") are modelled — `@@nullable` collections vs. guideline rule.**
  The guideline rule "Never define nullable collections" is too broad: it is meant for data a caller *receives*
  (query results, receipts, return values), where `null` and `[]` would both mean "nothing". In optional *input*
  fields, `null` means "not set" and is distinct from any value — exactly like every other `@@nullable` field in the
  same type. Two guideline rules even contradict each other: `@@oneOf` requires all listed fields to be `@@nullable`,
  but collections must never be `@@nullable`. The validator reports these cases as `collection.nullable` (6 errors
  on `spec/`, expected until this is decided):
  - Update semantics ("`null` = leave unchanged"):
    `spec/consensus-node-admin-client/transactions-nodes.md` — `NodeUpdateTransaction.gossipEndpoints`,
    `NodeUpdateTransaction.serviceEndpoints`
  - `@@oneOf` alternatives: `spec/consensus-node-client/transactions-tokens-management.md` — `TokenWipeTransaction.serials`;
    `spec/consensus-node-client/transactions-tokens.md` — `TokenMintTransaction.metadata`,
    `TokenBurnTransaction.serials`; `spec/consensus-node-client/transactions-accounts.md` — `NftAllowance.serials`

  Options discussed so far:
  - **Rejected:** "an empty collection means not set". It hides a second meaning in `[]` that is only visible in the
    documentation.
  - **A — scope the rule and make the exceptions explicit:** collections may be `@@nullable` (a) as members of a
    `@@oneOf` / `@@oneOrNoneOf` group (`null` = alternative not chosen) and (b) in types annotated with a new
    type-level annotation `@@partialUpdate` (`null` on any `@@nullable` field = leave unchanged; today this is only a
    comment on `NodeUpdateTransaction`). All other collections stay strictly non-nullable. Requires: guideline text,
    `@@partialUpdate` in grammar / `KnownAnnotation` / validator, and annotating all update transactions in `spec/`
    (`AccountUpdate`, `TokenUpdate`, `TopicUpdate`, `FileUpdate`, `NodeUpdate`, ... — list to be confirmed).
  - **B — explicit types instead of `null`:** sealed payload types for the `@@oneOf` cases (e.g.
    `@@sealed(FungibleBurn, NftBurn) abstraction BurnPayload`) and an update wrapper type for update fields. Fully
    type-safe, but adds types per transaction and moves further away from HAPI; essentially replaces `@@oneOf`.

  Facts to keep in mind: HAPI `repeated` fields (proto3) carry no presence information, and for
  `NodeUpdateTransactionBody` the endpoint lists "MUST NOT be empty" and "If set, the new list SHALL replace the
  existing list" (`services/node_update.proto`). Whatever is chosen, a future field that must distinguish "clear"
  from "leave unchanged" needs an explicit type (e.g. `Unchanged` / `Replace` / `Clear`), not `null`.

## Spec follow-ups

These surfaced during the migration to the explicit `requires {Type} from namespace` import syntax. Because the new
syntax imports only the types that are actually used, several previously declared but unused namespace dependencies
were dropped. Confirm each is intended, or re-add a concrete import (`requires {Type} from ns`) once a type is
actually referenced.

- [ ] **`spec/consensus-node-client/transactions-accounts.md`** — the recently added `consensusnode.proto.account`
  dependency was dropped because no type from it is referenced yet. Re-add `requires {Type} from
  consensusnode.proto.account` when the body actually uses one.
- [ ] **`spec/consensus-node-client/proto.md`** and **`spec/consensus-node-client/proto-accounts.md`** — these are
  stubs; their `requires` declarations were removed entirely since they import nothing. Flesh out the proto
  placeholders and add concrete imports when the protobuf types are defined (intended source: `hedera-protobufs`).
- [ ] **`spec/base/ledger-config.md`** — the `nativeToken` dependency was dropped (unused). Note that
  `NetworkSetting.ledger` is typed as the now-generic `Ledger`, which needs a type argument
  (`Ledger<$$Unit>`) — decide whether `NetworkSetting` should itself be generic, which would re-introduce a
  `nativeToken` import.
- [ ] **`spec/mirror-node-client/mirror-node-{nft,token,transaction,common}.md`** — the unused `keys` import was
  dropped; `mirror-node-nft.md` additionally dropped the unused `mirrornode.common` import.
- [ ] **`spec/consensus-node-client/client.md`** — pre-existing issues unrelated to imports: the `createClient`
  factories reference an undefined `OperatorAccount` type (likely meant to be `Account`) and use `HieroClient<?>`
  instead of the meta-language wildcard `HieroClient<ANY>`.
