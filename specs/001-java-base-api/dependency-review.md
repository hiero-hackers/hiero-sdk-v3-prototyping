# Dependency Review: Java Base Public API Prototype

**Assessment date**: 2026-08-04

**Status**: Approved by accountable human reviewer

**Gate**: No public, build, test, plugin, wrapper, or transitive dependency may be adopted until this
review is complete. AI agents may collect evidence and make recommendations but cannot approve a
dependency.

## Review Scope

The review covers the planned JSpecify compile-time annotation dependency, JUnit test dependencies,
Maven build plugins, Maven Wrapper artifacts, and every transitive dependency they introduce. The
published `hiero-sdk-base-api` artifact must have no runtime dependency beyond Java 21 platform
modules.

## Proposed Baseline

| Dependency or artifact | Exact version and scope | Necessity and alternatives | Maintenance and provenance | License and transitive compatibility | Public API or JPMS impact | Recommendation |
|---|---|---|---|---|---|---|
| `org.jspecify:jspecify` | `1.0.0`; `provided` in the API module and `test` where contract fixtures compile consumer code | Required by the Java guideline for explicit public nullness. Platform annotations do not provide equivalent type-use nullness semantics. | First stable release, published 2024-07-16; signed artifacts and SHA-256/SHA-512 checksums are available from Maven Central; source is maintained by the JSpecify project. | Apache-2.0; its published POM declares no dependencies. | Its annotations appear in public signatures. JPMS must use `requires static org.jspecify`; it must not be required at runtime or packaged into the API JAR. | Provisional accept; human API and dependency approval required. |
| `org.junit:junit-bom` | `6.1.2`; `import` in `dependencyManagement` for test modules only | Keeps all JUnit modules on one release. Explicit independent versions are more error-prone. | JUnit 6.1.2 was released 2026-07-12; JUnit 6 has a Java 17 baseline and is compatible with Java 21. Artifacts are published from the JUnit project to Maven Central. | EPL-2.0; version-management POM only. JUnit remains test-only and is not redistributed in the API artifact. | None. It must not occur in the API POM dependency graph, module descriptor, public signatures, or packaged JAR. | Provisional accept; security and dependency approval required. |
| `org.junit.jupiter:junit-jupiter` | `6.1.2`; `test` in the contract-test and generator modules only | Supplies Jupiter API, parameterized tests, and the engine required by the verification plan. JDK assertions or a custom harness would lose discovery, parameterization, and standard reports. | Current stable JUnit release as of the assessment date; active release notes and supported project security policy. | EPL-2.0 direct modules; exact transitives are listed below and are EPL-2.0 or Apache-2.0. | None; test classpath only. Vintage, suite, reporting, launcher, and console artifacts are excluded unless separately reviewed. | Provisional accept; human dependency and security approval required. |
| Apache Maven | `3.9.16`; build tool downloaded by wrapper | Provides the reactor, lifecycle, plugin model, JPMS compilation, tests, source/Javadoc artifacts, and reproducible entry point selected in the plan. Maven 4 is still a release candidate and is not accepted. | Apache identifies 3.9.16 as the current recommended Maven 3 release. The binary is signed and accompanied by an Apache SHA-512 checksum. | Apache-2.0. Maven and its internal libraries execute only as build tooling and are not application dependencies. | None in the API artifact. The Enforcer rule must reject Maven versions other than the approved compatible range. | Provisional accept; human build and security approval required. |
| Apache Maven Wrapper plugin | `org.apache.maven.plugins:maven-wrapper-plugin:3.3.4`; setup-time only | Creates cross-platform pinned wrapper scripts. Requiring a manually installed Maven version is less reproducible. | Current stable wrapper plugin in the Apache plugin index; Apache source and Maven Central provenance. | Apache-2.0. Use `only-script`, so no wrapper JAR or downloader source is committed. | None. Wrapper scripts are repository tooling and may only download the pinned Maven distribution. | Provisional accept; human build and security approval required. |
| Maven lifecycle and verification plugins | Exact versions and plugin-realm overrides listed below; build/test only | Pins every lifecycle goal that this feature invokes and supports compilation, unit tests, packaging, documentation, dependency checks, and deterministic generator execution. | Current stable non-preview releases from Apache Maven or MojoHaus as of the assessment date. | Apache-2.0 for Apache and MojoHaus plugins. The remediated graph is locked in `dependency-inventory.json`; all 192 records have identified licenses. | None. Plugins and their dependencies must not enter the API dependency tree or JAR. | Provisional accept; human dependency and security approval required. |

