---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Include test tasks whenever the feature changes observable behavior,
validation rules, serialization, code-generation output, or conformance
expectations. Pure editorial-only work may omit tests, but the reason MUST be
stated in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Primary specs**: `spec/base/`, `spec/consensus-node-client/`,
  `spec/consensus-node-admin-client/`, `spec/mirror-node-client/`,
  `spec/enterprise/`, `spec/dependencies.md`
- **Guidelines**: `guidelines/api-guideline.md`,
  `guidelines/testing-guideline.md`, `guidelines/api-best-practices-*.md`,
  `guidelines/java-files/`, `guidelines/js-files/`
- **Traceability & verification**: `docs/adr/`, `missing-features.md`,
  `TCK.md`, `tck-ideas.md`
- **Planning artifacts**: `.specify/`, `specs/[###-feature-name]/`
- Add other directories only when they are explicitly part of the feature plan

<!--
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.

  The /speckit-tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/

  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment

  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create or confirm the feature workspace in `specs/[###-feature-name]/`
- [ ] T002 Inventory touched spec, guideline, ADR, and verification files in
          the implementation plan
- [ ] T003 [P] Document any parser, generator, or validation tooling that this
          feature depends on

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust based on your project):

- [ ] T004 Establish shared meta-language or namespace changes in
          `guidelines/api-guideline.md` or shared `spec/base/` files
- [ ] T005 [P] Update affected language mapping guidance in
          `guidelines/api-best-practices-*.md`
- [ ] T006 [P] Record ADR, backlog, or dependency updates in `docs/adr/`,
          `missing-features.md`, or `spec/dependencies.md`
- [ ] T007 Add or revise `## Testing` strategy in each touched spec
- [ ] T008 Capture TCK or conformance impact in `TCK.md` or `tck-ideas.md`
- [ ] T009 Confirm unresolved questions remain visible in `## Questions &
          Comments`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1 (include when constitution or spec requires) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T010 [P] [US1] Add or revise `## Testing` scenarios in `spec/[area]/[file].md`
- [ ] T011 [P] [US1] Document conformance, `solo`, or TCK verification in
          `TCK.md`, `tck-ideas.md`, or feature quickstart artifacts

### Implementation for User Story 1

- [ ] T012 [P] [US1] Update the primary namespace spec in `spec/[area]/[file].md`
- [ ] T013 [P] [US1] Update dependent shared types or imports in
          `spec/base/[file].md` or `spec/dependencies.md`
- [ ] T014 [US1] Update impacted language guidance in
          `guidelines/api-best-practices-[language].md`
- [ ] T015 [US1] Update illustrative reference snippets in
          `guidelines/java-files/` or `guidelines/js-files/` if needed
- [ ] T016 [US1] Record ADR or open-question follow-up in `docs/adr/` or the
          target spec's `## Questions & Comments`
- [ ] T017 [US1] Update verification/backlog docs in `missing-features.md`,
          `TCK.md`, or `tck-ideas.md`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2 (include when constitution or spec requires) ⚠️

- [ ] T018 [P] [US2] Add or revise `## Testing` scenarios in `spec/[area]/[file].md`
- [ ] T019 [P] [US2] Document conformance, `solo`, or TCK verification in
          `TCK.md`, `tck-ideas.md`, or feature quickstart artifacts

### Implementation for User Story 2

- [ ] T020 [P] [US2] Update the primary namespace spec in `spec/[area]/[file].md`
- [ ] T021 [US2] Update impacted language guidance in
          `guidelines/api-best-practices-[language].md`
- [ ] T022 [US2] Update illustrative reference snippets or generator-facing
          artifacts in `guidelines/` or another planned location
- [ ] T023 [US2] Integrate the change with User Story 1 artifacts and traceability
          documents

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3 (include when constitution or spec requires) ⚠️

- [ ] T024 [P] [US3] Add or revise `## Testing` scenarios in `spec/[area]/[file].md`
- [ ] T025 [P] [US3] Document conformance, `solo`, or TCK verification in
          `TCK.md`, `tck-ideas.md`, or feature quickstart artifacts

### Implementation for User Story 3

- [ ] T026 [P] [US3] Update the primary namespace spec in `spec/[area]/[file].md`
- [ ] T027 [US3] Update impacted language guidance, ADRs, or backlog documents
- [ ] T028 [US3] Update verification artifacts and cross-story dependencies

**Checkpoint**: All user stories should now be independently functional

---

[Add more user story phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] TXXX [P] Documentation updates in `README.md`, `CLAUDE.md`, or `docs/`
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Performance, code-generation, or authoring-workflow optimization
          across all stories
- [ ] TXXX [P] Additional verification updates in `spec/`, `TCK.md`, or
          generated-output fixtures
- [ ] TXXX Cross-language consistency review across touched guides and specs
- [ ] TXXX Run quickstart.md validation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Shared spec changes before language guidance updates
- Language guidance before illustrative snippet updates
- Core specification updates before verification and backlog integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (if tests requested):
Task: "Add or revise `## Testing` scenarios in spec/[area]/[file].md"
Task: "Document conformance, `solo`, or TCK verification in TCK.md or tck-ideas.md"

# Launch independent spec updates for User Story 1 together:
Task: "Update the primary namespace spec in spec/[area]/[file].md"
Task: "Update dependent shared types or imports in spec/base/[file].md"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
