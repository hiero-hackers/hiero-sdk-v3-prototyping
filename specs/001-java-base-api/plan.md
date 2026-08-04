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
public API has no runtime library dependency.

**Storage**: N/A. The generator reads repository files and writes deterministic Java sources and a
manifest; the API contains no persistence or registry state.

**Testing**: JUnit Jupiter, Java Compiler API consumer fixtures, reflection/API-shape checks, JPMS
module-path checks, Javadoc checks, dependency analysis, and clean-regeneration comparison

**Target Platform**: Cross-platform JVM and JPMS consumers supporting Java 21 or later

**Project Type**: Maven multi-module Java library prototype plus build-time code generator

**Performance Goals**: Generate and verify the eleven base specifications in under 10 seconds on a
developer workstation; API classes must perform no operational work at class initialization

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
  reversible. The only planned variances are operation companion interfaces, lowercase package
  normalization, a single aggregate JPMS module, and approved structural HBAR metadata.
- **Security - PASS WITH REVIEW GATE**: No provider, codec, parser, registry, crypto, transport, or
  network implementation is in the API module. Byte arrays are copied at every structural value
  boundary, and sensitive `toString()` output is prohibited.
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

1. Bootstrap the pinned Maven wrapper, reactor, Java 21 toolchain checks, and empty named modules.
2. Implement the fenced-schema lexer/parser, typed intermediate model, source diagnostics, and
   semantic validator for only the constructs exercised by `spec/base`.
3. Implement deterministic Java mapping and rendering, including Javadocs, JSpecify annotations,
   generated headers, module metadata, and the generation manifest.
4. Generate the complete base API, then audit every companion contract and deferred enforcement
   item before treating the output as a compatibility baseline.
5. Add positive/negative consumer compilation, API-shape, ownership/invariant, JPMS, dependency,
   documentation, security, operational-scope, and clean-regeneration checks.
6. Obtain human API and security approval; only then freeze the prototype as the initial V3 Java
   base compatibility baseline.
