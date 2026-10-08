# Base API Mapping Matrix

This matrix is the review inventory for the directly maintained Java API. The authoritative inputs are
`spec/base/*.md`, `guidelines/api-guideline.md`, and `guidelines/api-best-practices-java.md`. Every named declaration,
member, constraint, error, constant, dependency, and retained question is accounted for below. No Markdown schema
parser or generator is part of this workspace.

## Namespace and declaration inventory

| Source | Namespace / dependencies | Java declarations | Members and operations | Constraints, errors, questions |
|---|---|---|---|---|
| `spec/base/authority.md` | `authority`; requires `PublicKey`, `ContractId` | sealed `Authority`; records `PublicKeyAuthority`, `ContractAuthority`, `AuthorityList` | `publicKey`, `contractId`, `delegatable`, `children`, `threshold`; `Authority.of`, `ofContract`, `ofDelegatable` | `@@sealed`, `@@finalType`, non-empty children, `1 <= threshold <= children.size`; private keys structurally excluded; all six question topics remain in this file and in API Javadoc terminology |
| `spec/base/common.md` | `common` | abstract `Page<T>`; `MirrorNodeException` | `data`, `size`, `pageIndex`, `hasNext`, `isFirst`, async `next`, async `first` | immutable non-null list; `mirror-node-error` maps to exceptional `CompletionStage` completion and documented `MirrorNodeException` |
| `spec/base/grpc.md` | `grpc` | record `MethodDescriptor` | `serviceName`, `methodName` | both fields non-null; transport implementation deferred |
| `spec/base/hedera.md` | `hedera`; requires `NetworkSetting`, `NativeToken`, `NativeTokenUnit` | abstract `HederaNetworkSetting`, enum `HbarUnit`, abstract `Hbar`, `HederaConstants` | inherited network setting fields; `symbol`, `baseUnitFactor`; inherited amount/unit/to/toBaseUnits; `toTinybars`; two identifiers | unit values and factors are exact; conversions remain abstract |
| `spec/base/keys.md` | `keys` | enums `KeyType`, `KeyAlgorithm`, `RawFormat`, `KeyEncoding`, `KeyContainer`, `ByteImportEncoding`, `KeyFormat`; abstract `Key`, `PublicKey`, `PrivateKey`; record `KeyPair`; interfaces `KeyFactory`, `KeyFormatOperations` | all declared fields, imports, exports, generation, signing, verification, derivation, decoding, and support checks | immutable defensive byte ownership; all `illegal-format` contracts documented; crypto, parsing, encoding, and generation body-free |
| `spec/base/ledger-config.md` | `ledger.config`; requires `Network`, `ConsensusNode`, `MirrorNode` | abstract `NetworkSetting`; `NetworkSettingRegistry` | `network`, `getConsensusNodes`, `getMirrorNodes`; register/get | immutable non-null set snapshots; `not-found-error` maps to documented `NoSuchElementException`; mutable registry behavior deferred |
| `spec/base/ledger.md` | `ledger`; requires `NativeTokenUnit` | `Network<U>`; sealed abstract `BaseAddress`, final `Address`, sealed abstract `EvmCapableAddress`, final `ContractId`, final `AccountId`, abstract `EvmAddress`, abstract `TransactionId`; `IpAddress`, `ConsensusNode`, `MirrorNode`; `LedgerConstants`, `LedgerFactory`, `LedgerOperations` | every declared identifier field and method; `ZERO_ADDRESS`, `ZERO_ACCOUNT_ID`, `ZERO_CONTRACT_ID`; all parsing, checksum, raw-byte, EVM-field, and transaction-id factory operations | unsigned values checked non-negative; exact 20/4-byte lengths; one-of selectors; nullable alias/EVM/nonce/name; uint16 port; inherited `num` narrowing; `illegal-format` documented; both open questions retained below |
| `spec/base/native-token.md` | `nativeToken` | `NativeTokenUnit`; abstract `NativeToken<S,U>`; abstract `ExchangeRate`; `NativeTokenOperations` | `symbol`, `baseUnitFactor`, `amount`, `unit`, `to`, `toBaseUnits`, `expirationTime`, `exchangeRateInUsdCents`, `isExpired` | recursive generic bounds retained; non-null unit/time; conversion and clock observation deferred; unresolved `double` versus decimal question retained below |
| `spec/base/proto.md` | `proto` | `ProtoNamespace` | namespace anchor only | declaration-free namespace remains exported; protocol implementation deferred |
| `spec/base/solo.md` | `solo`; requires `NetworkSetting` | abstract `SoloNetworkSetting`; `SoloConstants` | inherited setting members; `SOLO_IDENTIFIER` | no operational behavior |
| `spec/base/token.md` | `token` | enums `TokenType`, `TokenSupplyType` | all four enum constants | typed identifier follow-up question retained below |

