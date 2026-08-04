# Research: Java Base Public API Prototype

## Decision 1: Java Baseline

**Decision**: Target Java 21 and compile with `--release 21`. Newer JDKs may run the build, but the
generated class files and public source must remain Java 21 compatible.

**Rationale**: Records and sealed types are stable by Java 17, and Java 21 provides the modern
language baseline already assumed by the repository's Java guidance. It supports the required API
forms without forcing V3 consumers onto the newest runtime.

**Alternatives considered**:

- Java 17: technically sufficient for records and sealed types, but below the repository's stated
  Java 21 streaming baseline and less suitable for a new-generation SDK.
- Java 25: available in the development environment and an LTS release, but would unnecessarily
  reduce consumer compatibility; this feature needs no Java 25 language feature.

## Decision 2: Build and Artifact Shape

**Decision**: Introduce a pinned Maven wrapper and a three-module reactor: generator, generated base
API, and contract tests. Publish only the `hiero-sdk-base-api` module.

**Rationale**: Maven is present in the development environment, is conventional for Java libraries,
and supports Java toolchains, JPMS, source/javadoc artifacts, and reproducible plugin pinning. One
API artifact directly matches the approved feature scope.

**Alternatives considered**:

- Gradle: viable, but there is no repository precedent and it adds a second DSL without solving a
  feature requirement.
- One JPMS module per namespace: gives stronger module-level dependency enforcement but turns the
  first prototype into eleven artifacts and complicates consumer setup. Package-level architecture
  tests are sufficient for this bounded base prototype.
- Handwritten source only: faster initially but cannot satisfy deterministic, traceable generation.

## Decision 3: Package and Module Names

**Decision**: Use JPMS module `org.hiero.sdk.v3.base` and package root `org.hiero.sdk.v3`.
Normalize namespace segments to lowercase Java package segments; therefore `nativeToken` maps to
`nativetoken` and `ledger.config` maps to `ledger.config`.

**Rationale**: The versioned root avoids collision with V2 and makes the prototype's compatibility
boundary explicit. One package per source namespace preserves discoverability and dependency
traceability.

**Alternatives considered**:

- `org.hiero.<namespace>` exactly as the current Java guideline examples: concise, but it does not
  distinguish the independently designed V3 prototype from existing SDK packages.
- A flat `org.hiero.sdk.v3.base` package: simpler module descriptor, but erases source namespace
  boundaries and invites naming collisions.

## Decision 4: Generator Architecture

**Decision**: Build a deterministic Java CLI with four stages: Markdown schema extraction, lexical
analysis and parsing into a typed AST, semantic validation and source inventory, then Java rendering.
The parser supports the documented grammar constructs used by `spec/base`; unsupported syntax fails
with file/line diagnostics instead of being copied through or ignored.

**Rationale**: A typed model makes imports, generic bounds, inheritance, annotations, overloads, and
constraints verifiable before rendering. It avoids fragile line-based code generation and makes
silent omissions testable.

**Alternatives considered**:

- Regex/template-only generation: too fragile for recursive generics, annotations, overloads, and
  comments.
- A general parser-generator dependency: useful after the meta-language has a formal grammar, but
  premature for the small current syntax and an unnecessary build dependency now.
- AI-generated Java without a deterministic generator: cannot pass clean regeneration or provenance
  requirements.

## Decision 5: Generated Boundary and Provenance

**Decision**: Treat all of `java/hiero-sdk-base-api/src/main/java` as generated and checked in. Every
file receives a generated header with source path and declaration identity. A deterministic manifest
records source Git revision, SHA-256 for every input, generator artifact version, configuration hash,
Java target, package root, ordered declaration inventory, deferred mappings, and output hashes.

**Rationale**: Checked-in source is directly reviewable as the proposed public API. Making the full
source tree generated removes ambiguity about manual edits, while hashes cover dirty-working-tree
inputs that a Git revision alone would miss.

**Alternatives considered**:

