## Context

The Hiero SDK V3 architecture establishes a clean separation between public API contracts and internal implementations. Common feature specifications define cross-SDK semantics, while language-specific SDKs adapt them into idiomatic constructs. For TypeScript, this requires clean ES modules, strong typing with class inheritance, immutable state enforced via `Object.freeze()`, and accurate 64-bit integer mathematics using JavaScript `bigint`.

See `proposal.md` for motivation and source reference.

## Goals / Non-Goals

**Goals:**
- Provide `BaseAddress` as an abstract base class with `shard`, `realm`, `checksum`, and nullable `num`.
- Provide `Address` as a concrete class tightening `num` to non-null `bigint`.
- Implement `Address.fromString(address: string)` with robust regex validation for `"shard.realm.num"` and optional `"-<checksum>"` (5 lowercase ASCII letters).
- Implement standard HIP-15 checksum computation in `validateChecksum(network: Network<any>)`.
- Implement `toString()` ("shard.realm.num") and `toStringWithChecksum()` ("shard.realm.num-checksum" or "shard.realm.num" if checksum is empty).
- Provide `ZERO_ADDRESS` as an immutable frozen singleton.
- Throw custom `IllegalFormatError` on format violations.

**Non-Goals:**
- Implement `EvmAddress` (deferred to change 6: `add-evm-address-public-api`).
- Implement `ContractId` and `AccountId` (deferred to changes 7 and 8).
- Implement network communication or node lookup.

## Decisions

### 1. `BaseAddress` Abstract Class and `Address` Concrete Subclass
- **Decision:** Model `BaseAddress` as an abstract class and `Address` as a concrete class extending `BaseAddress`.
- **Rationale:** Aligns with the object-oriented LSP hierarchy described in `spec/base/ledger.md` and ADR-0003. Future addressing types (`EvmCapableAddress`, `ContractId`, `AccountId`) will cleanly extend `BaseAddress` and reuse `validateChecksum`, `toStringWithChecksum`, and common properties.
- **Alternatives Considered:** Making `BaseAddress` purely an interface. This would duplicate string serialization and HIP-15 checksum logic in every concrete address subtype.

### 2. Representation of 64-bit Unsigned Integers (`uint64`)
- **Decision:** Use JavaScript `bigint` for `shard`, `realm`, and `num`.
- **Rationale:** Shard, realm, and entity numbers are 64-bit unsigned integers in HAPI. `bigint` prevents precision issues and naturally rejects floating-point values.

### 3. HIP-15 Checksum Algorithm Implementation
- **Decision:** Implement the canonical HIP-15 checksum algorithm internally in `src/base/internal/ledger-impl.ts`.
- **Rationale:** Provides accurate verification against `network.id` bytes for mainnet, testnet, previewnet, and custom private ledgers, matching the official specification.

### 4. Custom `IllegalFormatError`
- **Decision:** Define `IllegalFormatError extends Error` to represent the meta-language `@@throws(illegal-format)`.
- **Rationale:** Allows callers to catch formatting and parsing errors explicitly without catching unrelated errors.

## Risks / Trade-offs

- **[Regex parsing overhead]** → Address strings are parsed at transaction build time, so regular expression compilation and matching overhead is negligible.
