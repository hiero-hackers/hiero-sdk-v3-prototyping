# SDK V2 testing infrastructure

## Purpose and scope

This document describes how the V2 Java SDK organizes and runs its tests. It is
an architectural inventory, not a requirement that V3 reproduce the Java test
implementation.

The important boundary for V3 is:

- test scenarios are programming-language agnostic;
- each SDK implementation supplies its own test adapter and test framework;
- all implementations exercise the same observable behavior against a
  compatible Hiero network.

The companion [integration-test scenario catalog](../../guidelines/integration-tests-scenarios/README.md)
translates the V2 Java integration suite into language-agnostic Given/When/Then
scenarios. The normative scenario conventions are defined in
[testing-guideline.md](../../guidelines/testing-guideline.md).

### Inspected snapshot

| Item | Value |
| --- | --- |
| Repository | `hiero-sdk-java` |
| Branch | `main` |
| Commit | `32825b9232bb00b8e4553e783400b47b4ba8a10e` |
| Commit date | 2026-08-24 |
| Inspection date | 2026-08-25 |

Paths in this document are relative to the V2 `hiero-sdk-java` repository.

## Architecture overview

The V2 repository has four complementary verification layers:

```text
                         Gradle build
                              |
          +-------------------+-------------------+
          |                   |                   |
       Unit tests       Integration tests      TCK adapter
       sdk:test         sdk:testIntegration     tck:test / bootRun
          |                   |                   |
  values, codecs,       Java SDK client      JSON-RPC contract
  builders, mocks       + funded operator    exposed to TCK driver
                              |
                        Hiero Solo network
                    +---------+----------+
                    |                    |
              consensus node        mirror node

Additional smoke coverage: examples:runAllExamples against Solo
Combined code coverage: aggregation:testCodeCoverageReport
```

The layers answer different questions:

| Layer | Primary question | Requires a network? |
| --- | --- | --- |
| Unit tests | Does an individual Java type behave correctly in isolation? | No |
| Integration tests | Does the SDK perform the expected operation against a real network? | Yes |
| Example smoke tests | Do documented usage examples still execute end to end? | Yes |
| TCK adapter tests | Does the Java JSON-RPC adapter accept and dispatch TCK requests correctly? | No for adapter unit tests; yes for full TCK execution |

## Source sets and Gradle tasks

The `sdk` module applies `org.hiero.gradle.feature.test-integration` in
`sdk/build.gradle.kts`. That convention adds the integration-test source set
and its Gradle task alongside the standard Java unit-test source set.

| Purpose | Source location | Gradle task |
| --- | --- | --- |
| SDK unit tests | `sdk/src/test/java` | `test` or `:sdk:test` |
| SDK integration tests | `sdk/src/testIntegration/java` | `testIntegration` or `:sdk:testIntegration` |
| TCK adapter unit tests | `tck/src/test/java` | `:tck:test` |
| Example smoke tests | `examples/src/main/java` | `:examples:runAllExamples` |
| Combined coverage | `gradle/aggregation` | `:aggregation:testCodeCoverageReport` |

The V2 snapshot contains the following source inventory:

| Suite | Java files | Plain `@Test` declarations | Other observations |
| --- | ---: | ---: | --- |
| SDK unit | 184 | 1,451 | 28 parameterized test declarations |
| SDK integration | 96 | 721 | 629 runnable and 92 disabled scenarios after class-level disabling is expanded |
| TCK adapter unit | 9 | 32 | Spring/JSON-RPC adapter tests |

These are static source counts. Parameterized tests can produce multiple test
invocations, and a runnable declaration is not evidence that it passed in a
particular environment.

## Unit-test infrastructure

### Scope

Unit tests cover public value behavior and internal mechanics without requiring
a live Hiero network. Common subjects include:

- identifier parsing and formatting;
- protobuf serialization and deserialization;
- transaction and query construction;
- signing and key handling;
- equality, hashing, copying, and string representations;
- retry and error mapping;
- client configuration and endpoint selection.

### Frameworks and test doubles

The modular test configuration in `sdk/build.gradle.kts` declares JUnit 5,
JUnit parameters, AssertJ, Mockito, and JSON snapshots. Representative tests
also use in-process gRPC servers and a JDK HTTP server.

The result is a mixed unit-testing strategy:

- direct state and value assertions with AssertJ;
- Mockito collaborators where an object boundary is sufficient;
- in-process protocol servers when request/response behavior matters;
- snapshots for stable textual or serialized representations;
- fixture resources for address books, client configuration, and keystores.

Unit-test resources live in `sdk/src/test/resources`, including bundled network
address books, JSON client configurations, and test keystores.

### Snapshot maintenance

The `updateSnapshots` Gradle task deletes existing `.snap` files from the unit
test source set and then runs `test` to recreate them. Snapshot updates are
therefore explicit maintenance actions rather than normal test execution.

## Integration-test infrastructure

### Execution policy

The integration suite is deliberately conservative:

