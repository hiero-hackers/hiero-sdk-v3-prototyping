## 1. Public API and Addressing Implementation

- [x] 1.1 Define `IllegalFormatError` in `src/base/ledger.ts`.
- [x] 1.2 Implement canonical HIP-15 checksum calculation function in `src/base/internal/ledger-impl.ts`.
- [x] 1.3 Implement `BaseAddress` abstract base class with immutable state, `validateChecksum()`, `toString()`, and `toStringWithChecksum()` in `src/base/ledger.ts`.
- [x] 1.4 Implement `Address` concrete class with tightened non-null `num`, constructor validation, and `Address.fromString()` in `src/base/ledger.ts`.
- [x] 1.5 Define `ZERO_ADDRESS` constant in `src/base/ledger.ts` and export all public types from `src/base/index.ts` and `src/index.ts`.

## 2. Contract Tests and Verification

- [x] 2.1 Verify public-type shapes, inheritance, and consumer compilation in `tests/contract/AddressPublicApiShape.test.ts`.
- [x] 2.2 Verify immutable address state observation in `tests/contract/AddressContract.test.ts`.
- [x] 2.3 Verify `Address.fromString()` parsing valid address strings with and without checksums in `tests/contract/AddressContract.test.ts`.
- [x] 2.4 Verify `Address.fromString()` rejects malformed strings with `IllegalFormatError` in `tests/contract/AddressContract.test.ts`.
- [x] 2.5 Verify `validateChecksum()` across valid and invalid networks using official HIP-15 test vectors in `tests/contract/AddressContract.test.ts`.
- [x] 2.6 Verify `ZERO_ADDRESS` properties and clear-sentinel behavior in `tests/contract/AddressContract.test.ts`.
- [x] 2.7 Run full TypeScript type-checking (`tsc --noEmit`) and contract test suite (`npm test`).
