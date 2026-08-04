# Feature Specification: Java Base Public API Prototype

**Feature Branch**: `feature/sdd-java-github-spec-kit`

**Created**: 2026-08-04

**Status**: Approved for planning on 2026-08-04

**Input**: Generate a compile-only Java public API prototype for every language-neutral contract
under `spec/base`, without providing operational SDK implementations.

## Contract & Classification *(mandatory)*

**Authoritative Sources**:

- `spec/base/authority.md`
- `spec/base/common.md`
- `spec/base/grpc.md`
- `spec/base/hedera.md`
- `spec/base/keys.md`
- `spec/base/ledger-config.md`
- `spec/base/ledger.md`
- `spec/base/native-token.md`
- `spec/base/proto.md`
- `spec/base/solo.md`
- `spec/base/token.md`
- `guidelines/api-guideline.md` for language-neutral semantics
- `guidelines/api-best-practices-java.md` for Java mapping constraints
- `docs/adr/0004-authority-authorization-sum-type.md` for the Authority design

**Compatibility Classification**: Additive. This feature establishes the initial Java V3 base API
prototype and does not promise compatibility with V2. After this baseline is approved, subsequent
changes must be classified against it for source and binary compatibility.

**Security Classification**: Security-sensitive. The public surface includes private and public key
material, signing and verification contracts, authorization trees, key import and export formats,
and representations that must not leak secrets. This feature defines those contracts but does not
implement cryptography, signing, verification, parsing, or serialization.

### Requirement Traceability

| Requirement | Authoritative Source | Required Behavior | Verification |
|-------------|----------------------|-------------------|--------------|
| FR-001 | All `spec/base/*.md` files | Cover every base namespace and declaration | Source-to-API inventory audit |
| FR-002 | `guidelines/api-guideline.md` | Preserve namespace dependencies and language-neutral semantics | Dependency and API-shape tests |
| FR-003 | All base API schemas | Preserve abstractions, values, enums, constants, generics, inheritance, and constraints | API-shape and compilation tests |
| FR-004 | All base schemas; constitution VI | Preserve immutability, nullability, collection, and ownership semantics | Reflection and contract tests |
| FR-005 | `spec/base/common.md` | Expose pagination state and asynchronous navigation contracts | Consumer compilation tests |
| FR-006 | `spec/base/grpc.md`; `spec/base/proto.md` | Expose protocol integration placeholders without transport behavior | Package and API-shape tests |
| FR-007 | `spec/base/native-token.md`; `spec/base/hedera.md`; `spec/base/token.md` | Expose native-token, HBAR, and token classification contracts | Consumer compilation tests |
| FR-008 | `spec/base/ledger.md` | Expose networks, identifiers, address variants, nodes, constants, and factory contracts | API-shape and invariant tests |
| FR-009 | `spec/base/ledger-config.md`; `spec/base/hedera.md`; `spec/base/solo.md` | Expose network configuration and built-in network profile contracts | Consumer compilation tests |
| FR-010 | `spec/base/keys.md` | Expose key material, algorithms, formats, and operation contracts without cryptographic behavior | Security review and API-shape tests |
| FR-011 | `spec/base/authority.md`; ADR-0004 | Expose the closed recursive authorization model and invariants | Exhaustiveness and invariant tests |
| FR-012 | User scope; constitution II and VI | Limit output to public contracts and structural value types without changing cross-language behavior | Java API review and compile fixtures |
| FR-013 | User scope; constitution VII | Exclude operational implementation and fake placeholder behavior | Dependency, bytecode, and behavior audit |
| FR-014 | Constitution III | Prevent secret disclosure and private-key use in authorization structures | Security API review and negative tests |
| FR-015 | Constitution VII | Make generation deterministic and traceable | Clean-regeneration comparison |
| FR-016 | Repository documentation conventions | Document every public element and unresolved source question | Documentation audit |
| FR-017 | Constitution V | Map every normative requirement to automated verification | Requirement-to-test traceability audit |

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Implement Against Stable Base Contracts (Priority: P1)