## JUnit Transitive Inventory

The selected `junit-jupiter:6.1.2` aggregator resolves to the following unique artifacts according to
the published Maven Central POMs. All remain in Maven `test` scope in this project.

| Coordinate | Effective role | License | Further dependencies |
|---|---|---|---|
| `org.junit.jupiter:junit-jupiter-api:6.1.2` | Test authoring API | EPL-2.0 | Platform Commons, OpenTest4J, API Guardian, JSpecify |
| `org.junit.jupiter:junit-jupiter-params:6.1.2` | Parameterized test API | EPL-2.0 | Jupiter API, API Guardian, JSpecify |
| `org.junit.jupiter:junit-jupiter-engine:6.1.2` | Test runtime engine | EPL-2.0 | Platform Engine, Jupiter API, API Guardian, JSpecify |
| `org.junit.platform:junit-platform-engine:6.1.2` | Test engine SPI/runtime | EPL-2.0 | Platform Commons, OpenTest4J, API Guardian, JSpecify |
| `org.junit.platform:junit-platform-commons:6.1.2` | Shared JUnit utilities | EPL-2.0 | API Guardian, JSpecify |
| `org.opentest4j:opentest4j:1.3.0` | Assertion failure abstraction | Apache-2.0 | None in its published POM |
| `org.apiguardian:apiguardian-api:1.1.2` | API status annotations | Apache-2.0 | None in its published POM |
| `org.jspecify:jspecify:1.0.0` | Nullness annotations used by JUnit | Apache-2.0 | None in its published POM |

**Security finding**: CVE-2025-53103 / GHSA-m43g-m425-p68x affected the optional
`junit-platform-reporting` listener in JUnit 5.12.0 through 5.13.1 and was fixed in 5.13.2. The
selected 6.1.2 line is newer than the patched version, and `junit-platform-reporting` is not in the
approved graph. Open Test Reporting and Git metadata collection must remain disabled unless they
receive a separate security review.

## Maven Plugin Inventory

Only the following plugins are candidates for the reactor. `pluginManagement` must pin these exact
versions; a plugin is activated in `<plugins>` only where its listed purpose is required.

| Coordinate | Version | Intended use | Why it is needed |
|---|---|---|---|
| `org.apache.maven.plugins:maven-clean-plugin` | `3.5.0` | `clean` lifecycle | Removes generated build output for clean-room verification. |
| `org.apache.maven.plugins:maven-resources-plugin` | `3.5.0` | Resource lifecycle | Copies generator and test resources with an explicit lifecycle version. |
| `org.apache.maven.plugins:maven-compiler-plugin` | `3.15.0` | Main and test compilation | Enforces `--release 21`, warnings, and JPMS compilation. |
| `org.apache.maven.plugins:maven-surefire-plugin` | `3.5.5` | Unit and contract tests | Latest stable non-milestone Surefire line; `3.6.0-M1` is excluded as a preview. |
| `org.apache.maven.plugins:maven-jar-plugin` | `3.5.1` | API JAR packaging | Creates the modular API JAR with reproducible archive configuration. |
| `org.apache.maven.plugins:maven-source-plugin` | `3.4.0` | Source artifact | Produces the reviewable generated source JAR required by the plan. |
| `org.apache.maven.plugins:maven-javadoc-plugin` | `3.12.0` | Javadoc verification/artifact | Validates source-linked public docs and produces the Javadoc JAR. |
| `org.apache.maven.plugins:maven-enforcer-plugin` | `3.6.3` | Build policy | Enforces Java/Maven baselines, dependency convergence, and banned scopes. |
| `org.apache.maven.plugins:maven-dependency-plugin` | `3.11.0` | Dependency verification | Produces dependency evidence and checks unused or leaked dependencies. |
| `org.apache.maven.plugins:maven-wrapper-plugin` | `3.3.4` | Wrapper setup only | Generates the reviewed `only-script` wrapper files; it is not bound to the reactor lifecycle. |
| `org.codehaus.mojo:exec-maven-plugin` | `3.6.3` | Generator profiles | Runs the repository-owned Java generator without making it an API dependency. |

