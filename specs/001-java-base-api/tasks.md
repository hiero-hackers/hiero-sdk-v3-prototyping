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

- [ ] T001 Complete the dependency assessment and record accountable human approval for every planned public, build, test, and wrapper dependency in `specs/001-java-base-api/dependency-review.md` before adding dependency coordinates or plugin artifacts to Maven build files (FR-014, FR-017, SC-003, SC-008)
- [ ] T002 Create the Java 21 parent reactor and pinned Maven wrapper in `pom.xml`, `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/maven-wrapper.properties` (FR-012, FR-015)
- [ ] T003 [P] Create the generator module and pin its build plugins in `tools/spec-codegen-java/pom.xml` (FR-015, FR-017)
- [ ] T004 [P] Create the generated API and contract-test modules with JSpecify 1.0.0 and pinned JUnit/build dependencies in `java/hiero-sdk-base-api/pom.xml` and `tests/java-base-api-contract/pom.xml` (FR-012, FR-014, FR-017)
- [ ] T005 [P] Define deterministic Java 21 package, input, output, encoding, and manifest settings in `codegen/java-base.yml` (FR-002, FR-015, FR-016)
- [ ] T006 Add Maven `target/` and generator staging exclusions without ignoring checked-in generated API sources in `.gitignore` (FR-015)

**Checkpoint**: Dependencies are approved, `./mvnw --version` uses the pinned wrapper, and the empty
reactor validates with a Java 21 release target.

---

## Phase 2: Foundational Generator (Blocking Prerequisites)

**Purpose**: Implement the typed, deterministic parser/validator/renderer infrastructure required
by every user story.

**CRITICAL**: No user-story generation task starts until this phase is complete.

### Tests for the Foundation

- [ ] T007 [P] Write fenced-schema extraction and repository-relative line diagnostic tests in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/parser/MarkdownSchemaExtractorTest.java` (FR-001, FR-016)
- [ ] T008 [P] Write lexer/parser tests for imports, generics, inheritance, annotations, enums, constants, overloads, varargs, and comments in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/parser/SchemaParserTest.java` (FR-001, FR-003, FR-004)
- [ ] T009 [P] Write semantic validation tests for unresolved/unused imports, duplicate declarations, invalid bounds, unsupported syntax, and unmapped elements in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/validation/SchemaValidatorTest.java` (FR-002, FR-003, FR-017)
- [ ] T010 [P] Write deterministic ordering, UTF-8/LF, fixed-format, and no-host-metadata tests in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/render/JavaRendererDeterminismTest.java` (FR-015, SC-006)
- [ ] T011 [P] Write provenance content, input/output hashing, and secret/path exclusion tests in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/manifest/GenerationManifestTest.java` (FR-014, FR-015, FR-016)

### Pre-Implementation Approval Gate

- [ ] T012 Record accountable Java API maintainer approval of every pre-implementation mapping variance listed in `specs/001-java-base-api/contracts/public-api.md` and every deferred-enforcement decision, covering package/module structure, companion operations, ledger finality/factories, registry abstraction, and Authority construction, in `specs/001-java-base-api/api-review.md` (FR-003, FR-007, FR-008, FR-009, FR-011, FR-012, FR-017, SC-008)

**Gate**: T012 must be complete before T013 or any later implementation task begins.

### Foundation Implementation

- [ ] T013 Define the sealed schema AST, source locations, type references, declarations, annotations, and mapping decisions in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/model/` (FR-001, FR-003, FR-017)
- [ ] T014 Implement Markdown fenced-schema extraction with repository-relative diagnostics in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/parser/MarkdownSchemaExtractor.java` (FR-001, FR-016)
- [ ] T015 Implement the lexer and recursive-descent parser for the `spec/base` grammar in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/parser/SchemaLexer.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/parser/SchemaParser.java` (FR-001, FR-003)
- [ ] T016 Implement namespace/type resolution and fail-closed semantic validation in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/validation/SchemaResolver.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/validation/SchemaValidator.java` (FR-002, FR-003, FR-017)
- [ ] T017 Implement configuration loading and canonical source-to-Java type mapping in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/config/CodegenConfiguration.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/JavaTypeMapper.java` (FR-002, FR-003, FR-004, FR-012)
- [ ] T018 Implement deterministic Java source/Javadoc rendering and staged atomic output replacement in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/render/JavaRenderer.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/render/GeneratedTreeWriter.java` (FR-015, FR-016)
- [ ] T019 Implement `validate`, `inventory`, and `generate` commands plus deterministic manifest creation in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/cli/Main.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/manifest/ManifestWriter.java` (FR-001, FR-015, FR-017)
- [ ] T020 Wire validation, generation, and clean-regeneration Maven profiles into `pom.xml` without making the generator a dependency of the API artifact (FR-013, FR-015, FR-017)

