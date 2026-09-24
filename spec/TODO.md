# APIs to Implement

This file lists the implementation deliverables for every language-agnostic API currently defined under `spec/`.
Each language SDK must translate these contracts into idiomatic public APIs while preserving the specified fields,
constraints, errors, asynchronous behavior, and namespace boundaries.

## Base APIs

### `base/common.md`

- Implement `Page<T>` with immutable `data`, `size`, and `pageIndex` values.
- Implement `hasNext()` and `isFirst()`.
- Implement asynchronous `next()` and `first()` operations.
- Map `mirror-node-error` into the language's public error model.

### `base/ledger.md`

- Implement `Network<Unit>`.
- Implement `BaseAddress`, `Address`, `EvmCapableAddress`, `ContractId`, and `AccountId`.
- Implement `EvmAddress`, `TransactionId`, and `IpAddress`.
- Implement `ConsensusNode` and `MirrorNode`.
- Implement the zero-address, zero-account, and zero-contract constants.
- Enforce numeric, EVM-address, alias, length, nullability, final-type, override, and one-of constraints.
- Enforce absolute-URL validation for `MirrorNode.restBaseUrl`.
- Implement checksum validation and canonical string representations.
- Implement address, account, contract, EVM-address, IP-address, and transaction-ID factories and parsers.
- Implement transaction-ID generation.

### `base/ledger-config.md`

- Implement immutable `NetworkSetting` values.
- Expose the configured network, consensus nodes, and mirror nodes.
- Implement network-setting registration by identifier.
- Implement network-setting lookup and `not-found-error` behavior.

### `base/keys.md`

- Implement `Key`, `PublicKey`, `PrivateKey`, and `KeyPair`.
- Implement `KeyType` and `KeyAlgorithm`.
- Implement `RawFormat`, `KeyEncoding`, `KeyContainer`, `ByteImportEncoding`, and `KeyFormat`.
- Implement raw-byte export.
- Implement DER and PEM encoding and decoding.
- Implement HEX and Base64 decoding.
- Implement container and key-type compatibility checks.
- Implement key generation, import factories, public-key derivation, signing, and signature verification.
- Enforce `illegal-format` behavior for invalid key data and incompatible formats.

### `base/authority.md`

- Implement the sealed `Authority` abstraction.
- Implement `PublicKeyAuthority`, `ContractAuthority`, and `AuthorityList`.
- Implement factories for a public key, a contract, a delegatable contract, n-of-n authorities, and m-of-n authorities.
- Enforce non-empty child lists and `1 <= threshold <= children.size()`.

### `base/native-token.md`

- Implement `NativeTokenUnit`.
- Implement `NativeToken<Self, Unit>`.
- Implement unit conversion and base-unit conversion.
- Implement `ExchangeRate` and its expiry check.

### `base/token.md`

- Implement `TokenType` and all declared values.
- Implement `TokenSupplyType` and all declared values.

### `base/hedera.md`

- Implement `HederaNetworkSetting`.
- Implement the Mainnet and Testnet identifiers.
- Implement `HbarUnit` with every declared symbol and base-unit factor.
- Implement `Hbar` and `toTinybars()`.

### `base/solo.md`

- Implement `SoloNetworkSetting`.
- Implement `SOLO_IDENTIFIER`.

### `base/grpc.md`

- Implement `MethodDescriptor`.
- Expose immutable service and method names.

### `base/http.md`

- Implement `HttpMethod` with the complete declared method set.
- Implement immutable `HttpConfiguration`, `HttpRequest`, and `HttpResponse` values.
- Enforce absolute, well-formed URLs when constructing an `HttpRequest`.
- Implement the `HttpClient` abstraction and its configuration property.
- Implement asynchronous, thread-safe request execution and propagate `connection-error`, `timeout-error`, and
  `client-closed-error` when no response can be produced.
