# Dependency Review

## Published API artifact

`org.hiero.sdk.v3:hiero-sdk-base-api` has one non-platform dependency:

| Dependency | Scope | Public purpose | Runtime required? |
|---|---|---|---|
| `org.jspecify:jspecify:1.0.0` | `provided` / JPMS `static transitive` | `@NullMarked` and `@Nullable` public nullness metadata | No |

Public signatures otherwise use Java platform types and types from the API artifact. There is no crypto provider,
transport, protobuf runtime, parser, generator, service loader, implementation module, or logging dependency.

## Build and verification dependencies

| Dependency/tool | Scope | Purpose | Published API runtime? |
|---|---|---|---|
| JUnit Jupiter 6.1.2 | test | Contract test execution | No |
| AssertJ 3.27.7 | test | Readable contract assertions | No |
| CycloneDX Maven Plugin 2.9.1 | build | Per-module SBOM generation under `target/` | No |
| Maven lifecycle/source/Javadoc/enforcer/dependency/wrapper plugins | build | Reproducible compilation and verification | No |

Only `hiero-sdk-base-api` is publishable. The reactor POM and `java-base-api-contract` module set Maven install/deploy
skip flags. Plugin versions are pinned in `java/pom.xml`, including clean, resources, compiler, Surefire, JAR,
install, and deploy lifecycle plugins.
