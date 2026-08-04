# Implementation Plan: Java Base Public API Prototype

**Branch**: `feature/sdd-java-github-spec-kit` | **Feature ID**: `001-java-base-api` |
**Date**: 2026-08-04 | **Spec**: [spec.md](spec.md)

**Input**: Approved feature specification from `/specs/001-java-base-api/spec.md`

## Summary

Generate a compile-only, implementation-independent Java API prototype from all eleven
`spec/base/*.md` contracts. A versioned Java generator will parse the fenced meta-language into a
typed intermediate model and render one JPMS API module containing only exported interfaces,
immutable records or final value classes, enums, exceptions, annotations, constants, and module
metadata. Operations that require cryptography, parsing, formatting, conversion, time, networking,
or mutable registries remain abstract contracts. Maven contract tests verify source coverage,
public shape, package boundaries, invalid constructions, consumer compilation, documentation, and
clean deterministic regeneration.

## Technical Context

**Language/Version**: Java 21 API baseline; Maven builds use a JDK 21 toolchain or a newer JDK
with `--release 21`

**Primary Dependencies**: Java platform APIs; JSpecify 1.0.0 as a compile-time nullness annotation
dependency; Maven build plugins and JUnit Jupiter are test/build-only dependencies. The generated
public API has no runtime library dependency. All non-platform dependencies remain provisional until
the accountable review in [dependency-review.md](dependency-review.md) is completed.

**Storage**: N/A. The generator reads repository files and writes deterministic Java sources and a
manifest; the API contains no persistence or registry state.

**Testing**: JUnit Jupiter, Java Compiler API consumer fixtures, reflection/API-shape checks, JPMS
module-path checks, Javadoc checks, dependency analysis, and clean-regeneration comparison

**Target Platform**: Cross-platform JVM and JPMS consumers supporting Java 21 or later

**Project Type**: Maven multi-module Java library prototype plus build-time code generator

**Performance Goals**: No generator timing target is an acceptance criterion because this prototype
has no reproducible hardware baseline. API classes must perform no operational work at class
initialization.

**Constraints**: API-only output; no operational SDK implementation; deterministic offline
generation after dependencies are cached; no secret material in source, tests, diagnostics, or
manifests; no implementation or unapproved third-party types in public signatures

**Scale/Scope**: Eleven namespaces, 37 declared domain types, 6 namespace constants, all declared
fields/methods/factories/constraints/errors, one generated API module, one generator module, and one
contract-test module

## Constitution Check

*GATE: Passed before Phase 0 research and re-checked after Phase 1 design.*

### Pre-Research Gate

- **Specification authority - PASS**: FR-001 through FR-017 trace to all eleven `spec/base` files,
  both API guidelines, and ADR-0004 in [spec.md](spec.md). Open source questions remain unresolved.
- **Semantic parity - PASS WITH REVIEW GATE**: Canonical type mappings follow the Java guideline.
  Java cannot put abstract operations on static namespaces, enum constants, or final structural
  values, so those declarations map to companion contract interfaces. This is recorded in
  [public-api.md](contracts/public-api.md) and requires human API approval before implementation.
- **Security - PASS WITH REVIEW GATE**: The feature is security-sensitive. Threats include mutable
  byte ownership, secret disclosure, private-key authority leaves, accidental crypto bodies, and
  provider leakage. Negative tests and a human security review are mandatory.
- **Dependency governance - PASS WITH REVIEW GATE**: JSpecify, JUnit, Maven plugins, wrapper
  artifacts, and their transitive dependencies require documented necessity, maintenance, license,
  provenance, and security review before adoption. The required record is
  [dependency-review.md](dependency-review.md).
- **Determinism - PASS**: Canonical ordering, UTF-8/LF output, source hashes, generator version, and
  configuration are specified in [generation-contract.md](contracts/generation-contract.md).
  Runtime cryptographic/wire transformations are excluded, so golden vectors are deferred to the
  features that implement them.
- **Executable conformance - PASS**: [verification-matrix.md](contracts/verification-matrix.md)
  maps every FR to automated checks and required reviews.
- **Explicit semantics - PASS**: [data-model.md](data-model.md) defines nullability, immutable
  ownership, collections, errors, async failures, invariants, side effects, and concurrency.
  Retry, timeout, cancellation, and idempotency are N/A because no operation is executed here.
- **Reproducible generation - PASS**: Generated files are confined to the API module's entire
  `src/main/java` tree and carry generated headers. A manifest records source revision and hashes,
  generator version, Java baseline, package root, and output hashes.
- **Human authority - PASS WITH REVIEW GATE**: The project owner approved planning on 2026-08-04.
  API-mapping, compatibility-baseline, key/authority security, and every deferred enforcement item
  still require accountable human approval before prototype acceptance.