- Return non-success HTTP status codes as normal `HttpResponse` values without domain interpretation.
- Merge default and request headers, giving request headers precedence, and apply the declared timeout rules.
- Implement graceful and timeout-bounded asynchronous close operations with idempotent lifecycle behavior.
- Implement the default `createHttpClient(configuration)` factory while allowing custom transport implementations.

### `base/proto.md`

- Provide the public `proto` namespace boundary.
- No concrete public members are currently declared by this API.

## Consensus Node Client APIs

### `consensus-node-client/client.md`

- Implement `Account` and `NodeSignature`.
- Implement `TransactionSigner`.
- Implement `Submittable<Result>` with max-attempt, backoff, and attempt-timeout settings.
- Implement asynchronous submission, node selection, transport execution, retry, timeout, and error mapping.
- Implement `HieroClient<Unit>`.
- Implement client creation with the operator's default signer.
- Implement client creation with an externally supplied signer.

### `consensus-node-client/transactions.md`

- Implement `TransactionStatus` and `BasicTransactionStatus`.
- Implement `NodeBody`.
- Implement `Transaction<Receipt>` and `PackedTransaction<Receipt, Transaction>`.
- Implement `Response<Receipt>`, `Receipt`, and `Record<Receipt>`.
- Implement transaction building and packing.
- Implement per-node body generation and offline signing.
- Implement operator signing and externally supplied signature collection.
- Implement serialization and deserialization.
- Implement asynchronous submission.
- Implement receipt and record retrieval.
- Preserve transaction immutability after packing and preserve the signed bytes for each target node.

### `consensus-node-client/transactions-accounts.md`

- Implement `AccountCreateTransaction` and `AccountCreateReceipt`.
- Implement `AccountUpdateTransaction` and `AccountUpdateReceipt`.
- Implement `AccountDeleteTransaction` and `AccountDeleteReceipt`.
- Implement `HbarTransfer`, `TokenTransfer`, and `NftTransfer`.
- Implement `TransferTransaction` and `TransferReceipt`.
- Implement `HbarAllowance`, `TokenAllowance`, and `NftAllowance`.
- Implement `AccountAllowanceApproveTransaction` and its receipt.
- Implement `NftAllowanceDeletion`, `AccountAllowanceDeleteTransaction`, and its receipt.
- Implement all declared alias, memo, authority, auto-renew, receiver-signature, automatic-association, allowance, and
  staking fields and constraints.

### `consensus-node-client/transactions-tokens.md`

- Implement token create, update, delete, associate, dissociate, mint, and burn transactions.
- Implement the typed receipt corresponding to each transaction.
- Implement token type, supply type, treasury, decimals, initial supply, maximum supply, metadata, memo, expiry, and
  auto-renew fields.
- Implement all declared token authorities.
- Implement fungible-token and NFT mint and burn inputs.
- Implement total-supply and minted-serial receipt values.

### `consensus-node-client/transactions-tokens-management.md`

- Implement token wipe and its receipt.
- Implement token freeze and unfreeze and their receipts.
- Implement token KYC grant and revoke and their receipts.
- Implement token pause and unpause and their receipts.
- Implement NFT metadata update and its receipt.
- Enforce every declared token ID, account ID, amount, serial, metadata, and collection constraint.

### `consensus-node-client/transactions-tokens-airdrops.md`

- Implement `PendingAirdrop` and `TokenReference`.
- Implement token airdrop and its receipt.
- Implement token airdrop claim and its receipt.
- Implement token airdrop cancellation and its receipt.
- Implement token rejection and its receipt.
- Enforce fungible-versus-NFT selector rules and collection limits.

### `consensus-node-client/transactions-files.md`

- Implement file create, append, update, and delete transactions.
- Implement the typed receipt corresponding to each transaction.
- Implement file content, chunking, memo, expiry, and authority behavior.

### `consensus-node-client/transactions-topics.md`

- Implement topic create, update, and delete transactions.
- Implement the typed receipt corresponding to each transaction.
- Implement topic memo, admin authority, submit authority, auto-renew account, auto-renew period, and expiry behavior.

