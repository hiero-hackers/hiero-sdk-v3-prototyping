## Context

See [proposal.md](proposal.md) for motivation and
[specs/java-base-public-api/spec.md](specs/java-base-public-api/spec.md) for the behavioral contract. The repository is
currently specification-only. The eleven Markdown files under `spec/base` use a language-neutral meta-language and mix
data declarations with operations whose eventual behavior requires cryptography, parsing, networking, mutable state,
or provider selection.

The Java API is intentionally created before those implementations. The agreed boundary permits only the mechanics
needed to make public values valid and honest Java values. This design deliberately avoids implementing a Markdown
schema parser or code generator.

## Goals / Non-Goals

**Goals:**

- Provide a buildable Java 21 API artifact covering every `spec/base` namespace.
- Keep every source-to-Java decision reviewable in a declaration-level mapping matrix.
- Permit validation, defensive ownership, equality, hashing, structural factories, and deterministic field-derived
  canonical representations.
- Keep later implementations dependent on the API while preventing the API from depending on them.
- Detect unreviewed API drift through consumer tests, boundary audits, and a public-signature snapshot.

**Non-Goals:**

- Parse the meta-language or generate Java sources from Markdown.
- Select or wire a runtime provider.
- Implement parsing, encoding, cryptography, networking, persistence, retries, scheduling, mutable registries, or
  business/service behavior.
- Resolve source-level `Questions & Comments` or change the language-neutral schemas.
- Promise compatibility with V2.

## Decisions

### 1. Use a self-contained two-module Maven reactor under `java/`

The Maven reactor, wrapper, build configuration, modules, tests, and Java-specific documentation all live under
`java/`. The repository root remains specification-oriented. The Java workspace contains:

- `java/hiero-sdk-base-api`: directly maintained Java public sources and the only publishable runtime artifact;
- `java/tests/java-base-api-contract`: black-box compilation, signature, module, security, documentation, and source
  traceability verification.
- `java/docs/base-api`: mapping, dependency, structural-boundary, consumption, and conformance documentation.
- `java/pom.xml`, `java/mvnw`, `java/mvnw.cmd`, and `java/.mvn`: the complete Java build entry point.

There is no parser or generator module. Later implementation modules depend on the API in the normal direction.

**Alternative considered:** A parser and generator were rejected because the user explicitly chose direct contract
maintenance and does not want a schema parsing step. The API remains reviewable as ordinary Java changes.

### 2. Use an explicit mapping matrix instead of parsing the schema

A reviewed Markdown mapping matrix accounts for every namespace, declaration, field, method, constant, annotation
constraint, error, dependency, mapping variance, retained question, and deferred behavior under `spec/base`. Each entry
links to its source file and Java element and identifies whether behavior is structural or operational.

A base-specification change is incomplete until its Java source, matrix entry, and affected verification are updated
together. Unknown or unapproved mappings block review rather than being guessed in Java. This provides a fail-closed
human review gate without maintaining a second parser for the project meta-language.

### 3. Permit structural value mechanics and defer SDK operations

A method body belongs in the API only when all of these statements are true:

1. It establishes or preserves validity, immutability, defensive ownership, equality, hashing, or a canonical
   representation of the receiving value.
2. Its result is determined solely by its arguments and the value's fields.
3. It is deterministic and side-effect free.
4. It does not consult providers, registries, clocks, randomness, environment state, storage, or network resources.
5. It does not perform cryptography, interpret an external serialized representation, or select an implementation.

Allowed API behavior includes constructor null/range/length/one-of/threshold checks, defensive copies, immutable
collection snapshots, structural equality and hashing, field-only construction factories, and canonical formatting
derived from validated fields.

Deferred behavior includes parsing external representations, checksum rules, key generation and crypto, token
conversion, transaction-id generation, mutable registry behavior, provider discovery, I/O, scheduling, retries, and
network calls.

