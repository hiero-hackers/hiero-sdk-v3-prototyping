# ADR-0010: A collection is never nullable

**Status:** Proposed
**Date:** 2026-10-15

## Context

### The rule and its current state

`guidelines/api-guideline.md` (*Never define nullable collections*) states: "The data types `list`, `set`, and `map`
should never be nullable. It is best practice to return an empty collection instead of `null`." The validator
enforces it as `collection.nullable` at severity `ERROR` (`Rule.java:230`) for fields, return types and parameters
(`MemberCheck.java:60,88,117`). `null` *inside* a collection is not expressible at all: the grammar allows no
annotation on a type argument (`MetaLang.g4:178`).

Six declarations violate the rule today, and generation stops on them. They fall into two groups:

- two **partial-update** fields — `NodeUpdateTransaction.gossipEndpoints` and `.serviceEndpoints`, where `null` is
  meant to say "leave this list unchanged";
- four **`@@oneOf` arms** — `TokenMintTransaction.metadata`, `TokenBurnTransaction.serials`,
  `TokenWipeTransaction.serials`, `NftAllowance.serials`.

The second group is not an authoring mistake: the `@@oneOf` rules in the same guideline open with "All listed fields
must be declared with `@@nullable`", so a collection arm *must* be written in a form the collection rule then
rejects. Two guideline rules contradict each other.

### Why the third state does not exist

The wire format has two states, not three. Protobuf: "Repeated fields and maps do not track presence: there is no
distinction between an *empty* and a *not-present* repeated field." HAPI says the same in prose for the exact fields
in question — `node_update.proto` documents `gossip_endpoint` with "If set, the new list SHALL replace the existing
list." and "This list MUST NOT be empty." Clearing a node's endpoint list is impossible by protocol, so *empty*,
*absent* and *unchanged* are one and the same value. Google's API style guide reaches the same convention from the
other end: an omitted update mask is "equivalent to all fields that are populated (have a non-empty value)".

### Why a nullable collection does not survive the five targets

| Language | `list<T>` | `@@nullable list<T>` |
|---|---|---|
| Java | `List<T>` | `@Nullable List<T>` — contradicts `api-best-practices-java.md:887` ("must never return `null` for collections"), *Effective Java* item 54, SEI CERT MET55-J |
| Go | `[]T` | `[]T` — **identical**; `api-best-practices-go.md:80` maps `@@nullable T` to `*T` "except for interfaces, slices and maps, whose zero value is already `nil`" |
| Rust | `Vec<T>` | `Option<Vec<T>>` |
| TypeScript | `readonly T[]` | `readonly T[] \| null` |
| Python | `list[T]` | `list[T] \| None` |

Go cannot express the distinction, and its ecosystem rule asks for exactly that: "When designing interfaces, avoid
making a distinction between a nil slice and a non-nil, zero-length slice." A spec construct that silently
evaporates in one of five targets is not a language-agnostic construct.

### The parent principle

ADR-0004 already governs this: "**No in-band sentinels.** Each distinct meaning is its own type or variant — never a
magic value or an overloaded `null`." This ADR applies that principle to collections and makes it enforceable.

## Decision

**A collection is never nullable.** No `list`, `set` or `map` — as a field, a return type or a parameter — carries
`@@nullable`. Absence *is* the empty collection.

**When absence genuinely must be distinguished from emptiness, it is modeled as a type, not as `null`** — a sum type
or a wrapper (`Unchanged | Replace(list<T>)`). We deliberately do **not** define that type now: it is introduced when
a real case needs it, in `base/common` and with its own record.

**No exceptions.** The rule stays at `ERROR`; there is no suppression annotation, no downgrade to `WARNING`, and
`--ignore-spec-errors` is a transitional tool, not a way to keep a violation. So that this is not quietly undone, the
guideline section and the `Rule` catalogue entry link back to this ADR.

**The validator is the only enforcement point.** Generators are not changed to reject such a declaration: that would
also close the override that the transition below depends on.

Rejected alternatives:

- **Keep `@@nullable` on collections and relax the rule to `WARNING` / add a suppression annotation.** Unblocks the
  six sites immediately, but the Go output still cannot carry the semantics, and the API then promises a distinction
  that one of its five bindings does not make.
- **Define `ListUpdate<$$T>` now, for the two update fields.** Speculative: the protocol has no third state for them
  today, and a generic sum type is the most expensive construct to map across Java, Go and Python.
- **An explicit `clearGossipEndpoints: bool` companion field.** A boolean trap — two fields that must be read
  together to understand one value — and the protocol has nothing to map `true` onto.
