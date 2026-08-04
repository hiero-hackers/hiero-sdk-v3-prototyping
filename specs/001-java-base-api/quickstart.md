# Quickstart: Generate and Verify the Java Base API

These commands describe the intended workflow after the implementation tasks are complete.

## Prerequisites

- JDK 21 or newer; builds target Java 21 with `--release 21`
- Git
- Network access only for the first Maven dependency download

## Validate Specifications

```bash
./mvnw -q -pl tools/spec-codegen-java -am package
./mvnw -q -Pvalidate-java-base-api verify
```

Validation parses all `spec/base` schema blocks, resolves namespace imports and type references,
checks mapping completeness, and writes nothing.

## Generate the API

```bash
./mvnw -q -Pgenerate-java-base-api generate-sources
```

Only `java/hiero-sdk-base-api/src/main/java` and the generation manifest may change. Files in that
tree are generated and must not be edited manually.

## Verify the Prototype

```bash
./mvnw verify
```

The build compiles on the module path, runs generator and structural tests, compiles positive and
negative consumer fixtures, scans the public surface and dependencies, validates Javadocs, and
compares clean regenerated output with the checked-in API.

## Review Generated Changes

```bash
git diff -- spec/base guidelines docs/adr codegen java/hiero-sdk-base-api
git status --short
```

Every generated API change must be explained by an approved specification, mapping configuration,
or generator change. Security-sensitive key and Authority changes require human review before the
prototype is accepted.