### Post-Design Gate

- **Specification authority - PASS**: The type inventory accounts for every declared base type,
  constant, operation category, constraint, and empty namespace without modifying `spec/base`.
- **Semantic parity - PASS WITH REVIEW GATE**: Package and Java-form mappings are explicit and
  reversible. The planned variances listed in [public-api.md](contracts/public-api.md) cover operation
  companions, lowercase package normalization, the aggregate JPMS module, sealed/final ledger forms,
  the abstract registry contract, and direct Authority variant construction. Source-defined HBAR
  metadata maps directly and is not a Java variance.
- **Security - PASS WITH REVIEW GATE**: No provider, codec, parser, registry, crypto, transport, or
  network implementation is in the API module. Byte arrays are copied at every structural value
  boundary, and sensitive `toString()` output is prohibited.
- **Dependency governance - PASS WITH REVIEW GATE**: Public, build, test, and wrapper dependencies
  remain blocked until the accountable dependency review is approved; leakage checks then enforce
  the approved scope.
- **Determinism - PASS**: The generation contract has no clock-, locale-, filesystem-order-, or
  machine-dependent output. Source timestamps are excluded from generated files.
- **Executable conformance - PASS**: Positive and negative consumer fixtures, API inventory,
  forbidden-dependency scanning, and regeneration checks cover the designed surface.
- **Explicit semantics - PASS**: Async methods return non-null `CompletionStage`; declared async
  errors complete stages exceptionally. Collections are non-null immutable snapshots. Nullable
  scalars retain explicit JSpecify annotations and boxed primitive forms where required.
- **Reproducible generation - PASS**: The manifest and clean-room output comparison make every
  generated change attributable to inputs, configuration, or generator version.
- **Human authority - PASS WITH REVIEW GATE**: No agent approval is claimed. The approvals listed
  in [verification-matrix.md](contracts/verification-matrix.md) remain release blockers.

## Project Structure

### Documentation (this feature)

```text
specs/001-java-base-api/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── dependency-review.md
├── contracts/
│   ├── public-api.md
│   ├── generation-contract.md
│   └── verification-matrix.md
└── tasks.md             # Created later by /speckit-tasks, not by this plan
```

### Source Code (repository root)

```text
pom.xml
mvnw
.mvn/wrapper/

codegen/java-base.yml

tools/spec-codegen-java/
├── pom.xml
└── src/
    ├── main/java/org/hiero/sdk/v3/codegen/
    │   ├── cli/
    │   ├── model/
    │   ├── parser/
    │   ├── render/
    │   └── validation/
    └── test/java/org/hiero/sdk/v3/codegen/

java/hiero-sdk-base-api/
├── pom.xml
└── src/main/java/                   # Entire tree is generated; never edit manually
    ├── module-info.java
    └── org/hiero/sdk/v3/
        ├── authority/
        ├── common/
        ├── grpc/
        ├── hedera/
        ├── keys/
        ├── ledger/
        │   └── config/
        ├── nativetoken/
        ├── proto/package-info.java
        ├── solo/
        └── token/

tests/java-base-api-contract/
├── pom.xml
└── src/test/
    ├── java/org/hiero/sdk/v3/contract/
    └── resources/fixtures/
        ├── positive/
        └── negative/
```

**Structure Decision**: Use one Maven reactor because the repository currently has no build system.
The checked-in API module is the only consumer artifact and is one named JPMS module,
`org.hiero.sdk.v3.base`, exporting one package per source namespace. The generator and contract
tests are build-time modules and cannot leak into the API artifact. A single module satisfies the
requested coherent base artifact; package dependency tests preserve namespace direction until a
future feature has evidence that separate namespace artifacts are worth the compatibility cost.

## Complexity Tracking

No constitution violations are planned. Java mapping variances are compatibility-sensitive design
decisions, not waivers; they remain unapproved until human review and are listed in the contracts.

## Implementation Sequence

1. Review and approve all planned dependencies, then bootstrap the pinned Maven wrapper, reactor,
   Java 21 toolchain checks, and empty named modules.
2. Write the foundational parser, validation, rendering, provenance, and determinism tests.
3. Obtain approval for Java mapping variances, then implement the parser, typed model, validator,
   mapper, renderer, manifest, and Maven integration until the foundational tests pass.
4. For each user story, complete any required threat analysis, write positive and negative contract
   tests, implement its isolated mapping rules, regenerate its output, and run its independent checks.
5. Serialize changes to aggregate mapping files and the generated manifest while allowing isolated
   mapper and test work from different stories to proceed concurrently.
6. Run cross-cutting traceability, documentation, module-boundary, clean-regeneration, and final
   signature-baseline checks.
7. Obtain final human API and security approval; only then freeze the prototype as the initial V3
   Java base compatibility baseline.
