# Hiero SDK v3 — Java

Java 21 / Maven / JPMS implementation of the V3 public API contracts. Nullness is declared with JSpecify; tests use
JUnit Jupiter and AssertJ (test scope only).

## Modules

| Module | Description |
| --- | --- |
| `hiero-sdk-base-api` | Public foundational contracts (`org.hiero.sdk.v3.common`, `org.hiero.sdk.v3.nativeToken`), exported by the JPMS module `org.hiero.sdk.v3.base`. |

Build and verify with `mvn verify` (Java 21). Javadoc is generated with `mvn javadoc:javadoc`.

## Common pagination (`1-add-common-pagination-public-api`)

### Source reference

- [Common proposal](../openspec/changes/1-add-common-pagination-public-api/proposal.md)
- [Common specification](../openspec/changes/1-add-common-pagination-public-api/spec.md)
- Source revision: `4a79a0abce08a3d515e2b2ead8dad81e4ae8d358` (repository `HEAD` when applied; the common files were
  still untracked at that point and are byte-identical to
  `openspec/changes/1-add-common-pagination-public-api/specs/common-pagination/spec.md`).
- The earlier unnumbered change `add-common-pagination-public-api` was derived from revision `544b523f3db204433ccc7d3731cde73685914fbc`
  of the former `openspec-common-delta-changes/` location. Its implementation satisfies this numbered change unchanged.

### Java contract mappings

| Common contract | Java mapping |
| --- | --- |
| `abstraction Page<$$T>` | `public abstract class org.hiero.sdk.v3.common.Page<T>` |
| `data: list<$$T>` (immutable, non-null) | `final List<T> data()`; constructor rejects `null` with `NullPointerException` and stores `List.copyOf(...)` |
| `size: int32` | `final int size()`; stored as supplied, no additional constraint |
| `pageIndex: int32` | `final int pageIndex()`; stored as supplied, no additional constraint |
| `bool hasNext()` / `bool isFirst()` | `abstract boolean hasNext()` / `abstract boolean isFirst()`; no retrieval |
| `@@async Page<$$T> next()` / `first()` | `abstract CompletionStage<Page<T>> next()` / `first()` |
| `mirror-node-error` | unchecked `org.hiero.sdk.v3.common.MirrorNodeException` (message and cause); navigation stages complete exceptionally with it |

The constructor is `protected`; concrete retrieval, HTTP, parsing, retries, and executors are deferred to later
implementation modules.

### Test coverage of the shared scenarios

| Scenario | Test |
| --- | --- |
| Page state is observed | `PageContractTest#rejectsNullData`, `#storesAnImmutableSnapshot`, `#exposesDeclaredStateAndPosition` |
| Page position is inspected | `PageContractTest#exposesDeclaredStateAndPosition` |
| Next / first page is retrieved | `PageContractTest#completesNavigationWithTheRequestedPages` |
| Page retrieval fails | `PageContractTest#completesNavigationExceptionallyWithMirrorNodeException`, `#preservesMirrorNodeExceptionMessageAndCause` |

## Native token (`2-add-native-token-public-api`)

### Source reference

- [Common proposal](../openspec/changes/add-native-token-public-api/proposal.md)
- [Common specification](../openspec/changes/add-native-token-public-api/spec.md)
- Source revision: `4a79a0abce08a3d515e2b2ead8dad81e4ae8d358` (repository `HEAD` when applied). The Java copies under
  `openspec/changes/2-add-native-token-public-api/` are byte-identical to the common files. The shared files have no
  `2-` numbered folder at the repository root; they live in the unnumbered `add-native-token-public-api` folder.

### Java contract mappings