- `maxParallelForks = 1` serializes Gradle test forks;
- `failFast = true` stops the suite after the first failure;
- all Gradle `Test` tasks receive the same network and operator properties;
- the CI network is provisioned once and shared by the suite.

Serial execution reduces collisions between tests that create, update, and
delete ledger entities, but it also makes the suite slower and does not by
itself guarantee isolation between scenarios.

### Configuration contract

`sdk/build.gradle.kts` forwards these Gradle properties as Java system
properties:

| Property | Purpose |
| --- | --- |
| `HEDERA_NETWORK` | Selects `localhost`, `testnet`, or `previewnet` |
| `OPERATOR_ID` | Identifies the funded payer/operator account |
| `OPERATOR_KEY` | Supplies the operator private key |
| `CONFIG_FILE` | Provides an alternative SDK client configuration file |

`IntegrationTestEnv` recognizes `previewnet`, `testnet`, and `localhost`
directly. If none matches, it tries `CONFIG_FILE`; otherwise environment
creation fails. It does not have a direct `mainnet` branch.

A typical local invocation is:

```bash
./gradlew \
  -PHEDERA_NETWORK=localhost \
  -POPERATOR_ID="$OPERATOR_ID" \
  -POPERATOR_KEY="$OPERATOR_KEY" \
  testIntegration
```

The caller is responsible for provisioning the network before running this
command.

### Local network endpoints

For `HEDERA_NETWORK=localhost`, `IntegrationTestEnv` creates a client with:

| Service | Endpoint or identity |
| --- | --- |
| Consensus-node gRPC | `127.0.0.1:35211` |
| Consensus-node account | `0.0.3` |
| Mirror-node gRPC | `127.0.0.1:5600` |

The fixture comment distinguishes mirror gRPC port `5600` from the local mirror
REST port `8084`. Integration tests configure the mirror gRPC endpoint because
SDK mirror streaming and address-book queries use gRPC.

### `IntegrationTestEnv` lifecycle

Most integration classes use
`sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/IntegrationTestEnv.java`.
The fixture performs the following sequence:

1. Select a public-network client, a localhost client, or a client loaded from
   configuration.
2. Read the operator identifier and private key and install them on the client.
3. Assert that the client exposes an operator account and public key.
4. Detect whether the selected consensus endpoint is the local node.
5. Shuffle the configured consensus endpoints.
6. Probe candidate nodes with a minimal HBAR transfer and receipt lookup.
7. Restrict the client to the requested number of working nodes.
8. Expose the configured client, operator identifier, and operator public key to
   the test.

The endpoint probe means fixture construction performs real ledger operations;
it is not merely configuration setup.

### Throwaway operators and cleanup

Tests that should not mutate the shared operator call `useThrowawayAccount()`.
The fixture:

1. generates a fresh ED25519 key;
2. creates an account, normally funded with 100 HBAR;
3. creates a replacement client with the same consensus and mirror networks;
4. installs the new account as operator;
5. raises the retry limit to 15.

When the fixture closes, it attempts to return the throwaway account's
remaining HBAR balance to the original operator and closes the clients. Entity
cleanup beyond that balance transfer remains the responsibility of individual
tests.

### Shared entity helpers

`EntityHelper` provides reusable setup for accounts, fungible tokens, NFTs, and
contracts. These helpers reduce test duplication, but they are Java fixtures;
they are not part of the language-agnostic scenario contract.

### Capability-based skipping

`IntegrationTestEnv.assumeNotLocalNode()` skips scenarios that require behavior
the single-node local environment cannot provide. Other tests use JUnit
assumptions or `@Disabled` directly.

At the inspected snapshot, common disabled reasons include:

- the behavior cannot run with the Solo action;
- a service capability such as EVM hooks is not enabled in the tested services
  release;
- an issue is awaiting a services-side fix;
- a test is temporarily disabled without a structured capability marker.

Class-level disabling explains why 40 explicit `@Disabled` annotations expand
to 92 disabled scenarios in the generated scenario catalog.

## Solo-backed CI architecture

The active workflow is `.github/workflows/build.yml`.

### Main unit and integration job

The `Unit and Integration Tests` job:

1. installs Java 21 and configures Gradle;
2. provisions Hiero Solo through `hiero-solo-action`;
3. installs a mirror node as part of the deployment;
4. builds the SDK and runs quality checks;
5. runs `test` and `testIntegration` against `localhost`;
6. creates an aggregated JaCoCo report;
7. uploads coverage to Codecov when workflow conditions permit it.

The inspected workflow pins Solo `v0.65.0`, Hiero services `v0.73.0`, and mirror
node `v0.153.0`. These are observations from the snapshot, not permanent V3
requirements.

The primary job passes `-PskipNodeUpdateTest=true`, excluding
`NodeUpdateTransactionIntegrationTest` from the general run.

### Dynamic address-book job

The separate `Test Dynamic Addressbook` job provisions Solo in dual mode and
runs only `NodeUpdateTransactionIntegrationTest`. This isolates a topology and
capability requirement that differs from the ordinary single-network fixture.

### Example smoke-test job

