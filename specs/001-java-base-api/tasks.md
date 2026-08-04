# Tasks: Java Base Public API Prototype

**Input**: Design documents from `/specs/001-java-base-api/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, and
`quickstart.md`

**Tests**: Tests are mandatory for FR-001 through FR-017. Cryptographic and wire golden vectors are
not tasks in this feature because operational cryptography, parsing, serialization, and conversion
are explicitly excluded.

**Organization**: Tasks are grouped by user story. Generated Java files under
`java/hiero-sdk-base-api/src/main/java` must be changed only by the generator.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel because it changes different files and has no dependency on an
  incomplete task in the same phase
- **[Story]**: Maps the task to a user story from `spec.md`
- Every task names the file or directory it changes

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish a reproducible Java 21 Maven reactor without introducing SDK behavior.

- [ ] T001 Create the Java 21 parent reactor and pinned Maven wrapper in `pom.xml`, `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/maven-wrapper.properties`
- [ ] T002 [P] Create the generator module and pin its build plugins in `tools/spec-codegen-java/pom.xml`
- [ ] T003 [P] Create the generated API and contract-test modules with JSpecify 1.0.0 and pinned JUnit/build dependencies in `java/hiero-sdk-base-api/pom.xml` and `tests/java-base-api-contract/pom.xml`
- [ ] T004 [P] Define deterministic Java 21 package, input, output, encoding, and manifest settings in `codegen/java-base.yml`
- [ ] T005 Add Maven `target/` and generator staging exclusions without ignoring checked-in generated API sources in `.gitignore`

**Checkpoint**: `./mvnw --version` uses the pinned wrapper and the empty reactor validates with a
Java 21 release target.

---

## Phase 2: Foundational Generator (Blocking Prerequisites)

**Purpose**: Implement the typed, deterministic parser/validator/renderer infrastructure required
by every user story.

**CRITICAL**: No user-story generation task starts until this phase is complete.

### Tests for the Foundation

- [ ] T006 [P] Write fenced-schema extraction and repository-relative line diagnostic tests in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/parser/MarkdownSchemaExtractorTest.java`
- [ ] T007 [P] Write lexer/parser tests for imports, generics, inheritance, annotations, enums, constants, overloads, varargs, and comments in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/parser/SchemaParserTest.java`
- [ ] T008 [P] Write semantic validation tests for unresolved/unused imports, duplicate declarations, invalid bounds, unsupported syntax, and unmapped elements in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/validation/SchemaValidatorTest.java`
- [ ] T009 [P] Write deterministic ordering, UTF-8/LF, fixed-format, and no-host-metadata tests in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/render/JavaRendererDeterminismTest.java`
- [ ] T010 [P] Write provenance content, input/output hashing, and secret/path exclusion tests in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/manifest/GenerationManifestTest.java`

### Foundation Implementation

- [ ] T011 Define the sealed schema AST, source locations, type references, declarations, annotations, and mapping decisions in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/model/`
- [ ] T012 Implement Markdown fenced-schema extraction with repository-relative diagnostics in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/parser/MarkdownSchemaExtractor.java`
- [ ] T013 Implement the lexer and recursive-descent parser for the `spec/base` grammar in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/parser/SchemaLexer.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/parser/SchemaParser.java`
- [ ] T014 Implement namespace/type resolution and fail-closed semantic validation in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/validation/SchemaResolver.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/validation/SchemaValidator.java`
- [ ] T015 Implement configuration loading and canonical source-to-Java type mapping in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/config/CodegenConfiguration.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/JavaTypeMapper.java`
- [ ] T016 Implement deterministic Java source/Javadoc rendering and staged atomic output replacement in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/render/JavaRenderer.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/render/GeneratedTreeWriter.java`
- [ ] T017 Implement `validate`, `inventory`, and `generate` commands plus deterministic manifest creation in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/cli/Main.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/manifest/ManifestWriter.java`
- [ ] T018 Wire validation, generation, and clean-regeneration Maven profiles into `pom.xml` without making the generator a dependency of the API artifact

**Checkpoint**: Generator unit tests pass, malformed input fails before output is replaced, and two
empty-model renders are byte-identical.

---

## Phase 3: User Story 1 - Implement Against Stable Base Contracts (Priority: P1) MVP

**Goal**: Produce one coherent Java base API skeleton covering every source declaration and usable
without an implementation artifact.

**Independent Test**: Compile a consumer fixture importing representative types from all eleven
base namespace packages using only the API artifact and compile-time JSpecify metadata.

### Tests for User Story 1

- [ ] T019 [P] [US1] Write the 11-namespace, 37-type, 6-constant, member, constraint, and error inventory contract test for FR-001/FR-003/FR-016 in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/SourceInventoryTest.java`
- [ ] T020 [P] [US1] Create a representative all-namespace positive consumer fixture for FR-002/FR-012 in `tests/java-base-api-contract/src/test/resources/fixtures/positive/AllNamespacesConsumer.java`
- [ ] T021 [P] [US1] Write public-signature and dependency allowlist tests for FR-012/FR-014 in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/PublicApiBoundaryTest.java`

### Implementation for User Story 1

- [ ] T022 [US1] Implement direct declaration, field, generic, inheritance, enum, constant, error, and Java companion-contract mappings in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/BaseApiMapper.java`
- [ ] T023 [US1] Generate source-linked Javadocs, JSpecify annotations, retained questions, deferred mappings, and do-not-edit headers from `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/render/JavaRenderer.java`
- [ ] T024 [US1] Generate the named module descriptor and all eleven exported package descriptors in `java/hiero-sdk-base-api/src/main/java/module-info.java` and `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/`
- [ ] T025 [US1] Generate the complete API skeleton and provenance inventory in `java/hiero-sdk-base-api/src/main/java/` and `java/hiero-sdk-base-api/generated-api-manifest.json`
- [ ] T026 [US1] Create the initial generated public-signature snapshot in `tests/java-base-api-contract/src/test/resources/api-signatures/base-api.txt`
- [ ] T027 [US1] Run the all-namespace consumer, inventory, JPMS, Javadoc, and public-boundary checks through `tests/java-base-api-contract/pom.xml`