### Required Plugin-Realm Overrides

The direct plugin releases above are not acceptable with their published transitive graphs. The
following exact plugin dependencies must be declared inside the corresponding `<plugin>` elements.
They replace known vulnerable transitives only within isolated Maven plugin realms and do not alter
the project or consumer dependency graph.

| Plugins | Required override | Security basis |
|---|---|---|
| Clean, Compiler, Source, Javadoc, Wrapper, Exec | `org.codehaus.plexus:plexus-utils:4.0.3` | Replaces vulnerable `4.0.2`; GHSA-6fmv-xxpf-w3cw is fixed in `4.0.3`. |
| Resources | `org.codehaus.plexus:plexus-utils:3.6.1` | Replaces vulnerable `3.6.0`; GHSA-6fmv-xxpf-w3cw is fixed in `3.6.1`. |
| Compiler | `commons-io:commons-io:2.21.0` | Replaces vulnerable `2.11.0`; GHSA-78wr-2p64-hpwj is fixed in `2.14.0` and later. |
| Source, Javadoc, Wrapper | `io.airlift:aircompressor:2.0.3` | Replaces vulnerable `0.27`; GHSA-vx9q-rhv9-3jvg is fixed in `2.0.3`. |
| Javadoc, Dependency | `commons-beanutils:commons-beanutils:1.11.0` | Replaces vulnerable `1.9.4`; GHSA-wxr5-93ph-8wr9 is fixed in `1.11.0`. |

The following are intentionally excluded: Failsafe, Surefire reporting, Site, Shade, Assembly,
Checkstyle, PMD, GPG, Deploy, and third-party code-generation plugins. Adding any of them requires a
new review entry and approval.

### Machine-Resolved Review Result

[dependency-inventory.json](dependency-inventory.json) records 192 artifact records covering 178
unique coordinates: project/test dependencies, all reviewed plugin realms, two wrapper artifacts,
and 52 JARs shipped in Maven 3.9.16. Every artifact has an exact version, SHA-256, license, and
origin; no artifact or license entry is missing.

The initial unmodified plugin graph produced five high-severity OSV findings across Commons
BeanUtils 1.9.4, Commons IO 2.11.0, Aircompressor 0.27, and Plexus Utils 3.6.0/4.0.2. That graph is
rejected. After applying the exact overrides above, all 177 executable Maven package/version
queries returned zero OSV findings on 2026-08-04; the non-executable JUnit BOM POM is recorded but
excluded from that query count. The inventory preserves the initial findings, final query and
response hashes, exact override-to-plugin mapping, excluded implicit Super POM plugins, and
artifact-level evidence.

An offline Maven 3.9.16 compatibility run loaded or executed Clean, Resources, Compiler, Surefire,
Source, Javadoc, Dependency, Wrapper, and Exec goals successfully with the overrides. This proves
plugin-realm resolution and basic linkage only. T002-T004 must exercise the same overrides against
the real reactor, and any linkage failure or changed resolved graph invalidates the provisional
recommendation.

## Maven Wrapper Integrity Record

The wrapper configuration proposed for T002 is:

| Property | Required value |
|---|---|
| Wrapper implementation | Apache Maven Wrapper `3.3.4`, `only-script` type |
| `distributionUrl` | `https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.zip` |
| `distributionSha256Sum` | `5af3b743dd8b876b5c45da33b676251e5f1687712644abb4ee519ca56e1d89ce` |
| Committed wrapper binary | None; `.mvn/wrapper/maven-wrapper.jar` is prohibited |
| Allowed download hosts | `repo.maven.apache.org` for the pinned distribution; repository-manager substitution requires renewed provenance review |

The SHA-256 above was calculated from the Apache-hosted Maven 3.9.16 binary after its SHA-512
(`ed41650d42485cfc243fad22158caf9cbb5dc408ce7a09ddb94dd42a019de929ca43065bfa450612cf12bf78b5cafa3884b96c090de326ff590448c933454af3`)
matched Apache's published checksum. The Maven Central distribution was downloaded independently
on 2026-08-04, produced the same SHA-256, and was byte-identical to the Apache-hosted binary.

