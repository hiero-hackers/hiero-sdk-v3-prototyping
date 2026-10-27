# Java Public API

The `hiero-sdk-base-api` Maven module contains the public pagination, native token, exchange-rate, and network identity APIs and their contract tests in
`src/main/java` and `src/test/java`. Use Java 21 and Maven from this directory:

```sh
mvn verify
mvn org.apache.maven.plugins:maven-javadoc-plugin:3.12.0:javadoc
```

To preserve existing `target/` output, add `-Dsdk.build.directory=.verification/pagination` to either command.
Lifecycle plugins are pinned, Javadoc uses full doclint with warnings treated as failures, and CycloneDX generates
module and aggregate SBOMs during packaging. Test dependencies are excluded from the SBOMs.

## Pagination API Mapping

The numbered change reuses the existing `org.hiero.sdk.v3.common` API instead of introducing duplicate types:

| Common contract | Java mapping |
|-----------------|--------------|
| `Page<T>` | Public abstract class with a protected constructor |
| Immutable ordered data | `List<T> data()` returns a snapshot whose list structure cannot be modified |
| Declared size and page index | Final `int size()` and `int pageIndex()` accessors |
| Position inspection | Abstract synchronous `boolean hasNext()` and `boolean isFirst()` |
| Asynchronous navigation | Abstract `CompletionStage<Page<T>> next()` and `first()` |
| `mirror-node-error` | `MirrorNodeException` supplied through exceptional stage completion |

The named JPMS module `org.hiero.sdk.v3.base` exports `org.hiero.sdk.v3.common`, `org.hiero.sdk.v3.ledger`, and
`org.hiero.sdk.v3.nativeToken`, and declares JSpecify with
`requires static`. Public reference types have explicit annotations. The annotation dependency is optional for Maven
consumers; neither JSpecify nor test libraries are needed to run a consumer.

The constructor rejects null data immediately with `data must not be null`, and the existing non-null element
behavior of `List.copyOf` is preserved. The snapshot protects the list structure, not mutable state inside an element.
Declared size and page-index values are retained; no numeric bounds or size-to-list-length relationship is inferred.
`MirrorNodeException` retains the nullable message/cause conventions of `RuntimeException`.

Providers implement position and navigation behavior. The API contains no concrete provider, HTTP client, parser,
retry mechanism, or executor. Tests use small providers without network access. Equality remains inherited from the
existing API; this change does not introduce page-value equality or additional public methods.

## Common Source and Verification

The shared sources are [the proposal](../openspec/changes/1-add-common-pagination-public-api/proposal.md) and
[the specification](../openspec/changes/1-add-common-pagination-public-api/spec.md), last changed at revision
`1d0e998933b81136b491866bcddc467ced77ddd6`.

- Proposal SHA-256: `665b162efba2f5ae1479e71fbc7ccab1fddc4d1f74d80031b8f84a64a722ead8`.
- Specification SHA-256: `e5bd2ddfd39f73f16491726cba8fbe60d5b2892c602ffb82714fd63d55c1ddd8`.

The corresponding Java proposal and specification remain unchanged. Java mapping details are recorded here at the
user's request; no `design.md` is created for the numbered change.

| Shared scenario | Test coverage |
|-----------------|---------------|
| Page state is observed | `PageContractTest.State`: source snapshots, immutable output, null input, empty data, declared state |
| Page position is inspected | `PageContractTest.Position`: position values and zero retrieval calls |
| Next page is retrieved | `PageContractTest.Navigation.completesNextWithTheNextPageAsynchronously` |
| First page is retrieved | `PageContractTest.Navigation.completesFirstWithTheSameOperationsFirstPage` |
| Page retrieval fails | `PageContractTest.Navigation`: both operations with already-failed and later-failing stages |

`PublicApiShapeTest` verifies modifiers, generics, annotations, and JPMS boundaries. `ModuleConsumerTest` compiles and
runs a separate named module without annotation or test libraries at runtime, and rejects overriding final accessors.