- Generate only into `target/`: clean source tree, but reviewers cannot inspect the exact baseline
  without running tooling and diffs are harder to review.
- Mixed handwritten and generated files in one package: obscures ownership and makes regeneration
  prone to accidental overwrite.

## Decision 6: Java Type Forms

**Decision**: Apply these rules in order:

1. Provider-owned or behavior-only abstractions become interfaces.
2. Pure immutable data with safe component equality becomes records, including records that
   implement structural interfaces.
3. Pure immutable data containing arrays becomes final value classes with defensive copies and
   content-based equality, because Java record equality for arrays is reference-based.
4. Closed data hierarchies become sealed interfaces with public record or final-class variants.
5. Fixed value sets become enums; enum metadata may have structural fields and accessors.
6. Classes otherwise appear only for value classes, exceptions, annotations, and constants holders.

**Rationale**: This exposes implementation-independent contracts, preserves immutable value
semantics, and avoids pretending that operational behavior exists.

**Alternatives considered**:

- Records for every immutable declaration: fails for inheritance and gives incorrect equality for
  arrays.
- Public concrete classes for behavior-bearing final types: would require fake method bodies or
  operational implementations.
- Abstract classes as the default: unnecessarily constrains future implementations and extension.

## Decision 7: Operations Without Implementations

**Decision**: Keep operations abstract on provider-owned interfaces such as `Page`, `Key`,
`PublicKey`, and `PrivateKey`. Map source namespace/static factories to focused companion interfaces
(`AuthorityFactory`, `KeyFactory`, `LedgerFactory`, and `NetworkSettingRegistry`). Move operational
methods from structural ledger/native-token values and key-format enums to `LedgerOperations`,
`NativeTokenOperations`, and `KeyFormatOperations`, receiving the value or enum as an explicit
argument. No provider loader, global singleton, default implementation, or method that only throws
is generated.

**Rationale**: Java has neither abstract static interface methods nor enum/final value methods
without bodies. Companion contracts preserve operation inputs, outputs, and errors while allowing
the data itself to remain usable, immutable, sealed/final, and free of operational code. They are a
Java mapping variance and require human API approval.

**Alternatives considered**:

- Static methods that throw `UnsupportedOperationException`: explicitly prohibited fake behavior.
- `ServiceLoader`-backed static methods: introduces provider discovery and registry behavior into
  the API prototype.
- Omit the methods: violates declaration coverage.
- Implement parsing, formatting, crypto, or decoding now: violates the API-only scope.

## Decision 8: Ledger Finality and Constants

**Decision**: Generate the ledger identifier hierarchy as sealed structural interfaces with
generated final value variants. Move checksum, canonical formatting, and parsing behavior to the
abstract ledger companions. This permits `Address`, `ContractId`, and `AccountId` to enforce
`@@finalType` and permits all three zero sentinels to be real immutable constants without embedding
operational behavior.

**Rationale**: A public interface alone cannot enforce source finality or instantiate typed
constants. Separating structural state from operations preserves both requirements and keeps the
artifact implementation-free in the operational sense defined by the feature.

**Alternatives considered**:

- Interface-only identifiers: cannot enforce finality or provide typed constants.
- Final classes with unsupported or provider-delegating instance methods: introduces fake behavior
  or provider state into the API artifact.

## Decision 9: HBAR Unit Metadata

**Decision**: Generate `HbarUnit` with the symbols shown in `hedera.md` and these proposed decimal
base-unit factors: `TINYBAR=1`, `MICROBAR=100`, `MILLIBAR=100_000`, `HBAR=100_000_000`,
`KILOBAR=100_000_000_000`, `MEGABAR=100_000_000_000_000`, and
`GIGABAR=100_000_000_000_000_000`. Record them as an approved Java mapping entry rather than
pretending every value is explicitly encoded in the schema.

**Rationale**: The enum must implement `NativeTokenUnit` to satisfy the source generic bound. The
names and symbols establish standard decimal prefixes around HBAR's documented 100,000,000 base
units, but the full factor table requires explicit human approval before generation.

