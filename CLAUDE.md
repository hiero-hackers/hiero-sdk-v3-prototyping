# CLAUDE.md

Guidance for Claude Code (and other agents) when working in this repository.

## What this repository is

This is a **proof-of-concept (PoC) for the next generation ("V3") of client SDKs/libraries** for the
[Hiero](https://hiero.org) project under Linux Foundation Decentralized Trust (LFDT). Because Hedera and other
networks build on Hiero, V3 targets all of them — it is explicitly broader than just Hedera.

The current SDKs are all at version 2; **V3 is the codename for the new generation** that replaces them. V3 is
designed from scratch with **no backward-compatibility constraints** with V2.

**This repo contains specifications, not a shippable SDK.** The specs themselves have no build system and do not
compile; the only buildable module is the spec tooling under `tooling/metalang` (Maven, Java 21). The API is defined once in a **language-agnostic meta-language** and is meant to be translated into
idiomatic implementations per language (Java, JavaScript/TypeScript, Go, Rust, Python, C++, Swift). The `.java`/`.js`
files under `guidelines/` are reference snippets, not a buildable module — except `guidelines/rust-files/`: the
single source of the Rust support types (`BoxFuture`, `BoxStream`, `StreamItem`, `InvalidArgumentError`), which the
generator copies 1:1 into `generated/rust`, so they must always compile. The Java and TypeScript support types are not
copied: they are the hand-written modules `sdk-java/support` (Maven `hiero-sdk-support`, JPMS module
`org.hiero.sdk.support`: `@ThreadSafe`, `HieroStream`, `StreamItem`, `HieroPublisher`, `HieroSubscription`) and
`sdk-ts/support` (npm `@hiero/support`: `Duration`, `StreamItem`, `AbstractConstructor`), on which the generated
modules depend (ADR-0007).

## Repository structure

```
guidelines/
  api-guideline.md              # THE meta-language: syntax + cross-cutting API best practices. Read this first.
  api-best-practices-java.md    # How meta-language concepts map to idiomatic Java
  api-best-practices-rust.md    # ... Rust (the mapping of the Rust generator)
  api-best-practices-js.md      # ... JavaScript
  api-best-practices-ts.md      # ... TypeScript (the mapping of the TypeScript generator)
  rust-files/                   # Rust support types (BoxFuture, InvalidArgumentError, ...); copied 1:1 into generated/rust
  js-files/                     # Illustrative JS reference snippets

openspec-common-delta-changes/  # Plain language-neutral feature proposals and specifications (not an OpenSpec root)
  changes/
    add-common-pagination-public-api/
      proposal.md              # Shared motivation and scope used by every SDK
      spec.md                  # Authoritative Page<$$T> behavior copied into SDK-specific OpenSpec changes

sdk-java/openspec/             # Java-only OpenSpec root
sdk-java/generator.properties  # Java generator configuration (e.g. java.interfaces), see tooling/metalang/README.md
sdk-java/support/              # Hand-written Java support types (streaming, thread-safety) as Maven module
                               #   hiero-sdk-support; generated/java depends on it (install it first)
generated/java/                # Generated Java API as Maven project, one sub-module/JAR per Java module (tracked in
                               #   git; regenerate after spec or generator changes, commands in
                               #   tooling/metalang/README.md). Never edit by hand. `metalang check` verifies
                               #   that it (or an implementation based on it) provides the API of the specs.
                               #   src/test/java holds generated JUnit tests of the spec contract; the tests of
                               #   method stubs fail until the methods are implemented.
sdk-ts/openspec/               # TypeScript-only OpenSpec root
sdk-ts/generator.properties    # TypeScript generator configuration (npm scope, version, location of the support package)
sdk-ts/support/                # Hand-written TypeScript support types (Duration, StreamItem, AbstractConstructor) as
                               #   npm package @hiero/support; generated/ts links it as workspace and depends on it
generated/ts/                  # Generated TypeScript API as npm workspace, one package per spec folder, with
                               #   generated node:test tests (tracked in git; regenerate after spec or generator
                               #   changes; `npm install && npm test` builds and tests it). Never edit by hand.
sdk-rust/generator.properties  # Rust generator configuration (crate prefix, version)
generated/rust/                # Generated Rust API as Cargo workspace, one crate per spec folder, with a generated
                               #   integration test per crate (tracked in git; regenerate after spec or generator
                               #   changes; `cargo test` builds and tests it). Never edit by hand.
tck/                           # TCK binding spike (see tck-binding.md):
  bindings/                    #   bindings of TCK methods to the API (`bindings` code blocks in Markdown)
  runtime/java/                #   hand-written runtime of the Java TCK server (JSON-RPC, converters, setup); Maven
                               #   module hiero-sdk-tck-runtime, implements generated/java-tck/contract
  runtime/ts/                  #   the same for TypeScript (@hiero/tck-runtime), implements generated/ts-tck/contract
  solo.env, run-tck.sh         #   TCK configuration for a local Solo network (the default) and the script that runs
                               #   the TCK against a generated server
generated/java-tck/            # Generated from tck/bindings and the converter catalogue (tracked in git; `metalang
                               #   tck generate`, see tooling/metalang/README.md). Never edit by hand.
                               #   contract/: interfaces the runtime implements; server/: the TCK server, compiled
                               #   against API + contract only (the runtime is a runtime dependency)
generated/ts-tck/              # The same for TypeScript (@hiero/tck-contract, @hiero/tck-server)
package.json                   # npm workspace of all TypeScript modules (generated API, sdk-ts/support, TCK contract,
                               #   server and runtime); `npm run build:tck-ts` builds the TypeScript TCK server
.sdkmanrc                      # SDKMAN! toolchain pin (JDK 25); `sdk env` activates it, no manual JAVA_HOME
mvnw, mvnw.cmd, .mvn/          # Maven wrapper (pinned Maven version) — use `./mvnw -f <module>` for every Maven
                               #   build, run from the repository root; never call a locally installed `mvn`

spec/                           # The actual V3 public-API specifications, written in the meta-language
  base/                         # Foundational namespaces shared by everything
    common.md (common)          #   Page<$$T> and other shared types
    ledger.md (ledger)          #   Ledger, Address, ConsensusNode, MirrorNode
    ledger-config.md (ledger.config)
    keys.md (keys)              #   Keys + key import/export (PKCS#8, SPKI, DER, PEM)
    authority.md (authority) #  Authorization model (HAPI Key): Authority sum type (public key / contract / m-of-n)
    native-token.md (nativeToken + hbar)  # NativeToken abstraction + Hedera-specific HBAR implementation
    token.md (token)              #   HTS token classifier enums (TokenType, TokenSupplyType); future home for TokenId / NftId
    grpc.md (grpc)
    proto.md (proto)
  consensus-node-client/        # Talking to the consensus node
    client.md (consensusnode.client)              # HieroClient, Account, TransactionSigner
    transactions.md (consensusnode.transactions)  # Transaction<$$Receipt, $$Self>, Response, Receipt, Record
    transactions-accounts.md (consensusnode.transactions.accounts)
    transactions-spi.md (consensusnode.transactions.spi)   # SPI for custom services/transaction types
    proto.md / proto-accounts.md (consensusnode.proto[.account])
  mirror-node-client/           # Querying the Hiero Mirror Node REST API
    mirror-node.md (mirrornode) # MirrorNodeClient with per-domain repositories
    mirror-node-{account,contract,network,nft,token,topic,transaction,common}.md
  enterprise/                   # High-level "service" layer on top of the raw SDK for app/framework integration
    service.md (enterprise.service)
    service-account.md (enterprise.service.account)
    service-contract.md (enterprise.service.contract)

tooling/metalang/               # Prototype tooling, multi-module Maven build (see its README):
  metalang-core/                #   ANTLR grammar, parser, semantic model, validator, shared generator support,
                                #   TCK bindings (parser, resolver, coverage check)
  metalang-java/                #   Java generator + Java conformance check + Java TCK server generator
  metalang-typescript/          #   TypeScript generator + TypeScript conformance check
  metalang-rust/                #   Rust generator + Rust conformance check (rs-api, a syn-based helper)
  metalang-cli/                 #   command line tool; builds tooling/metalang/target/metalang-*-cli.jar
```

### How the layers relate

- **`base`** — primitives every layer depends on (`ledger`, `keys`, `nativeToken`/`hbar`, `common`, `proto`, `grpc`).
  The native token is modeled as an abstraction (`nativeToken`) with HBAR as one concrete implementation (`hbar`), so
  the SDK is not bound to Hedera.
- **`consensusnode.*`** — the low-level client: build/sign/execute transactions against the consensus node. The
  `spi` namespace exists because the consensus node is service-oriented and supports *custom* services and transaction
  types, so the SDK must be extensible (hence `TransactionStatus` is an abstraction with an `int32` code, not a closed
  enum).
- **`mirrornode.*`** — read-side: query historical/state data from a Mirror Node over REST, returning paginated
  `Page<$$T>` results.
- **`enterprise.service.*`** — a higher-level convenience/service layer designed for easy use and deep framework
  integration (cf. Hiero Enterprise Java / JS). Provides factory methods today; real deployments use dependency
  injection.

## The spec meta-language (most important convention)

**Before writing or editing any `spec/*.md`, read `guidelines/api-guideline.md`** — it is the source of truth. Key
points to keep specs valid and consistent:

- Each spec file follows the same skeleton: `# Title` → `## Description` → optional `## Design Notes` →
  `## API Schema` (a fenced code block) → optional `## Default Instances` → optional `## Examples` → optional
  `## Testing` → `## Questions & Comments`.
- `## Default Instances` records the standard way to obtain an instance of the spec's types through the API
  (`instance PublicKey = DEFAULT(PrivateKey).createPublicKey()`), with concrete values where implementations check them
  (keys, addresses). Generated tests use them first; the validator warns (`instance.missing`) about types that cannot
  be obtained at all. See "Default instances" in `guidelines/api-guideline.md`.
- `## Description` and the comments directly above declarations become the **public API documentation** (Javadoc,
  rustdoc, …): write them for SDK users. Spec-author rationale, links to other spec files, ADR references and
  meta-language details go to `## Design Notes` (resolved) or `## Questions & Comments` (open). The validator warns
  about leaks (`doc.internal-reference`).
- The `## API Schema` block opens with `namespace <name>`. Types from other namespaces are imported explicitly, one
  statement per source namespace: `requires {Address, Ledger} from ledger`. Import only what is used; `requires {*}
  from ns` imports everything.
- **Imported (and same-namespace) types are referenced by their simple name** (`Address`, `Page`) — like an ES6/Java
  import. The qualified form `namespace.Type` is only used to disambiguate a name collision between two namespaces.
- **Naming:** Types `PascalCase`; fields & methods `lowerCamelCase`; enum values `UPPER_SNAKE_CASE`; namespaces
  `lowerCamelCase` (dots for sub-namespaces, no hyphens); constants `UPPER_SNAKE_CASE`; `@@throws` error ids
  `lowercase-kebab-case` (e.g. `not-found-error`).
- **Generic type parameters are prefixed with `$$`** (e.g. `Page<$$T>`, `Transaction<$$Receipt extends Receipt>`).
- **Annotations use a `@@` prefix.** Common ones: `@@immutable`, `@@nullable`, `@@deprecated`, `@@default(v)`,
  `@@static`, `@@async`,
  `@@streaming`, `@@throws(...)`, `@@threadSafe[(group)]`, validation (`@@min/@@max/@@minLength/...`), and type-level
  `@@oneOf(...)`, `@@oneOrNoneOf(...)`, `@@finalType`.
- Abstract types use `abstraction`; inheritance uses `extends`; enums use `enum`.

### Cross-cutting design principles (enforce these in specs and reviews)

- **Immutability first:** annotate fields `@@immutable` by default; introduce mutability only with a clear reason.
- **Never define nullable collections** (`list`/`set`/`map`) — return empty instead of null.
- **Avoid `ANY` as a standalone type** — prefer a generic `$$T`, a concrete base type, a `@@oneOf` union, or `bytes`
  with a documented schema. (`ANY` as a *wildcard type argument* like `ContractParam<ANY>` is fine.)
- `@@async` returns a future/promise; `@@streaming` returns a pull-based async stream of items (`streamResult<TYPE>`
  for per-item errors). They are mutually exclusive.

## How to make changes

- **Editing/adding a spec:** keep the section skeleton, declare the `namespace` and import external types with
  `requires {Type} from ns`, reference them by simple name, and follow the naming + annotation rules above. Match the
  style of neighboring spec files. Validate the result with the spec tooling (see `tooling/metalang/README.md`):
  `./mvnw -f tooling/metalang/pom.xml -q package -DskipTests` then
  `java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar validate spec`. A change must not introduce
  new `syntax.error` or ERROR findings.
- **Changing the meta-language itself** (guideline syntax, new annotation): update the grammar
  (`MetaLang.g4`), `KnownAnnotation`, the `Rule` catalog and the tests in `tooling/metalang` in the same change. Every
  new rule needs a fixture in `tooling/metalang/metalang-core/src/test/resources/rule-fixtures/<rule-id>/` (enforced by
  `RuleFixturesTest`).
- **Generated vs. hand-written:** every module is either completely generated (under `generated/`, every file carries
  the generator header, never edited by hand) or completely hand-written; generated code never contains copies of
  hand-written code. Hand-written code that generated code needs is a support module or implements a generated
  contract — see [ADR-0007](docs/adr/0007-separate-generated-and-hand-written-modules.md).
- **Open design questions** belong under each file's `## Questions & Comments` (often attributed to a GitHub handle).
  Don't silently resolve them; surface them.
- **Language best-practice docs** (`api-best-practices-*.md`) describe how a meta-language concept maps to one
  language — when you add a new meta-language feature, consider whether each language guide needs a mapping (the
  guideline lists per-language mappings for varargs, wildcards, streaming cancellation, `streamResult`, etc.).
- Note: `api-guideline.md` references a `proposals/` folder, but in this repo the specs live under `spec/`.
- Some language guides referenced by `api-guideline.md` (cpp, python, go, swift) do not exist yet — that's
  expected; Java, TypeScript, Rust and JS guides are present so far. The Java, TypeScript and Rust guides are
  implemented by the generators in `tooling/metalang` (`generate --language=java|ts|rust`,
  `check --language=java|ts|rust`).

## Relevant skills

When working here, these skills are particularly useful:

- **`java-api-design`** and **`java-best-pratices`** — when reasoning about API surface, SPI, interfaces vs. records,
  module boundaries, breaking changes.
- **`modern-java`** — when writing or reviewing the Java support types in `sdk-java/support/` (use modern idioms:
  records, sealed types, pattern matching, etc.).
- **`adr-create`** — when a genuine architectural decision is made, record it as an ADR.
- **`hiero-info`** / **`hedera-info`** — background when writing prose about Hiero/Hedera concepts.
- **`grill-me`** — to stress-test an API design before committing to it.

## Audience & language

The repository (README, guidelines, specs) is written in **English** for an international open-source audience — keep
new docs in English. Commit messages and PR descriptions in English as well.