### `consensus-node-client/transactions-schedule.md`

- Implement schedule create, sign, and delete transactions.
- Implement the typed receipt corresponding to each transaction.
- Implement captured inner transactions, payer, admin authority, signers, expiry, and wait-for-expiry behavior.
- Implement schedule IDs and scheduled transaction IDs returned by receipts.

### `consensus-node-client/transactions-batch.md`

- Implement `BatchTransaction` and `BatchReceipt`.
- Implement inner-transaction packing, batch-key signing, validation, submission, and response correlation.

### `consensus-node-client/transactions-utility.md`

- Implement `PrngTransaction` and `PrngReceipt`.
- Enforce the exclusive PRNG number-or-bytes receipt result.

### `consensus-node-client/transactions-spi.md`

- Implement `TransactionSupport<Receipt, Transaction>`.
- Implement transaction-type and gRPC-method-descriptor lookup.
- Implement protobuf transaction-body updates.
- Implement transaction, response, receipt, and record reconstruction from protobuf values.
- Implement transaction-support registration, lookup, enumeration, and `not-found-error` behavior.

### `consensus-node-client/queries.md`

- Implement `QueryResponse<T>` and `PaidQueryResponse<T>`.
- Implement `Query<Result>` and `PaidQuery<Result>`.
- Implement asynchronous free-query execution.
- Implement paid-query cost discovery and payment construction.
- Implement paid-query submission, retry, timeout, and declared errors.

### `consensus-node-client/queries-accounts.md`

- Implement `AccountBalance` and `AccountBalanceQuery`.
- Implement `AccountInfo` and `AccountInfoQuery`.
- Implement `AccountRecords` and `AccountRecordsQuery`.
- Implement account-versus-contract selectors and all returned account, token-balance, allowance, staking, and record
  fields.

### `consensus-node-client/queries-files.md`

- Implement `FileContents` and `FileContentsQuery`.
- Implement `FileInfo` and `FileInfoQuery`.
- Implement file ID, content, size, expiry, deletion, memo, and authority response fields.

### `consensus-node-client/queries-tokens.md`

- Implement `TokenInfo` and `TokenInfoQuery`.
- Implement `TokenNftInfo` and `TokenNftInfoQuery`.
- Implement all declared token configuration, authority, supply, status, owner, serial, metadata, and timestamp fields.

### `consensus-node-client/queries-topics.md`

- Implement `TopicInfo` and `TopicInfoQuery`.
- Implement topic IDs, authorities, memo, sequence, running hash, expiry, and auto-renew response fields.

### `consensus-node-client/queries-schedule.md`

- Implement `ScheduleInfo` and `ScheduleInfoQuery`.
- Implement creator, payer, captured transaction, scheduled transaction ID, signers, admin authority, memo, expiry,
  execution, and deletion response fields.

### `consensus-node-client/proto.md`

- Implement `TransactionBody`, `TransactionResponse`, `TransactionReceipt`, and `TransactionRecord` abstractions.

### `consensus-node-client/proto-accounts.md`

- Provide the account-service protobuf namespace boundary.
- No concrete public members are currently declared by this API.

## Consensus Node Administration APIs

### `consensus-node-admin-client/transactions-freeze.md`

- Implement `FreezeType` and every declared freeze mode.
- Implement `FreezeTransaction` and `FreezeReceipt`.
- Implement file, hash, start-time, and type selector behavior.

### `consensus-node-admin-client/transactions-system.md`

- Implement `SystemDeleteTransaction` and `SystemDeleteReceipt`.
- Implement `SystemUndeleteTransaction` and `SystemUndeleteReceipt`.
- Implement file-versus-contract selection and expiry behavior.

### `consensus-node-admin-client/transactions-nodes.md`

- Implement `ServiceEndpoint`.
- Implement node create, update, and delete transactions.
- Implement the typed receipt corresponding to each transaction.
- Implement node account, description, gossip endpoints, service endpoints, gossip certificate, gRPC certificate hash,
  and admin-authority fields.