**Checkpoint**: Planned mappings are approved, generator unit tests pass, malformed input fails
before output is replaced, and two empty-model renders are byte-identical.

---

## Phase 3: User Story 1 - Implement Against Stable Base Contracts (Priority: P1) MVP

**Goal**: Produce one coherent Java base API skeleton covering every source declaration and usable
without an implementation artifact.

**Independent Test**: Compile a consumer fixture importing representative types from all eleven
base namespace packages using only the API artifact and compile-time JSpecify metadata.

### Tests for User Story 1

- [ ] T021 [P] [US1] Write the 11-namespace, 37-type, 6-constant, member, constraint, and error inventory contract test in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/SourceInventoryTest.java` (FR-001, FR-003, FR-016, SC-001)
- [ ] T022 [P] [US1] Create a representative all-namespace positive consumer fixture in `tests/java-base-api-contract/src/test/resources/fixtures/positive/AllNamespacesConsumer.java` (FR-002, FR-012, SC-002)
- [ ] T023 [P] [US1] Write public-signature and dependency allowlist tests in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/PublicApiBoundaryTest.java` (FR-012, FR-014, SC-003)

### Implementation for User Story 1

- [ ] T024 [US1] Implement direct declaration, field, generic, inheritance, enum, constant, error, and Java companion-contract mappings in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/BaseApiMapper.java` (FR-001, FR-002, FR-003, FR-004, FR-012)
- [ ] T025 [US1] Generate source-linked Javadocs, JSpecify annotations, retained questions, deferred mappings, and do-not-edit headers from `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/render/JavaRenderer.java` (FR-003, FR-004, FR-016)
- [ ] T026 [US1] Generate the named module descriptor and all eleven exported package descriptors in `java/hiero-sdk-base-api/src/main/java/module-info.java` and `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/` (FR-002, FR-012, FR-014)
- [ ] T027 [US1] Generate the complete API skeleton and provenance inventory in `java/hiero-sdk-base-api/src/main/java/` and `java/hiero-sdk-base-api/generated-api-manifest.json` (FR-001, FR-003, FR-015, FR-016, SC-001)
- [ ] T028 [US1] Create the initial generated public-signature snapshot in `tests/java-base-api-contract/src/test/resources/api-signatures/base-api.txt` (FR-003, FR-012, SC-003)
- [ ] T029 [US1] Run the all-namespace consumer, inventory, JPMS, Javadoc, and public-boundary checks through `tests/java-base-api-contract/pom.xml` (FR-001, FR-002, FR-003, FR-012, FR-014, FR-016, FR-017, SC-001, SC-002, SC-003, SC-005)

**Checkpoint**: User Story 1 compiles independently with every declaration represented or explicitly
recorded as deferred, and no implementation artifact is present.

---

## Phase 4: User Story 2 - Use Ledger and Token Domain Types (Priority: P1)

**Goal**: Complete the immutable native-token, HBAR, token, ledger identifier, node, network-setting,
constant, factory, and operation contracts.

**Independent Test**: Compile every ledger identifier alternative and token/network relationship,
then verify ownership, lengths, one-of rules, unsigned ranges, constants, and generic bounds.

### Source Verification and Tests for User Story 2

- [ ] T030 [US2] Verify that every generated HBAR symbol and factor traces to the approved `spec/base/hedera.md` revision and record that source revision in `specs/001-java-base-api/api-review.md` (FR-003, FR-007, FR-012, FR-017, SC-008)
- [ ] T031 [P] [US2] Create positive native-token, HBAR, token, ledger, node, and network-setting consumer fixtures in `tests/java-base-api-contract/src/test/resources/fixtures/positive/LedgerAndTokenConsumer.java` (FR-007, FR-008, FR-009, SC-002)
- [ ] T032 [P] [US2] Write defensive byte/collection ownership and structural constant tests in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/LedgerValueContractTest.java` (FR-004, FR-008, SC-004)
- [ ] T033 [P] [US2] Create negative compilation and construction fixtures for selector one-of, byte length, nullability narrowing, unsigned ranges, and immutable collections in `tests/java-base-api-contract/src/test/resources/fixtures/negative/ledger/` (FR-004, FR-008, SC-004)

