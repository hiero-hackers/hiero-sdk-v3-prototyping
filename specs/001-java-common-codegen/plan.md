# Implementation Plan: Java Common Code Generation

**Branch**: `001-java-common-codegen` | **Date**: 2026-07-28 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-java-common-codegen/spec.md`

**Note**: This plan covers the first end-to-end Java code generation slice for
`spec/base/common.md` only.

## Summary

Create a standalone Java code generator module that reads the fenced
`## API Schema` block from `spec/base/common.md`, parses it into a normalized
intermediate representation, and emits deterministic Java API source for the
`common` namespace. The first slice proves the pipeline with one abstraction
(`Page<$$T>`) and adds golden-output verification so later namespaces can build
on a stable foundation.

## Technical Context

**Language/Version**: Java 21 for the generator and generated API stubs

**Primary Dependencies**: ANTLR 4 for schema parsing, JavaPoet for source
emission, JSpecify annotations for generated nullability, JUnit 5 with
golden-file assertions for verification

**Storage**: Repository files plus generated output under a dedicated tool
module and test fixtures

**Testing**: JUnit 5, deterministic golden-output comparisons, CLI smoke tests

**Target Platform**: JVM 21 on developer machines and CI runners

**Project Type**: code generator / compiler-style tooling module

**Performance Goals**: Parse and emit `spec/base/common.md` in under one second
on a normal developer machine; repeated runs with unchanged input must produce
byte-identical output

**Constraints**: Respect `spec/` as source of truth, support only
`spec/base/common.md` in this feature, perform no silent omission of source
members or annotations, and keep the implementation self-contained without any
network dependency

**Scale/Scope**: One namespace (`common`), one abstraction (`Page<$$T>`), one
target language (Java), one generation module

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Source-of-truth impact is identified across `spec/`, `guidelines/`,
      `docs/adr/`, `missing-features.md`, `TCK.md`, and any other touched
      documentation.
      Notes: input is `spec/base/common.md`; design rules come from
      `guidelines/api-guideline.md` and `guidelines/api-best-practices-java.md`.
- [x] Shared-layer changes remain broader than Hedera or are explicitly
      justified as ecosystem-specific.
      Notes: the `common` namespace is Hiero-wide and network-neutral.
- [x] Cross-language semantic impact is documented, including affected
      `guidelines/api-best-practices-*.md` files.
      Notes: the feature emits Java only, but the intermediate representation
      is language-neutral and keeps future languages open.
- [x] `## Testing` / `solo` / TCK impact is captured for each touched spec, or
      an explicit exemption is documented for pure data or editorial changes.
      Notes: this slice does not alter network behavior, so verification is
      deterministic code-generation output rather than `solo` or TCK changes.
- [x] Open questions, ADR needs, and follow-up artifacts are listed before
      implementation begins.
      Notes: no ADR is required in this slice; error emission and output layout
      decisions are resolved in `research.md`.

## Project Structure

### Documentation (this feature)

```text
specs/001-java-common-codegen/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── java-common-codegen-cli.md
└── tasks.md
```

### Repository Structure (repository root)

```text
guidelines/
├── api-guideline.md
├── testing-guideline.md
├── api-best-practices-java.md
├── api-best-practices-js.md
├── api-best-practices-rust.md
├── java-files/
└── js-files/

spec/
├── base/
│   └── common.md
├── consensus-node-client/
├── consensus-node-admin-client/
├── mirror-node-client/
├── enterprise/
└── dependencies.md

tools/
└── spec-codegen-java/
    ├── pom.xml
    ├── src/main/antlr4/
    ├── src/main/java/
    ├── src/test/java/
    └── src/test/resources/goldens/

docs/
└── adr/
```

**Structure Decision**: Introduce a single scoped generator module under
`tools/spec-codegen-java/`. This keeps the repo's authoritative specifications
in place while isolating the new build system and generator runtime from the
documentation-first root.

## Complexity Tracking

No constitution violations are anticipated. The new Java tooling module is an
intentional, explicitly scoped exception to the repository's no-build-system
default because this feature's goal is to prove code generation from the specs.
