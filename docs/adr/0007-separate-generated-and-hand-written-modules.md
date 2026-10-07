# ADR-0007: Every module is either completely generated or completely hand-written

**Status:** Accepted (implemented for Java and TypeScript; Rust follows)
**Date:** 2026-10-04

## Context

The V3 API is specified once in the meta-language (`spec/`) and translated into each language by the generators in
`tooling/metalang`. Besides the API itself, generated code needs code that is **not** derived from the specs:

- **Support types** that appear in the generated API but that no spec declares — e.g. the thread-safety annotation,
  the streaming types (`HieroStream`, `StreamItem`, …), `Duration` in TypeScript, `BoxFuture` in Rust.
- **Runtimes** that generated code calls but cannot derive — e.g. the TCK server ([`tck-binding.md`](../tck-binding.md)),
  whose request handlers are generated from bindings, while the JSON-RPC server, the value converters and `setup`
  are written per language.

Two patterns had grown for this, and both mixed the two kinds of code:

1. **Copying.** The hand-written support files (`guidelines/java-files`, `guidelines/ts-files`,
   `guidelines/rust-files`) were copied 1:1 into the generated projects, and the first version of the TCK server
   copied its hand-written runtime into the generated server the same way. A generated module then contained files
   whose real source lived elsewhere, and "generated" no longer told a reader whether a file could be regenerated or
   had to be maintained.
2. **Implicit contracts.** After the copying was replaced by a dependency, the generated TCK server called the
   hand-written runtime by name (`Params.value(...)`, `Converters::accountId`, …). The contract existed only in string
   templates of the generator; nothing failed when the runtime and the generator drifted apart, and the converter
   names of the core catalogue had to match method names of every runtime without any check.

### Forces

- **Regenerability.** A generated module must be reproducible from versioned inputs at any time, so that a change of
  the specs or the generator shows up as a reviewable diff, and stale files can be removed automatically.
- **Ownership.** For every file it must be obvious whether it is maintained by hand or by the generator — for
  reviewers, for agents working in the repository, and for the build that deletes stale generated files.
- **Drift.** When generated code needs something from hand-written code, a change on one side must break the build of
  the other side, not a test run or a TCK run much later.
- **Languages.** The rule must hold for Java, TypeScript and Rust (and later Go, Python, C++, Swift), whose module and
  linking models differ.

## Decision

**1. Every module is either completely generated or completely hand-written.** A *module* is the unit a language
builds and publishes: a Maven module / JPMS module in Java, an npm package in TypeScript, a crate in Rust.

- A **generated module** consists only of files written by a generator. Every file carries the generator's header
  line, it is reproducible from versioned inputs (specs, generator configuration, TCK bindings, the generator itself),
  it is never edited by hand, and files the generator no longer produces are deleted by it. Generated modules live
  under a `generated/` directory, so the path itself says "do not edit" — since
  [ADR-0008](0008-one-folder-per-target-language.md) that directory is `sdk-<lang>/generated/` rather than a single
  top-level `generated/`.
- A **hand-written module** contains no generated files in version control. Code that a build tool derives from a
  hand-written source of the same module (e.g. the ANTLR parser of `MetaLang.g4`) is build output and never
  committed.

**2. Generated code never contains copies of hand-written code.** Hand-written code that generated code needs is its
own hand-written module, and the generated module declares a dependency on it.

**3. The interface between the two sides is explicit, in one of two forms:**

- **Support module** — for a small, fixed vocabulary that does not depend on the generator's inputs (annotations,
  streaming types, `Duration`, …). The generated module depends on the hand-written support module at compile time.
  The generator references these types by name in one place (Java: `SupportFiles`), and the generator tests compile
  the generated code against the real support module.
- **Generated contract, hand-written implementation** — whenever what the generated code needs follows from the
  generator's inputs (e.g. one conversion per entry of the TCK converter catalogue). The generator also generates
  the contract — declarations only (interfaces / traits / types and records), no implementation. The hand-written
  module implements the contract; the generated code is compiled against the contract only and receives the
  implementation at runtime. A change of the inputs changes the contract and thereby breaks the build of every
  implementation that does not follow.