### `consensus-node-admin-client/queries-network.md`

- Implement `SemanticVersion` and `NetworkVersionInfo`.
- Implement `NetworkVersionInfoQuery`.
- Implement `NodeAddress` and `NodeAddressBook`.
- Implement `NodeAddressBookQuery`.
- Implement the declared free-versus-paid query behavior.

The administration APIs must be delivered in a separately consumable module and must not be exposed automatically by
the ordinary consensus client.

## Mirror Node Client APIs

### `mirror-node-client/mirror-node.md`

- Implement `MirrorNodeClient`.
- Implement synchronous Mirror Node client creation from a `MirrorNode` endpoint.
- Expose account, contract, network, NFT, token, topic, and transaction repositories.
- Expose the `MirrorNodeHttpClient` used by the repositories.

### `mirror-node-client/mirror-node-http.md`

- Implement immutable `MirrorNodeHttpRequest` values with method, path, body, timeout, and headers.
- Enforce a leading-slash, whitespace-free relative path and reject absolute URLs.
- Implement the `MirrorNodeHttpClient` abstraction with immutable Mirror Node and underlying HTTP-client properties.
- Resolve paths against `MirrorNode.restBaseUrl`, normalize the joining slash, and avoid duplicating `/api/v1`.
- Delegate asynchronous execution to the underlying `HttpClient` and propagate its transport errors unchanged.
- Return HTTP responses without interpreting status codes or applying retry, pagination, or domain-error policies.
- Keep ownership and lifecycle of the underlying `HttpClient` with its creator.

### `mirror-node-client/mirror-node-common.md`

- Implement `Transfer`, `TokenTransfer`, `NftTransferParties`, `NftTransfer`, `StakingRewardTransfer`, and `FixedFee`.
- Enforce all declared immutable fields, defaults, and nullability.

### `mirror-node-client/mirror-node-account.md`

- Implement `AccountInfo`, `StakingReward`, `CryptoAllowance`, `TokenAllowance`, `NftAllowance`, `TokenAirdrop`,
  `TokenBalance`, and `AccountBalance`.
- Implement account lookup and account listing.
- Implement reward, crypto-allowance, token-allowance, and NFT-allowance queries.
- Implement outstanding and pending airdrop queries.
- Implement the network account-balances query.

### `mirror-node-client/mirror-node-contract.md`

- Implement `Contract` and `ContractRepository`.
- Implement contract listing and lookup by contract ID.

### `mirror-node-client/mirror-node-network.md`

- Implement `ExchangeRate`, `ExchangeRates`, `NetworkFee`, `NetworkStake`, `NetworkSupplies`, `ServiceEndpoint`,
  `NetworkNode`, and `FeeEstimate`.
- Implement `NetworkRepository`.
- Implement exchange-rate, fee, stake, supply, and node-list queries.
- Implement transaction fee estimation.

### `mirror-node-client/mirror-node-nft.md`

- Implement `NftMetadata`, `Nft`, and `NftRepository`.
- Implement lookup by owner, token type, serial number, and owner-and-type.
- Implement NFT-type listing, owner-type listing, and metadata lookup.

### `mirror-node-client/mirror-node-token.md`

- Implement `Balance`, `RoyaltyFee`, `FractionalFee`, `CustomFee`, `TokenInfo`, and `Token`.
- Implement `TokenRepository`.
- Implement token listing by account, token lookup by ID, token balances, and account-specific token balances.

### `mirror-node-client/mirror-node-topic.md`

- Implement `Topic`, `ChunkInfo`, `TopicMessage`, and `TopicRepository`.
- Implement topic lookup.
- Implement paginated message lookup and sequence-number lookup.

### `mirror-node-client/mirror-node-transaction.md`

- Implement `TransactionType`, `TransactionResult`, and `BalanceModification`.
- Implement `TransactionInfo` and `TransactionRepository`.
- Implement transaction lookup by account, type, result, balance modification, and transaction ID.

