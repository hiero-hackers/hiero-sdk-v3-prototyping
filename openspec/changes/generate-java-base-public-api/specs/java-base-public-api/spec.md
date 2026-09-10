## Purpose

Provide a buildable, implementation-independent Java contract for every language-neutral base API so downstream Java
SDK modules can compile against a stable public surface before operational implementations are introduced.

## ADDED Requirements

### Requirement: Complete base specification coverage
The Java public API SHALL account for every namespace, declaration, member, generic bound, inheritance
relationship, annotation constraint, constant, error, and namespace dependency defined by every Markdown file under
`spec/base`. An element that cannot be represented directly in Java MUST be reported as an explicit, reviewable mapping
or deferral and MUST NOT be silently omitted.

#### Scenario: Every source element is accounted for
- **WHEN** the Java API is reviewed against the current `spec/base` directory
- **THEN** a traceability inventory maps every source element to a Java API element or an explicit reviewed deferral

#### Scenario: Unsupported source construct is encountered
- **WHEN** a base specification contains syntax or semantics for which no approved Java mapping exists
- **THEN** the Java API change is rejected until a source-linked mapping decision is reviewed and recorded

### Requirement: Limited structural value implementation
The Java base artifact SHALL contain public contracts and immutable structural values. Executable code in a structural
value MUST be limited to establishing and preserving that value's validity, nullability, immutability, defensive
ownership, equality, hashing, and deterministic representation derived only from its fields. Such code MUST be
side-effect free and MUST NOT consult external state, providers, clocks, randomness, storage, or network resources.

#### Scenario: Structural value is created
- **WHEN** a caller constructs a public value with valid fields
- **THEN** the API may validate and defensively own those fields and provide stable value equality, hashing, and canonical field-derived representation

#### Scenario: Structural method depends on external state
- **WHEN** a proposed value method requires a provider, registry, clock, randomness, I/O, or mutable external state
- **THEN** that method remains a contract for a later implementation and is not implemented in the API artifact

### Requirement: Operational implementation is deferred
The Java base artifact MUST NOT provide networking, cryptography, signing, verification, parsing external
representations, serialization, persistence, mutable registry, retry, scheduling, transport, provider selection, or
other business and infrastructure implementations. Operations requiring such behavior SHALL remain contracts that a
later implementation or provider artifact can satisfy.

#### Scenario: Public API artifact is inspected
- **WHEN** the compiled base API artifact and bytecode are inspected
- **THEN** no operational SDK implementation, hidden service state, network call, cryptographic execution, provider selection, or placeholder method that only throws an unsupported-operation failure is present

#### Scenario: A later implementation consumes the API
- **WHEN** a separate implementation module supplies behavior for a declared operation
- **THEN** it can compile against the public API without modifying public contract sources or exposing implementation types to API consumers

### Requirement: Idiomatic Java contract mapping
The public surface SHALL preserve the observable semantics of the language-neutral API while using the mappings defined
by `guidelines/api-best-practices-java.md`. Immutable concrete data SHALL have value semantics, abstractions SHALL remain
implementation-independent contracts, closed variant sets SHALL support exhaustive handling where Java permits it, and
generic relationships SHALL retain their declared bounds.

#### Scenario: Java consumer uses representative base types
- **WHEN** consumer code compiles against representative types from every base namespace
- **THEN** the code can use their fields, operations, variants, constants, and generic relationships through idiomatic Java signatures without depending on tooling or implementation internals

#### Scenario: Language-neutral construct lacks a direct Java equivalent
- **WHEN** a source construct cannot be expressed exactly by the Java language
- **THEN** the selected mapping preserves its externally observable contract and is recorded in source-linked documentation and traceability metadata

### Requirement: Immutable and null-safe public values
The Java API SHALL preserve `@@immutable`, `@@nullable`, collection non-nullability, one-of, length, range,
threshold, final-type, and inherited-nullability-narrowing semantics. Byte arrays and collections exposed as structural
values MUST NOT allow callers to mutate stored state.

