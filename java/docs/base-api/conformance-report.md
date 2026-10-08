# Java Base API Conformance Report

- Change: `generate-java-base-public-api`
- Review date: 2026-08-14
- Java baseline: 21
- Public module: `org.hiero.sdk.v3.base`
- Publishable artifact: `org.hiero.sdk.v3:hiero-sdk-base-api`

## Scope

The API covers all eleven `spec/base` namespaces and contains only contracts plus approved structural value mechanics.
Operational networking, cryptography, signing, verification, external parsing/serialization, persistence, registries,
provider selection, I/O, clocks, randomness, scheduling, retries, transports, and service behavior remain deferred.

## Verification status

`./mvnw clean verify` passed on 2026-08-14. It compiled 61 API source files, built the modular API/source/Javadoc
artifacts, generated CycloneDX SBOMs, and ran 22 contract tests with zero failures, errors, or skips. The suite covered
consumer compilation, traceability, signatures, JPMS exports, dependencies, security exclusions, immutable value
mechanics, and structural-body boundaries.

## Approval

- Java API design review: approved by the user on 2026-08-14.
- Security/operational-boundary review: approved by the user on 2026-08-14.
- Operational implementation remains deferred: yes.
- The subsequently discussed `Page` revisions were explicitly withdrawn and are not part of this approved change.