### Implementation for User Story 2

- [ ] T034 [P] [US2] Implement native-token and HBAR structural/operation mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/NativeTokenMapping.java` (FR-003, FR-004, FR-007, FR-012)
- [ ] T035 [P] [US2] Implement sealed ledger hierarchy, final value, defensive array, one-of, range, and zero-constant mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/LedgerMapping.java` (FR-003, FR-004, FR-008, FR-012)
- [ ] T036 [P] [US2] Implement ledger-config, Hedera, Solo, and token enum/profile mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/NetworkProfileMapping.java` (FR-003, FR-007, FR-009, FR-012)
- [ ] T037 [US2] Generate `LedgerFactory`, `LedgerOperations`, `NativeTokenOperations`, and `NetworkSettingRegistry` as body-free contracts from `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/BaseApiMapper.java` (FR-003, FR-007, FR-008, FR-009, FR-012, FR-013)
- [ ] T038 [US2] Regenerate the affected public packages and manifest entries in `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativetoken/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/hedera/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/solo/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/token/`, and `java/hiero-sdk-base-api/generated-api-manifest.json` (FR-007, FR-008, FR-009, FR-015, FR-016)
- [ ] T039 [US2] Run all US2 positive, negative, ownership, generic-bound, constant, and operational-scope checks through `tests/java-base-api-contract/pom.xml` (FR-004, FR-007, FR-008, FR-009, FR-013, FR-017, SC-004, SC-005, SC-007)

**Checkpoint**: User Story 2 is independently usable as immutable ledger/token vocabulary with
abstract operational boundaries and no parser, formatter, converter, or registry implementation.

---

## Phase 5: User Story 3 - Integrate Key and Authority Contracts Safely (Priority: P1)

**Goal**: Complete the key material, format, factory, signing/verification, and recursive Authority
contracts while structurally excluding private keys from authorization.

**Independent Test**: Compile all key and Authority variants, exhaustively switch over Authority,
reject private-key leaves and invalid threshold lists, and prove no crypto provider or secret-bearing
diagnostic exists.

### Threat Analysis for User Story 3

- [ ] T040 [US3] Document the key/Authority threat model, trust boundaries, abuse cases, mitigations, residual risks, and required review evidence in `specs/001-java-base-api/security-review.md` before implementing key or Authority mappings (FR-010, FR-011, FR-014, FR-017, SC-008)

**Gate**: T040 must be complete before T041 or any later User Story 3 task begins.

### Tests for User Story 3

- [ ] T041 [P] [US3] Create positive key hierarchy, key-format, factory, and operation consumer fixtures in `tests/java-base-api-contract/src/test/resources/fixtures/positive/KeyContractsConsumer.java` (FR-010, FR-012, SC-002)
- [ ] T042 [P] [US3] Create positive exhaustive Authority construction and pattern-matching fixtures in `tests/java-base-api-contract/src/test/resources/fixtures/positive/AuthorityConsumer.java` (FR-011, FR-012, SC-002)
- [ ] T043 [P] [US3] Create negative private-key Authority, external Authority variant, empty-list, and invalid-threshold fixtures in `tests/java-base-api-contract/src/test/resources/fixtures/negative/authority/` (FR-004, FR-011, FR-014, SC-004)
- [ ] T044 [P] [US3] Write key signature, deferred byte-ownership manifest, secret-safe diagnostic, forbidden crypto dependency, and no-operational-bytecode tests in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/KeySecurityContractTest.java` (FR-004, FR-010, FR-013, FR-014, SC-003, SC-007)

### Implementation for User Story 3

