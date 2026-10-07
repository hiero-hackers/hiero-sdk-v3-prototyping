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

## Build and tooling

- [ ] **Get the protobuf-generated Java classes out of the `com.hedera*` packages — by contributing managed options
  to `protobuf-maven-plugin` upstream.**

  **Why it matters.** Java is the only target language where this is a real problem. Rust puts the messages in
  `hiero_consensus_node_client::proto` (the crate name is the namespace, `mod proto` is not `pub`), TypeScript in a
  non-exported subpath of `@hiero/consensus-node-client`, and Go will land them under
  `github.com/hiero-ledger/hiero-sdk-go/.../internal/proto`. Only Java package names are global to the class path.

  And the collision is not hypothetical:
  [`tooling/protobuf/consensus-node/sdk/transaction_list.proto`](../tooling/protobuf/consensus-node/sdk/transaction_list.proto)
  declares `option java_package = "com.hedera.hashgraph.sdk.proto"` — literally the package of **hiero-sdk-java v2** —
  and the bulk of the tree declares `com.hederahashgraph.api.proto.java`, which v2 also ships. An application that
  depends on both v2 and v3 gets duplicate classes on the class path, and on the module path a **split package**,
  which is a hard JPMS resolution failure rather than a warning.

  **Until it lands we keep the current packages.** They are wrong only for the coexistence case above, and the
  module exports them with a qualified `exports … to …`, so nothing outside the SDK can reach them anyway.

  **Why upstream rather than locally.** `io.github.ascopes:protobuf-maven-plugin` has no equivalent of buf's managed
  mode — all 49 of its parameters, across its three mojos, are about languages, sources, imports, plugins and
  descriptors; none rewrites a file option. But the plugin already owns every building block, so the feature is small:

  | Building block | State in the plugin (3.6.0) |
  |---|---|
  | emit a descriptor set | `outputDescriptorFile`, `outputDescriptorIncludeImports`, `outputDescriptorRetainOptions` |
  | consume a descriptor set | `sourceDescriptorPaths`, `ProtocInvocation.getInputDescriptorFiles()` |
  | parse and mutate one | `protobuf-java` is already a `compile` dependency |
  | tell "generate" from "import" apart | the `sources/FilesToCompile` class |

  `ProtocExecutor` passes `--descriptor_set_in` / `--descriptor_set_out` / `--include_imports` / `--retain_options`
  straight through to protoc; no class in the plugin touches `DescriptorProtos` yet.

  **The mechanism, verified with protoc alone** (buf can do managed mode because it builds the
  `CodeGeneratorRequest` itself; a protoc-invoking plugin cannot — but it can round-trip a descriptor set):

  ```
  protoc --descriptor_set_out=set.binpb --include_imports …   # 1. compile the sources
  <rewrite java_package in the FileDescriptorSet>             # 2. mutate
  protoc --descriptor_set_in=set.binpb --java_out=… …         # 3. generate
  → org/hiero/sdk/protobuf/com/hederahashgraph/api/proto/java/TokenBalances.java
  ```

  **The one correctness trap, found by getting it wrong first.** Rewriting *every* `java_package` also rewrites the
  well-known types, and the generated code then references `org.hiero.sdk.protobuf.com.google.protobuf.UInt32Value`
  — a class that does not exist at runtime, because the well-known types come from `protobuf-java` under their real
  package. The rule is: **rewrite only the files being generated, never the imported ones.** That is exactly the
  distinction `FilesToCompile` already models.

  **Sketch of the contribution:**
  1. a configuration bean, e.g. `<managedOptions><javaPackagePrefix>…</javaPackagePrefix></managedOptions>`;
  2. a branch in `ProtobufBuildOrchestrator.createProtocInvocation`: with managed options set, one protoc call
     becomes the three-step chain above;
  3. a transform of ~50 lines — `FileDescriptorSet.parseFrom`, set `options.javaPackage` for each file in
     `FilesToCompile`, write it back;
  4. an invoker IT (`maven-invoker-plugin` is already a dependency).

  **Two design questions for the maintainer** ([ascopes/protobuf-maven-plugin](https://github.com/ascopes/protobuf-maven-plugin)):
  - *Prefix semantics.* buf replaces `java_package` with `<prefix>.<proto package>`; the simpler rule is
    `<prefix>.<existing java_package>`. buf's gives shorter names (`org.hiero.sdk.protobuf.proto` for the 134 files
    that declare `package proto`) but changes more.
  - *Interaction with `outputDescriptorFile`*: should the emitted descriptor carry the managed options or the
    originals?

  **When it lands here:** set the option in
  [`sdk-java/protobuf/pom.xml`](../sdk-java/protobuf/pom.xml), update the five qualified `exports … to …` in its
  `module-info.java`, and rewrite the imports in the four spike files that use protobuf classes
  (`AccountCreateTransaction`, `ClientRuntime`, `DefaultPackedTransaction`, `Protobuf` — all of them import from
  `com.hederahashgraph.api.proto.java` only, so it is one prefix).

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