| Common contract | Java mapping |
| --- | --- |
| `abstraction NativeTokenUnit` | `public interface org.hiero.sdk.v3.nativeToken.NativeTokenUnit` (enum-friendly) |
| `symbol: string`, `baseUnitFactor: int64` | `String symbol()`, `long baseUnitFactor()`; implementations must be immutable |
| `abstraction NativeToken<$$Self, $$Unit>` | `public abstract class NativeToken<SELF extends NativeToken<SELF, UNIT>, UNIT extends NativeTokenUnit>` |
| `amount: int64`, `unit: $$Unit` | `final long amount()`, `final UNIT unit()`; set once in the `protected` constructor, `unit` null-checked, any `long` amount accepted |
| `$$Self to(targetUnit: $$Unit)` | `abstract SELF to(UNIT targetUnit)` |
| `int64 toBaseUnits()` | `abstract long toBaseUnits()` |

### Deferred work

This change is contract-only. Conversion arithmetic, concrete tokens (e.g. HBAR), exchange rates and network behavior
are deferred. **Open contract decisions** that must be settled in the shared specification before production
arithmetic is written: handling of fractional conversion results (rounding/truncation), `long` overflow, and
validation of `baseUnitFactor` (e.g. zero or negative). The Java API intentionally selects none of these; `to` and
`toBaseUnits` are abstract and the null-handling of `targetUnit` is a provider obligation.

### Test coverage of the shared scenarios

| Scenario | Test |
| --- | --- |
| Unit state is observed | `NativeTokenContractTest#unitExposesSymbolAndFactor`, `NativeTokenApiShapeTest#unitIsAnInterfaceWithOnlyReadAccessors` |
| Token amount is observed | `NativeTokenContractTest#tokenExposesAmountAndUnit`, `#acceptsZeroAndExtremeAmounts`, `#rejectsNullUnit` |
| Token amount is converted to a different unit | `NativeTokenContractTest#conversionReturnsConcreteTypeAndLeavesSourceUnchanged` (provider-contract test only) |
| Base units are calculated | `NativeTokenContractTest#baseUnitsOfBaseAmountAreUnchanged`, `#baseUnitsOfLargerUnitAreScaled` |

`NativeTokenModuleConsumerTest` compiles and runs an independent named module against the API and verifies that an
unrelated unit type is rejected by the compiler. `NativeTokenApiShapeTest` covers generic bounds, constructor
visibility and primitive `long` mappings.

## Exchange rate (`3-add-exchange-rate-public-api`)

### Source reference

- [Common proposal](../openspec/changes/3-add-exchange-rate-public-api/proposal.md)
- [Common specification](../openspec/changes/3-add-exchange-rate-public-api/spec.md)
- Source revision: `4a79a0abce08a3d515e2b2ead8dad81e4ae8d358` (repository `HEAD` when applied). The Java copies under
  `openspec/changes/3-add-exchange-rate-public-api/` are byte-identical to the common files.

### Java contract mappings

| Common contract | Java mapping |
| --- | --- |
| `abstraction ExchangeRate` (namespace `nativeToken`) | `public abstract class org.hiero.sdk.v3.nativeToken.ExchangeRate`, exported with the existing `nativeToken` package |
| `expirationTime: zonedDateTime` | `final java.time.ZonedDateTime expirationTime()`; set in the `protected` constructor, null-checked, zone preserved |
| `exchangeRateInUsdCents: double` | `final double exchangeRateInUsdCents()`; denominated in USD cents, any `double` accepted |
| `bool isExpired()` | `abstract boolean isExpired()` |

Expiration compares instants: the result is `true` only when the current instant is strictly after the expiration
instant. Equality returns `false` regardless of time zone.

### Deferred work

The API owns immutable state and the null check only. Expiration evaluation and clock selection are provider-defined.
Fetching, refreshing, caching, currency/token conversion, network access and token-specific implementations are
deferred. No bounds are placed on the rate (zero, negative, `NaN` and infinite values are accepted) or on past
expiration times.

### Test coverage of the shared scenarios

| Scenario | Test |
| --- | --- |
| Exchange-rate state is observed | `ExchangeRateContractTest#exposesExpirationTimeWithZoneAndRate`, `#rejectsNullExpirationTimeWithDescriptiveMessage`, `#doesNotInventValidationRules`, `ExchangeRateApiShapeTest` |
| Exchange rate has expired | `ExchangeRateContractTest#isExpiredWhenExpirationIsBeforeNow` |
| Exchange rate has not expired | `ExchangeRateContractTest#isNotExpiredWhenExpirationEqualsNow`, `#isNotExpiredWhenExpirationIsAfterNow`, `#equalInstantsInDifferentZonesAreNotExpired` |

