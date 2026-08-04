# Data Model: Java Base Public API Prototype

## Generation Model

### SourceDocument

- `path`: repository-relative Markdown path
- `sha256`: exact input content hash
- `namespace`: declared namespace
- `schemaStartLine`: source location of the fenced API block
- `imports`: ordered `NamespaceImport` values
- `declarations`: ordered `Declaration` values
- `openQuestions`: retained prose questions and comments

Validation: exactly one API namespace per base document; every imported type must be used and
resolvable; duplicate namespace/type declarations fail generation.

### Declaration

Closed variants: `AbstractionDeclaration`, `ValueDeclaration`, `EnumDeclaration`,
`ConstantDeclaration`, and `NamespaceOperationDeclaration`.

Shared fields include source location, documentation, annotations, generic parameters, parent
types, fields, and methods. Declaration order is retained for traceability but output ordering is
canonical by package and type name.

### TypeReference

Closed variants cover primitives, bytes, temporal values, declared types, generic parameters,
wildcards, lists, sets, and method return types. Resolution produces a fully qualified Java type and
the namespace dependency edge that justified it.

### MappingDecision

- `sourceElement`: stable `namespace#declaration.member` identity
- `javaElement`: fully qualified generated element
- `mappingKind`: direct, structural, companion-contract, documented-only, or deferred-enforcement
- `rationale`: deterministic rule identifier
- `requirementIds`: FR references

Every parsed declaration, field, method, constraint, and error must have exactly one mapping
decision. Unmapped or multiply mapped source elements fail generation.

### GenerationManifest

Contains the source revision and hashes, generator version, configuration hash, Java release,
package root, ordered source-to-Java inventory, retained questions, deferred enforcement items, and
generated output hashes. It contains no timestamps, absolute paths, hostnames, usernames, or secret
data.

## Public Domain Model

### Namespace Inventory

| Source namespace | Package | Declared types | Java form summary |
|---|---|---:|---|
| `common` | `org.hiero.sdk.v3.common` | 1 | `Page<T>` interface, mirror-node exception |
| `grpc` | `org.hiero.sdk.v3.grpc` | 1 | immutable `MethodDescriptor` record |
| `proto` | `org.hiero.sdk.v3.proto` | 0 | documented reserved package only |
| `nativeToken` | `org.hiero.sdk.v3.nativetoken` | 3 | unit/token/rate interfaces |
| `hedera` | `org.hiero.sdk.v3.hedera` | 3 | setting/token interfaces and unit enum |
| `token` | `org.hiero.sdk.v3.token` | 2 | classifier enums |
| `keys` | `org.hiero.sdk.v3.keys` | 11 | key interfaces, value carrier, format enums, operation contracts |
| `ledger` | `org.hiero.sdk.v3.ledger` | 11 | network/value carriers and identifier interfaces |
| `ledger.config` | `org.hiero.sdk.v3.ledger.config` | 1 | setting and registry interfaces |
| `authority` | `org.hiero.sdk.v3.authority` | 4 | sealed sum and immutable variants |
| `solo` | `org.hiero.sdk.v3.solo` | 1 | setting interface and identifier constant |

The count is 37 source-declared types. Companion interfaces and exception/constants holders are
Java mapping artifacts and are separately identified in the manifest.

### Common and Protocol

- `Page<T>` owns an immutable ordered data snapshot plus `size` and `pageIndex`. `hasNext()` and
  `isFirst()` are synchronous observations. `next()` and `first()` are non-blocking contracts
  returning `CompletionStage<Page<T>>`; terminal mirror-node errors complete the stage
  exceptionally.
- `MethodDescriptor` is an immutable pair of non-null service and method names. It contains no gRPC
  library type or transport behavior.
- `proto` intentionally contains only `package-info.java`; adding a fabricated type is invalid.

### Native Token, Hedera, and Token

- `NativeTokenUnit` exposes `symbol` and `baseUnitFactor`.
- `NativeToken<Self, Unit>` preserves its recursive self bound and exposes amount and unit;
  conversion and base-unit-total operations remain on `NativeTokenOperations`.
