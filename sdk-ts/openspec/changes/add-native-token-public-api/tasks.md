## 1. Public Native Token Contract

- [x] 1.1 Add the internal `requireInt64` helper for `bigint` type and int64 range validation.
- [x] 1.2 Add the abstract `NativeTokenUnit` class with validated, read-only `symbol` and positive `baseUnitFactor`.
- [x] 1.3 Add the abstract `NativeToken<Self, Unit>` class with validated, read-only `amount` and `unit`, exact
  `toBaseUnits()`, exact `to()`, and the protected `create` hook.
- [x] 1.4 Export `NativeToken` and `NativeTokenUnit` from the package entry point, keeping `requireInt64` internal.
- [x] 1.5 Document every exported type, constructor, accessor, and method with TSDoc.

## 2. Contract Tests

- [x] 2.1 Verify invalid-input handling for units and tokens, and the immutability of their state.
- [x] 2.2 Verify exact conversion in both directions, rejection of inexact and out-of-range conversions, and that `to()`
  returns a new instance of the concrete subclass.
- [x] 2.3 Verify public types: `bigint` accessors, `to()` returning `Self`, and rejection of a foreign unit type.
- [x] 2.4 Verify through the consumer-compilation check that consumers can define a concrete unit and token through the
  published entry point.

## 3. Final Verification

- [x] 3.1 Run type checking, build, contract tests, and consumer compilation.
- [x] 3.2 Verify that the package still has no runtime dependencies and exports only the expected types.
- [x] 3.3 Verify traceability to the linked common proposal, common specification, and recorded source revision.