The expiration tests use a fixed-clock test provider as contract examples, not a production implementation.
`ExchangeRateModuleConsumerTest` compiles and runs an independent named module against the exported API.

## Network identity (`4-add-network-identity-public-api`)

### Source reference

- [Common proposal](../openspec/changes/add-network-identity-public-api/proposal.md)
- [Common specification](../openspec/changes/add-network-identity-public-api/spec.md)
- Source revision: `4a79a0abce08a3d515e2b2ead8dad81e4ae8d358` (repository `HEAD` when applied). The Java copies under
  `openspec/changes/4-add-network-identity-public-api/` are byte-identical to the common files. The shared files live
  in the unnumbered `add-network-identity-public-api` folder; there is no `4-` folder at the repository root.

### Java contract mappings

| Common contract | Java mapping |
| --- | --- |
| `Network<$$Unit extends NativeTokenUnit>` (namespace `ledger`) | `public record org.hiero.sdk.v3.ledger.Network<UNIT extends NativeTokenUnit>(byte[] id, @Nullable String name, UNIT nativeTokenUnit)`, in the new exported `org.hiero.sdk.v3.ledger` package |
| `id: bytes` | `byte[] id()`; required, cloned on construction and on every call, any length or content (including empty) |
| `name: string` (nullable) | `@Nullable String name()`; `null` means absent, empty and non-empty names are kept as supplied (no trimming) |
| `nativeTokenUnit: $$Unit` | `UNIT nativeTokenUnit()`; required, stored as supplied (unit providers must be immutable) |

Required inputs are checked with `Objects.requireNonNull` (messages `id` and `nativeTokenUnit`). `NativeTokenUnit` is
reused from the native-token change.

The record choice is a Java mapping, not a shared requirement. `equals`/`hashCode` compare ID contents (not array
identity), the name and the unit; `toString` renders the ID as lowercase hex.

### Deferred work

Predefined networks, ID assignment rules, checksums, address parsing, node discovery, network settings, refresh and
transport behavior are deferred. No ID length, format or uniqueness policy is imposed.

### Test coverage of the shared scenario

| Scenario | Test |
| --- | --- |
| Network identity state is observed | `NetworkContractTest#exposesIdNameAndTypedUnit`, `#copiesTheIdOnConstruction`, `#returnsIndependentIdCopies`, `#acceptsEmptyAndLeadingZeroIdsAndExtremeBytes`, `#rejectsNullIdAndUnitWithDescriptiveMessages`, `#preservesAbsentEmptyAndUntrimmedNames`, `#equalityUsesIdContentsNameAndUnit`, `NetworkApiShapeTest` |

`NetworkModuleConsumerTest` compiles and runs an independent named module against the exported API and checks that the
compiler rejects a non-`NativeTokenUnit` type argument and assignment to an unrelated unit type.

## Numeric address (`5-add-numeric-address-public-api`)

### Source reference

- [Common proposal](../openspec/changes/add-numeric-address-public-api/proposal.md)
- [Common specification](../openspec/changes/add-numeric-address-public-api/spec.md)
- Source revision: `4a79a0abce08a3d515e2b2ead8dad81e4ae8d358` (repository `HEAD` when applied). The Java copies under
  `openspec/changes/5-add-numeric-address-public-api/` are byte-identical to the common files. The shared files live in
  the unnumbered `add-numeric-address-public-api` folder.

### Java contract mappings

