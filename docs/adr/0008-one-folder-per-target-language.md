# ADR-0008: One folder per target language

**Status:** Accepted
**Date:** 2026-10-07

## Context

[ADR-0007](0007-separate-generated-and-hand-written-modules.md) decided that a module is either completely
generated or completely hand-written, and made that visible by putting every generated module under a top-level
`generated/` directory. The repository was therefore cut along the axis *generated vs. hand-written*: `sdk-<lang>/`
held the configuration and the hand-written modules, `generated/<lang>/` the generated ones, and `tck/` the TCK
parts of every language at once.

That cut spread one language over several places. Java lived in five locations across three top-level directories:

| | Java |
|---|---|
| generator configuration | `sdk-java/generator.properties` |
| hand-written modules | `sdk-java/support`, `sdk-java/protobuf` |
| generated API | `generated/java` |
| generated TCK contract and server | `generated/java-tck` |
| hand-written TCK runtime | `tck/runtime/java` |

The symptom that made this visible is that `sdk-rust/` and `sdk-go/` contained **one file each**
(`generator.properties`), while everything else belonging to those languages sat under `generated/`.

Two further consequences:

- **The npm workspace had to live at the repository root.** Its members (`sdk-ts/support`,
  `generated/ts/packages/*`, `generated/ts-tck/*`, `tck/runtime/ts`) spanned three top-level directories, and npm
  requires the workspace root to be an ancestor of every member. `package.json` and `package-lock.json` were in the
  root only because the layout forced them there.
- **Adding a language touched three places** — a new `sdk-<lang>/`, a new `generated/<lang>/`, and later a
  `tck/runtime/<lang>/`.

## Decision

**Cut the repository by target language. Each language owns one top-level folder that holds its generator
configuration, its hand-written modules and its generated modules.**

```
sdk-<lang>/
  generator.properties      the generator configuration for this language
  <hand-written modules>    support/, protobuf/, …
  generated/                the generated API
  tck/generated/            the generated TCK contract and server
  tck/runtime/              the hand-written TCK runtime

tck/                        only the language-NEUTRAL parts: bindings/, solo.env, run-tck.sh
```

ADR-0007 is **unchanged in substance**: its invariant is about *modules*, not directories — "a module is the unit a
language builds and publishes: a Maven module / JPMS module in Java, an npm package in TypeScript, a crate in
Rust". A `sdk-java/` that contains both `support/` (hand-written) and `generated/` (generated) does not mix the two
inside any module. What changes is only *where* the `generated/` directory sits; the marker stays in every path
(`sdk-java/generated/...`).

Two rules keep the distinction visible:

1. **`generated/` always appears in the path of a generated module**, so "do not edit by hand" is readable from the
   path alone, exactly as before.
2. **Language-neutral things stay out of `sdk-<lang>/`.** The TCK bindings are declared once and generated per
   language, so they remain in `tck/`, together with `run-tck.sh` and the Solo configuration.

The npm workspace moves to `sdk-ts/`, which is now the common ancestor of every TypeScript module.

## Consequences

**Good:**

- One language is one folder. Adding Python, C++ or Swift means adding one directory, not three.
- The repository root loses `generated/`, `package.json` and `package-lock.json`.
- ADR-0007's consequence table becomes more precise: the npm workspace root is `sdk-ts/` rather than "the
  repository root", which was an artefact of the old layout.
- Per-language ownership, CI jobs and `CODEOWNERS` entries map onto a single path.

**Costs:**

- About 2,400 generated files moved. They are generated, so the move is mechanical; `sdk-rust/generated` and
  `sdk-go/generated` were regenerated at the new path and produced `0 changed`, which confirms the generators are
  path-independent.
- `sdk-java/generated` and `sdk-ts/generated` currently carry the hand-edited `AccountCreateTransaction` spike.
  They were moved with `git mv` rather than regenerated, and the relative paths inside them
  (`tsconfig` project references) were adjusted by hand. A regeneration reproduces them from `ts.support` in
  `sdk-ts/generator.properties`, which was updated accordingly.
- 121 path references in documentation, scripts and build files were rewritten.

**Neutral:**

- No generator code changed. Output locations are command-line arguments, and the only path-shaped configuration
  values are `ts.support` (updated) and `rust.protobufRoot` (unchanged — the crate directories have the same depth
  relative to the repository root as before).

## Alternatives considered

- **Keep the layout and only merge `sdk-<lang>/` into `generated/<lang>/`.** Rejected: it would put hand-written
  modules under a directory named `generated/`, which breaks the one signal ADR-0007 relies on.
- **Move only the TypeScript modules under `sdk-ts/`** to free the root of `package.json`. Rejected as
  inconsistent: it would solve the npm problem but leave Java, Rust and Go spread out.
- **Leave everything as it was.** Rejected: the one-file `sdk-rust/` and `sdk-go/` folders and the root-level
  `package.json` are symptoms of a cut that does not match how the repository is worked on.
