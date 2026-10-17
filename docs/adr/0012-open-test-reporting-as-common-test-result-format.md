# ADR-0012: Open Test Reporting is the common format of all test results

**Status:** Accepted
**Date:** 2026-10-17

## Context

The repository runs tests in several languages and wants to read their results in one way — by people and by agents —
and to compare the **same** test across languages: a spec test of a `tests` block is generated into Java, TypeScript and
Rust, a contract test into every language with a generator.

### What the test runs produce today

- **Java** (`sdk-java/impl`, `sdk-java/generated`, `tooling/metalang`): Surefire XML; `impl` uses JUnit 6.0.3 and
  Surefire 3.5.3 [1].
- **TypeScript** (`sdk-ts/generated`): `node:test`, console output; JUnit XML only inside the tooling tests [2], where
  every test case had `classname="test"` (measured in this repository on 2026-10-17), so the test classes are lost.
- **Rust** (`sdk-rust/generated`): `cargo test` console output only. Its event output is not available on the stable
  toolchain: `cargo test -- --format json` fails with *"only accepted on the nightly compiler with -Z unstable-options"*
  (measured with Rust 1.98.0 on 2026-10-17).
- **TCK**: mocha with the `mochawesome` reporter in parallel mode [3]. The report of the run of 2026-10-17 holds only
  the totals (1,436 tests, 431 passed, 663 failed, 342 pending), no single test result (measured).
- The classification that matters here — passed, failed at a stub (`Not implemented yet`), failed at the network, failed
  otherwise — has been computed by throw-away scripts each time.

### The candidate: Open Test Reporting

Open Test Reporting (OTR) is a language-agnostic XML format with tooling, maintained by the JUnit team [4]:

- An event-based form: `events` with `started` (`id`, `name`, `time`, optional `parentId` for the hierarchy),
  `reported` and `finished`; a `result` with the status `SUCCESSFUL`, `SKIPPED`, `ABORTED`, `FAILED` or `ERRORED` and an
  optional `reason`; attachments `data`, `file` and `output`; `metadata/tags`; and extension points for other
  namespaces in `metadata`, `sources`, `attachments` and `result` [5].
- JUnit writes it natively with the artifact `org.junit.platform:junit-platform-reporting` and the configuration
  parameter `junit.platform.reporting.open.xml.enabled` into `open-test-report.xml` [6]. A probe with JUnit 6.0.3 and
  Surefire 3.5.4 showed: a `@Tag` becomes `metadata/tags/tag`, `TestReporter.publishEntry` becomes `attachments/data/entry`,
  an exception becomes `result/java:throwable` with its message, and a test that throws
  `UnsupportedOperationException("Not implemented yet: …")` is `FAILED` (measured, 2026-10-17).
- The CLI (`open-test-reporting-cli` 0.2.7, standalone jar on Maven Central) offers `validate`, `convert` (event-based
  to hierarchical) and `html-report` (one HTML file, also from several input files); it validated the probe and
  rendered names, tags, data entries and the failure message (measured, 2026-10-17) [7].
- The only implementation is the Java reference [4]. The schema is at version 0.2.0 [5]; the project is licensed under
  Apache-2.0, had releases r0.2.6 (2026-06-27) and r0.2.7 (2026-07-25) and commits on 2026-10-17 (GitHub, accessed
  2026-10-17) [8].
- Other producers can be built from event sources that exist: mocha calls a reporter for suite and test events, in
  parallel mode buffered and reported per finished file [9]; `node:test` has a reporter API over a stream of test
  events [10]; cargo-nextest writes JUnit XML [11].

## Decision

We will use **Open Test Reporting (event-based XML)** as the common format of all test results of the repository: the
spec and contract tests of every SDK language, the TCK runs, and the tests of `tooling/metalang`. The results of the
tooling stay separate from those of the SDKs.

- **Additionally, not instead:** the existing outputs (Surefire XML, mochawesome, console) stay for now.
- **Producers:** Java writes OTR natively. TypeScript (`node:test`) and the TCK (mocha) get small reporters of our own;
  the TCK is run with our reporter, the TCK repository is not changed. Rust uses **cargo-nextest** once Rust is
  implemented. Go and Python follow when they have tests. A source that can only write JUnit XML may be converted.
- **Only the standard means of OTR** carry our data: tags (the test id), data entries, output. We define no schema or
  namespace of our own.
- **Test identity across languages:** a spec test gets a new annotation `@@testId("…")`; its full id is the namespace
  plus that id, unique **per namespace** across all spec files of the namespace (a namespace can span several files —
  `consensusnode.transactions.tokens` spans three), checked by the validator. A contract test is identified by
  `namespace.Type` plus a language-neutral check id that every generator derives from one scheme in the shared
  generator core.
- **Rendering, first step:** the HTML report of the OTR CLI. The views it lacks are **required** follow-ups (see
  below), not options.