**Alternatives considered**:

- Plain enum plus external metadata provider: breaks `HbarUnit extends NativeTokenUnit` and the
  `Hbar extends NativeToken<Hbar, HbarUnit>` generic relationship.
- Invent abstract enum accessors: Java requires each constant to implement them, which merely hides
  the same missing values in method bodies.

## Decision 10: Nullability, Collections, and Bytes

**Decision**: Use JSpecify 1.0.0 annotations explicitly on all public reference parameters and return
types, with `requires static org.jspecify` in JPMS. Map nullable numeric values to boxed types. Return
and store collection snapshots using `List.copyOf`/`Set.copyOf`; reject null collections and null
elements. Copy all incoming and outgoing `byte[]` values. Never expose private-key bytes from
`toString()`, exception messages, or generated diagnostics.

**Rationale**: This follows the Java guideline and closes the primary mutability and secret-leak
risks in structural API values.

**Alternatives considered**:

- No nullness library: loses machine-checkable narrowing and violates the Java guideline.
- `Optional` fields/parameters: conflicts with repository null-handling guidance.
- `ByteBuffer`: not the canonical `bytes` mapping and can still expose mutable backing state.

## Decision 11: Errors and Async Semantics

**Decision**: Map `illegal-format` to `IllegalArgumentException` and `not-found-error` to
`NoSuchElementException`. Introduce a documented unchecked `MirrorNodeException` because
`mirror-node-error` has no Java platform equivalent. `Page.next()` and `Page.first()` return
non-null `CompletionStage<Page<T>>`; errors are delivered as exceptional completion causes. The API
does not select executors, schedules, cancellation policies, retries, timeouts, or blocking wrappers.

**Rationale**: This follows the repository's exception and async mappings without adding execution
policy to a contract-only artifact.

**Alternatives considered**:

- Checked mirror-node exception on async methods: checked exceptions cannot be represented on the
  `CompletionStage` type and add no compile-time handling benefit.
- `CompletableFuture`: exposes a concrete implementation type rather than the intended interface.

## Decision 12: Constraint Enforcement

**Decision**: Enforce non-null, defensive ownership, length, range, non-empty, threshold, and
one-of constraints in generated structural constructors where a concrete value type exists. For
interface-backed types, preserve constraints in Javadocs and the manifest, then use contract tests
against future providers; mark those checks deferred rather than claiming runtime enforcement now.
Unsigned Java primitives retain their signed storage type, with non-negative validation required at
construction or provider boundaries.

**Rationale**: Structural validation is explicitly allowed; implementation-dependent validation is
not. The manifest gives an auditable distinction.

**Alternatives considered**:

- Custom public validation annotations: invents a new API vocabulary not defined by the source.
- Silent documentation-only handling for every constraint: too weak where constructors can enforce
  the invariant immediately.

## Decision 13: Verification and Security Review

**Decision**: Use generated inventory assertions, positive and negative Java compiler fixtures,
reflection/API-shape tests, module-path tests, byte ownership tests, forbidden-package/dependency
scans, Javadoc linting, two-pass regeneration, and a manual security/API checklist. No sample fixture
may contain real private keys or credentials.

**Rationale**: The feature's value is its contract fidelity. Compilation and structural inspection
test that directly, while human review remains mandatory for key and authority decisions.

**Alternatives considered**:

- Runtime integration tests: there is intentionally no runtime provider to integrate.
- Cryptographic golden vectors: belong to later implementation features and would create pressure to
  add crypto behavior here.

## Retained Source Questions

The generator must reproduce these as Javadocs and manifest entries without resolving them:

- Whether `ExchangeRate.exchangeRateInUsdCents` should eventually change from `double` to decimal.
- How `Network.id` bytes are assigned and created.
- Whether Authority naming or convenience factories should change.
- The pending formal meta-language definition of `@@sealed`.
- Future IPv6 widening for `IpAddress` and future typed token/NFT identifiers.