Every Mirror Node repository must implement asynchronous HTTP execution, response decoding, pagination, endpoint
handling, and `mirror-node-error` mapping as declared by its methods.

## Enterprise Service APIs

### `enterprise/common.md`

- Implement `Subscription`.

### `enterprise/service.md`

- Implement the thread-safe `Session` abstraction.
- Implement session creation with the operator's default signer.
- Implement session creation with an external signer.
- Implement asynchronous consistency waiting and `service-error` behavior.
- Maintain the consistency high-water mark between consensus writes and Mirror Node reads.

### `enterprise/service-account.md`

- Implement `AccountInformation` and `AccountService`.
- Implement account creation with optional initial balance.
- Implement account-authority updates and account deletion.
- Implement account lookup and account listing.

### `enterprise/service-token.md`

- Implement `FungibleTokenService`.
- Implement token creation, association, dissociation, mint, burn, and transfer.
- Implement token lookup, account-token listing, and token balance queries.

### `enterprise/service-nft.md`

- Implement `NftService`.
- Implement NFT type creation, association, dissociation, mint, burn, and transfer.
- Implement NFT type and NFT instance lookup operations.

### `enterprise/service-topic.md`

- Implement the enterprise `Topic`, `TopicMessage`, and `TopicService`.
- Implement topic creation, lookup, listing, and deletion.
- Implement message submission, paginated message lookup, sequence-number lookup, and message subscription.

### `enterprise/service-file.md`

- Implement `FileService`.
- Implement file creation, reading, content update, expiry update, and deletion.
- Implement deletion-state, size, and expiration-time queries.

### `enterprise/service-contract.md`

- Implement `ParamSupplier`, `Param`, `ContractCallResult`, `Contract`, and `SmartContractService`.
- Implement contract deployment from a file or bytecode.
- Implement contract calls, contract lookup, and contract listing.
- Implement the declared string, bytes, address, boolean, signed-integer, and unsigned-integer parameter factories.

## Dependency and Verification Deliverables

### `dependencies.md`

- Preserve the declared namespace dependency direction in the language module structure.
- Prevent circular dependencies and higher-level APIs leaking into `base`.
- Update the dependency document whenever a specification namespace dependency changes.

### Tests for every API

- Implement public API shape and consumer-compilation tests.
- Implement validation, immutability, defensive-copy, equality, hashing, and representation tests for value types.
- Implement successful-operation and declared-error tests for every method.
- Implement protocol mapping tests for consensus and Mirror Node clients.
- Implement integration scenarios for consensus submission, Mirror Node reads, administration, and enterprise workflows.
- Maintain traceability from every public declaration and normative scenario to its implementation and tests.

## Questions and Open Options Mentioned in the Specifications

Only questions and open options stated in the specifications' `Questions & Comments` sections are included below.
No additional questions are inferred from the API declarations or implementation work.

### Base APIs

#### [`base/authority.md`](base/authority.md)

- Should convenience overloads accept `PublicKey` values directly?
- Should the m-of-n factory be renamed from `of(...)` to `multiSig(...)` or `threshold(...)`?

#### [`base/ledger.md`](base/ledger.md)

- Should `Ledger` be renamed to `Network`, or should `NetworkSetting` be renamed to `LedgerSetting`?
- What are the rules for assigning and creating `Ledger.id` bytes?

#### [`base/native-token.md`](base/native-token.md)

- Should `ExchangeRate.exchangeRateInUsdCents` use `decimal` instead of `double`?

#### [`base/http.md`](base/http.md)

- Should the API guidelines define a sub-second `duration` type, or should HTTP timeout fields use another type?

### Consensus Node Client APIs

#### [`consensus-node-client/queries.md`](consensus-node-client/queries.md)

- Should a future `submitValue(client)` shortcut return the unwrapped payload?
- Should paid queries support an explicit payment override if a concrete use case appears?
- Should paid queries support a payer other than the client's operator, and which payer model should be used?
- Should `QueryResponse` carry the ledger origin on the response envelope?

