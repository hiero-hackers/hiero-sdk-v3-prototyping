---

description: "Task list for the Java common code generation feature"
---

# Tasks: Java Common Code Generation

**Input**: Design documents from `/specs/001-java-common-codegen/`

**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Tests are included because this feature requires deterministic output verification, golden comparisons, and CLI validation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., `US1`, `US2`, `US3`)
- Include exact file paths in descriptions

## Path Conventions

- **Primary specs**: `spec/base/common.md`
- **Guidelines**: `guidelines/api-guideline.md`, `guidelines/api-best-practices-java.md`
- **Generator module**: `tools/spec-codegen-java/`
- **Planning artifacts**: `specs/001-java-common-codegen/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Bootstrap the scoped Java generator module and feature workspace

- [ ] T001 Add Maven wrapper entrypoints in `mvnw`, `.mvn/wrapper/maven-wrapper.properties`, and `.mvn/wrapper/maven-wrapper.jar`
- [ ] T002 Create the generator module build in `tools/spec-codegen-java/pom.xml`
- [ ] T003 [P] Add generator module usage notes in `tools/spec-codegen-java/README.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core parser, model, and fixture infrastructure that MUST exist before any user story work

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Define the first-slice meta-language grammar in `tools/spec-codegen-java/src/main/antlr4/org/hiero/sdk/v3/codegen/spec/MetaSpec.g4`
- [ ] T005 [P] Implement source document loading and fenced schema extraction in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/spec/SourceSpecDocumentLoader.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/spec/MarkdownSchemaExtractor.java`
- [ ] T006 [P] Implement the normalized IR model in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/model/NamespaceModel.java`, `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/model/TypeModel.java`, `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/model/FieldModel.java`, `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/model/MethodModel.java`, and related model files
- [ ] T007 [P] Implement generation configuration and package-root handling in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/config/JavaGenerationConfig.java`
- [ ] T008 Implement parse-tree to IR normalization in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/spec/SpecModelBuilder.java`
- [ ] T009 Create the approved output baseline fixture in `tools/spec-codegen-java/src/test/resources/goldens/org/hiero/sdk/v3/common/Page.java`
- [ ] T010 Create shared generation test support in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/testsupport/GenerationTestSupport.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Generate the First Java Slice (Priority: P1) 🎯 MVP

**Goal**: Generate Java API source from `spec/base/common.md` end to end

**Independent Test**: Run the generator against `spec/base/common.md` and confirm it emits `Page.java` under the configured package path

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T011 [P] [US1] Add a parser smoke test for `spec/base/common.md` in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/spec/CommonSpecParseTest.java`
- [ ] T012 [P] [US1] Add an end-to-end generation test for the `common.Page` type in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/generation/CommonJavaGenerationTest.java`

### Implementation for User Story 1

- [ ] T013 [P] [US1] Implement CLI argument parsing for the `generate` command in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/cli/GenerateCommand.java`
- [ ] T014 [US1] Implement the CLI entrypoint in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/cli/SpecJavaCodegenCli.java`
- [ ] T015 [US1] Implement the ANTLR parser facade in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/spec/MetaSpecParserFacade.java`
- [ ] T016 [US1] Implement generation orchestration in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/SpecJavaCodegenService.java`
- [ ] T017 [US1] Implement the first Java emitter for `common.Page` in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/java/JavaCommonEmitter.java`
- [ ] T018 [US1] Implement output file writing for generated Java sources in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/io/JavaOutputWriter.java`

**Checkpoint**: User Story 1 is fully functional when the generator emits `Page.java` from `spec/base/common.md`

---

## Phase 4: User Story 2 - Preserve Spec Semantics in Java Output (Priority: P2)

**Goal**: Keep the generated Java API semantically faithful to the source specification

**Independent Test**: Compare generated `Page.java` against the source spec and confirm that generic parameters, field accessors, async methods, and throws metadata are preserved consistently

### Tests for User Story 2 ⚠️

- [ ] T019 [P] [US2] Add a semantic mapping test for field accessors and generic preservation in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/generation/PageSemanticMappingTest.java`
- [ ] T020 [P] [US2] Add an async and throws metadata test in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/generation/AsyncAndThrowsMappingTest.java`

### Implementation for User Story 2