Pure immutable data maps to records where inheritance and array ownership permit it. State-bearing abstractions map to
abstract classes when a base constructor can enforce source invariants, nullability, immutable collection ownership,
or defensive byte ownership. Their SDK operations remain abstract and body-free. Data inheritance uses abstract base
classes plus final concrete values where Java's single-inheritance rules preserve the source hierarchy. Closed sums map
to sealed interfaces and public variants. Stateless operation companions remain interfaces.

`EvmAddress` is an intentional abstract-class mapping even though the source calls it a value type. Its base constructor
owns and validates the exact 20-byte representation, while canonical EIP-55 `toString()` remains abstract because the
required Keccak hashing is cryptographic behavior deferred to a later implementation. Its external-string parser and
raw-byte construction entry point therefore remain on a body-free companion contract. The same abstract-base pattern
is used for `Page`, key types, native-token types, ledger bases, transaction identifiers, and network settings wherever
it can enforce structural guarantees without supplying operational behavior.

### 4. Map deferred static operations to companion interfaces

Java cannot declare abstract static methods. A source `@@static` operation that is not structural maps to a named
factory or operations interface with an instance method. Later implementation modules implement those interfaces; this
change does not instantiate, discover, or globally register them.

Structural factories remain static methods on their value type when their bodies only validate fields and construct
values. Every exception to direct source syntax is recorded in the mapping matrix.

### 5. Use Java 21 API idioms without preview features

The API follows `guidelines/api-best-practices-java.md`: primitives for non-null primitive values, boxed types for
nullable primitive values, immutable Java collections, `java.time` types, bounded generics, records where appropriate,
sealed variants where possible, and `CompletionStage<T>` for `@@async` operations.

JSpecify 1.0.0 carries public nullness semantics as a compile-time-only dependency. The named module
`org.hiero.sdk.v3.base` exports the eleven normalized namespace packages and uses a static transitive JSpecify
requirement. The API artifact has no operational runtime dependency.

### 6. Verify direct maintenance as a consumer

Contract tests treat the API artifact as a downstream consumer would and verify:

- complete mapping-matrix coverage and retained source questions;
- positive compilation from every namespace;
- negative compilation or construction for type-system and invariant exclusions;
- canonical formatting and immutable ownership;
- public signatures, JPMS exports, dependency allowlists, and absence of implementation/test/tooling leakage;
- key and authority security exclusions;
- structural body allowlists and absence of operational bodies or unsupported-operation placeholders;
- source-linked Javadocs and requirement-to-test traceability.

The signature snapshot detects accidental API drift. Changes to it require corresponding source mappings and review.
Tests do not claim deferred operations work.

## Risks / Trade-offs

- **[Direct maintenance can drift from `spec/base`]** → Require exhaustive source-linked mapping review, signature
  checks, and contract tests in the same change as every affected source declaration.
- **[The structural/operational boundary can drift]** → Keep an explicit allowlist and inspect structural bytecode.
- **[Java cannot represent every meta-language construct exactly]** → Record each variance and block unreviewed ones.
- **[Companion interfaces are less discoverable than static methods]** → Use consistent names and cross-linked Javadocs.
- **[Abstract bases consume Java's single inheritance slot]** → Use them only for state-bearing source abstractions
  where constructor enforcement is valuable; retain interfaces for stateless operation companions and closed sums.
- **[JSpecify is visible public metadata]** → Pin and review it, use it only statically, and verify no runtime leakage.

## Migration Plan

1. Remove parser, generator, configuration, staging, manifest, and regeneration artifacts from the in-progress work.
2. Keep the Java 21 API and contract-test modules.
3. Complete and approve the declaration-level mapping matrix before finalizing public signatures.
4. Implement the directly maintained API contracts and structural value mechanics.
5. Run consumer compilation, boundary, security, documentation, structural-body, and signature checks from the
   self-contained `java/` reactor.
6. Add operational implementation/provider modules in later OpenSpec changes.

There is no production data migration. Reverting the API and test modules returns the repository to its
specification-only state.