- [ ] T045 [P] [US3] Implement key interfaces, key pair, enum metadata, and interface-backed byte-ownership deferral rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/KeyMapping.java` (FR-003, FR-004, FR-010, FR-012, FR-014)
- [ ] T046 [P] [US3] Implement the sealed Authority sum, immutable record variants, recursive equality, and threshold validation mapping in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/AuthorityMapping.java` (FR-003, FR-004, FR-011, FR-012, FR-014)
- [ ] T047 [US3] Generate body-free `KeyFactory`, `KeyFormatOperations`, and `AuthorityFactory` contracts from `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/BaseApiMapper.java` (FR-003, FR-010, FR-011, FR-012, FR-013)
- [ ] T048 [US3] Regenerate the key and Authority packages and provenance entries in `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/keys/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/authority/`, and `java/hiero-sdk-base-api/generated-api-manifest.json` (FR-010, FR-011, FR-015, FR-016)
- [ ] T049 [US3] Run all US3 compilation, invariant, exhaustive-switch, ownership-deferral, dependency, secret-scan, and operational-scope checks through `tests/java-base-api-contract/pom.xml` (FR-004, FR-010, FR-011, FR-013, FR-014, FR-017, SC-004, SC-005, SC-007)

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

- [ ] T050 [P] [US4] Create a positive `Page<T>` implementation/consumer fixture in `tests/java-base-api-contract/src/test/resources/fixtures/positive/PageConsumer.java` (FR-005, SC-002)
- [ ] T051 [P] [US4] Create a positive method descriptor and empty proto-package fixture in `tests/java-base-api-contract/src/test/resources/fixtures/positive/ProtocolConsumer.java` (FR-006, SC-002)
- [ ] T052 [P] [US4] Write async signature/error and forbidden transport/protobuf/gRPC dependency checks in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/CommonProtocolContractTest.java` (FR-005, FR-006, FR-013, FR-014, SC-003, SC-007)

### Implementation for User Story 4

- [ ] T053 [P] [US4] Implement `Page<T>`, `CompletionStage`, and `MirrorNodeException` mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/CommonMapping.java` (FR-003, FR-004, FR-005, FR-012, FR-013)
- [ ] T054 [P] [US4] Implement immutable `MethodDescriptor` and reserved proto-package mapping rules in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/mapping/ProtocolMapping.java` (FR-003, FR-006, FR-012, FR-013)
- [ ] T055 [US4] Regenerate common, gRPC, and proto packages and provenance entries in `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/grpc/`, `java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/proto/`, and `java/hiero-sdk-base-api/generated-api-manifest.json` (FR-005, FR-006, FR-015, FR-016)
- [ ] T056 [US4] Run all US4 consumer, async-shape, Javadoc, package-reservation, and forbidden-dependency checks through `tests/java-base-api-contract/pom.xml` (FR-005, FR-006, FR-013, FR-014, FR-016, FR-017, SC-002, SC-003, SC-005, SC-007)

**Checkpoint**: User Story 4 compiles without a scheduler, transport, protobuf, gRPC, HTTP, retry, or
network implementation.

---

## Phase 7: Polish and Cross-Cutting Conformance

**Purpose**: Freeze a reviewed, reproducible public API baseline after every story passes.

- [ ] T057 [P] Add full requirement-to-test metadata and assert FR-001 through FR-017 have no unmapped verification in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/RequirementTraceabilityTest.java` (FR-017, SC-005)
- [ ] T058 [P] Add two-fresh-directory and checked-in-tree byte comparison coverage in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/CleanRegenerationTest.java` (FR-015, SC-006)
- [ ] T059 [P] Add Javadoc completeness and retained-question coverage in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/DocumentationContractTest.java` (FR-016, SC-001)
- [ ] T060 [P] Add JPMS export, split-package, runtime dependency, and generator/test leakage checks in `tests/java-base-api-contract/src/test/java/org/hiero/sdk/v3/contract/ModuleBoundaryTest.java` (FR-002, FR-012, FR-013, FR-014, SC-003, SC-007)
- [ ] T061 Update Java prototype generation and generated-source ownership documentation in `README.md` and `CLAUDE.md` (FR-016)
- [ ] T062 Run `./mvnw -Pvalidate-java-base-api verify`, `./mvnw -Pgenerate-java-base-api generate-sources`, and `./mvnw verify` as documented in `specs/001-java-base-api/quickstart.md` (FR-001 through FR-017, SC-001 through SC-007)
- [ ] T063 Verify clean regeneration leaves no unexplained diff in `java/hiero-sdk-base-api/src/main/java/` and `java/hiero-sdk-base-api/generated-api-manifest.json` (FR-015, SC-006)
- [ ] T064 Regenerate the complete public-signature snapshot after all user-story outputs in `tests/java-base-api-contract/src/test/resources/api-signatures/base-api.txt` and verify its deterministic diff from the initial T028 snapshot contains only approved mappings (FR-003, FR-012, FR-015, SC-003)
- [ ] T065 Record accountable Java API maintainer approval that the generated surface and signature baseline conform to the mapping decisions approved in T012 in `specs/001-java-base-api/api-review.md` (FR-003, FR-012, FR-017, SC-008)
- [ ] T066 Record accountable security reviewer approval of key handling, Authority structure, secret safety, and operational exclusions in `specs/001-java-base-api/security-review.md` (FR-010, FR-011, FR-014, FR-017, SC-008)
- [ ] T067 Re-run the complete reactor after review changes and record final FR/SC results in `specs/001-java-base-api/conformance-report.md` (FR-001 through FR-017, SC-001 through SC-008)