As a Java SDK implementation author, I can compile implementation code against one coherent base
API artifact containing all foundational V3 contracts, without depending on implementation classes.

**Why this priority**: Every later consensus-node, mirror-node, and enterprise Java component
depends on these shared contracts.

**Independent Test**: Compile a consumer fixture that imports representative types from every base
namespace and implements or consumes the exposed contracts without loading a runtime provider.

**Acceptance Scenarios**:

1. **Given** the approved base specifications, **When** the public API artifact is produced, **Then**
   every declared namespace and public element is mapped or explicitly deferred with approval.
2. **Given** only the public API artifact, **When** an SDK component compiles against it, **Then** no
   implementation artifact or internal package is required at compile time.
3. **Given** a public signature, **When** its referenced types are inspected, **Then** it exposes no
   internal implementation type or unapproved third-party type.

---

### User Story 2 - Use Ledger and Token Domain Types (Priority: P1)

As a Java SDK consumer, I can write type-safe code for networks, addresses, accounts, contracts,
transactions, native-token amounts, HBAR units, token classifications, nodes, and network settings.

**Why this priority**: These types form the vocabulary used by all higher-level APIs.

**Independent Test**: Compile consumer fixtures covering every ledger identifier variant, token
classification, network profile, node descriptor, and configuration lookup contract.

**Acceptance Scenarios**:

1. **Given** numeric, EVM-address, and key-alias identifier forms, **When** consumers use the public
   contracts, **Then** the permitted alternatives and required fields are distinguishable.
2. **Given** Hiero, Hedera, and Solo network concepts, **When** consumers declare configuration and
   native-token values, **Then** generic relationships and named constants match the source specs.
3. **Given** an invalid combination prohibited by `@@oneOf`, length, threshold, or nullability
   constraints, **When** a contract test models that combination, **Then** the public contract marks
   it as invalid without inventing a new semantic rule.

---

### User Story 3 - Integrate Key and Authority Contracts Safely (Priority: P1)

As a security-sensitive SDK component author, I can compile against key, key-format, signing,
verification, and recursive authority contracts while cryptographic execution remains outside the
API prototype.

**Why this priority**: Key and authorization mistakes have direct security consequences and must be
correct before transaction APIs depend on them.

**Independent Test**: Compile positive fixtures for each key and authority variant and negative
fixtures proving that private keys cannot be used as authority leaves and invalid authority-list
shapes are rejected by the declared contract.

**Acceptance Scenarios**:

1. **Given** a public key, contract identifier, or nested threshold structure, **When** an authority
   is declared, **Then** only the variants permitted by `authority.md` are representable.
2. **Given** private-key material, **When** a consumer attempts to use it as an authority, **Then**
   the public type system provides no such path.
3. **Given** key operation contracts, **When** the API artifact is inspected, **Then** it contains no
   cryptographic algorithm implementation, provider selection, or sensitive diagnostic output.

---

### User Story 4 - Use Shared Pagination and Protocol Contracts (Priority: P2)

As a client or transport implementation author, I can compile against pagination, method
descriptor, and protocol namespace contracts without receiving a bundled transport implementation.

**Why this priority**: These contracts are shared integration points, but they do not block the
core ledger and security vocabulary.

**Independent Test**: Compile fixtures for asynchronous page navigation and method descriptors,
then verify that no network request, scheduler, transport, or protocol codec is included.

**Acceptance Scenarios**:

1. **Given** a page contract, **When** a client implementation provides first-page and next-page
   behavior, **Then** page metadata, asynchronous results, and declared errors remain observable.
2. **Given** a method descriptor, **When** a transport implementation consumes it, **Then** service
   and method names are available without a concrete gRPC dependency in the public signature.
3. **Given** the empty `proto` namespace, **When** the artifact is inspected, **Then** its reserved
   role is documented without fabricating public types.

### Edge Cases