## Native Token API Mapping

The native token change reuses the Java 21 API module, its JSpecify dependency, and its JUnit Jupiter/AssertJ tests.

| Common contract | Java mapping |
|-----------------|--------------|
| `nativeToken` namespace | Exported package `org.hiero.sdk.v3.nativeToken` in `org.hiero.sdk.v3.base` |
| `NativeTokenUnit` | Public interface, compatible with future enum-based providers |
| Immutable unit attributes | `String symbol()` and primitive `long baseUnitFactor()`; providers own immutable storage |
| `NativeToken<$$Self, $$Unit>` | Public abstract class `NativeToken<SELF extends NativeToken<SELF, UNIT>, UNIT extends NativeTokenUnit>` |
| Immutable amount and unit | Private final state, final `long amount()` and `UNIT unit()` accessors |
| Construction | Protected `NativeToken(long amount, UNIT unit)`; null units fail with `unit must not be null` |
| Unit conversion | Abstract `SELF to(UNIT targetUnit)` preserves the concrete self type and associated unit type |
| Base-unit retrieval | Abstract primitive `long toBaseUnits()` |

The abstract token class owns state and constructor validation, following the existing `Page` convention. The unit
interface allows enums to provide units. Providers must keep unit values immutable, return a non-null symbol, and
reject null target units with a descriptive null check. The package is `@NullMarked`; public reference inputs and
outputs also carry explicit `@NonNull` annotations. No new dependency or runtime library is required. Equality remains
inherited; the contract does not introduce value equality.

Consumers with a concrete provider can use the preserved self type directly:

```java
import org.hiero.sdk.v3.nativeToken.NativeToken;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;

static <S extends NativeToken<S, U>, U extends NativeTokenUnit> S convert(
        final S amount, final U target) {
    final S converted = amount.to(target);
    final long baseUnits = converted.toBaseUnits();
    return converted;
}
```

This change publishes only the public contract. Production conversion arithmetic, token-specific implementations,
exchange-rate fetching, and network behavior remain deferred. Fractional-result, overflow, and unit-factor validation policies
need a shared contract decision before production arithmetic is implemented. This change chooses no rounding,
truncation, saturation, positivity constraints on factors, or amount bounds. Test providers use only exactly
representable conversions; their arithmetic demonstrates provider obligations and is not production implementation.

## Native Token Source and Verification

The shared sources are [the proposal](../openspec/changes/2-add-native-token-public-api/proposal.md) and
[the specification](../openspec/changes/2-add-native-token-public-api/spec.md), read at source revision
`65dded267c95d003d444d3e3f897c9e04b8dcd6d` (before the directory was renamed to its numbered name).

- Proposal SHA-256: `db5b4102c7d86c698837b356a8aa7faf13a68673f523c9958a45ffd9e5d5cae5`.
- Specification SHA-256: `7ad86fc515d4e40e928c99bb7c101340ba0b0375ec1153390d8d21d68305987e`.

The Java proposal and native-token specification are byte-for-byte copies of those shared files. They remain unchanged;
Java mappings and traceability are recorded here, with no separate design artifact.

| Shared scenario | Passing provider-contract test |
|-----------------|--------------------------------|
| Unit state is observed | `NativeTokenContractTest.UnitState.observesImmutableUnitStateWithoutMutationMethods` |
| Token amount is observed | `NativeTokenContractTest.AmountState.observesStoredAmountAndAssociatedUnit` |
| Token amount is converted to a different unit | `NativeTokenContractTest.Conversion.returnsNewConcreteTokenInTargetUnitWithoutChangingSource` |
| Base units are calculated | `NativeTokenContractTest.BaseUnits.calculatesExactlyRepresentableBaseUnits` |

