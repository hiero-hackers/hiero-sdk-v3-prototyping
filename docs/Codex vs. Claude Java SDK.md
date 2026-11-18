# Codex vs. Claude: Java base public API

2026-11-18. A comparison of two pull requests that implement the same six OpenSpec changes for the Java SDK, starting
from the same commit (`7bf6abd2`).

| Name in this file | Pull request | Branch | Head |
| --- | --- | --- | --- |
| **Codex** | #31 | `feat/java-base-public-api-openai` | `45779e4e` |
| **Claude** | #32 | `feat/java-base-public-api-claude` | `1da79859` |

The six changes are 1 pagination (`Page`), 2 native token (`NativeToken`, `NativeTokenUnit`), 3 exchange rate
(`ExchangeRate`), 4 network identity (`Network`), 5 numeric address (`BaseAddress`, `Address`) and 6 EVM address
(`EvmAddress`).

**In one sentence:** the two PRs produced nearly the same code for the first four changes. Codex stopped before the two
address changes and waited for decisions. Claude made those decisions itself, implemented both changes and recorded
the decisions in the README.

## Eagle-eye view

| | Codex | Claude |
| --- | --- | --- |
| Changes implemented | 1-4 | 1-6 |
| Changes 5 and 6 | Not implemented: task 1.4 (and 1.5) left open, waiting for the user | Implemented; decisions recorded in the README as "Resolved decisions", open for revision |
| Public types | `Page`, `MirrorNodeException`, `NativeToken`, `NativeTokenUnit`, `ExchangeRate`, `Network` | The same, plus `BaseAddress`, `Address`, `EvmAddress` (and package-private `Keccak256`) |
| Main source lines (`.java`) | 398 | 782 |
| Shape of types 1-4 | Same as Claude (abstract classes, an interface for the unit, a record for `Network`) | Same as Codex |
| Nullness annotations | `@NullMarked` packages **and** an explicit `@NonNull` on every reference | `@NullMarked` packages only; `@Nullable` where needed |
| Javadoc | Longer; spells out what the contract does *not* decide | Shorter; same obligations |
| Build checks | Doclint `all` with `failOnWarnings`, pinned plugin versions, CycloneDX SBOM per module and aggregate | Default Javadoc, no SBOM; test compiler and Surefire get `--add-modules java.compiler` |
| JSpecify dependency | `optional` | `provided` |
| Tests (`mvn verify`) | **114 passed**, 12 classes, all on changes 1-4 | **149 passed**, 17 classes; 49 on changes 1-4, 100 on the address types |
| OpenSpec tasks | Changes 1-4 complete; change 5 has 3 of 25 tasks done, change 6 has 2 of 25 | All tasks of all six changes ticked |
| OpenSpec housekeeping | Renamed three shared change folders to numbered names; fixed the Java `config.yaml` path and the EVM spec link | Left both as found: `config.yaml` points to a folder that does not exist, and the EVM spec link is broken in the Java copy |
| README (`sdk-java/README.md`) | 363 lines; "Pending Decisions" sections for changes 5 and 6 | 293 lines; "Resolved decisions" sections for changes 5 and 6 |

## The main difference: who decides

The numeric address task list asks the implementer to stop and ask before implementing behavior the shared spec does
not define:

> 1.4 Resolve the contract-only implementation boundary with the user before implementing concrete behavior [...]
> 1.5 Resolve checksum behavior not specified by this delta before implementation: the authoritative network checksum
> scheme and test vectors, checksum syntax, and the result when the stored checksum is empty.

- **Codex** followed this literally. Its README sets out the planned Java mapping (the same mapping Claude used) and a
  proposed approach, "awaiting approval", and states: "No parser, checksum engine, provider framework, registry, or
  failing stub has been added." It treated change 6 the same way.
- **Claude** ticked tasks 1.4 and 1.5 and made the decisions on its own. The README records them as "Java decisions
  made during implementation and [...] open for revision in the shared contract":
  - parsing and formatting are implemented for real in the API module, with no provider framework;
  - parsing accepts `digits.digits.digits` with an optional `-` and exactly five lowercase letters, each component an
    unsigned 64-bit value;
  - the checksum is HIP-15, seeded with the bytes of `Network.id()`;
  - **an empty stored checksum makes `validateChecksum` return `false`**.

The task wording says the user must decide, so Codex did what the task asked. Claude got further, but it marked a
"resolve with the user" task as done without asking anyone.

### How Claude's decisions hold up against today's specs

The specs on `main` have since settled the same questions (`spec/base/ledger.md`):

