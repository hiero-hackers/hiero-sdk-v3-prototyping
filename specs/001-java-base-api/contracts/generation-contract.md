# Contract: Deterministic Java API Generation

## Inputs

- Exact ordered set of `spec/base/*.md` files
- `guidelines/api-guideline.md`
- `guidelines/api-best-practices-java.md`
- `docs/adr/0004-authority-authorization-sum-type.md`
- `codegen/java-base.yml`
- Generator artifact version

The configuration fixes the package root, Java release, input globs, output directory, line ending,
encoding, and generated-header format. Unknown keys and missing inputs fail generation.

## CLI Behavior

The generator exposes `validate`, `generate`, and `inventory` commands. Commands accept explicit
repository-relative input/output/configuration paths. Invalid syntax, unresolved references,
unsupported constructs, duplicate declarations, unmapped elements, or output collisions produce a
non-zero exit and deterministic file/line diagnostics.

`validate` and `inventory` never modify files. `generate` writes through a staging directory and
replaces the generated tree only after all inputs validate and all outputs render successfully.

## Determinism Rules

- Read and write UTF-8 with LF line endings.
- Sort input paths, packages, imports, generated type files, manifest entries, and set-valued
  metadata canonically.
- Preserve source declaration order only where it is semantically visible, such as record
  components, parameters, enum constants, and overload documentation.
- Never include wall-clock time, absolute paths, environment variables, locale-dependent text,
  random values, host data, or filesystem iteration order.
- Use fixed formatting and exactly one terminal newline.
- Delete stale files only inside the declared generated root.

## Provenance Manifest

The checked-in manifest contains:

- source Git revision plus SHA-256 per input file
- generator Maven coordinates and version
- configuration SHA-256
- Java release and package root
- source-to-Java mapping inventory
- retained questions and deferred enforcement entries
- SHA-256 per generated output

The manifest excludes timestamps and any sensitive value.

## Generated File Boundary

Every file under `java/hiero-sdk-base-api/src/main/java` is generated and begins with a do-not-edit
header. No generated file may exist outside that tree except the provenance manifest. No handwritten
file may exist inside it.

## Clean-Regeneration Acceptance

1. Build the generator from a clean checkout with pinned Maven/plugin/dependency versions.
2. Generate into a fresh temporary directory.
3. Generate a second time into a different fresh directory.
4. Compare both temporary trees byte-for-byte.
5. Compare one temporary tree with the checked-in generated tree and manifest.
6. Fail on any missing, stale, additional, or changed file.

The comparison must not mutate the working tree. A generated diff is accepted only after its source,
configuration, or generator change is identified and reviewed.