**Checkpoint**: User Story 1 compiles independently with every declaration represented or explicitly
recorded as deferred, and no implementation artifact is present.

---

## Phase 4: User Story 2 - Use Ledger and Token Domain Types (Priority: P1)

**Goal**: Complete the immutable native-token, HBAR, token, ledger identifier, node, network-setting,
constant, factory, and operation contracts.

**Independent Test**: Compile every ledger identifier alternative and token/network relationship,
then verify ownership, lengths, one-of rules, unsigned ranges, constants, and generic bounds.

### Approval and Tests for User Story 2

- [ ] T028 [US2] Record accountable maintainer approval or require a specification correction for the proposed HBAR symbols and factor table in `specs/001-java-base-api/api-review.md`
- [ ] T029 [P] [US2] Create positive native-token, HBAR, token, ledger, node, and network-setting consumer fixtures for FR-007/FR-008/FR-009 in `tests/java-base-api-contract/src/test/resources/fixtures/positive/LedgerAndTokenConsumer.java`
- [ ] T030 [P] [US2] Write defensive byte/collection ownership and structural constant tests for FR-004/FR-008 in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/LedgerValueContractTest.java`
- [ ] T031 [P] [US2] Create negative compilation and construction fixtures for selector one-of, byte length, nullability narrowing, unsigned ranges, and immutable collections in `tests/java-base-api-contract/src/test/resources/fixtures/negative/ledger/`

### Implementation for User Story 2

- [ ] T032 [P] [US2] Implement native-token and HBAR structural/operation mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/NativeTokenMapping.java`
- [ ] T033 [P] [US2] Implement sealed ledger hierarchy, final value, defensive array, one-of, range, and zero-constant mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/LedgerMapping.java`
- [ ] T034 [P] [US2] Implement ledger-config, Hedera, Solo, and token enum/profile mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/NetworkProfileMapping.java`
- [ ] T035 [US2] Generate `LedgerFactory`, `LedgerOperations`, `NativeTokenOperations`, and `NetworkSettingRegistry` as body-free contracts from `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/BaseApiMapper.java`
- [ ] T036 [US2] Regenerate the affected public packages and manifest entries in `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativetoken/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/hedera/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/solo/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/token/`, and `java/hiero-sdk-base-api/generated-api-manifest.json`
- [ ] T037 [US2] Run all US2 positive, negative, ownership, generic-bound, constant, and operational-scope checks through `tests/java-base-api-contract/pom.xml`

**Checkpoint**: User Story 2 is independently usable as immutable ledger/token vocabulary with
abstract operational boundaries and no parser, formatter, converter, or registry implementation.

---

## Phase 5: User Story 3 - Integrate Key and Authority Contracts Safely (Priority: P1)

**Goal**: Complete the key material, format, factory, signing/verification, and recursive Authority
contracts while structurally excluding private keys from authorization.