The examples job provisions Solo with a mirror node, writes the localhost
operator configuration to `examples/.env`, builds the Android example, and
runs all Java examples. This acts as executable-documentation coverage in
addition to the formal SDK suites.

## Code-coverage aggregation

`gradle/aggregation/build.gradle.kts` collects JaCoCo results from both the
standard test suite and the `testIntegration` suite. The workflow executes:

```bash
./gradlew :aggregation:testCodeCoverageReport
```

This produces a combined report for network-independent and network-backed
tests. Coverage aggregation is useful for the Java implementation, but line
coverage is not a cross-language conformance measure. V3 scenario coverage
should therefore be tracked separately by stable scenario identifier.

## TCK adapter architecture

The `tck` module is a Spring Boot JSON-RPC server. It receives requests from the
external Hiero SDK TCK driver, maps JSON-RPC methods and parameters to Java SDK
operations, executes them, and maps results or Hedera errors back to JSON.

```text
TCK test driver
      |
      | JSON-RPC
      v
Java SDK TCK server
      |
      | Java SDK calls
      v
configured Hiero network
```

The active application configuration uses port `8544`. The TCK README still
states that the default port is `80`; this is a documentation inconsistency in
the inspected snapshot.

The active CI job builds the TCK module and runs `:tck:test`. It does not launch
the server and external TCK driver, so that job verifies the Java adapter's unit
tests rather than complete cross-SDK conformance by itself.

## Strengths of the V2 design

- Unit and network-backed tests are separate Gradle suites.
- Integration tests use a real consensus node and mirror node in CI.
- Solo versions are pinned, making the CI environment reproducible.
- Operator configuration is injected rather than embedded in test classes.
- Throwaway accounts reduce mutation of the shared operator.
- A separate dual-mode job isolates dynamic-address-book behavior.
- Unit and integration coverage are aggregated.
- The TCK adapter provides a language-neutral JSON-RPC boundary.
- Examples are executed as an additional end-to-end signal.

## Limitations and risks

- The Java fixture and many scenarios are coupled to Java class names and JUnit
  mechanisms; these details cannot be the V3 cross-language contract.
- Serial execution limits throughput and can conceal shared-state coupling.
- A failed test can leave ledger entities behind; cleanup is not transactional.
- Endpoint probing mutates ledger state during fixture construction.
- Disabled tests use free-form reasons instead of machine-readable capability
  requirements.
- Some public-network tests do not map directly to the Solo fixture.
- The fixture hardcodes localhost endpoints and the local node account.
- The main TCK CI job does not run the external conformance driver.
- Static source counts and line coverage do not prove behavioral conformance.

## V3 testing architecture implications

V3 should preserve the useful separation in V2 without making Java's test
implementation normative.

### Language-agnostic scenario contract

Each scenario should contain only:

- a stable namespace-prefixed identifier;
- a **Given** precondition expressed in domain terms;
- a single **When** behavior;
- an observable **Then** result;
- optional capability requirements;
- optional traceability to V2 tests.

The preferred baseline is:

```markdown
 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** a thing with a valid name is created and its details are subsequently retrieved.
 - **Then** both operations complete without error and the retrieved name equals the submitted name.
```

Scenario prose must not mention JUnit, AssertJ, Java exception classes, Java
method calls, or concrete fixture classes.

### Per-language adapters

Every SDK implementation may choose its native test framework, but its adapter
must provide equivalent capabilities:

- resolve the Solo consensus and mirror endpoints;
- construct a client with a funded operator;
- create isolated prerequisite entities;
- expose protocol statuses and observable results;
- clean up recoverable state;
- report results by stable scenario identifier.

### Explicit capability metadata

Free-form disabling should be replaced with explicit requirements such as:

```text
requires: mirror-grpc
requires: multi-node
requires: dynamic-address-book
requires: evm-hooks
```

The runner can then skip a scenario because the active environment lacks a
declared capability, while still reporting the scenario as part of the contract.

### Shared lifecycle, isolated scenarios

Solo should be deployed once per test run, not once per scenario. Within that
deployment, scenarios must create their own prerequisite state and must not
depend on execution order or state left by another scenario.

### Separate measurements

V3 should report at least three independent signals:

1. scenario conformance by stable identifier;
2. implementation-specific unit and coverage results;
3. TCK protocol compatibility.

Combining these signals gives a more accurate picture than treating Java line
coverage or the presence of a test method as proof of SDK conformance.

## Primary V2 sources

| Concern | Source |
| --- | --- |
| Gradle test configuration | `sdk/build.gradle.kts` |
| Integration fixture | `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/IntegrationTestEnv.java` |
| Shared entity creation | `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/EntityHelper.java` |
| Integration module boundary | `sdk/src/testIntegration/java/module-info.java` |
| Coverage aggregation | `gradle/aggregation/build.gradle.kts` |
| Solo and CI orchestration | `.github/workflows/build.yml` |
| TCK server build | `tck/build.gradle.kts` |
| TCK server description | `tck/README.md` |
| TCK server port | `tck/src/main/resources/application.yml` |