## Mapping variances

| ID | Source construct | Java mapping | Review rationale |
|---|---|---|---|
| MV-001 | Namespace-level operational `@@static` functions | body-free companion interfaces (`KeyFactory`, `KeyFormatOperations`, `LedgerFactory`, `LedgerOperations`, `NetworkSettingRegistry`, `NativeTokenOperations`) | Java cannot declare abstract static methods; later modules implement these contracts without discovery or global state. |
| MV-002 | Structural namespace factories | static methods on `Authority`, `AccountId`, `ContractId`, and `IpAddress` | Their results depend only on validated arguments and are approved structural bodies. |
| MV-003 | `@@async Page<T>` results | `CompletionStage<Page<T>>` | Preserves asynchronous completion without selecting an executor or transport. |
| MV-004 | Declared terminal errors | `MirrorNodeException`, `IllegalArgumentException`, and `NoSuchElementException` documentation | Java uses typed exceptional completion/runtime exceptions; no operational throwing placeholder is supplied. |
| MV-005 | `uint64`, `uint16`, `int32`, `int64` | `long`, validated `int`, `int`, `long` | Java has no unsigned primitive API equivalents; construction enforces source ranges where required. |
| MV-006 | Nullable primitive fields | boxed `Long` / `Integer` plus JSpecify `@Nullable` | Retains absence without `Optional` fields or parameters. |
| MV-007 | Nullable-to-non-null inherited `num` | covariant `Address.num(): Long` with constructor guarantee | Java cannot override `Long` with primitive `long`; every `Address` instance is still guaranteed non-null. |
| MV-008 | Pure immutable data | records where inheritance and array ownership do not prevent them | Records provide transparent value semantics and compact validation. |
| MV-009 | State-bearing abstractions | abstract classes with protected validating constructors | Enforces nullability, range, collection, and byte ownership while leaving SDK operations abstract. |
| MV-010 | Closed authority sum | sealed interface plus public record variants | Supports exhaustive Java 21 pattern matching and excludes private keys by type. |
| MV-011 | `EvmAddress` value with cryptographic EIP-55 string form | abstract class owning exactly 20 bytes; abstract `toString`; construction/parsing on `LedgerFactory` | Byte validity is structural; Keccak-based formatting and parsing are operational and deferred. |
| MV-012 | Empty `proto` namespace | exported package with `ProtoNamespace` anchor | JPMS cannot export a package with no compiled type. The anchor has no behavior. |
| MV-013 | JSpecify nullness | `@NullMarked` package declarations and targeted `@Nullable`; `requires static transitive org.jspecify` | Exposes compile-time nullness while adding no operational runtime dependency. |

## Structural versus operational operations

Structural implementations are limited to the entries in [structural-allowlist.md](structural-allowlist.md). All of
these operation families remain body-free: external string parsing; checksum computation/validation; EIP-55 hashing;
key generation, signing, verification, derivation, import/export and decoding; token conversion; expiration clock
observation; transaction-id generation/parsing; registry mutation/lookup; transport and protocol work; provider
selection/discovery; networking, I/O, scheduling, retries, persistence, and serialization.

## Retained source questions

- `authority.md`: forthcoming `@@sealed` guideline mapping; separation from `keys.Key`; naming and `Endorsement`;
  m-of-n ergonomics; factory discoverability; key clearing; wire canonicalization.
- `ledger.md`: whether `Ledger` should be named `Network`; rules for creating network identifier bytes.
- `native-token.md`: whether `exchangeRateInUsdCents` should be decimal rather than `double`.
- `token.md`: future `TokenId`, `NftId`, and `PendingAirdropId` types.

These questions are intentionally not resolved by the Java translation.

## Direct-maintenance rule

A change to `spec/base` is incomplete until reviewers inspect the affected Java source, this matrix, contract tests,
and the public-signature snapshot together. An unlisted source construct or signature change fails review rather than
being inferred by tooling.