- `ExchangeRate` exposes `ZonedDateTime` and the source-defined `double`; time-sensitive expiration
  observation remains on `NativeTokenOperations` without implementing clock policy.
- `HbarUnit` is a fixed enum with source-documented symbol and base-unit metadata.
- `Hbar` specializes `NativeToken<Hbar, HbarUnit>`; `toTinybars()` remains an abstract companion
  operation.
- `TokenType` and `TokenSupplyType` are direct enums with no runtime behavior.

### Ledger and Configuration

- `Network<Unit extends NativeTokenUnit>` owns copied identifier bytes, an optional name, and a
  non-null native-token unit.
- `BaseAddress` exposes shard, realm, checksum, and nullable number as a sealed structural interface.
  `Address` narrows number to non-null. `EvmCapableAddress` adds nullable EVM selector. Checksum and
  formatting behavior remains on `LedgerOperations`.
- `ContractId` has exactly one of number/EVM selector. `AccountId` has exactly one of number/EVM/key
  alias. `EvmAddress` is exactly 20 bytes. `IpAddress` is exactly 4 bytes in this revision.
- `TransactionId` contains payer account, zoned valid-start, optional nonce, and formatting
  contracts.
- `ConsensusNode` contains IP, unsigned-16-bit port semantics, and fee account. `MirrorNode` contains
  the REST base URL as specified, without performing URL access.
- `ZERO_ADDRESS`, `ZERO_ACCOUNT_ID`, and `ZERO_CONTRACT_ID` are structural sentinel values only.
- `NetworkSetting` exposes a network wildcard and immutable sets of consensus and mirror nodes.
  `HederaNetworkSetting` and `SoloNetworkSetting` specialize it. Registry mutation/lookup stays on
  `NetworkSettingRegistry`; the API owns no registry state.

### Keys

- `Key` exposes copied raw bytes plus algorithm and key type, and declares export contracts.
- `PublicKey` declares verification; `PrivateKey` declares signing and public-key derivation. No
  method has a default body and diagnostics must never render raw key bytes.
- `KeyPair` is an immutable public/private pair.
- `KeyType`, `KeyAlgorithm`, `RawFormat`, `KeyEncoding`, `KeyContainer`,
  `ByteImportEncoding`, and `KeyFormat` preserve exactly the source enum constants and structural
  metadata.
- Key generation/import factories remain abstract on `KeyFactory`. Operational enum decoding and
  support checks remain abstract on `KeyFormatOperations`.

### Authority

`Authority` is a sealed pure-data sum with exactly three public variants:

- `PublicKeyAuthority(publicKey)` accepts only `PublicKey`; no private-key path exists.
- `ContractAuthority(contractId, delegatable)` preserves the false default as factory semantics.
- `AuthorityList(children, threshold)` owns a non-empty ordered list and enforces
  `1 <= threshold <= children.size()`.

Structural equality is recursive. Factory operation signatures remain abstract on
`AuthorityFactory`, while direct record construction remains available for read-side pattern
matching and prototype fixtures.

## Ownership and Lifecycle

- Generated concrete public values are immutable after construction.
- Generated concrete values copy incoming arrays and collections before storage, copy outgoing
  arrays on every access, and expose collections as non-null immutable snapshots.
- Interface-backed values such as `Key`, `PublicKey`, and `PrivateKey` document byte-ownership
  requirements and record their behavioral enforcement as deferred in the manifest. Provider TCKs
  must verify copy-in/copy-out behavior when implementations are introduced.
- Interfaces define no lifecycle, resource, scheduler, executor, retry, timeout, or cancellation
  behavior.
- API values are safely shareable by construction. Implementations of operation interfaces define
  their own thread-safety in later features; this prototype does not claim it.

## Validation States

Generation has three terminal outcomes:

1. **Valid**: every source element maps exactly once and all references resolve.
2. **Invalid source/mapping**: generation fails before writing output and reports deterministic
   repository-relative file/line diagnostics.
3. **Valid with deferred enforcement**: Java cannot enforce a source rule in an implementation-free
   type. The generated Javadoc and manifest identify the gap and its required human approval.

Generated output is accepted only when two clean generations are byte-identical, all contract tests
pass, and API/security reviewers approve the mapping variances and deferred enforcement list.