- **The first step covers Java only.**

Rejected:

- **JUnit XML as the common format:** there is no official specification; Surefire's output is the de facto reference
  that other tools imitate [12]. It is flat (suite → test case), and in TypeScript it already lost the test classes.
  It stays acceptable as an input that is converted.
- **CTRF:** a JSON schema with reporters and a JUnit converter, but before 1.0 [13] like OTR, with a flat list of tests
  instead of a hierarchy and no native producer in Java, our reference language.
- **Allure:** a rich report, but every framework writes its results through its own adapter [14] — more to install
  and maintain than the few events we need.
- **TAP:** `node:test` writes it [10], but it is a line protocol for humans and streams without structured metadata,
  attachments or tags.

## Consequences

### Positive
- One format for all languages, the TCK and the tooling; one validator and one HTML report for all of them.
- Java needs no code, only a dependency and a configuration parameter.
- Test ids make the same test comparable across languages and the TCK results readable per test.
- The format leaves room: hierarchy, attachments and extension points cover later needs without a new format.

### Negative
- The schema is before 1.0 and has a single implementation; a schema change can force changes in our reporters.
- We write and maintain reporters for TypeScript and the TCK (and later Go, Python): code that exists only for the
  report.
- The test language changes: a new annotation, a validator rule and ids for every existing spec test (386 today).
- Failures carry their details in different places (`java:throwable` from Java, `reason` from our reporters), so the
  classification has to read both.
- In the TCK's parallel mode the events of a file arrive when the file is finished [9]; the times in the report are
  then those of the reporter, durations are approximate.
- Until the old outputs are removed, every run writes two kinds of result files.
- Surefire 3.5.4 is the version of JUnit's documented setup [6]; `impl` uses 3.5.3 [1] and must be checked or upgraded.

### Follow-ups
- Java: add `junit-platform-reporting` and the configuration parameter to `sdk-java` and `tooling/metalang`; emit the
  test id as a tag.
- Test language: `@@testId` (grammar, `KnownAnnotation`, validator rule with fixture, guideline) and ids for all spec
  tests; the scheme of the language-neutral check ids of contract tests in the generator core.
- Define the syntax of the tag that carries the test id, the same in every language.
- Reporters for `node:test` and the TCK (mocha); Rust with cargo-nextest (which of its outputs is used is decided then).
- **Required:** a `metalang report` command that reads OTR and produces the views the CLI lacks: the matrix spec test ×
  language and the classification passed / stub / network / other.
- Open: when the existing outputs are removed; how a standstill of the OTR project is measured and who watches it.
- **Revisit** this decision if the OTR project stands still. Gaps in the format are closed by contributing to it.

---

**References:**
1. `sdk-java/impl/pom.xml:25` (`junit.version` 6.0.3), `sdk-java/impl/pom.xml:71-72` (Surefire 3.5.3)
2. `tooling/metalang/metalang-typescript/src/test/java/org/hiero/sdk/v3/metalang/generator/ts/GeneratedTs.java:130`
3. `hiero-sdk-tck/package.json`, script `test` (`--parallel --jobs 7 … --reporter mochawesome`), revision `a3ea777`
4. Open Test Reporting: https://github.com/ota4j-team/open-test-reporting (accessed 2026-10-17)
5. Schemas `events-0.2.0.xsd` and `core-0.2.0.xsd`:
   https://github.com/ota4j-team/open-test-reporting/tree/main/schema/src/main/resources/org/opentest4j/reporting/schema
   (accessed 2026-10-17)
6. JUnit User Guide, JUnit Platform Reporting:
   https://docs.junit.org/6.0.3/advanced-topics/junit-platform-reporting.html (accessed 2026-10-17)
7. `org.opentest4j.reporting:open-test-reporting-cli:0.2.7` (`-standalone.jar`):
   https://repo1.maven.org/maven2/org/opentest4j/reporting/open-test-reporting-cli/0.2.7/ (accessed 2026-10-17)
8. GitHub API for `ota4j-team/open-test-reporting`: releases, last commit, `LICENSE.md` (Apache License 2.0), 114
   stars, 20 open issues (accessed 2026-10-17)
9. Mocha, parallel mode: https://mochajs.org/features/parallel-mode/ (accessed 2026-10-17)
10. Node.js `node:test` reporters (type definitions): https://cdn.jsdelivr.net/npm/@types/node@25.9.1/test/reporters.d.ts
11. cargo-nextest / quick-junit: https://quick-junit.nexte.st/
12. JUnit XML reports (Surefire as de facto format): https://howtodoinjava.com/junit5/xml-reports/
13. CTRF: https://github.com/ctrf-io ; TypeScript reference README: https://cdn.jsdelivr.net/npm/ctrf@0.2.1/README.md
14. Allure Report 3: https://allurereport.org/docs/v3/