## Security and Update Controls

- Resolve release artifacts from Maven Central only. Snapshots, milestones, release candidates,
  dynamic versions, version ranges, and unreviewed repositories are prohibited.
- Pin every direct dependency, BOM, plugin, and wrapper distribution. Record SHA-256 values in the
  transitive lock evidence before approval.
- Run a current vulnerability and license scan over dependencies and plugin dependencies before
  approval and in CI. A clean scan is evidence, not a substitute for human review.
- Configure Maven checksum policy to fail on mismatch. The wrapper must validate
  `distributionSha256Sum` before executing Maven.
- Use `mvn dependency:tree`, the effective POM, JPMS analysis, and JAR inspection to prove JUnit,
  plugin, generator, and wrapper artifacts do not leak into `hiero-sdk-base-api`.
- The API module may resolve JSpecify only while compiling. Runtime `jdeps` output must contain only
  approved Java platform modules, and the module descriptor may contain only
  `requires static org.jspecify` beyond platform requirements.
- Review versions at implementation start and at least every 90 days while this prototype remains
  active. Any coordinate, version, checksum, scope, repository, transitive graph, license,
  maintenance-status, or advisory change invalidates the corresponding approval.

## Evidence Sources

- [JSpecify 1.0.0 release](https://github.com/jspecify/jspecify/releases/tag/v1.0.0),
  [license](https://github.com/jspecify/jspecify/blob/main/LICENSE), and
  [Maven Central artifacts](https://repo.maven.apache.org/maven2/org/jspecify/jspecify/1.0.0/)
- [JUnit 6.1.2 release notes](https://docs.junit.org/6.1.2/release-notes.html),
  [security policy](https://github.com/junit-team/junit-framework/security), and
  [CVE-2025-53103 advisory](https://github.com/advisories/GHSA-m43g-m425-p68x)
- [Apache Maven 3.9.16 download and verification record](https://maven.apache.org/download.cgi)
- [Apache Maven plugin index](https://maven.apache.org/plugins/index.html) and
  [Surefire documentation](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [Apache Maven Wrapper documentation](https://maven.apache.org/tools/wrapper/) and
  [Exec Maven Plugin 3.6.3 documentation](https://www.mojohaus.org/exec-maven-plugin/plugin-info.html)
- [OSV findings for BeanUtils](https://osv.dev/vulnerability/GHSA-wxr5-93ph-8wr9),
  [Commons IO](https://osv.dev/vulnerability/GHSA-78wr-2p64-hpwj),
  [Aircompressor](https://osv.dev/vulnerability/GHSA-vx9q-rhv9-3jvg), and
  [Plexus Utils](https://osv.dev/vulnerability/GHSA-6fmv-xxpf-w3cw)

## Approval Resolution

- No dependency-review blockers remain. The accountable decisions below were explicitly provided by
  the human repository owner; the AI agent only recorded those decisions.

## Approval Conditions

- Every artifact and transitive dependency has an exact version and intended scope.
- Each dependency is necessary, actively maintained, and has no simpler platform or repository-local
  alternative that satisfies the requirement.
- Licenses are identified and confirmed compatible with the project and intended artifact usage.
- Artifact origin, integrity controls, known vulnerability review, and update policy are recorded.
- Only approved compile-time annotation types may appear in public signatures or JPMS metadata.
- Build and test dependencies cannot leak into the generated API artifact or its runtime graph.
- Any coordinate, version, scope, transitive graph, license, or maintenance-status change requires
  renewed review before adoption.

## Accountable Approval

| Role | Name | Decision | Date | Evidence or conditions |
|---|---|---|---|---|
| Dependency reviewer | `housniabdellatif` | Approved | 2026-08-04 | Explicit human approval of the direct and complete transitive inventory and the approval conditions above |
| Java API maintainer | `housniabdellatif` | Approved | 2026-08-04 | Explicit human approval of JSpecify in public signatures and `requires static` JPMS metadata |
| Security reviewer | `housniabdellatif` | Approved | 2026-08-04 | Explicit human approval of build execution, wrapper provenance, advisory results, and leakage controls |