The dependency arrows therefore are: *generated → hand-written support module* (compile time), *hand-written
implementation → generated contract* (compile time), and *generated code → hand-written implementation* only at
runtime. How the implementation is found is language-specific.

**4. The language mapping:**

| | Java (implemented) | TypeScript (implemented) | Rust (next) |
|---|---|---|---|
| Module | Maven + JPMS module | npm package | crate |
| Support module | `sdk-java/support` (`hiero-sdk-support`, `org.hiero.sdk.support`) | `sdk-ts/support` (`@hiero/support`), linked into the generated workspace (npm workspaces, `tsconfig` references; location from `ts.support`) | from `guidelines/rust-files` |
| Contract form | interfaces and records | interfaces / types | traits and types |
| Finding the implementation | `java.util.ServiceLoader`, implementation as `runtime`-scope dependency | dynamic `import` of the implementation's package name (the contract declares the module shape); the server's `tsconfig` does not reference the implementation | to be decided |
| Linking hand-written and generated packages | Maven repository (`install`) | npm workspace at `sdk-ts/`: a package resolves its imports from its real path, so all TypeScript modules need a common parent `node_modules` (ADR-0008 made `sdk-ts/` that common parent; before it, it had to be the repository root) | to be decided |

**Rejected alternatives:**

- *Keep copying support files into generated modules.* Rejected: generated modules would again contain files that
  cannot be regenerated from the inputs alone, and the copies have to be kept identical by tests.
- *Let generated code call hand-written code without a contract (convention by name).* Rejected: the contract lives
  only in the generator's templates; a renamed method or a missing converter is noticed late, and only for the
  parts the current inputs happen to use.
- *Generate the support and runtime code from templates inside the generator.* Rejected: the code would still be
  hand-written, only hidden in string literals of the generator, where it is neither compiled nor reviewed as code.

## Consequences

### Positive

- Every file has exactly one owner, visible from its location and its header: `generated/` is regenerated and
  never edited; everything else is maintained by hand.
- Generated modules are fully reproducible; the generator can delete stale files safely, and the conformance check
  (`metalang check`) compares a project against the API generated in memory.
- Drift between generator and hand-written code fails the build: a converter added to the catalogue breaks every
  runtime that lacks it, and the TCK server cannot reach runtime internals because it is compiled without the
  runtime.
- Hand-written modules are ordinary projects with their own build and tests, independent of the generator.

### Negative

- More modules and a fixed build order (Java: support → generated API → generated contract → runtime → generated
  server). Versions of hand-written modules (`0.1.0-SNAPSHOT`) must match the generator configuration; nothing checks
  this yet.
- Declarations of a contract that do not follow from inputs (e.g. `TckRuntime`, which describes the binding flow) are
  fixed text in the generator. They change only with the binding language, but they are hand-written content that is
  emitted by the generator.
- The runtime lookup (e.g. `ServiceLoader`) adds indirection compared to a direct call.

### Follow-ups

- Rust: move `guidelines/rust-files` into a hand-written support crate that the generated crates depend on.
- Decide for Rust how a generated contract finds its implementation, when the TCK server is generated for it.
- The implementations of the API itself: today the generated API contains method stubs. By this decision the real
  implementation cannot be written into `generated/`; whether it follows the contract pattern (generated API types as
  contract, hand-written implementation module) has to be decided before the first method is implemented.
- Check that the versions of hand-written modules match the generator configuration.

---

**References:**

- [`tck-binding.md`](../tck-binding.md) — TCK bindings, contract (`sdk-java/tck/generated/contract`), runtime
  (`sdk-java/tck/runtime`) and server (`sdk-java/tck/generated/server`)
- `sdk-java/support` — Java support module; `tooling/metalang/metalang-java/.../generator/java/SupportFiles.java`
- `sdk-ts/support` — TypeScript support package; `ts.support` in `sdk-ts/generator.properties`
- `tooling/metalang/metalang-java/.../generator/java/JavaTckContractGenerator.java`, `JavaTckGenerator.java`
- `tooling/metalang/metalang-core/.../tck/Converter.java` — the converter catalogue the contract is derived from
- `tooling/metalang/README.md` — build order and generator documentation