---

## Dependencies and Execution Order

### Phase Dependencies

- **Phase 1 Setup**: no external task dependencies; T001 blocks dependency-adoption tasks T002-T004
- **Phase 2 Foundation**: depends on Phase 1; T012 blocks T013 and all later implementation tasks,
  and the completed phase blocks every story
- **US1**: depends on Phase 2 and establishes the complete generated skeleton and artifact boundary
- **US2, US3, US4**: depend on US1's generated skeleton; isolated mapping and test work can proceed
  in parallel after T029, but shared aggregate and regeneration tasks are serialized
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
- Complete T001 through accountable human dependency review before adding Maven dependencies,
  plugins, or wrapper artifacts in T002-T004.
- Complete T012 through accountable human review before starting T013-T020 or any user-story
  implementation task.
- Complete the T040 threat analysis before starting T041-T049.
- Complete mapping rules before regenerating checked-in output.
- Serialize aggregate `BaseApiMapper.java` changes in T037 then T047, and serialize shared manifest
  regeneration in T038, T048, then T055.
- Never hand-edit generated Java files.
- Run the story's independent verification before its checkpoint.
- Human approval tasks are blocking gates, not agent-completable checkboxes.

## Parallel Opportunities

- T003-T005 can run concurrently after T002 defines parent coordinates.
- T007-T011 can run concurrently because each creates an isolated test class.
- US1 test tasks T021-T023 can run concurrently.
- After US1, isolated US2, US3, and US4 test and mapper tasks can be assigned concurrently; aggregate
  mapper and shared-manifest regeneration tasks follow the serialized order above.
- Within US2, T031-T033 and T034-T036 are parallel groups.
- Within US3, T041-T044 and T045-T046 are parallel groups after T040 is complete.
- Within US4, T050-T052 and T053-T054 are parallel groups.
- T057-T060 can run concurrently after all story outputs stabilize.

## Parallel Example: User Story 3

```text
Task T040: Complete the key/Authority threat analysis in security-review.md

After the threat analysis is complete:
Task T041: Create key consumer fixture in fixtures/positive/KeyContractsConsumer.java
Task T042: Create Authority consumer fixture in fixtures/positive/AuthorityConsumer.java
Task T043: Create negative Authority fixtures in fixtures/negative/authority/
Task T044: Create key security contract tests in KeySecurityContractTest.java

After those tests fail as expected:
Task T045: Implement KeyMapping.java
Task T046: Implement AuthorityMapping.java
```

## Implementation Strategy

### MVP First

1. Complete Setup and Foundational Generator phases.
2. Complete US1 through T029.
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
- Every task is linked to FR/SC IDs in its description, and T057 performs the full automated
  traceability audit.
- Runtime implementation, provider loading, network calls, crypto, parsing, formatting, conversion,
  registry state, serialization, retries, scheduling, and storage remain out of scope.
- Commit after each logical task group and stop at every checkpoint for review.
