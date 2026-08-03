<!--
Sync Impact Report
- Version change: unratified template -> 1.0.0
- Added principles:
  - I. Specifications Are Authoritative
  - II. Cross-Language Semantic Parity
  - III. Security by Construction
  - IV. Deterministic Protocol Behavior
  - V. Executable Conformance
  - VI. Explicit API Semantics
  - VII. Reproducible Generation
  - VIII. AI Under Human Authority
- Added sections:
  - Engineering Constraints
  - Development Workflow
- Removed sections: None
- Templates:
  - UPDATED: .specify/templates/plan-template.md
  - UPDATED: .specify/templates/spec-template.md
  - UPDATED: .specify/templates/tasks-template.md
  - NO CHANGE: .specify/templates/checklist-template.md
  - NO CHANGE: .specify/templates/constitution-template.md
- Runtime guidance:
  - REVIEWED, NO CHANGE: README.md
  - REVIEWED, NO CHANGE: CLAUDE.md
- Codex Spec Kit skills: REVIEWED, NO CHANGE
- Follow-up TODOs: None
-->

# Hiero SDK V3 Java Constitution

## Core Principles

### I. Specifications Are Authoritative

Approved language-neutral contracts under `spec/` define required SDK behavior. Java
implementations MUST conform to those contracts and MUST NOT invent, omit, or silently alter
protocol semantics. Every normative feature requirement MUST have a stable identifier and MUST
trace to its authoritative source. When code, tests, documentation, and an approved specification
conflict, the approved specification governs until it is amended through review.

### II. Cross-Language Semantic Parity

The Java SDK MUST preserve the observable behavior shared by all conforming V3 SDKs while exposing
an idiomatic Java API. Java-specific conveniences MAY be added only when they do not alter protocol
behavior or impose Java-specific requirements on the language-neutral contract. Any intentional
language variance MUST be documented and approved in the feature specification or implementation
plan.

### III. Security by Construction

Cryptographic primitives MUST use reviewed libraries. Custom cryptographic implementations are
prohibited unless explicitly approved through dedicated security review. Private keys, secrets,
credentials, and sensitive signing material MUST NOT appear in logs, prompts, snapshots, fixtures,
or generated artifacts. Signing, serialization, key handling, payments, deserialization, and
network-trust changes MUST include threat analysis, negative tests, and accountable human review.

### IV. Deterministic Protocol Behavior

Serialization, hashing, signing, checksums, transaction identifiers, and protocol conversion MUST
be deterministic wherever the language-neutral contract requires a canonical result. Sensitive
transformations MUST be verified with approved golden vectors. Equivalent valid inputs MUST produce
equivalent protocol outputs across conforming SDK implementations.

### V. Executable Conformance

Every normative feature requirement MUST map to at least one automated conformance test. A feature
is incomplete until its required unit, integration, TCK, and golden-vector tests pass. Tests MUST
cover success, failure, boundary, cancellation, concurrency, and compatibility behavior when those
dimensions apply. Test omissions MUST be justified and approved before implementation begins.

### VI. Explicit API Semantics

Every public API MUST define applicable nullability, mutability, ownership, error, side-effect,
thread-safety, cancellation, timeout, retry, and idempotency semantics. Collections MUST represent
absence with an empty collection rather than `null`. Asynchronous APIs MUST NOT silently block.
Errors MUST preserve actionable context without exposing sensitive information.

### VII. Reproducible Generation

Generated Java code MUST be deterministic and traceable to an exact specification revision,
generator version, and generation configuration. Generated files MUST be identifiable and MUST NOT
be edited manually. Changes to generated APIs MUST originate in an approved specification or
generator change. Regeneration MUST produce no unexplained repository differences.

### VIII. AI Under Human Authority

AI agents MAY draft specifications, plans, code, tests, and review findings. Agent-produced changes
MUST identify the approved requirements they address. Agents MUST NOT invent protocol behavior,
weaken validation, expose sensitive material, or approve their own work. Security-sensitive,
wire-sensitive, and compatibility-sensitive changes require accountable human approval.

## Engineering Constraints

The language-neutral API specification MUST remain separate from Java implementation decisions.
Feature specifications define required behavior and acceptance criteria; implementation plans define
Java versions, packages, modules, dependencies, type mappings, build tools, and generated-code
boundaries.

Public API changes MUST be classified as additive, behavioral, source-breaking, binary-breaking,
wire-breaking, or security-sensitive. Dependencies used in public or sensitive paths MUST be
necessary, maintained, license-compatible, and reviewed. Implementations MUST favor immutability,
explicit types, minimal public surface area, and established repository patterns. New abstractions
MUST represent a specified domain concept or remove demonstrated complexity.

The terms MUST, MUST NOT, SHOULD, SHOULD NOT, and MAY are normative. A SHOULD deviation requires a
documented rationale in the feature specification or implementation plan.

## Development Workflow

Every significant Java implementation feature MUST proceed through these gates:

1. Create a bounded feature specification with authoritative-source links, stable requirement IDs,
   acceptance scenarios, compatibility classification, and security classification.
2. Resolve material ambiguities and obtain human approval before planning security-sensitive work.
3. Produce an implementation plan that passes every Constitution Check before research and again
   after design.
4. Produce requirement-linked tasks, including mandatory tests and reviews before implementation.
5. Run cross-artifact analysis and resolve all critical constitution conflicts.
6. Implement without changing the approved contract implicitly.
7. Pass unit, integration, TCK, golden-vector, compatibility, and security checks that apply.
8. Obtain required human review before merge.

Small changes MAY omit artifacts that are demonstrably irrelevant, but every omission MUST be
explicitly justified in the plan. Specification changes and implementation changes SHOULD be
reviewed separately when combining them would obscure protocol or compatibility impact.

## Governance

This constitution supersedes conflicting implementation plans, task lists, agent instructions, and
local conventions. Amendments require a dedicated pull request containing rationale, impact
analysis, affected templates, and any migration plan. Every pull request MUST demonstrate
constitution compliance, and reviewers MUST reject unexplained violations.

Constitution versions follow semantic versioning: MAJOR for removal or incompatible redefinition of
a principle, MINOR for a new principle or materially expanded obligation, and PATCH for a
non-semantic clarification. Exceptions MUST be explicit, approved by accountable maintainers,
documented with scope and rationale, and time-bounded with a removal condition.

**Version**: 1.0.0 | **Ratified**: 2026-08-03 | **Last Amended**: 2026-08-03