**Independent Test**: Compile all key and Authority variants, exhaustively switch over Authority,
reject private-key leaves and invalid threshold lists, and prove no crypto provider or secret-bearing
diagnostic exists.

### Tests for User Story 3

- [ ] T038 [P] [US3] Create positive key hierarchy, key-format, factory, and operation consumer fixtures for FR-010 in `tests/java-base-api-contract/src/test/resources/fixtures/positive/KeyContractsConsumer.java`
- [ ] T039 [P] [US3] Create positive exhaustive Authority construction and pattern-matching fixtures for FR-011 in `tests/java-base-api-contract/src/test/resources/fixtures/positive/AuthorityConsumer.java`
- [ ] T040 [P] [US3] Create negative private-key Authority, external Authority variant, empty-list, and invalid-threshold fixtures in `tests/java-base-api-contract/src/test/resources/fixtures/negative/authority/`
- [ ] T041 [P] [US3] Write key byte ownership, secret-safe diagnostics, forbidden crypto dependency, and no-operational-bytecode tests for FR-013/FR-014 in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/KeySecurityContractTest.java`

### Implementation for User Story 3

- [ ] T042 [P] [US3] Implement key interfaces, key pair, enum metadata, and defensive byte mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/KeyMapping.java`
- [ ] T043 [P] [US3] Implement the sealed Authority sum, immutable record variants, recursive equality, and threshold validation mapping in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/AuthorityMapping.java`
- [ ] T044 [US3] Generate body-free `KeyFactory`, `KeyFormatOperations`, and `AuthorityFactory` contracts from `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/BaseApiMapper.java`
- [ ] T045 [US3] Regenerate the key and Authority packages and provenance entries in `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/keys/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/authority/`, and `java/hiero-sdk-base-api/generated-api-manifest.json`
- [ ] T046 [US3] Document the key/Authority threat analysis and review evidence in `specs/001-java-base-api/security-review.md`
- [ ] T047 [US3] Run all US3 compilation, invariant, exhaustive-switch, ownership, dependency, secret-scan, and operational-scope checks through `tests/java-base-api-contract/pom.xml`

**Checkpoint**: User Story 3 exposes no private-key Authority path, crypto body, provider choice,
secret output, or unapproved dependency.

---

## Phase 6: User Story 4 - Use Shared Pagination and Protocol Contracts (Priority: P2)

**Goal**: Complete asynchronous pagination, mirror-node failure, method descriptor, and reserved
protocol namespace contracts without transport behavior.

**Independent Test**: Compile a custom `Page<T>` implementation and method descriptor consumer, then
verify `CompletionStage` signatures, exceptional completion documentation, and absence of transport
or codec dependencies.

### Tests for User Story 4

- [ ] T048 [P] [US4] Create a positive `Page<T>` implementation/consumer fixture for FR-005 in `tests/java-base-api-contract/src/test/resources/fixtures/positive/PageConsumer.java`
- [ ] T049 [P] [US4] Create a positive method descriptor and empty proto-package fixture for FR-006 in `tests/java-base-api-contract/src/test/resources/fixtures/positive/ProtocolConsumer.java`
- [ ] T050 [P] [US4] Write async signature/error and forbidden transport/protobuf/gRPC dependency checks in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/CommonProtocolContractTest.java`

### Implementation for User Story 4

- [ ] T051 [P] [US4] Implement `Page<T>`, `CompletionStage`, and `MirrorNodeException` mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/CommonMapping.java`
- [ ] T052 [P] [US4] Implement immutable `MethodDescriptor` and reserved proto-package mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/ProtocolMapping.java`
- [ ] T053 [US4] Regenerate common, gRPC, and proto packages and provenance entries in `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/grpc/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/proto/`, and `java/hiero-sdk-base-api/generated-api-manifest.json`
- [ ] T054 [US4] Run all US4 consumer, async-shape, Javadoc, package-reservation, and forbidden-dependency checks through `tests/java-base-api-contract/pom.xml`

**Checkpoint**: User Story 4 compiles without a scheduler, transport, protobuf, gRPC, HTTP, retry, or
network implementation.

---

## Phase 7: Polish and Cross-Cutting Conformance

**Purpose**: Freeze a reviewed, reproducible public API baseline after every story passes.

