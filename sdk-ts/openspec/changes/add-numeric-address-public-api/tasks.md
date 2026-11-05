## 1. Public Numeric Address Contract

- [x] 1.1 Add the internal `requireUint64` helper and the exported `IllegalFormatError` class.
- [x] 1.2 Add the internal `hip15Checksum` function implementing the HIP-15 algorithm.
- [x] 1.3 Add the abstract `BaseAddress` class with validated, read-only state, the nullable `num` slot, abstract
  `toString()` and `validateChecksum()`, and the shared `toStringWithChecksum()`.
- [x] 1.4 Add the `Address` class with a private constructor, `Address.of()`, canonical `fromString()`, narrowed `num`,
  formatting, and HIP-15 `validateChecksum()`.
- [x] 1.5 Add the `ZERO_ADDRESS` constant.
- [x] 1.6 Export `Address`, `BaseAddress`, `IllegalFormatError`, and `ZERO_ADDRESS`, keeping the helpers internal.
- [x] 1.7 Document every exported type, constant, constructor, accessor, and method with TSDoc.

## 2. Contract Tests

- [x] 2.1 Verify `Address.of()` validation, uint64 boundaries, immutability, and `IllegalFormatError`.
- [x] 2.2 Verify parsing of valid input, rejection of every HIP-15 rejected input and out-of-range values, formatting,
  and round-trips.
- [x] 2.3 Verify all HIP-15 reference vectors, `validateChecksum()` with matching, other, and empty checksums, and
  rejection of a non-`Network` argument.
- [x] 2.4 Verify `ZERO_ADDRESS`.
- [x] 2.5 Verify public types: `num` narrowing, `bigint` accessors, read-only state, the private constructor, and the
  `Network<NativeTokenUnit>` parameter.
- [x] 2.6 Verify through the consumer-compilation check that consumers can parse, format, and validate an address
  through the published entry point and cannot extend `Address`.

## 3. Final Verification

- [x] 3.1 Run type checking, build, contract tests, and consumer compilation.
- [x] 3.2 Verify that the package still has no runtime dependencies and exports only the expected types.
- [x] 3.3 Verify traceability to the linked common proposal, common specification, and recorded source revision.
