# Java Base Public API

This directory documents the directly maintained Java 21 contract translated from `spec/base`. It deliberately ships
without an implementation/provider artifact.

## Build and verify

From the `java/` directory:

```shell
./mvnw clean verify
```

The command compiles the named API module, builds source/Javadoc artifacts, generates SBOMs under each module's
`target/`, and runs the black-box contract suite. No parser or code generator runs.

## Consume the API only

Maven consumers compile against:

```xml
<dependency>
  <groupId>org.hiero.sdk.v3</groupId>
  <artifactId>hiero-sdk-base-api</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

JPMS consumers declare `requires org.hiero.sdk.v3.base;`. They can construct structural values, inspect enums and
constants, extend approved abstract bases, and implement body-free companion contracts. Operational calls require a
future implementation chosen explicitly by the consuming application; the API performs no provider discovery.

## Ownership and evolution

The Java source is maintained directly. Any `spec/base` change must review the corresponding Java contracts,
`mapping-matrix.md`, affected tests, Javadocs, and signature snapshot in the same change. The matrix is the deliberate
human review gate; this workspace does not parse the Markdown schema.

Future implementation modules belong under a later OpenSpec change. They depend on `hiero-sdk-base-api`, implement
companion interfaces or extend behavioral abstract classes, remain non-exported except for intentionally designed
provider entry points, and must not alter or leak through this public API.