- **Resolving the `@@oneOf` contradiction in this ADR.** How mutually exclusive variants are modeled (sealed payload,
  separate transaction types, or an empty arm as "not chosen") is a decision with its own alternatives and its own
  blast radius; bundling it would break the one-decision-per-ADR rule.

## Consequences

### Positive

- The semantics of every collection in the API are the same in all five target languages, including Go.
- The two `NodeUpdateTransaction` fields end up with exactly the semantics of the wire format instead of a third
  state the protocol does not have.
- `collection.nullable` becomes a rule with a recorded reason, so a future reader finds the argument, not just the
  constraint.
- Consistent with ADR-0004, Effective Java 54 / SEI CERT MET55-J, the Go wiki and Google AIP-134.

### Negative

- **The four `@@oneOf` arms have no legal spelling until the follow-up decision.** `@@nullable list` violates this
  ADR, a plain `list` violates the `@@oneOf` rule. The specs stay red and generation needs `--ignore-spec-errors`
  until then. This is accepted knowingly; the status stays `Proposed` while it lasts.
- "Empty means unchanged" for the node endpoints is carried by a doc comment, not by the type. A caller who passes an
  empty list expecting a clear gets a silent no-op — matching the protocol, but not self-explanatory.
- The rule remains circumventable by wrapping a collection in a type and making *that* nullable — the trick HAPI
  itself uses (`AssociatedRegisteredNodeList`, `node_update.proto:205`). This stays legal; see the follow-up.
- If the protocol ever gains a way to clear a list, the affected fields change type (field → wrapper), which is a
  breaking change for callers.

### Follow-ups

- Drop `@@nullable` from `NodeUpdateTransaction.gossipEndpoints` and `.serviceEndpoints`, documenting "empty leaves
  the list unchanged" — together with this ADR.
- Link this ADR from the *Never define nullable collections* section of `guidelines/api-guideline.md` and from the
  `collection.nullable` entry in the `Rule` catalogue.
- Add a `WARNING` rule for the wrapper loophole (a `@@nullable` reference to a type whose only content is a
  collection), with the rule fixture that `RuleFixturesTest` requires.
- Decide how `@@oneOf` and collections coexist — either `@@oneOf` stops demanding `@@nullable` for collection arms,
  or collections stop being valid arms and the four sites are remodeled. Separate ADR; this one cannot be `Accepted`
  before it lands.
- **Revisit when** a protocol change allows *clearing* a list. That does not reinstate nullable collections: it is
  the case that introduces the wrapper type.

---

**References:**

- `guidelines/api-guideline.md` — *Never define nullable collections*; *Rules and recommendations for `@@oneOf`*
- `tooling/metalang/metalang-core/src/main/java/org/hiero/sdk/v3/metalang/diagnostic/Rule.java:230` — severity `ERROR`
- `tooling/metalang/metalang-core/src/main/java/org/hiero/sdk/v3/metalang/validation/MemberCheck.java:60,88,117` — checked elements
- `tooling/metalang/metalang-core/src/main/antlr4/org/hiero/sdk/v3/metalang/grammar/MetaLang.g4:178` — `typeArgument` takes no annotation
- Violating declarations: `spec/consensus-node-admin-client/transactions-nodes.md:133,134`;
  `spec/consensus-node-client/transactions-accounts.md:193`; `spec/consensus-node-client/transactions-tokens.md:268,292`;
  `spec/consensus-node-client/transactions-tokens-management.md:71`
- `tooling/protobuf/consensus-node/services/node_update.proto` — `gossip_endpoint` (field 4) comments; `AssociatedRegisteredNodeList` (line 205)
- `guidelines/api-best-practices-go.md:80`, `guidelines/api-best-practices-java.md:887` — the per-language mappings
- [`docs/adr/0004-authority-authorization-sum-type.md`](0004-authority-authorization-sum-type.md) — "No in-band sentinels"
- [`docs/nullable-collections.md`](../nullable-collections.md) — the full research note behind this decision
- [protobuf.dev — Field presence](https://protobuf.dev/programming-guides/field_presence/) (accessed 2026-10-15)
- [Google AIP-134 — Standard methods: Update](https://google.aip.dev/134) (accessed 2026-10-15)
- [Go wiki — Code Review Comments, "Declaring Empty Slices"](https://go.dev/wiki/CodeReviewComments) (accessed 2026-10-15)
- [SEI CERT MET55-J](https://wiki.sei.cmu.edu/confluence/display/java/MET55-J.+Return+an+empty+array+or+collection+instead+of+a+null+value+for+methods+that+return+an+array+or+collection) (accessed 2026-10-15)