`NativeTokenApiShapeTest` checks recursive generic bounds, protected construction, immutable state/accessors,
primitive mappings, explicit nullability, and public signature boundaries. `NativeTokenModuleConsumerTest` compiles
an independent named module with enum units and a concrete token, rejects unrelated unit types in construction and
conversion, and executes with only the API and consumer on the module path.

The native-token change was verified on Java 21 with 63 tests (31 native token tests and 32 existing pagination/API tests).
To reproduce the build without replacing existing `target/` output, set `JAVA_HOME` to a Java 21 JDK and run:

```sh
mvn -Dsdk.build.directory=.verification/native-token verify
mvn -Dsdk.build.directory=.verification/native-token org.apache.maven.plugins:maven-javadoc-plugin:3.12.0:javadoc
```

## Exchange Rate API Mapping

The exchange-rate change reuses the Java 21 API module and its existing exported
`org.hiero.sdk.v3.nativeToken` package, JSpecify annotations, and JUnit Jupiter/AssertJ test setup.

| Common contract | Java mapping |
|-----------------|--------------|
| `nativeToken.ExchangeRate` | Public abstract `ExchangeRate` in `org.hiero.sdk.v3.nativeToken` |
| Immutable `expirationTime: zonedDateTime` | Private final `ZonedDateTime`, final `expirationTime()` accessor |
| Immutable `exchangeRateInUsdCents: double` | Private final primitive `double`, final `exchangeRateInUsdCents()` accessor |
| Construction | Protected `ExchangeRate(ZonedDateTime expirationTime, double exchangeRateInUsdCents)` |
| `isExpired(): bool` | Abstract primitive `boolean isExpired()` supplied by providers |

The constructor preserves the supplied expiration time, including its zone, and the double-precision USD-cent value
without normalization. A null expiration fails immediately with `expirationTime must not be null`. Public reference
inputs and outputs carry explicit `@NonNull` annotations within the existing `@NullMarked` package. Past expiration
times and negative, zero, non-finite, or other double values receive no additional validation. Equality remains inherited.

Expiration compares instants: a current instant strictly after expiration returns `true`; equality or an earlier instant
returns `false`, regardless of time zone. The API owns immutable state and required-input validation, while providers
choose the clock and implement evaluation. There is no public clock configuration API. Fetching, refresh, caching,
currency/token conversion, network access, and token-specific implementations remain deferred. Fixed-clock test
providers demonstrate the contract; they are not production expiration implementations.

Consumers use the provider through the exported API:

```java
import org.hiero.sdk.v3.nativeToken.ExchangeRate;

static boolean isUsable(final ExchangeRate rate) {
    return !rate.isExpired();
}
```

## Exchange Rate Source and Verification

The shared sources are [the proposal](../openspec/changes/3-add-exchange-rate-public-api/proposal.md) and
[the specification](../openspec/changes/3-add-exchange-rate-public-api/spec.md), read at source revision
`2d4eb407cd88eeb30920288960c6d85962ab532e`.

- Proposal SHA-256: `4fc9763469d6af3d66a67ebec22d2dccf6c41ac0b01b2ce2b7eba4a9fe0e835e`.
- Specification SHA-256: `4f95c9edf47fa548347028ad0349028a3195a9142c04c65b94c2a481e467732d`.

The Java proposal matches the shared proposal byte for byte. The Java specification retains the shared contract,
differing only by a trailing newline. Both Java artifacts remain unchanged, and no design artifact is created.

| Shared scenario | Passing provider-contract test |
|-----------------|--------------------------------|
| Exchange-rate state is observed | `ExchangeRateContractTest.State.observesStoredExpirationIncludingZoneAndUsdCentValue` |
| Exchange rate has expired | `ExchangeRateContractTest.Expiration.expiresOnlyWhenCurrentInstantIsStrictlyLater` |
| Exchange rate has not expired | `ExchangeRateContractTest.Expiration.doesNotExpireAtOrBeforeExpiration` and `equalInstantsDoNotExpireRegardlessOfZone` |

