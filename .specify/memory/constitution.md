<!--
Sync Impact Report
- Version change: template -> 1.0.0
- Modified principles:
  - Template Principle 1 -> I. Spec-First Source of Truth
  - Template Principle 2 -> II. Cross-Language and Network-Neutral Design
  - Template Principle 3 -> III. Idiomatic Mappings, Shared Semantics
  - Template Principle 4 -> IV. Behavioral Verification with Real Networks
  - Template Principle 5 -> V. Explicit Open Questions and Decision Traceability
- Added sections:
  - Repository Constraints
  - Workflow & Quality Gates
- Removed sections:
  - None
- Templates requiring updates:
  - ✅ /Users/housniabdellatif/hiero-sdk-v3-prototyping/.specify/templates/plan-template.md
  - ✅ /Users/housniabdellatif/hiero-sdk-v3-prototyping/.specify/templates/spec-template.md
  - ✅ /Users/housniabdellatif/hiero-sdk-v3-prototyping/.specify/templates/tasks-template.md
- Follow-up TODOs:
  - None
-->
# Hiero SDK V3 Prototyping Constitution

## Core Principles

### I. Spec-First Source of Truth
All public API work MUST begin in `spec/` using the meta-language defined in
`guidelines/api-guideline.md`. Illustrative code under `guidelines/java-files/`
and `guidelines/js-files/` is explanatory only and MUST never outrank the
specification. If a change affects public API shape, naming, imports,
annotations, namespace boundaries, or lifecycle semantics, the authoritative
spec files MUST be updated in the same work item before downstream guidance,
examples, or generator logic.

Rationale: this repository exists to define V3 once and translate it many
times; drift between prose, schema, and examples destroys that value.

### II. Cross-Language and Network-Neutral Design
The specification MUST describe the Hiero ecosystem, not a Hedera-only SDK.
Shared layers MUST prefer network-neutral abstractions such as `nativeToken`,
`ledger`, `authority`, and extensible status or result types. Any
Hedera-specific surface that appears in shared namespaces MUST be explicitly
justified. Every API change MUST preserve semantic parity across the target
languages even when concrete mappings differ idiomatically.

Rationale: V3 is intended for any Hiero-based network and for seven language
bindings; portability is a core project promise, not a later cleanup task.

### III. Idiomatic Mappings, Shared Semantics
The meta-language defines shared concepts; the per-language guides in
`guidelines/api-best-practices-*.md` define idiomatic mappings. Changes to the
meta-language or to shared semantics MUST identify the affected language guides
and update them whenever the mapping changes materially. Language-specific
convenience MAY vary, but it MUST NOT change the underlying behavior, error
model, or transaction lifecycle defined by the specification.

Rationale: the repo should enable consistent SDKs without forcing every
language into the idioms of another one.

### IV. Behavioral Verification with Real Networks
Every spec that defines observable behavior MUST include a `## Testing` section
that follows `guidelines/testing-guideline.md`. Test scenarios MUST be
language-agnostic, independently executable, and expressed in terms of public
behavior. Network behavior MUST be verified against a real Hiero network via
`solo` or another explicitly documented integration, and consensus, mirror, and
enterprise surfaces SHOULD record TCK impact when relevant. A spec that is pure
data MAY omit executable scenarios only if it explicitly states why.

Rationale: a V3 spec is incomplete until an implementation can prove it behaves
correctly on a real network.

### V. Explicit Open Questions and Decision Traceability
Open design questions MUST remain visible in each file's `## Questions &
Comments` section or in `missing-features.md`; they MUST NOT be silently
"resolved" in code or prose without rationale. Architecturally significant
decisions MUST be recorded as ADRs under `docs/adr/`. When a change introduces
a new invariant, narrows a contract, or removes a design path, the work item
MUST document the reason, affected namespaces, and any follow-up required for
TCK, language guides, or downstream generators.

Rationale: V3 is intentionally exploratory, so unresolved questions and major
decisions need durable, auditable context.

## Repository Constraints

- Repository language MUST be English for all README, guidelines, specs, ADRs,
  feature specs, and planning artifacts.
- Every `spec/*.md` file MUST keep the standard section order: `## Description`,
  `## API Schema`, optional `## Examples`, `## Testing`, `## Questions &
  Comments`.
- Specs MUST use explicit `requires {Type} from namespace` imports, simple type
  references after import, project naming conventions, `@@` annotations, and
  `$$`-prefixed generic parameters exactly as described in
  `guidelines/api-guideline.md`.
- Collections MUST NOT be nullable. Fields SHOULD be immutable unless
  mutability is explicitly justified. `ANY` MUST NOT be introduced as a
  standalone escape hatch when a concrete type, constrained generic, union, or
  bytes-with-schema would do.
- This repository is a specification workspace, not a shippable SDK. New build
  systems, runtime modules, or generated code directories MUST be explicitly
  scoped and justified before they are added.
- When a change adds or materially revises a cross-cutting concept, the author
  MUST review `guidelines/testing-guideline.md`, relevant
  `guidelines/api-best-practices-*.md`, `spec/dependencies.md`,
  `missing-features.md`, and `TCK.md` for required follow-up edits.

## Workflow & Quality Gates

- Start every non-editorial change by identifying the affected namespaces,
  guidelines, ADRs, and verification artifacts.
- Plans produced from Spec Kit MUST pass a constitution check before
  implementation work begins. At minimum the plan MUST show source-of-truth
  files, network-neutrality impact, cross-language impact, testing impact, and
  open-question or ADR handling.
- A feature is not complete until the authoritative spec, any impacted guidance
  documents, and required testing or backlog documents are in sync.
- Pure wording cleanups MAY skip ADR, TCK, or backlog updates only when they do
  not alter behavior, invariants, or mappings; the absence of downstream
  changes MUST be an explicit decision, not an omission.
- Reviews MUST reject changes that move faster than the source of truth, hide
  unresolved design questions, or add ecosystem-specific assumptions to shared
  layers without justification.

## Governance

This constitution governs the working rules for `.specify/` artifacts and
project planning in this repository. It complements, but does not override,
direct repository instructions in `README.md`, `CLAUDE.md`, and contributor
guidance.

Amendment rules:

- Any amendment MUST update `.specify/memory/constitution.md` and any affected
  Spec Kit templates in the same change.
- Semantic versioning applies to this constitution: MAJOR for incompatible
  governance changes, MINOR for new principles or materially expanded
  obligations, PATCH for clarifications that do not change obligations.
- The Sync Impact Report at the top of this file MUST be refreshed whenever the
  constitution changes.
- Constitution compliance MUST be checked during planning and review for every
  non-trivial feature or architecture change.

**Version**: 1.0.0 | **Ratified**: 2026-07-27 | **Last Amended**: 2026-07-27