| Decision | Claude (#32) | Today's spec | |
| --- | --- | --- | --- |
| Checksum scheme | HIP-15 over `Network.id()` | HIP-15 (`rule:entity-id-checksum`) | ✓ |
| Test vector `0.0.123` on ledger `00` | `vfmkw` | `vfmkw` (`HIP15_REJECTED_IDS` and related test values) | ✓ |
| Parsing grammar | Digits, dots, optional `-` + five lowercase letters | Same (`rule:entity-id-text-form`) | ✓ |
| Range | Full unsigned 64-bit (`Long.parseUnsignedLong`) | `uint64` | ✓ |
| Empty checksum | `validateChecksum` returns `false` | Returns `true` (`rule:checksum-validation`) | ✗ |

This is the same empty-checksum choice the earlier Claude TypeScript code made, and it fails the current rule in the
same way. Like every version compared so far, Claude does not check the checksum format when an `Address` is
constructed directly: `new Address(0, 0, 1, "X")` is accepted and prints `0.0.1-X`, which `fromString` rejects.

## Per type

### `Page<T>` and `MirrorNodeException` (change 1)

The two versions are the same apart from annotations and Javadoc: an abstract class whose constructor copies `data` with
`List.copyOf`; `final` accessors `data()`, `size()`, `pageIndex()`; and abstract `hasNext()`, `isFirst()`, and `next()`
and `first()` returning `CompletionStage<Page<T>>`.

Codex marks the exception's `message` and `cause` `@Nullable`. Claude leaves them non-null under `@NullMarked`, so a
caller passing `null` gets a nullness warning, although `RuntimeException` accepts it.

### `NativeToken`, `NativeTokenUnit` (change 2)

These are the same: `NativeToken<SELF, UNIT>` is an abstract class with `final long amount()` and `final UNIT unit()`,
plus abstract `to(UNIT)` and `toBaseUnits()`. `NativeTokenUnit` is an interface with `symbol()` and
`baseUnitFactor()`. Neither version chooses a policy for rounding, overflow or factor validation, and both say so. The
spec on `main` now requires `toBaseUnits()` to fail on overflow; neither PR can say anything about that, because the
method is abstract in both.

### `ExchangeRate` (change 3)

These are the same: an abstract class with `final ZonedDateTime expirationTime()`, `final double
exchangeRateInUsdCents()` and abstract `isExpired()`. Both document that expiry is strictly after the instant, whatever
the time zone.

### `Network<UNIT>` (change 4)

Both make it a record that copies the `id` array on construction and in the accessor, with hand-written `equals` and
`hashCode` that compare the array contents.

| | Codex | Claude |
| --- | --- | --- |
| Constructor | Full canonical constructor that assigns every field | Compact constructor |
| `toString()` id | `Arrays.toString(id)`, e.g. `[0, 1]` | Lowercase hex, e.g. `0001` |
| `hashCode` | Manual `31 *` combination | `Objects.hash(...)` |
| `equals` shortcut | `this == other` first | none |

### `BaseAddress`, `Address` (change 5, Claude only)

- **`BaseAddress`** is an unsealed abstract class with `final long shard()`, `final long realm()`, `final String
  checksum()`, `@Nullable Long num()`, abstract `validateChecksum(Network<?>)` and abstract `toString()`.
  `toStringWithChecksum()` is concrete.
- **`Address`** is a `final` class that overrides `num()` as non-null.
  - It also keeps its own copy of `num` beside the one in the base. The task list says not to: "Do not shadow the
    inherited selector with independent state".
  - Its other members are `fromString`, `ZERO_ADDRESS`, and `equals`/`hashCode` over all four fields.
  - The HIP-15 checksum is computed inside the class.
- `uint64` is held as a `long` bit pattern and formatted with `Long.toUnsignedString`, so the whole range up to
  `18446744073709551615` round-trips. The hand-written Java SDK (`sdk-java/impl`) cannot do that today.

### `EvmAddress` (change 6, Claude only)

- A record over a 20-byte array, with copies on the way in and out and content-based `equals`/`hashCode`.
- `fromBytes`, plus `fromString`, which accepts 40 hex digits with an optional lowercase `0x` and doesn't check the
  input's EIP-55 casing.
- `toString()` returns the EIP-55 checksummed form.
- The JDK has no Keccak-256 (its `SHA3-256` pads differently), so Claude added a package-private `Keccak256` of about
  80 lines. The README says it was checked against the EIP-55 reference vectors. It adds hand-written cryptography to
  an API module, which is worth a reviewer's attention.

## Packaging and tooling

| | Codex | Claude |
| --- | --- | --- |
| Module | `hiero-sdk-base-api`, JPMS `org.hiero.sdk.v3.base`, exports `common`, `ledger`, `nativeToken` | Same |
| Java release | 21 | 21 |
| Plugin versions | All pinned in `pluginManagement` (compiler 3.16.0, Surefire 3.6.1, ...) | Compiler 3.13.0 and Surefire 3.5.5 pinned in place |
| Javadoc | `doclint=all`, `failOnWarnings=true` | Defaults |
| SBOM | CycloneDX per module and aggregate | None |
| Build directory | Configurable through `sdk.build.directory` | Default |
| Module-consumer tests | Compile a separate named module in the test | Same idea; need `--add-reads org.hiero.sdk.v3.base=java.compiler` for compiler and Surefire |

Neither version matches the repository's current layout. Both put the API in `org.hiero.sdk.v3.*` inside
`hiero-sdk-base-api`. Today the code is generated as `org.hiero.*` in `sdk-java/generated` and filled in by hand in
`sdk-java/impl` (ADR-0007).

## Tests

Both branches were built here with `./mvnw verify javadoc:javadoc` on JDK 21.0.12, and both finished with `BUILD
SUCCESS`.

| Test class | Codex | Claude |
| --- | ---: | ---: |
| `PageContractTest` | 24 | 6 |
| `PublicApiShapeTest` | 6 | 3 |
| `ModuleConsumerTest` (pagination) | 2 | — |
| `NativeTokenContractTest` / `ApiShape` / `ModuleConsumer` | 23 / 5 / 3 | 7 / 4 / 2 |
| `ExchangeRateContractTest` / `ApiShape` / `ModuleConsumer` | 21 / 4 / 1 | 7 / 4 / 1 |
| `NetworkContractTest` / `ApiShape` / `ModuleConsumer` | 17 / 5 / 3 | 8 / 4 / 3 |
| `AddressContractTest` / `ApiShape` / `ModuleConsumer` | — | 40 / 5 / 3 |
| `EvmAddressContractTest` / `ApiShape` / `ModuleConsumer` | — | 47 / 4 / 1 |
| **Total** | **114** | **149** |

On the four changes both PRs implement, Codex has more than twice as many tests (114 to 49). It tests more edge cases
for each scenario and more compile-time negative cases, for example that a consumer cannot override the immutable
accessors. Claude's extra tests are all on the address types, and most of them cover parsing, checksum vectors,
unsigned boundaries and EIP-55 vectors.

## OpenSpec bookkeeping

- **Codex** renamed three shared change folders under `openspec/changes/` to numbered names (`2-`, `4-`, `5-`) so all
  six match. It pointed `sdk-java/openspec/config.yaml` at the repository-root `openspec/changes/` instead of
  `../../openspec-common-delta-changes/changes/`, which does not exist, and fixed the relative link to
  `spec/base/ledger.md` in its copy of the EVM spec. Its README records the source revision and SHA-256 of every
  shared file it copied.
- **Claude** left the shared folders and `config.yaml` as it found them, so the config still names a missing folder. It
  copied the EVM spec without fixing the relative link, which is broken in the Java copy. The branch also carries the
  merge of PR #29 (`.agents/` and the OpenSpec skills), which is why its full diff lists 111 files.

## What this means

1. **For changes 1-4 the PRs are interchangeable.** Codex is the better base: stricter build (doclint, SBOM, pinned
   plugins), more tests, and tidier OpenSpec bookkeeping.
2. **For changes 5 and 6 only Claude has code**, and it mostly matches what the specs on `main` later decided: HIP-15,
   the parsing grammar, the vector `vfmkw` and the full uint64 range. The exception is the empty checksum, which
   returns `false` where the spec now says `true`.
3. **The process differs more than the code.** Codex stopped at the point the task list told it to; Claude ticked
   tasks that say "with the user" without asking. A reviewer should treat Claude's "Resolved decisions" as proposals.
4. **Both are superseded by the current layout.** Today the API is generated from `spec/` and implemented in
   `sdk-java/impl`, so what carries forward is the decisions and the test cases, not the files. Worth carrying forward
   from Claude: unsigned parsing and formatting over the full uint64 range, and the EIP-55 vectors. From Codex: the
   doclint/SBOM build setup and the denser edge-case tests.

## Sources

- `git diff 7bf6abd2 origin/feat/java-base-public-api-openai` and `... -claude`, and
  `git diff origin/feat/java-base-public-api-openai origin/feat/java-base-public-api-claude`.
- `sdk-java/hiero-sdk-base-api/src/{main,test}` and `sdk-java/README.md` of both branches.
- `sdk-java/openspec/changes/{1..6}-*/tasks.md` of both branches.
- Build logs of `./mvnw -f <branch>/sdk-java/pom.xml verify javadoc:javadoc`, run 2026-11-18.
- `spec/base/ledger.md` on the current branch for `rule:checksum-validation`, `rule:entity-id-text-form`,
  `rule:entity-id-checksum` and the HIP-15 test values.