`ExchangeRateContractTest.State` also checks descriptive null rejection and unchanged double values, including signed
zero, negative values, infinities, and NaN, without adding rate validation. `ExchangeRateApiShapeTest` checks protected
construction, final fields/accessors, primitive mappings, nullability, abstract evaluation, and the public signature
boundary. `ExchangeRateModuleConsumerTest` compiles and runs an independent named module with only the API and consumer
at runtime. Its fixed-clock provider verifies state and expiration before, at, and after the nanosecond boundary.

The exchange-rate change was verified on Java 21 with 89 tests (26 exchange-rate tests and 63 existing tests). Set `JAVA_HOME`
to a Java 21 JDK and run these commands to preserve existing `target/` output:

```sh
mvn -Dsdk.build.directory=.verification/exchange-rate verify
mvn -Dsdk.build.directory=.verification/exchange-rate org.apache.maven.plugins:maven-javadoc-plugin:3.12.0:javadoc
```

## Network Identity API Mapping

This change reuses the Java 21 API module, JSpecify annotations, JUnit Jupiter/AssertJ setup, and the existing
`NativeTokenUnit` contract from `2-add-native-token-public-api`.

| Common contract | Java mapping |
|-----------------|--------------|
| `ledger.Network<$$Unit extends NativeTokenUnit>` | Public record `org.hiero.sdk.v3.ledger.Network<UNIT extends NativeTokenUnit>` |
| Immutable `id: bytes` | Required `byte[]`, cloned by the public constructor and every `id()` call |
| Nullable `name: string` | `@Nullable String name()`; absence remains `null` |
| Immutable `nativeTokenUnit: $$Unit` | Required typed `UNIT nativeTokenUnit()`, retaining the supplied immutable provider |
| Construction | Public canonical constructor `Network(byte[] id, String name, UNIT nativeTokenUnit)` |

The record owns private final state and exposes no mutation API. The ledger package is `@NullMarked`; ID arrays and
unit references have explicit `@NonNull` annotations, and the name has explicit `@Nullable` annotations. A null ID
fails with `id must not be null`; a null unit fails with `nativeTokenUnit must not be null`. IDs of any length retain
their exact contents and order, including empty arrays and leading zeros. Names retain their exact supplied value,
including null, empty strings, and whitespace. Construction and reads do not inspect, copy, or convert the unit;
unit providers must satisfy the existing immutability contract.

Java value equality compares ID bytes by content, the nullable name, and the unit using its own `equals` semantics.
The hash uses those same values, and `toString()` renders ID bytes by content as
`Network[id=[0, 1], name=example, nativeTokenUnit=BASE]`. Defensive copies prevent array mutations from changing
equality, hashing, or text output. These are Java representation choices, not additional common requirements or a
network-ID uniqueness policy; equal IDs with different names or units need not produce equal records.

For a concrete immutable unit provider, construction preserves its type:

```java
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;

static <U extends NativeTokenUnit> Network<U> unnamedNetwork(final byte[] id, final U unit) {
    return new Network<>(id, null, unit);
}
```

This change stores identity only. Predefined network instances, ID assignment rules, checksums, address parsing,
node addresses/discovery, network settings, refresh, and transport behavior remain deferred. Construction and
observation perform no metadata fetching or network access, and add no runtime dependencies.

## Network Identity Source and Verification

The shared sources are [the proposal](../openspec/changes/4-add-network-identity-public-api/proposal.md) and
[the specification](../openspec/changes/4-add-network-identity-public-api/spec.md), read at source revision
`bd1c16017837047432bf7cf2aa44789769a14e29` (before the directory was renamed to its numbered name).

- Proposal SHA-256: `0c975d8e20497362f53778a712fee37aa4a53c8f44e7a05779a580460f91dce9`.
- Specification SHA-256: `efb9ec51baf3e10cde5cb6453af2162f0e7de6660f8a1186fc3f3f9adf3ddb7e`.