- [ ] T055 [P] Add full requirement-to-test metadata and assert FR-001 through FR-017 have no unmapped verification in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/RequirementTraceabilityTest.java`
- [ ] T056 [P] Add FR-015 two-fresh-directory and checked-in-tree byte comparison coverage in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/CleanRegenerationTest.java`
- [ ] T057 [P] Add Javadoc completeness and retained-question coverage in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/DocumentationContractTest.java`
- [ ] T058 [P] Add JPMS export, split-package, runtime dependency, and generator/test leakage checks in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/ModuleBoundaryTest.java`
- [ ] T059 Update Java prototype generation and generated-source ownership documentation in `README.md` and `CLAUDE.md`
- [ ] T060 Run `./mvnw -Pvalidate-java-base-api verify`, `./mvnw -Pgenerate-java-base-api generate-sources`, and `./mvnw verify` as documented in `specs/001-java-base-api/quickstart.md`
- [ ] T061 Verify FR-015 clean regeneration leaves no unexplained diff in `java/hiero-sdk-base-api/src/main/java/` and `java/hiero-sdk-base-api/generated-api-manifest.json`
- [ ] T062 Record accountable Java API maintainer approval of all mapping variances and the generated signature baseline in `specs/001-java-base-api/api-review.md`
- [ ] T063 Record accountable security reviewer approval of key handling, Authority structure, secret safety, and operational exclusions in `specs/001-java-base-api/security-review.md`
- [ ] T064 Re-run the complete reactor after review changes and record final FR/SC results in `specs/001-java-base-api/conformance-report.md`

---

## Dependencies and Execution Order

### Phase Dependencies

- **Phase 1 Setup**: no dependencies
- **Phase 2 Foundation**: depends on Phase 1 and blocks every story
- **US1**: depends on Phase 2 and establishes the complete generated skeleton and artifact boundary
- **US2, US3, US4**: depend on US1's generated skeleton; their mapping/test work can proceed in
  parallel after T027
- **Phase 7**: depends on all four selected user stories

### User Story Dependencies

- **US1 (P1)**: no other story dependency; this is the MVP API skeleton
- **US2 (P1)**: depends on US1 generation infrastructure, not on US3 or US4
- **US3 (P1)**: depends on US1 generation infrastructure and the ledger `ContractId` contract used
  by Authority; it does not depend on US2 tests or US4
- **US4 (P2)**: depends only on US1 generation infrastructure

### Within Each Phase

- Write the listed tests before the corresponding mapping implementation and confirm they fail for
  the expected missing contract.
- Complete mapping rules before regenerating checked-in output.
- Never hand-edit generated Java files.
- Run the story's independent verification before its checkpoint.
- Human approval tasks are blocking gates, not agent-completable checkboxes.

## Parallel Opportunities

- T002-T004 can run concurrently after T001 defines parent coordinates.
- T006-T010 can run concurrently because each creates an isolated test class.
- US1 test tasks T019-T021 can run concurrently.
- After US1, US2, US3, and US4 can be assigned concurrently, except US3 consumes the stable ledger
  contract established by US1.
- Within US2, T029-T031 and T032-T034 are parallel groups.
- Within US3, T038-T041 and T042-T043 are parallel groups.
- Within US4, T048-T050 and T051-T052 are parallel groups.
- T055-T058 can run concurrently after all story outputs stabilize.

## Parallel Example: User Story 3

```text
Task T038: Create key consumer fixture in fixtures/positive/KeyContractsConsumer.java
Task T039: Create Authority consumer fixture in fixtures/positive/AuthorityConsumer.java
Task T040: Create negative Authority fixtures in fixtures/negative/authority/
Task T041: Create key security contract tests in KeySecurityContractTest.java

After those tests fail as expected:
Task T042: Implement KeyMapping.java
Task T043: Implement AuthorityMapping.java
```

## Implementation Strategy

### MVP First

1. Complete Setup and Foundational Generator phases.
2. Complete US1 through T027.
3. Stop and verify that all source declarations are represented and an all-namespace consumer
   compiles with only the API artifact.
4. Review the generated skeleton before adding domain-specific structural enforcement.

### Incremental Delivery

1. Add US2 ledger/token values and invariants; validate independently.
2. Add US3 key/Authority security contracts; validate and obtain security review.
3. Add US4 common/protocol integration contracts; validate independently.
4. Complete cross-cutting regeneration, documentation, compatibility, API, and security gates.

## Notes

- `[P]` means separate files and no incomplete dependency, not merely that work is desirable in
  parallel.
- Tests are linked to FR IDs in their task descriptions and completed by T055's full traceability
  audit.
- Runtime implementation, provider loading, network calls, crypto, parsing, formatting, conversion,
  registry state, serialization, retries, scheduling, and storage remain out of scope.
- Commit after each logical task group and stop at every checkpoint for review.