#### [`consensus-node-client/queries-accounts.md`](consensus-node-client/queries-accounts.md)

- Should `AccountRecordsQuery` be removed before V3 GA if no consumer needs V2 parity?

#### [`consensus-node-client/queries-tokens.md`](consensus-node-client/queries-tokens.md)

- If callers need the redundant "not applicable" state, should freeze, KYC, and pause statuses become explicit enums?

#### [`consensus-node-client/queries-topics.md`](consensus-node-client/queries-topics.md)

- If multi-version running-hash verification becomes necessary, should `TopicInfo` expose `runningHashVersion`?

#### [`consensus-node-client/transactions.md`](consensus-node-client/transactions.md)

- Should the enterprise layer eventually provide a single-signer helper that rebuilds and signs an expired transaction?

#### [`consensus-node-client/transactions-batch.md`](consensus-node-client/transactions-batch.md)

- Can a batch key be a composite `Authority`, or must it be a single primitive key?
- Does HIP-551 guarantee that inner transactions execute in list order?

#### [`consensus-node-client/transactions-files.md`](consensus-node-client/transactions-files.md)

- After HIP-1300 is specified, should chunking expose explicit control and per-chunk results, or remain hidden?
- Should language bindings expose separate `replaceContents(...)` and `appendContents(...)` operations?
- Should V3 anticipate future file auto-renew fields or stay aligned with the current protocol?

#### [`consensus-node-client/transactions-topics.md`](consensus-node-client/transactions-topics.md)

- Should V3 provide convenience methods for clearing the auto-renew account, admin authority, and submit authority?

### Consensus Node Administration APIs

#### [`consensus-node-admin-client/queries-network.md`](consensus-node-admin-client/queries-network.md)

- Should tombstoned address-book entries be returned, and how should their deleted state be represented?

#### [`consensus-node-admin-client/transactions-freeze.md`](consensus-node-admin-client/transactions-freeze.md)

- Should the enum-driven `FreezeTransaction` remain, or should each freeze mode have a concrete subtype?
- Should the administration client expose a typed event indicating that an upgrade has been applied?

#### [`consensus-node-admin-client/transactions-nodes.md`](consensus-node-admin-client/transactions-nodes.md)

- Should a language-binding or enterprise convenience safely read, modify, and replace endpoint lists?

#### [`consensus-node-admin-client/transactions-system.md`](consensus-node-admin-client/transactions-system.md)

- Should `SystemDeleteTransaction` be split into file-delete and contract-delete transaction types?
- Should an expired undelete window map to a dedicated `system-undelete-window-expired-error`?

### Mirror Node Client APIs

#### [`mirror-node-client/mirror-node-http.md`](mirror-node-client/mirror-node-http.md)

- Should `MirrorNodeHttpRequest.timeout` use a new sub-second `duration` type or another representation?
- Should applications be able to supply the `HttpClient` through an overload or a factory in this namespace?

### Enterprise Service APIs

#### [`enterprise/service.md`](enterprise/service.md)

- Should `Session` export and import an opaque consistency token for cross-process propagation?
- Should `awaitConsistency()` accept a timeout override per call or only use session configuration?
- Should consistency timeout use a distinct error instead of `service-error`?

#### [`enterprise/service-account.md`](enterprise/service-account.md),
[`enterprise/service-token.md`](enterprise/service-token.md), [`enterprise/service-nft.md`](enterprise/service-nft.md),
and [`enterprise/service-topic.md`](enterprise/service-topic.md)

- Should the enterprise services provide convenience overloads accepting a single `PublicKey` directly?

#### [`enterprise/service-nft.md`](enterprise/service-nft.md)

- Should NFT creation accept one immutable definition object, supplemented by a small set of convenience methods?

#### [`enterprise/service-token.md`](enterprise/service-token.md)

- Should annotations support positive-value and range constraints on both parameters and return values?
