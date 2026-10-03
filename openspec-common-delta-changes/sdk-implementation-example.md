# Common Pagination: Java and TypeScript Example

This example starts from the shared change:

```text
openspec-common-delta-changes/changes/add-common-pagination-public-api/
├── proposal.md
└── spec.md
```

Review and commit these files first. Both SDK proposals must reference that commit SHA.

The flow for each SDK is:

```text
create change -> copy both files -> adapt -> design and tasks -> validate -> implement -> test -> archive
```

## Java

### 1. Create the branch and OpenSpec change

Run from the repository root:

```shell
git switch main
git pull --ff-only
git switch -c feature/java-common-pagination-public-api
cd sdk-java
openspec new change add-common-pagination-public-api
```

### 2. Copy and adapt the common proposal

Run from `sdk-java`:

```shell
openspec instructions proposal --change add-common-pagination-public-api --json
cp ../openspec-common-delta-changes/changes/add-common-pagination-public-api/proposal.md \
  openspec/changes/add-common-pagination-public-api/proposal.md
```

### 3. Copy and adapt the common spec

```shell
openspec instructions specs --change add-common-pagination-public-api --json
mkdir -p openspec/changes/add-common-pagination-public-api/specs/common-pagination-public-api

cp ../openspec-common-delta-changes/changes/add-common-pagination-public-api/spec.md \
  openspec/changes/add-common-pagination-public-api/specs/common-pagination-public-api/spec.md
```

This creates:

```text
sdk-java/openspec/changes/add-common-pagination-public-api/
├── proposal.md
└── specs/common-pagination-public-api/spec.md
```

### 4. Complete and validate the plan

```shell
openspec instructions design --change add-common-pagination-public-api --json
openspec instructions tasks --change add-common-pagination-public-api --json
```

Create `design.md` and `tasks.md`, then validate:

```shell
openspec validate add-common-pagination-public-api --strict
openspec status --change add-common-pagination-public-api
```

Start implementation only when proposal, specs, design, and tasks are complete.

### 5. Implement, test, and archive

```shell
openspec instructions apply --change add-common-pagination-public-api --json
```

Implement the public types, validation, defensive copying, module exports, documentation, and contract tests. Leave HTTP
calls, parsing, retries, executors, and concrete page retrieval for later changes.

After all tasks and tests pass:

```shell
openspec validate add-common-pagination-public-api --strict
openspec archive add-common-pagination-public-api
```

The accepted Java spec is now:

```text
sdk-java/openspec/specs/common-pagination-public-api/spec.md
```

## TypeScript

### 1. Create the branch and OpenSpec change

Run from the repository root:

```shell
git switch main
git pull --ff-only
git switch -c feature/ts-common-pagination-public-api
cd sdk-ts
openspec new change add-common-pagination-public-api
```

### 2. Copy and adapt the common proposal

Run from `sdk-ts`:

```shell
openspec instructions proposal --change add-common-pagination-public-api --json
cp ../openspec-common-delta-changes/changes/add-common-pagination-public-api/proposal.md \
  openspec/changes/add-common-pagination-public-api/proposal.md
```

Update the TypeScript `proposal.md` with the common change path and SHA, TypeScript impact, and deferred work.

### 3. Copy and adapt the common spec

```shell
openspec instructions specs --change add-common-pagination-public-api --json
mkdir -p openspec/changes/add-common-pagination-public-api/specs/common-pagination-public-api

cp ../openspec-common-delta-changes/changes/add-common-pagination-public-api/spec.md \
  openspec/changes/add-common-pagination-public-api/specs/common-pagination-public-api/spec.md
```

This creates:

```text
sdk-ts/openspec/changes/add-common-pagination-public-api/
├── proposal.md
└── specs/common-pagination-public-api/spec.md
```

Keep every common requirement in the TypeScript `spec.md`, then add the TypeScript mapping:

| Common behavior | TypeScript API |
| --- | --- |
| Generic page | Exported `Page<T>` interface or abstract class |
| Immutable data | `ReadonlyArray<T>` and a runtime copy where data is constructed |
| Async navigation | `Promise<Page<T>>` |
| `pagination-error` | `PaginationError` |

Do not copy Java-specific choices such as JPMS, JSpecify, or `CompletionStage`.

### 4. Complete and validate the plan

```shell
openspec instructions design --change add-common-pagination-public-api --json
openspec instructions tasks --change add-common-pagination-public-api --json
openspec validate add-common-pagination-public-api --strict
openspec status --change add-common-pagination-public-api
```

Start implementation only when proposal, specs, design, and tasks are complete.

### 5. Implement, test, and archive

```shell
openspec instructions apply --change add-common-pagination-public-api --json
```

Implement the public types, runtime validation or copying required by the design, exports, documentation, and tests.
Leave network calls, parsing, retries, and concrete page retrieval for later changes.

After formatting, linting, type checking, build, and tests pass:

```shell
openspec validate add-common-pagination-public-api --strict
openspec archive add-common-pagination-public-api
```

The accepted TypeScript spec is now:

```text
sdk-ts/openspec/specs/common-pagination-public-api/spec.md
```

## Result

One common change produces two independent, idiomatic SDK implementations:

```text
common pagination behavior
├── Java public API + Java accepted spec
└── TypeScript public API + TypeScript accepted spec
```