- A source namespace is empty, as `proto` currently is; it must remain documented without invented
  types.
- A source declaration includes a static factory or registry operation that Java cannot express as
  an abstract static member; the public contract must remain callable without adding fake runtime
  bodies that only fail.
- A source enum carries fields or operation contracts; only structural metadata required by the
  public contract may exist in this feature, while operational decoding remains deferred.
- A type is immutable but contains bytes or collections; the public contract must prevent mutable
  state from being exposed through ownership leaks.
- A source question remains unresolved, such as exchange-rate precision; the Java API must follow
  the approved schema as written and retain the question rather than choosing a new type silently.
- A source constraint has no direct Java type-system representation; the constraint must be
  documented and made testable without weakening it silently.
- A key or authority value is rendered for diagnostics; private or sensitive bytes must not be
  disclosed.
- A source operation describes parsing, signing, verification, conversion, registration, or network
  access; its contract belongs to this feature, but its operational implementation does not.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The prototype MUST cover all eleven Markdown contracts under `spec/base` as one
  coherent Java public API surface.
- **FR-002**: The prototype MUST preserve all source namespace boundaries, imports, and dependency
  directions, including the dependency chain among native token, ledger, keys, authority, network
  configuration, Hedera, and Solo contracts.
- **FR-003**: Every declared abstraction, concrete value, enum, constant, field, operation,
  annotation constraint, generic bound, inheritance relationship, and factory contract MUST be
  represented or explicitly listed as deferred with human approval.
- **FR-004**: The public contract MUST preserve source immutability, nullability, non-null collection,
  one-of, length, range, override-narrowing, final-type, sealed-variant, asynchronous, and declared
  error semantics.
- **FR-005**: The common API MUST expose page data and metadata, first-page and next-page state, and
  asynchronous navigation contracts with their declared terminal error semantics.
- **FR-006**: The protocol base MUST expose `MethodDescriptor` semantics and reserve the `proto`
  namespace without adding concrete transport, protobuf, or codec behavior.
- **FR-007**: The token APIs MUST expose native-token units and amounts, exchange rates, HBAR units
  and amounts, Hedera identifiers, and HTS token type and supply classifications exactly as defined
  by their source contracts.
- **FR-008**: The ledger API MUST expose network identity, the complete address hierarchy, EVM and IP
  values, account and contract alternatives, transaction identifiers, consensus and mirror nodes,
  clear-sentinel constants, and all declared creation and parsing operation contracts.
- **FR-009**: The configuration APIs MUST expose network settings, registration and lookup contracts,
  Hedera mainnet and testnet profiles, and the Solo profile without bundling a registry or runtime
  provider.
- **FR-010**: The keys API MUST expose key types, algorithms, public and private key contracts, key
  pairs, import and export formats, generation and creation contracts, and sign and verify contracts
  without implementing cryptography, encoding, decoding, or provider selection.
- **FR-011**: The authority API MUST expose a closed recursive sum type containing public-key,
  contract, and threshold-list variants; MUST preserve the non-empty and threshold invariants; and
  MUST provide no authority variant for private keys.
- **FR-012**: Java consumers MUST see an idiomatic, minimal, strongly typed surface limited to
  public contracts, immutable data carriers, enums, constants, exception contracts, annotations,
  and packaging metadata. Its language-specific form MUST NOT alter observable source semantics.
- **FR-013**: The artifact MUST contain no network calls, storage, retries, scheduling, transport,
  cryptographic execution, serialization, parsing, registry state, service implementation, or other
  operational SDK behavior. It MUST NOT satisfy operation contracts with fake methods that only
  throw unsupported-operation failures.
- **FR-014**: Public signatures MUST expose only Java platform types and approved Hiero API types.
  Internal implementation types and unapproved third-party types MUST NOT leak into the API.
- **FR-015**: Generated output MUST be reproducible from an exact source revision and generation
  configuration, and regeneration MUST produce no unexplained differences.
