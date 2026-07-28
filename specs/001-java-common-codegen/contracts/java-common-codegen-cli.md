# Contract: Java Common Code Generation CLI

## Purpose

Define the external interface for the first Java code-generation slice so that
maintainers can run generation and verification consistently.

## Command Shape

```text
spec-java-codegen generate \
  --input-spec spec/base/common.md \
  --output-dir <absolute-or-relative-path> \
  --package-root org.hiero.sdk.v3 \
  [--verify-golden]
```

## Inputs

| Argument | Required | Description |
|----------|----------|-------------|
| `--input-spec` | Yes | Path to the authoritative source spec file |
| `--output-dir` | Yes | Directory where generated Java files are written |
| `--package-root` | Yes | Java package root used for emitted types |
| `--verify-golden` | No | When present, compare generated output against approved golden fixtures |

## Behavioral Rules

- `--input-spec` MUST support `spec/base/common.md` in this feature.
- The command MUST read only the fenced `## API Schema` block as generation
  input for this slice.
- The command MUST create Java output under a package path derived from
  `--package-root` plus the source namespace (`common`).
- The command MUST fail with a non-zero exit code if it encounters unsupported
  constructs, missing schema input, or golden verification drift.
- The command MUST print the generated file paths or a verification summary to
  standard output.

## Expected Output

For this feature the command generates at least:

```text
<output-dir>/
└── org/hiero/sdk/v3/common/
    └── Page.java
```

`Page.java` MUST expose:

- Java generic parameter `T`
- getter-style accessors for `data`, `size`, and `pageIndex`
- `hasNext()` and `isFirst()`
- async `next()` and `first()` methods using `CompletionStage`

## Error Behavior

| Condition | Result |
|-----------|--------|
| Missing `## API Schema` block | Non-zero exit, descriptive error |
| Unsupported meta-language construct in scoped input | Non-zero exit, descriptive error |
| Golden verification mismatch | Non-zero exit, diff summary |
| Successful generation without verification | Exit 0, generated file list |
| Successful generation with verification | Exit 0, verification summary |
