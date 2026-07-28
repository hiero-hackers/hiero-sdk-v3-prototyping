# Quickstart: Java Common Code Generation

## Purpose

Validate the first Java code-generation slice end-to-end after implementation.

## Prerequisites

- Java 21 installed
- Maven wrapper added by the feature under the repository root or generator
  module
- Working tree checked out at the feature implementation commit

## Validation Scenario 1: Run generator for `spec/base/common.md`

1. Build and test the generator module:

   ```bash
   ./mvnw -f tools/spec-codegen-java/pom.xml test
   ```

2. Create a temporary output directory:

   ```bash
   mkdir -p /tmp/java-common-codegen-out
   ```

3. Run generation:

   ```bash
   ./mvnw -f tools/spec-codegen-java/pom.xml -q exec:java \
     -Dexec.args="generate --input-spec spec/base/common.md --output-dir /tmp/java-common-codegen-out --package-root org.hiero.sdk.v3"
   ```

4. Confirm generated output exists:

   ```bash
   ls /tmp/java-common-codegen-out/org/hiero/sdk/v3/common/Page.java
   ```

## Expected Outcome

- `Page.java` exists under the expected package path.
- The generated type is a Java `interface`.
- The file includes getter-style accessors for `data`, `size`, and `pageIndex`.
- The file includes `hasNext()`, `isFirst()`,
  `CompletionStage<Page<T>> next()`, and `CompletionStage<Page<T>> first()`.

## Validation Scenario 2: Detect output drift

1. Run the generator's verification suite:

   ```bash
   ./mvnw -f tools/spec-codegen-java/pom.xml test -Dtest=GoldenOutputTest
   ```

2. Review the result:

- Exit code `0` means generated output matches the approved fixtures.
- A non-zero exit code means the generated output drifted and the diff must be
  reviewed before approval.
