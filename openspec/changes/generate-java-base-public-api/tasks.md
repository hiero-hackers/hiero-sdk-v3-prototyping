## 1. Establish the Java API Build and Review Gates

- [x] 1.1 Create a self-contained Java 21 Maven reactor at `java/` with `hiero-sdk-base-api`, `tests/java-base-api-contract`, and `docs/base-api`, ensuring only the API module is publishable.
- [x] 1.2 Pin the Maven wrapper and plugin versions under `java/`, review JSpecify and test/build dependencies, and verify that the API has no operational runtime dependency.
- [x] 1.3 Create and approve a source-linked mapping matrix covering every `spec/base` namespace, declaration, member, constraint, error, constant, dependency, retained question, mapping variance, and deferred behavior.
- [x] 1.4 Classify every source operation as structural or operational and approve the structural implementation allowlist before adding method bodies.
- [x] 1.5 Add build-output ignore rules and repository attributes appropriate for directly maintained Java sources.

## 2. Author the Java Public Contract Directly

- [x] 2.1 Add directly maintained contracts for the `common`, `grpc`, and `proto` namespaces.
- [x] 2.2 Add directly maintained contracts and structural values for `nativeToken`, `hedera`, `solo`, and `token`.
- [x] 2.3 Add the ledger hierarchy, identifiers, nodes, constants, operational companions, and `ledger.config` contracts.
- [x] 2.4 Add key enums, key contracts, operational companions, and the sealed authority hierarchy without executable cryptography.
- [x] 2.5 Add source-linked Javadocs, package documentation, declared error documentation, and `module-info.java` exports for all eleven namespaces.
- [x] 2.6 Confirm that every Java public signature uses only Java platform types, JSpecify annotations, and types from the API artifact.

## 3. Implement Only Structural Value Mechanics

- [x] 3.1 Implement constructor null, range, length, one-of, finality, inherited-nullability, and threshold checks at concrete value boundaries.
- [x] 3.2 Implement defensive array copying, immutable collection snapshots, and structural equality and hashing.
- [x] 3.3 Implement only approved field-validating construction factories, including Authority composition and raw-byte/EVM-field construction.
- [x] 3.4 Implement deterministic canonical representations derived solely from validated fields and add golden tests for every such representation.
- [x] 3.5 Keep parsing, checksums, crypto, encoding, conversion, clocks, randomness, registries, provider discovery, I/O, scheduling, retries, and networking as body-free contracts.
- [x] 3.6 Audit structural method bodies against the approved allowlist and reject unsupported-operation placeholders.

## 4. Verify the API as a Consumer

- [x] 4.1 Add a mapping-matrix coverage test or review check that accounts for every source element without parsing the Markdown schema.
- [x] 4.2 Add positive consumer-compilation fixtures covering representative contracts from all eleven base namespaces.
- [x] 4.3 Add structural value and negative fixtures for nullability, selectors, lengths, ranges, thresholds, immutable ownership, generic bounds, and sealed hierarchies.
- [x] 4.4 Add key and authority security fixtures proving private keys cannot be authority leaves, diagnostics contain no secrets, and no crypto provider is bundled.
- [x] 4.5 Add async and protocol fixtures proving completion/error signatures exist without executors, schedulers, transports, protobuf implementations, or network code.
- [x] 4.6 Add dependency, JPMS export, split-package, and API-only compilation checks that reject test, implementation, tooling, and unapproved third-party leakage.
- [x] 4.7 Add public-signature snapshot verification and require mapping review for every accepted signature change.
- [x] 4.8 Add bytecode and source audits that allow only structural mechanics and reject mutable service state, provider selection, operational bodies, and unsupported-operation placeholders.

## 5. Complete Traceability and Documentation

- [x] 5.1 Add requirement-to-test metadata and verify every normative OpenSpec requirement maps to automated verification or accountable review.
- [x] 5.2 Review every mapping variance, structural implementation, deferred operation, and retained `Questions & Comments` entry.
- [x] 5.3 Run Javadoc checks and `./mvnw clean verify` from the self-contained `java/` workspace in a clean checkout.
- [x] 5.4 Record a final conformance report and Java API/security approval confirming operational implementation remains deferred.
- [x] 5.5 Document how to compile and consume the API without an implementation/provider artifact.
- [x] 5.6 Document direct-source ownership: changes to `spec/base` and affected Java contracts, mappings, tests, and signatures must be reviewed together.
- [x] 5.7 Document the follow-up boundary for implementation modules, including how they implement companion and behavioral contracts without altering or leaking through the public API.