| Common contract | Java mapping |
| --- | --- |
| `abstraction BaseAddress` (namespace `ledger`) | `public abstract class org.hiero.sdk.v3.ledger.BaseAddress` with a `protected` constructor; not sealed, so later kinds (e.g. EVM selectors) can extend it |
| `shard`, `realm: uint64` | `final long shard()`, `final long realm()`; unsigned 64-bit **bit patterns** (values above `Long.MAX_VALUE` are negative Java values; use `Long.*Unsigned`) |
| `@@nullable num: uint64` | `@Nullable Long num()` on `BaseAddress` |
| `checksum: string` | `final String checksum()`; `""` means no checksum (the schema's convention is used rather than the prose's "optional") |
| `Address extends BaseAddress` (`@@finalType`) | `public final class Address`; `Long num()` overrides the base accessor and is never `null` (primitive `long` is stored and boxed on access) |
| `validateChecksum(network: Network<ANY>)` | `abstract boolean validateChecksum(Network<?> network)` on `BaseAddress`; implemented by `Address` |
| `toString()` / `toStringWithChecksum()` | `toString()` is abstract on `BaseAddress`; `toStringWithChecksum()` appends `-<checksum>` when the checksum is not empty |
| `Address.fromString(address)` | `public static Address Address.fromString(String)`; `illegal-format` is `IllegalArgumentException` |
| `ZERO_ADDRESS` | `public static final Address Address.ZERO_ADDRESS` (`0.0.0`, checksum `""`), usable without any initialization |

`Address` implements `equals`/`hashCode` over shard, realm, num and checksum (a Java-only addition).

### Resolved decisions (tasks 1.4 and 1.5)

These were not specified by the delta; they are Java decisions made during implementation and are open for revision in
the shared contract.

- **Boundary:** parsing and formatting are implemented for real (pure, deterministic). There is no provider framework
  or registry. Checksum behavior lives in `Address`; a future `BaseAddress` subclass supplies its own
  `validateChecksum`.
- **Parsing syntax:** `digits.digits.digits` with an optional `-` plus exactly five lowercase letters. Each component
  must fit an unsigned 64-bit value. No sign, whitespace or other normalization is accepted. A `null` input throws
  `NullPointerException`; malformed non-null input throws `IllegalArgumentException`.
- **Checksum scheme:** HIP-15 (five lowercase letters over `shard.realm.num`, seeded by `Network.id()` bytes). Test
  vectors: `0.0.123` is `vfmkw` for ledger id `00` (mainnet), `esxsf` for `01` (testnet) and `ogizo` for `02`
  (previewnet). Vectors are the published HIP-15 examples, reproduced here by the implementation.
- **Empty stored checksum:** `validateChecksum` returns `false`. A `null` network throws `NullPointerException`.

### Deferred work

EVM selectors, aliases, account/contract identifiers, checksum generation APIs and network configuration are deferred.

### Test coverage of the shared scenarios

| Scenario | Test |
| --- | --- |
| Address state is observed | `AddressContractTest#exposesStateThroughAddressAndBaseReferences`, `#baseAllowsAbsentNumWhileEveryAddressConstructionHasOne`, `#rejectsNullChecksum`, `AddressApiShapeTest` |
| Address is parsed from string | `AddressContractTest#parsesBothForms`, `#roundTripsThroughFormatting`, `#handlesUnsignedBoundaries` |
| Invalid address parsing fails | `AddressContractTest#rejectsInvalidInput` (parameterized), `#rejectsNullInputWithNullPointerException` |
| Checksum is validated | `AddressContractTest#validatesMatchingChecksumsPerNetwork`, `#rejectsMismatchingChecksums`, `#worksThroughBaseReferenceAndAnyUnitType`, `#emptyStoredChecksumNeverValidates`, `#requiresANetwork` |
| Sentinel address is used | `AddressContractTest#zeroAddressIsAValidZeroSentinel` |

`AddressModuleConsumerTest` compiles and runs an independent named module against the exported API, and checks that
`Address` cannot be extended and that a primitive `num()` override of the boxed accessor is rejected.

## EVM address (`6-add-evm-address-public-api`)

### Source reference

- [Common proposal](../openspec/changes/6-add-evm-address-public-api/proposal.md)
- [Common specification](../openspec/changes/6-add-evm-address-public-api/spec.md)
- Source revision: `4a79a0abce08a3d515e2b2ead8dad81e4ae8d358` (repository `HEAD` when applied). The Java copies under
  `openspec/changes/6-add-evm-address-public-api/` are byte-identical to the common files.

