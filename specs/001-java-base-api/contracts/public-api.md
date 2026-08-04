# Contract: Generated Java Base Public API

## Consumer Artifact

- Maven artifact working name: `org.hiero.sdk.v3:hiero-sdk-base-api`
- JPMS module: `org.hiero.sdk.v3.base`
- Java release: 21
- Runtime dependencies: none beyond `java.base`
- Compile-time annotation dependency: `org.jspecify:jspecify:1.0.0`
- Exported packages: exactly the eleven normalized base namespace packages
- Internal/generated-tool packages: never exported and never included in the consumer artifact

## Canonical Type Mapping

| Source form | Java API form |
|---|---|
| provider-owned/behavior-only `abstraction` | public interface with abstract methods |
| pure immutable value | public record |
| immutable value containing `byte[]` | public final value class with defensive copying/content equality |
| closed data hierarchy | public sealed interface plus public record/final-class variants |
| enum without operations | public enum |
| enum metadata | private final fields plus public accessors |
| namespace constant | public static final field in `<Namespace>Constants` |
| `@@async T` | non-null `CompletionStage<T>` |
| nullable reference | boxed/reference type annotated `@Nullable` |
| non-null reference | reference type annotated `@NonNull` and validated at concrete boundaries |
| `bytes` | `byte[]` with copy-in/copy-out ownership |
| `list<T>` / `set<T>` | immutable `List<T>` / `Set<T>` snapshot |
| `zonedDateTime` | `ZonedDateTime` |
| `uint16` / `uint64` | `int` / `long` plus non-negative/range constraints |
| `double` | primitive `double` unless nullable |
| `ANY` wildcard argument | Java `?` wildcard |

## Source Type Inventory

| Package | Required public source types |
|---|---|
| `common` | `Page<T>` |
| `grpc` | `MethodDescriptor` |
| `proto` | no type; documented package reservation |
| `nativetoken` | `NativeTokenUnit`, `NativeToken<Self, Unit>`, `ExchangeRate` |
| `hedera` | `HederaNetworkSetting`, `HbarUnit`, `Hbar` |
| `token` | `TokenType`, `TokenSupplyType` |
| `keys` | `KeyType`, `KeyAlgorithm`, `Key`, `KeyPair`, `PublicKey`, `PrivateKey`, `RawFormat`, `KeyEncoding`, `KeyContainer`, `ByteImportEncoding`, `KeyFormat` |
| `ledger` | `Network<Unit>`, `BaseAddress`, `Address`, `EvmAddress`, `EvmCapableAddress`, `ContractId`, `AccountId`, `TransactionId`, `IpAddress`, `ConsensusNode`, `MirrorNode` |
| `ledger.config` | `NetworkSetting` |
| `authority` | `Authority`, `PublicKeyAuthority`, `ContractAuthority`, `AuthorityList` |
| `solo` | `SoloNetworkSetting` |

## Java Companion Contracts

The following are Java mapping artifacts. They are part of the public prototype but do not add
domain behavior beyond source operations.

### AuthorityFactory

- all-of from `Authority...`
- threshold m-of-n from `int` and `Authority...`
- public-key leaf from `PublicKey`
- plain contract leaf from `ContractId`
- delegatable contract leaf from `ContractId`

### KeyFactory

- generate private/public keys by `KeyAlgorithm`
- create private/public keys from algorithm, byte-import encoding, and string
- create private/public keys from algorithm and raw bytes
- create private/public keys from key format and string or bytes
- create private/public keys from the default string format

All malformed inputs use `IllegalArgumentException`. The interface has no default methods and no
provider discovery.

### KeyFormatOperations

- decode a `KeyEncoding` for a key type and string
- test whether a `KeyContainer` supports a key type
- decode a `ByteImportEncoding` string
- test whether a `KeyFormat` supports a key type
- decode a `KeyFormat` for a key type and string

Passing the enum value explicitly replaces source enum instance operations because Java enum
constants cannot satisfy abstract methods without implementations.

### LedgerFactory

- create/parse `EvmAddress`, `IpAddress`, `Address`, `ContractId`, and `AccountId`
- generate and parse `TransactionId`
- preserve all declared inputs and `illegal-format` errors

### LedgerOperations

- validate a `BaseAddress` checksum against a network
- produce canonical and checksum-appended address strings
- produce canonical `EvmAddress`, `TransactionId`, and `IpAddress` strings

### NativeTokenOperations

- convert a native-token amount to another unit of the same token
- obtain base-unit totals and HBAR tinybar totals
- evaluate exchange-rate expiration

### NetworkSettingRegistry

- register a setting by identifier
- look up a setting by identifier, with `NoSuchElementException` for absence

No registry instance, global state, lookup strategy, or service loader is provided.

## Constants

- `HederaConstants.HEDERA_MAINNET_IDENTIFIER = "hedera-mainnet"`
- `HederaConstants.HEDERA_TESTNET_IDENTIFIER = "hedera-testnet"`
- `SoloConstants.SOLO_IDENTIFIER = "solo"`
- `LedgerConstants.ZERO_ADDRESS`
- `LedgerConstants.ZERO_ACCOUNT_ID`
- `LedgerConstants.ZERO_CONTRACT_ID`

The three ledger sentinels are immutable structural values. Their types contain no checksum,
formatting, or parsing implementations; those contracts live on the ledger companions.

## HBAR Unit Metadata

| Unit | Symbol | Base-unit factor |
|---|---|---:|
| `TINYBAR` | `tℏ` | 1 |
| `MICROBAR` | `μℏ` | 100 |
| `MILLIBAR` | `mℏ` | 100,000 |
| `HBAR` | `ℏ` | 100,000,000 |
| `KILOBAR` | `kℏ` | 100,000,000,000 |
| `MEGABAR` | `Mℏ` | 100,000,000,000,000 |
| `GIGABAR` | `Gℏ` | 100,000,000,000,000,000 |

The authoritative `spec/base/hedera.md` schema defines every symbol and base-unit factor in this
table. Generation must reproduce those values exactly and fail validation if they diverge; the
metadata is not a Java mapping variance.

## Required API Semantics

- Every public type/member has source-linked Javadoc, including constraints, errors, and retained
  source questions.
- No method returns a mutable internal array or collection.
- No collection is nullable and no collection contains null elements.
- `Page` async methods return immediately with non-null stages; execution policy is unspecified.
- Private key material never appears in `toString()`, exception text, Javadoc examples, manifests,
  tests, or logs.
- `Authority` is exhaustive and has no private-key variant.
- No public signature references generator, test, implementation, transport, crypto-provider,
  protobuf, gRPC, HTTP, JSON, or other unapproved types.
- No API class performs network, storage, crypto, parsing, formatting, serialization, conversion,
  registry, scheduling, retry, timeout, or provider-loading work.

## Mapping Variances Requiring Human Approval

1. Versioned package root `org.hiero.sdk.v3` instead of the guideline example `org.hiero`.
2. One aggregate JPMS base module instead of one module per namespace.
3. Companion interfaces for namespace/static and structural-value operations.
4. `KeyFormatOperations` for operational methods declared on enums.
5. Sealed/final ledger value mappings, concrete immutable sentinel constants, and companion factory
   contracts used to preserve source finality without operational implementations.
6. `NetworkSettingRegistry` as an abstract companion contract with no registry instance, state, or
   provider-loading behavior.
7. Direct public construction of Authority record variants, despite factories being the blessed
   path, because public variants are required for exhaustive matching.

Approval of this plan approves these as prototype mappings only. They become the permanent V3 Java
compatibility baseline only after generated-source API and security review.
