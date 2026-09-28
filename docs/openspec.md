# Using OpenSpec in This Project

[OpenSpec](https://openspec.dev/docs/) provides the proposal, design, implementation-task, validation, and archive
workflow used to make traceable changes in this repository. Use it when work changes a public contract, adds a generated
or directly maintained language API, or introduces operational implementation that should be reviewed against explicit
requirements.

## Sources of Truth

OpenSpec organizes changes, but it does not replace this project's specifications:

- `spec/` contains the authoritative language-agnostic public API.
- `guidelines/api-guideline.md` defines the API meta-language.
- Language-specific guides, such as `guidelines/api-best-practices-java.md`, define idiomatic mappings.
- `guidelines/testing-guideline.md` defines the testing expectations.
- `openspec/changes/<change-name>/` contains reviewable planning artifacts for one active change.
- `openspec/specs/` contains OpenSpec capability requirements produced by archived changes; it complements rather than
  supersedes `spec/`.

A change to a public API must update the relevant file under `spec/`. The associated OpenSpec delta describes what is
being added, changed, or removed and provides traceability to the design, tasks, implementation, and tests.

## Installation and Project Setup

Install the CLI using one of the methods in the official
[installation guide](https://openspec.dev/docs/installation). For example:

```shell
npm install -g @fission-ai/openspec@latest
openspec --version
```

If `openspec/config.yaml` and the agent integrations are not present, initialize them from the repository root:

```shell
openspec init . --tools codex,claude --profile core
openspec doctor
```

Initialization is a project-level operation. Review and commit the generated configuration and agent instructions. Do
not repeatedly initialize an already configured checkout. To refresh installed OpenSpec instructions after upgrading
the CLI, run:

```shell
openspec update .
openspec doctor
```

## Change Workflow

The normal lifecycle is:

```text
explore → propose → review → apply → validate → sync → archive
```

### 1. Explore an unclear change

Use exploration to understand the existing specifications, compare alternatives, and resolve important ambiguity. It
must not modify implementation files.

In Codex:

```text
$openspec-explore
```

Example prompt:

```text
Explore how consensus-node address refresh should interact with immutable NetworkSetting snapshots.
```

### 2. Create the proposal

Use a lowercase kebab-case change name:

```text
$openspec-propose add-consensus-node-refresh
```

The proposal workflow creates the planning artifacts under
`openspec/changes/add-consensus-node-refresh/`:

```text
proposal.md
design.md
tasks.md
specs/<capability>/spec.md
```

The artifacts have separate responsibilities:

- `proposal.md` explains why the change is needed, its scope, and its impact.
- `specs/<capability>/spec.md` contains testable requirements and scenarios describing the contract delta.
- `design.md` records technical decisions, boundaries, alternatives, and risks.
- `tasks.md` divides implementation and verification into reviewable work.

Proposal creation is a planning phase. Review and approve these artifacts before implementation begins.

Useful CLI checks during review are:

```shell
openspec list
openspec status --change add-consensus-node-refresh
openspec show add-consensus-node-refresh
openspec validate add-consensus-node-refresh --strict
```

### 3. Apply the approved change

After explicit approval, apply the tasks:

```text
$openspec-apply-change add-consensus-node-refresh
```

During apply:

- Read the proposal, delta spec, design, and tasks before editing.
- Update the authoritative files under `spec/` when the public contract changes.
- Follow every relevant language and testing guideline.
- Keep public contracts separate from operational or business implementation unless the approved change includes that
  implementation.
- Mark a task complete only after its implementation and verification are complete.
- Preserve unrelated working-tree changes.

Inspect the instructions that drive the apply phase with:

```shell
openspec instructions apply --change add-consensus-node-refresh
```

### 4. Validate the result

Validate both the OpenSpec structure and the project changes:

```shell
openspec status --change add-consensus-node-refresh
openspec validate add-consensus-node-refresh --strict
git diff --check
```

Run all builds, contract tests, generated-API checks, and language-specific tests required by `tasks.md`. Validation is
not complete merely because every task checkbox is selected; the implementation must satisfy each normative scenario.

### 5. Sync and archive

Sync the approved delta into the OpenSpec capability specs before archiving:

```text
$openspec-sync-specs add-consensus-node-refresh
$openspec-archive-change add-consensus-node-refresh
```

The CLI equivalent for the final archive operation is:

```shell
openspec archive add-consensus-node-refresh
```

Archive only after implementation, tests, review, and synchronization are complete. OpenSpec validates the change,
updates the main capability specs, and moves it under `openspec/changes/archive/`. For non-interactive automation, use
`--yes`; use `--skip-specs` only for a change that genuinely has no capability-spec delta.

## Updating an Existing Change

When review alters scope or requirements, update the planning artifacts before continuing implementation:

```text
$openspec-update-change add-consensus-node-refresh
```

Re-run strict validation after every material update. If the contract changed, review the corresponding files under
`spec/`, the delta requirements, affected implementation, and tests together.

## Project Review Checklist

Before considering an OpenSpec change complete, confirm that:

- Every public API change is represented in the appropriate `spec/` Markdown file.
- Schema declarations follow the project meta-language and namespace import rules.
- Normative behavior has technology-agnostic Given/When/Then scenarios.
- Language APIs follow their language-specific best-practice guide.
- Generated or maintained code remains in its language workspace, such as `java/`.
- Operational behavior is not added to a contract-only change.
- All task checkboxes correspond to completed, verified work.
- `openspec validate <change-name> --strict` and the relevant project tests pass.
- The change is synchronized and archived only after review.

## Useful Links

- [OpenSpec documentation](https://openspec.dev/docs/)
- [Getting started](https://openspec.dev/docs/getting-started)
- [Installation](https://openspec.dev/docs/installation)
- [CLI reference](https://openspec.dev/docs/cli)
- [OpenSpec command workflows](https://github.com/Fission-AI/OpenSpec/blob/main/docs/commands.md)
- [OpenSpec source repository](https://github.com/Fission-AI/OpenSpec)