### Java contract mappings

| Common contract | Java mapping |
| --- | --- |
| `type EvmAddress` (namespace `ledger`) | `public record org.hiero.sdk.v3.ledger.EvmAddress(byte[] bytes)`, standalone (does not extend `BaseAddress`, needs no `Network`) |
| `bytes: bytes` (exactly 20) | `byte[] bytes()`; cloned on construction and on every call; the canonical constructor enforces non-null and length 20 |
| `EvmAddress.fromBytes(value)` | `static EvmAddress fromBytes(byte[])`; `null` → `NullPointerException("value")`, other length → `IllegalArgumentException` (`illegal-format`) |
| `EvmAddress.fromString(value)` | `static EvmAddress fromString(String)`; `null` → `NullPointerException("value")`, malformed → `IllegalArgumentException` |
| `toString()` | canonical 42-character EIP-55 string |

`equals`/`hashCode` compare byte contents (a Java-only addition).

### Resolved decisions (task 1.3)

Not specified by the delta; Java decisions that are open for revision.

- **Boundary:** same as the numeric address change. Parsing and EIP-55 formatting are implemented for real and pure,
  with no provider framework, registry, or placeholders.
- **Hashing:** EIP-55 needs Keccak-256, which the JDK does not provide (`SHA3-256` uses different padding). A small
  package-private `Keccak256` class is included; it is not part of the public API and adds no dependency.
- **Verification of Keccak-256/EIP-55:** checked against the EIP-55 reference vectors (all caps, all lowercase and
  mixed-case), including the shared specification's `0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359`.
- **Grammar:** `[0-9a-fA-F]{40}` with an optional lowercase `0x`, ASCII only, matched against the whole string.

### Deferred work

Selector hierarchies, ledger checksum validation, chain-specific formatting, network lookup, and account/contract
existence checks are deferred.

### Test coverage of the 18 shared scenarios

All tests are in `EvmAddressContractTest` unless noted.

| Scenario | Test |
| --- | --- |
| EVM address state is observed | `exposesTheSameOrderedBytes` |
| Source bytes are changed after construction | `sourceMutationAfterConstructionHasNoEffect` |
| Returned bytes cannot modify the address | `returnedBytesAreIndependentCopies` |
| EVM address is constructed from valid bytes | `exposesTheSameOrderedBytes`, `retainsLeadingZerosAndExtremeBytesAndAllZeros` |
| EVM address byte length is invalid | `rejectsWrongByteLengths` |
| Null byte input is rejected | `rejectsNullBytes` |
| Zero bytes are valid address data | `retainsLeadingZerosAndExtremeBytesAndAllZeros` |
| Prefixed EVM address is parsed | `parsesPrefixedAndUnprefixedText` |
| Unprefixed EVM address is parsed | `parsesPrefixedAndUnprefixedText` |
| Letter case does not change the parsed address | `letterCaseDoesNotChangeTheParsedAddress` |
| EVM address text length is invalid | `rejectsMalformedText` (empty, `0x`, 39/41 digits) |
| EVM address text contains invalid characters | `rejectsMalformedText` (`g`, full-width digit, whitespace) |
| EVM address text contains unsupported decoration | `rejectsMalformedText` (whitespace, `0X`, repeated prefix, sign, separators, checksum suffix) |
| Null text input is rejected | `rejectsNullTextWithDescriptiveNullPointerException` |
| EVM address is formatted | `formatsTheEip55ReferenceVector`, `formatsEveryReferenceVectorCanonically` |
| Leading zeros are preserved in formatted text | `preservesLeadingZerosInFormattedText` |
| Raw address bytes survive a text round trip | `bytesSurviveATextRoundTrip` |
| Accepted spellings normalize to one canonical form | `acceptedSpellingsNormalizeToOneCanonicalForm` |

`EvmAddressApiShapeTest` covers the API shape and `EvmAddressModuleConsumerTest` compiles and runs an independent named
module against the exported API.