- **FR-016**: Every public type, member, constraint, error, deferred behavior, and source-level open
  question MUST have consumer-facing documentation traceable to its source.
- **FR-017**: Every normative requirement MUST map to automated API-shape, compilation, reflection,
  module-boundary, negative-compilation, or clean-regeneration verification, plus human security
  review where required.

### Key Entities

- **Common contracts**: Generic page data, metadata, navigation state, asynchronous navigation, and
  mirror-node error semantics.
- **Protocol contracts**: Method descriptors and the reserved base protocol namespace.
- **Native-token contracts**: Units, typed amounts, conversion operations, base-unit totals, and
  exchange-rate values.
- **Ledger contracts**: Networks, base and specialized addresses, EVM addresses, account and
  contract identifiers, transaction identifiers, IP addresses, node descriptors, and sentinel
  constants.
- **Configuration contracts**: Network settings, configuration discovery, Hedera profiles, HBAR,
  and Solo profiles.
- **Token contracts**: Token kind and supply-policy classifications.
- **Key contracts**: Public and private key material, key pairs, algorithms, containers, encodings,
  formats, and cryptographic operation boundaries.
- **Authority contracts**: Public-key leaves, contract leaves, and recursive threshold lists.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An inventory audit accounts for 100% of public declarations and constraints in all
  eleven `spec/base` files, with zero silent omissions.
- **SC-002**: At least one consumer fixture for each base namespace compiles using only the public
  API artifact and the Java platform.
- **SC-003**: Public-signature analysis reports zero references to internal implementation packages
  and zero unapproved third-party types.
- **SC-004**: Negative contract fixtures cover 100% of one-of, sealed-variant, non-empty,
  threshold, length, nullability-narrowing, and private-key-exclusion constraints that can be
  verified at compile time or through structural inspection.
- **SC-005**: Requirement traceability maps 100% of FR-001 through FR-017 to at least one automated
  verification or required human review.
- **SC-006**: Two consecutive generations from the same inputs produce byte-for-byte identical
  source output and no unexplained repository difference.
- **SC-007**: An operational-scope audit finds zero network, storage, retry, scheduler, transport,
  cryptographic, parsing, serialization, or registry implementations in the delivered artifact.
- **SC-008**: Human API and security reviewers approve all key, authority, secret-handling,
  compatibility, and deferred-behavior decisions before the prototype is accepted.

## Assumptions

- The API schema blocks in `spec/base` are authoritative when prose questions discuss alternatives;
  unresolved questions remain documented and do not silently change the generated Java surface.
- The prototype is allowed to contain only structural behavior required for a valid immutable API
  value, such as accessors, defensive ownership, equality, hashing, enum metadata, constants, and
  invariant enforcement. Operational behavior remains out of scope.
- Java construct selection, package names, module names, build tooling, Java baseline, nullness
  library, and code-generation architecture are implementation-plan decisions governed by
  `guidelines/api-best-practices-java.md`.
- Factory, parser, registry, signing, verification, conversion, and decoding operations remain part
  of the public contract even though their implementations are delivered by later features.
- V2 compatibility is not required. The approved output of this feature becomes the compatibility
  baseline for later V3 Java API work.

## Conformance Strategy *(mandatory)*

- **Unit verification**: Structural invariants and safe ownership behavior permitted in the API-only
  artifact.
- **Integration verification**: Consumer compilation fixtures spanning dependent base namespaces;
  no runtime provider integration is required.
- **TCK verification**: API inventory, signatures, generic relationships, inheritance, constants,
  constraints, and error declarations. Runtime behavioral scenarios are deferred to implementation
  features and must be linked back to these requirement IDs.
- **Golden vectors**: Not required for this API-only feature because parsing, serialization,
  hashing, signing, and conversion behavior is excluded. Later operational features must supply
  vectors before implementing those behaviors.
- **Human reviews**: Java public API design, source and binary compatibility baseline, key and
  authority security surface, secret-handling documentation, and every explicitly deferred source
  declaration.
