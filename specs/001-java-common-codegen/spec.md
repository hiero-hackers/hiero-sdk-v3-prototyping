# Feature Specification: Java Common Code Generation

**Feature Branch**: `001-java-common-codegen`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "Generate Java code from `spec/base/common.md` as the first code generation slice for `spec/base`."

## Affected Specifications *(mandatory)*

- **Primary spec files**: `spec/base/common.md`
- **Guidelines**: `guidelines/api-guideline.md`, `guidelines/api-best-practices-java.md`
- **Traceability docs**: `None`

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Generate the First Java Slice (Priority: P1)

As a spec maintainer, I want to generate Java API source files from
`spec/base/common.md` so that we can prove the V3 specification is concrete
enough to drive code generation.

**Why this priority**: This is the smallest end-to-end slice that proves the
repo can move from written specification to generated language output without
starting with the most complex base namespaces.

**Independent Test**: Run the generation flow against `spec/base/common.md` and
confirm that it produces Java source files representing every public type and
member declared in that file.

**Acceptance Scenarios**:

1. **Given** `spec/base/common.md` declares the `common` namespace and the
   abstract `Page<$$T>` type, **When** Java generation is run for this feature,
   **Then** Java source files are produced for that namespace and type without
   requiring manual editing of the spec.
2. **Given** the input spec is unchanged, **When** generation is run twice,
   **Then** both runs produce the same Java output.

---

### User Story 2 - Preserve Spec Semantics in Java Output (Priority: P2)

As a Java SDK implementer, I want the generated Java output to preserve the
meaning of the spec so that it can serve as a trustworthy starting point for
manual implementation and later automation.

**Why this priority**: A generated file is only useful if reviewers can trust
that the type names, members, and declared semantics still reflect the source
specification.

**Independent Test**: Compare the generated Java output with
`spec/base/common.md` and confirm that each public field and method in the spec
is represented in the generated output with matching names and compatible
semantics.

**Acceptance Scenarios**:

1. **Given** the source spec contains generic parameters, immutable fields, and
   async methods, **When** Java output is reviewed, **Then** those concepts are
   represented consistently and no public member from the source spec is
   silently dropped.
2. **Given** a reviewer inspects a generated Java member, **When** they trace
   it back to the spec, **Then** they can identify the originating declaration
   in `spec/base/common.md`.

---

### User Story 3 - Detect Generator Drift Early (Priority: P3)

As a generator maintainer, I want a stable expected-output check for this first
Java slice so that intentional and accidental output changes are visible before
we expand code generation to the rest of `spec/base`.

**Why this priority**: Drift detection keeps the first slice trustworthy and
reduces the risk of scaling broken generation behavior to more namespaces.

**Independent Test**: Change the generator or the source spec, run verification,
and confirm that output differences are clearly reported against the approved
expected output.

**Acceptance Scenarios**:

1. **Given** the approved expected Java output exists, **When** generated output
   changes, **Then** verification reports the difference instead of silently
   accepting it.

---

### Edge Cases

- What happens when the scoped spec file uses annotations whose Java mapping is
  not yet finalized?
- How does the generation flow handle a future change to `spec/base/common.md`
  that introduces a construct outside the supported first-slice scope?

## Specification Impact *(mandatory)*

### Touched Namespaces / Files

- **`spec/base/common.md`**: First authoritative input for Java generation.
- **`guidelines/api-guideline.md`**: Source of meta-language syntax and schema
  interpretation rules for the generation slice.
- **`guidelines/api-best-practices-java.md`**: Reference for idiomatic Java
  mapping decisions.

### Cross-Language Impact

- **Affected language guides**: `guidelines/api-best-practices-java.md`
- **Behavioral parity risk**: The Java slice must not reinterpret `common`
  semantics in a way that would make future language generators disagree on the
  meaning of the same source spec.

### Testing & Verification Impact

- **Spec testing updates**: No direct changes to `## Testing` sections are
  required for this initial code-generation slice.
- **Network/TCK impact**: No direct `solo` or TCK change is required because
  `spec/base/common.md` defines shared types rather than network behavior; this
  feature instead requires deterministic generated-output verification.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST treat `spec/base/common.md` as the sole
  authoritative source for this first Java generation slice.
- **FR-002**: The system MUST generate Java API source files for every public
  type declared in `spec/base/common.md`.
- **FR-003**: The system MUST preserve declared names, generic parameters,
  fields, methods, and relevant annotations from the source spec in a normalized
  model before Java files are emitted.
- **FR-004**: The system MUST produce deterministic Java output for unchanged
  source input.
- **FR-005**: The system MUST provide an approval-style verification artifact so
  maintainers can compare generated Java output against an expected baseline.
- **FR-006**: The system MUST report unsupported or out-of-scope constructs
  encountered in `spec/base/common.md` explicitly rather than silently omitting
  them.
- **FR-007**: The system MUST limit this feature's supported input scope to
  `spec/base/common.md` and MUST NOT claim full support for all `spec/base`
  files as part of this feature.

### Key Entities *(include if feature involves data)*

- **Source Schema Block**: The fenced `## API Schema` block in
  `spec/base/common.md` that defines the authoritative input.
- **Normalized Spec Model**: A structured representation of the source schema
  used to preserve meaning before Java emission.
- **Generated Java API**: The Java source files produced from the normalized
  model for the `common` namespace.
- **Expected Output Baseline**: The approved Java output used to detect drift in
  later runs.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A maintainer can generate Java output for `spec/base/common.md`
  from a clean checkout without manually editing the source spec.
- **SC-002**: Two consecutive generation runs against unchanged input produce
  byte-identical Java output.
- **SC-003**: Reviewers can trace every generated public Java member back to a
  declaration in `spec/base/common.md`.
- **SC-004**: Output verification reports any difference between newly
  generated Java files and the approved expected output.

## Assumptions

- The first code-generation milestone is intentionally limited to
  `spec/base/common.md`, not the full `spec/base` directory.
- This feature defines generated Java API output, not a complete production
  runtime implementation of every generated method.
- Decisions such as exact output directory structure and detailed Java type
  mappings can be finalized during planning as long as they do not violate the
  source semantics in this specification.
- Future `spec/base` namespaces will be added through later features after this
  first slice proves the end-to-end flow.

## Open Questions & ADRs

- **Open questions to preserve**: None
- **ADR required?**: No