The Java proposal and network-identity specification are byte-for-byte copies of the shared files. They remain
unchanged; Java mappings and traceability are recorded here, with no separate design artifact.

| Shared scenario | Passing contract tests |
|-----------------|------------------------|
| Network identity state is observed | `NetworkContractTest.observesExactIdentityNameAndConcreteUnit` and `ownsIdIndependentlyOfInputAndEveryReturnedArray` |

`NetworkContractTest` also covers absent/empty/unmodified names, descriptive null checks, no unit metadata inspection,
array-content equality, unit value equality, and stable hashing/text after external array mutation. `NetworkApiShapeTest`
checks record construction, final fields, typed accessors/bounds, explicit nullability, public signatures, and exports.
The canonical constructor is explicit to preserve the ID-array parameter annotation in Java 21 bytecode.
`NetworkModuleConsumerTest` compiles and runs an independent named module with only the API and consumer at runtime;
negative compilation cases reject non-unit type arguments and assignment to an unrelated unit type.

The Java 21 reactor verification passes all 114 tests (25 network-identity tests and 89 existing tests). Set `JAVA_HOME`
to a Java 21 JDK and run these commands to preserve existing `target/` output:

```sh
mvn -Dsdk.build.directory=.verification/network-identity verify
mvn -Dsdk.build.directory=.verification/network-identity org.apache.maven.plugins:maven-javadoc-plugin:3.12.0:javadoc
```

## Numeric Address Mapping and Pending Decisions

Change `5-add-numeric-address-public-api` has not implemented address types or behavior. It reuses the verified
`Network` contract from change 4 and the existing ledger package rather than introducing another network type.

| Common contract | Planned Java mapping |
|-----------------|----------------------|
| `BaseAddress` | Public unsealed abstract class in `org.hiero.sdk.v3.ledger`, protected state-owning construction |
| Final numeric `Address` | Public final `Address extends BaseAddress`, with a required numeric selector |
| `shard` and `realm`: `uint64` | Primitive `long` storing all 64 unsigned bits |
| Base nullable `num: uint64` | `@Nullable Long num()` |
| Concrete required `num: uint64` | Overriding `@NonNull Long num()` with a constructor-enforced non-null invariant |
| Immutable checksum | Required `String checksum()`; `""` represents absence |
| Checksum target | Required `Network<?>`, accepting any associated native-token unit type |
| Canonical formatting | `String toString()` and `String toStringWithChecksum()` |
| Parsing | Static `Address.fromString(String address)` returning `Address` |
| `illegal-format` | `IllegalArgumentException` for malformed non-null text |

The `long`/`Long` mapping follows the shared Java guideline. Values from `9223372036854775808` through
`18446744073709551615` use negative signed Java storage patterns but remain valid unsigned values. Decimal parsing
and formatting must preserve the full unsigned range; negative decimal input is invalid. The same boxed `Long`
return type on parent and child allows nullability narrowing without an illegal primitive-return override or
shadowing selector state. The schema resolves the prose's optional checksum as a non-null string with empty-string
absence, rather than a nullable checksum. Required reference inputs will use descriptive `NullPointerException`
checks; null parser input will be distinguished from malformed non-null input. No address API is published yet.

Tasks 1.4 and 1.5 require user decisions before concrete address behavior is implemented. Final `Address` and a static
factory cannot leave required parsing and formatting abstract. The proposed approach, awaiting approval, is to supply
the concrete local behavior in the API module. No parser, checksum engine, provider framework, registry, or failing
stub has been added.

The proposed checksum policy, also awaiting approval, uses the algorithm and published vectors in
[HIP-15](https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-15.md), five lowercase letters,
and strict decimal syntax without leading zeros, whitespace, or plus signs. The proposed absent-checksum result is
`false`; parsing would validate syntax and retain the supplied checksum, while `validateChecksum(network)` would
evaluate its network-specific value. These choices are not yet accepted requirements. Concrete construction details,
sentinel placement, and behavioral test coverage remain pending. EVM selectors, aliases, account/contract identifiers,
checksum-generation APIs, and network configuration remain outside this change.

