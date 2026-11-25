## 1. Public Network Identity Contract

- [x] 1.1 Add the `Network<Unit extends NativeTokenUnit>` class with validated input, copied-in and copied-out id bytes,
  a `string | null` name, and the native token unit.
- [x] 1.2 Export `Network` from the package entry point.
- [x] 1.3 Document the exported class, constructor, and accessors with TSDoc.

## 2. Contract Tests

- [x] 2.1 Verify state reads, an empty id and name, and that every validation rule throws a `TypeError`.
- [x] 2.2 Verify that changes to the constructor input or to a returned id do not change the stored id, and that the
  accessors cannot be reassigned.
- [x] 2.3 Verify public types: `Uint8Array`, `string | null`, `Unit` accessors, read-only accessors, and assignability to
  `Network<NativeTokenUnit>`.
- [x] 2.4 Verify through the consumer-compilation check that consumers can construct and read a network through the
  published entry point.

## 3. Final Verification

- [x] 3.1 Run type checking, build, contract tests, and consumer compilation.
- [x] 3.2 Verify that the package still has no runtime dependencies and exports only the expected types.
- [x] 3.3 Verify traceability to the linked common proposal, common specification, and recorded source revision.