#### Scenario: Consumer supplies mutable data
- **WHEN** a caller changes an array or collection after using it to construct a public value
- **THEN** the public value's observable state remains unchanged

#### Scenario: Consumer attempts an invalid structural value
- **WHEN** a caller constructs a value that violates a declared nullability, one-of, size, range, or threshold invariant
- **THEN** the value is rejected at compile time where Java can express the rule or at its public construction boundary otherwise

### Requirement: Stable API and implementation boundaries
The Java base API SHALL be delivered as a named module whose exported packages correspond to the base namespaces. Public
signatures MUST reference only Java platform types, approved public annotations, and other types from the Java base API;
they MUST NOT expose test, implementation, transport, tooling, or unapproved third-party types.

#### Scenario: Module and signature boundaries are checked
- **WHEN** the module descriptor and all public signatures are analyzed
- **THEN** every intended API package is exported and no internal or unapproved dependency type crosses the public boundary

#### Scenario: Consumer compiles with API-only dependencies
- **WHEN** a downstream module compiles representative usage against the Java base artifact
- **THEN** no implementation artifact, maintenance tooling, or runtime provider is required on its compile path

### Requirement: Security-sensitive contracts remain declarative
Key and authority APIs SHALL expose only the material and operations declared by the source specifications. The public
API MUST structurally exclude private keys from authorization variants and MUST NOT include cryptographic providers,
secret-bearing diagnostics, test secrets, or executable cryptographic behavior.

#### Scenario: Private key is used as an authority
- **WHEN** consumer code attempts to construct an authorization requirement directly from a private key
- **THEN** no public type-safe construction path permits it

#### Scenario: Key API artifact is inspected
- **WHEN** key-related public signatures, bytecode, documentation, tests, and traceability metadata are inspected
- **THEN** no secret value, cryptographic implementation, or provider selection is present

### Requirement: Asynchronous contracts preserve completion and error semantics
Operations annotated `@@async` SHALL be represented as asynchronous Java contracts that preserve result types and
declared terminal error semantics without bundling executors, schedulers, transports, or request implementations.

#### Scenario: Consumer implements an asynchronous operation
- **WHEN** a later provider implements an `@@async` base operation
- **THEN** consumers observe asynchronous completion and the declared failure contract through the public signature without depending on a specific execution mechanism

### Requirement: Reviewed and traceable direct maintenance
The directly maintained Java API SHALL include a source-linked mapping matrix that records every source element's Java
representation, structural or operational classification, approved mapping variance, and deferred behavior. Public
signature snapshots and contract tests MUST make unreviewed API drift visible without requiring a schema parser.

#### Scenario: Base specification changes
- **WHEN** a declaration or constraint under `spec/base` changes
- **THEN** the corresponding Java source, mapping entry, and affected contract verification are reviewed together

#### Scenario: Public API changes unexpectedly
- **WHEN** a Java public signature differs from the reviewed signature snapshot
- **THEN** verification fails until the source mapping and compatibility impact are explicitly reviewed

### Requirement: Source questions remain unresolved
Open questions recorded in each source file's `Questions & Comments` section SHALL remain visible in API
documentation or traceability metadata. API maintenance MUST NOT silently choose a different type, invariant, name, or
behavior in response to an unresolved question.

#### Scenario: Source contains an unresolved question
- **WHEN** the associated public API is reviewed
- **THEN** the API documentation or mapping matrix retains the question and the API follows the source schema as written

### Requirement: Conformance is independently verifiable
The delivered public API SHALL include automated verification for source coverage, representative consumer compilation,
invalid construction attempts, public signatures, module boundaries, documentation, security exclusions, structural
implementation limits, and source traceability. Every normative requirement in this capability MUST map to at least one
verification.

#### Scenario: Conformance suite is executed
- **WHEN** the Java base API verification suite runs from a clean checkout
- **THEN** it reports whether every requirement is covered and whether the API artifact conforms without requiring an operational SDK implementation