- [ ] T021 [US2] Implement Java type mapping for meta-language primitives, collections, and generics in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/java/JavaTypeMapper.java`
- [ ] T022 [US2] Implement Java member mapping for getter-style accessors, booleans, and nullability in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/java/JavaMemberMapper.java`
- [ ] T023 [US2] Implement `@@async` and `@@throws` documentation rendering in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/java/JavaDocumentationRenderer.java`
- [ ] T024 [US2] Update the approved `Page.java` golden fixture in `tools/spec-codegen-java/src/test/resources/goldens/org/hiero/sdk/v3/common/Page.java`

**Checkpoint**: User Stories 1 and 2 work together when generated `Page.java` is reviewable as a faithful Java representation of the source spec

---

## Phase 5: User Story 3 - Detect Generator Drift Early (Priority: P3)

**Goal**: Fail fast when generation output changes unexpectedly

**Independent Test**: Change the generator, rerun verification, and confirm that drift is reported against the approved golden baseline

### Tests for User Story 3 ⚠️

- [ ] T025 [P] [US3] Add a golden verification regression test in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/verification/GoldenOutputTest.java`
- [ ] T026 [P] [US3] Add a repeat-run determinism test in `tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/verification/DeterministicGenerationTest.java`

### Implementation for User Story 3

- [ ] T027 [US3] Implement golden verification and diff reporting in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/verification/GoldenVerifier.java`
- [ ] T028 [US3] Implement stable source ordering and formatting in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/java/JavaSourceFormatter.java`
- [ ] T029 [US3] Integrate `--verify-golden` mode and verification summaries in `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/cli/SpecJavaCodegenCli.java` and `tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/SpecJavaCodegenService.java`

**Checkpoint**: All user stories are functional when the generator both emits Java output and detects output drift automatically

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize docs and prove the first slice is runnable by other maintainers

- [ ] T030 [P] Update feature validation docs in `specs/001-java-common-codegen/quickstart.md` and `specs/001-java-common-codegen/contracts/java-common-codegen-cli.md`
- [ ] T031 [P] Refine module usage notes and generated-output review guidance in `tools/spec-codegen-java/README.md`
- [ ] T032 Run the quickstart validation flow and capture any necessary command or path adjustments in `specs/001-java-common-codegen/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel where staffing allows
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Starts after Foundational and delivers the first runnable MVP
- **User Story 2 (P2)**: Depends on User Story 1 generation flow existing so semantic fidelity can be enforced
- **User Story 3 (P3)**: Depends on User Story 1 and User Story 2 outputs so golden verification can compare stable emitted files

### Within Each User Story

- Tests MUST be written and fail before implementation
- Parsing and IR wiring before emission
- Emission before verification integration
- Story complete before moving to the next priority

### Parallel Opportunities

- T003 can run in parallel with T001-T002 after the module target is agreed
- T005, T006, and T007 can run in parallel in Phase 2
- T011 and T012 can run in parallel in User Story 1
- T019 and T020 can run in parallel in User Story 2
- T025 and T026 can run in parallel in User Story 3
- T030 and T031 can run in parallel in Polish

---

## Parallel Example: User Story 1

```bash
# Launch both User Story 1 tests together:
Task: "Add a parser smoke test for spec/base/common.md in tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/spec/CommonSpecParseTest.java"
Task: "Add an end-to-end generation test for the common.Page type in tools/spec-codegen-java/src/test/java/org/hiero/sdk/v3/codegen/generation/CommonJavaGenerationTest.java"

# Launch independent implementation tasks together once the parser facade contract is stable:
Task: "Implement CLI argument parsing for the generate command in tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/cli/GenerateCommand.java"
Task: "Implement output file writing for generated Java sources in tools/spec-codegen-java/src/main/java/org/hiero/sdk/v3/codegen/io/JavaOutputWriter.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Run the generator for `spec/base/common.md`
5. Demo the emitted `Page.java`

### Incremental Delivery

1. Complete Setup + Foundational → parsing and IR foundation ready
2. Add User Story 1 → emit Java output for `common.Page`
3. Add User Story 2 → tighten semantic fidelity to the source spec
4. Add User Story 3 → lock the output with golden verification
5. Finish Polish → make the slice runnable by other maintainers

### Parallel Team Strategy

With multiple developers:

1. One developer bootstraps the module and Maven wrapper
2. One developer focuses on parser/IR infrastructure
3. One developer focuses on emitter and verification tests once IR contracts are stable

---

## Notes

- `[P]` tasks touch different files and have no dependency on unfinished tasks
- `[US1]`, `[US2]`, and `[US3]` map directly to the feature specification user stories
- The suggested MVP scope is **User Story 1 only**
- Every task includes an exact file path so it can be executed directly by an implementation agent