The shared sources are [the proposal](../openspec/changes/5-add-numeric-address-public-api/proposal.md) and
[the specification](../openspec/changes/5-add-numeric-address-public-api/spec.md), read at source revision
`f27ddd82aa60479f4850dde4957ec9ac86e8fc88` (before the directory was renamed to its numbered name).

- Proposal SHA-256: `50de99e290fd6b3e56d62944a7fe2e5fa78de5f97bf6d5075be5706108d364ac`.
- Specification SHA-256: `f9a5b84331ef5b516764f5332b93eae8f50a4dcd7af4b820f6f56715b6e11cd6`.

The Java proposal and numeric-address specification match the shared files byte for byte and remain unchanged.
No design artifact is created. Scenario coverage remains pending until the required decisions and implementation.

## EVM Address Mapping and Pending Decision

Change `6-add-evm-address-public-api` has not implemented EVM address types or behavior. Its planned standalone
immutable value is `org.hiero.sdk.v3.ledger.EvmAddress`, independent of `BaseAddress`, `Network`, shard/realm values,
and account/contract existence. It can reuse the existing ledger export and Java 21 test/build setup.

| Common contract | Planned Java mapping |
|-----------------|----------------------|
| Exactly 20 immutable raw bytes | `byte[] bytes()`, with copies on construction and every read |
| Byte construction | Static `EvmAddress fromBytes(byte[] value)` |
| Hex parsing | Static `EvmAddress fromString(String value)` |
| Canonical formatting | Overridden `String toString()` returning the specified EIP-55 form |
| Required-input violation | `NullPointerException` with `value must not be null` on factory inputs |
| Non-null malformed input (`illegal-format`) | `IllegalArgumentException` |

The byte factory must accept all 20-byte values, including all zeros, with no padding, truncation, reordering, or
network validation. The text factory must accept exactly 40 ASCII hexadecimal digits with optional lowercase `0x`,
regardless of input letter casing, including mixed casing that fails EIP-55. It must reject whitespace, Unicode
lookalikes, `0X`, signs, separators, repeated prefixes, ledger-checksum suffixes, and shortened values. Output must
preserve all bytes and leading zeros as lowercase `0x` plus 40 digits with deterministic EIP-55 casing.

Task 1.3 requires user agreement on the concrete-behavior boundary before implementation. The proposed approach,
awaiting approval, is concrete parsing/formatting in the API module with private Keccak-256 support. The authoritative
[EIP-55 reference](https://eips.ethereum.org/EIPS/eip-55) specifies Keccak-256 over the lowercase hexadecimal text for
output casing and provides acceptance vectors. No parsing, hashing implementation/dependency, provider framework,
registry, placeholder, or failing stub has been added. Representation, equality/hash details, and all 18 scenario
mappings remain pending implementation. Ledger checksums, chain-specific formatting, selector hierarchies, network
lookups, and account/contract existence checks remain deferred.

The shared sources are [the proposal](../openspec/changes/6-add-evm-address-public-api/proposal.md) and
[the specification](../openspec/changes/6-add-evm-address-public-api/spec.md), read at source revision
`d60d40710b1ea00f39cd99134b58dd30ddaa9d02`.

- Proposal SHA-256: `e3cf1b69fbf8fcb021d315dc5fe4cbe0fca2de87d4f309ccc51e30d1e96bf134`.
- Specification SHA-256: `536bc61232b689d967f53c7eb3eef42183ed390a16db350c322f3453b76cb51b`.

The Java proposal matches the shared proposal byte for byte. The Java specification differs only in its two adjusted
relative links to `spec/base/ledger.md`; behavior is unchanged. Both Java artifacts remain unchanged, and no design
artifact is created.
