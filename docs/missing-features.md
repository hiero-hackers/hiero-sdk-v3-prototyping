# Missing Features in the V3 Specification

This document lists the features and APIs that are **not yet covered** by the V3 specification under
[`spec/`](../spec/): features of the Hedera/Hiero SDK v2 without a V3 counterpart, and open points of the V3 specs
themselves. It is a working backlog, not a final scope statement — items may move, get reprioritized, or be
intentionally excluded for V3. What is specified is not listed here; design questions of a single spec stay in its
`## Questions & Comments`.

Compared with every `*Transaction`, `*Query` and `*Flow` class of the Hiero Java SDK (v2, commit `f917e67b3`,
2026-08-17); last checked against the specs on 2026-11-11.

The section numbers are stable because specs and ADRs refer to them; a section whose items have all landed is
removed without renumbering the others.

---

## 1. `consensus-node-client` — transactions and queries

### 1.2 Token service (HTS)

- `TokenFeeScheduleUpdate` — its only payload is the custom-fee schedule; blocked on the write-side `CustomFee`
  hierarchy (§3.3).
- `TokenCreate.customFees` — the write-side custom fees of a new token; depends on §3.3.
- HIP-540 key clearing — deferred to a future write-side `KeyUpdate` type
  ([ADR-0004](adr/0004-authority-authorization-sum-type.md)).
- The pending airdrops of a `TokenAirdrop` record (`new_pending_airdrops`) — part of the record design question
  (§3.5).
- `TokenRejectFlow` — v2 convenience that rejects tokens and dissociates them in one call (two transactions); a
  candidate for `enterprise.service.*` rather than `consensusnode.*`.

### 1.3 File service

- Whether the spec states a maximum size of the contents of one `FileAppendTransaction` (chunking itself is decided:
  one transaction, split by the enterprise `FileService`).

### 1.4 Smart contract service

- `ContractCreateFlow` (upload the bytecode to a file, create, delete the file) and `EthereumFlow` (move call data that
  is too large into a file) — candidates for `enterprise.service.contract` rather than `consensusnode.*`.
- The hooks of HIP-1195 on `ContractCreate` / `ContractUpdate` (§1.7).
- `TransactionRecord.ethereum_hash` of an `EthereumTransaction` — a type-specific record attribute (§3.5).

### 1.5 Topic / HCS

- `TopicMessageSubmit` (chunked; presumably as for `FileAppend`: one chunk per transaction, split by the enterprise
  layer) with its receipt (`topicSequenceNumber`, `topicRunningHash`, `topicRunningHashVersion`). Without it,
  `TopicService.submitMessage` of the enterprise layer cannot be implemented.
- HIP-991 custom fees on topics (`customFees`, `feeScheduleAuthority`, `feeExemptAuthorities`) — depends on §3.3.

### 1.6 Schedule service

- `customFeeLimits` (HIP-991) on `ScheduleCreate` — blocked on the write-side custom-fee model (§3.3).

### 1.7 Hooks

- Hiero Hooks (HIP-1195): `HookStoreTransaction`, `EvmHook`, `EvmHookCall`, `HookCreationDetails`, … Hooks also reach
  into existing transactions: v2 has hook-creation details on `AccountCreate` / `AccountUpdate` and
  `ContractCreate` / `ContractUpdate`, and `HookCall`s on the transfers of `TransferTransaction`. (The mirror node side
  — hooks and hook storage of an account — is specified.)

### 1.8 Queries

- `ContractCallQuery` (local EVM call), `ContractInfoQuery`, `ContractBytecodeQuery`.
- `AccountInfoFlow` — v2 convenience that verifies a signature (or a signed transaction) with the key from
  `AccountInfoQuery`; like `TokenRejectFlow`, a candidate for `enterprise.service.*`.
- **Open design decision — `GetBySolidityID`** (Solidity/EVM address → entity id). Largely covered by the typed EVM
  addresses of [`base/ledger.md`](../spec/base/ledger.md) and mirror-node resolution. Is there a flow that needs a
  consensus-node-side lookup? If not, it stays omitted; if yes, it is a read query in `consensusnode.queries`.
- **Open design decision — `GetAccountDetails`** (privileged; token relationships, allowances, …). Already served by
  the mirror node. Is there a consensus-node-side need, and would it belong to the admin client because it is
  privileged?

### 1.9 Features on `Transaction<...>` / `PackedTransaction<...>`

- `getTransactionHash()` / `getTransactionHashPerNode()` — deliberately deferred. The hash (SHA-384 of the
  `SignedTransaction` bytes, one per target node) can be reconstructed from `PackedTransaction.toBytes()` or from
  `signableBodies()` plus the collected signatures. Revisit once a consumer in `mirrornode.*` or `enterprise.service.*`
  needs it (mirror-node lookup by hash); adding the accessors later is purely additive.
- Jumbo transactions (HIP-1300): `bodySize`, `bodySizeAllChunks`.
- **Child and duplicate records.** `TransactionReceiptQuery` returns the receipts of children and duplicates, but the
  record path (`Response.queryRecord(client)`) returns only the single record (`include_child_records`,
  `includeDuplicates`). The main use case is the child transactions of a contract call; child records are of other
  transaction types than their parent, so they would be `Record<ANY>`. It does not help atomic batches: their inner
  transactions keep their own `TransactionId`s.

---

## 2. `consensus-node-admin-client` — admin-only transactions

- **Registered nodes (HIP-1137, block-node discoverability)** — `RegisteredNodeCreate` / `RegisteredNodeUpdate` /
  `RegisteredNodeDelete`. The protobufs are vendored (`tooling/protobuf/consensus-node/services/registered_node_*.proto`);
  the mirror node side is read in [`mirror-node-network.md`](../spec/mirror-node-client/mirror-node-network.md).
- **System / network admin** ([`transactions-freeze.md`](../spec/consensus-node-admin-client/transactions-freeze.md),
  [`transactions-system.md`](../spec/consensus-node-admin-client/transactions-system.md)): a typed `NetworkAdminKey`;
  splitting the `freezeType` / `@@oneOf(fileId, contractId)` payloads into concrete subtypes.
- **Dynamic Address Book** ([`transactions-nodes.md`](../spec/consensus-node-admin-client/transactions-nodes.md)):
  typed `NodeId` and `X509Certificate` / `CertificateFingerprint`.
- **Admin-side queries** ([`queries-network.md`](../spec/consensus-node-admin-client/queries-network.md)): use a
  base `SemanticVersion` (§3.1); surface tombstoned node-address entries explicitly.

---

## 3. `base` — missing types and concepts

### 3.1 Identifier and value types

| Type | Composition / purpose | Lands with |
| --- | --- | --- |
| `NftId` | `(tokenId: Address, serial: int64)`; today two separate fields or parameters | the NFT APIs that take a single NFT |
| `PendingAirdropId` | `(senderId, receiverId, tokenId[, serial])`; today modelled as the `PendingAirdrop` shape | HIP-904 claim / cancel |
| `HookId` / `HookEntityId` | composite (entity-bound) per HIP-1195 | Hiero Hooks (§1.7) |
| `LedgerId` | identifies the network a response belongs to; today only identifier constants | `QueryResponse.ledgerId` |
| `SemanticVersion` | `(major, minor, patch, pre, build)`; declared twice locally today (`consensusnode.admin.network`, `blocknode.status`) | a type in `base/common` |
| `TransactionHash` | SHA-384 of the `SignedTransaction` bytes, the mirror-node lookup key | only if the deferred hash accessors of §1.9 land |

### 3.2 Keys

- `Mnemonic` (BIP-39 12 / 24 words and the legacy 22 words; `toStandardEd25519PrivateKey`,
  `toStandardECDSAsecp256k1PrivateKey`).
- `PublicKey.toEvmAddress()` / `toAccountId()` (open question in [`keys.md`](../spec/base/keys.md)).

### 3.3 Custom fees (HTS + HIP-991)

The mirror node has read models (`FixedFee`, `FractionalFee`, `RoyaltyFee`, the custom fee limits and assessed custom
fees of a transaction). Missing on the consensus-node side:

- the write-side `CustomFee` hierarchy (`CustomFixedFee`, `CustomFractionalFee` with `netOfTransfers`,
  `CustomRoyaltyFee` with `fallbackFee`) for `TokenCreate`, `TokenFeeScheduleUpdate` and `TopicCreate`;
- `CustomFeeLimit` (HIP-991, for scheduled transactions and topic messages);
- `AssessedCustomFee` in the record (§3.5).

### 3.4 Fee schedule

- `FeeSchedule`, `TransactionFeeSchedule`, `FeeComponents`, `FeeData` — the fee schedule file of the network.

### 3.5 Receipts and records

Receipt fields still missing:

- `evmAddress` on `AccountCreateReceipt` (set on the alias / auto-create path).
- The receipt of `TopicMessageSubmit` (§1.5).

Record fields still missing:

| Field | When set | Proposed home |
| --- | --- | --- |
| `assessedCustomFees` | transactions touching tokens with custom fees | base `Record` as `@@default([]) list<AssessedCustomFee>`; depends on §3.3 |
| `automaticTokenAssociations` | executions that auto-associate an account | base `Record` as `@@default([]) list<TokenAssociation>` |
| `paidStakingRewards` | executions that pay out a staking reward | base `Record` as `@@default([]) list<AccountAmount>` |
| `ethereumHash` | `EthereumTransaction` only | type-specific |
| `newPendingAirdrops` | `TokenAirdrop` only (HIP-904) | type-specific |

**Open architectural question:** should `Record<$$Receipt>` carry a fixed set of cross-cutting fields only, or also be
parameterised over a type-specific part (e.g. `Record<$$Receipt, $$Extras>`) for `ethereumHash` and
`newPendingAirdrops` — or should those two be `@@nullable` fields of the base?

### 3.6 Enums

- `TokenKeyValidation` (HIP-540) and `RequestType` (the HAPI functionalities, e.g. of fee schedules).

---

## 4. `mirror-node-client` — REST and gRPC API

- **The gRPC API of the mirror node.** The specs cover only REST. v2 also uses the two gRPC services whose protobufs
  are vendored in `tooling/protobuf/mirror-node`: `TopicMessageQuery` (subscription to the messages of a topic,
  `consensus_service.proto`; a `@@streaming` method) and `AddressBookQuery` (`network_service.proto`).
- **Update of the pinned OpenAPI definition.** `tooling/openapi` is pinned to v0.164.0; v0.165.0 added the nine CLPR
  transaction types (`CLPRREGISTERCHANNEL`, …, `CLPRREDACTMESSAGE`) to `TransactionTypes`, which `BasicTransactionType`
  does not list yet.
- **CLPR channels and connectors (HIP-1535).** `MirrorNodeClprChannelQuery` and `MirrorNodeClprConnectorQuery` of the
  proposal; their REST routes are not in the OpenAPI definition yet (see
  [`consensus-node-clpr/queries.md`](../spec/consensus-node-clpr/queries.md)).
- **An automatic coverage check** that compares the pinned OpenAPI definition with the specs and reports every element
  that neither has a counterpart nor is listed as a deviation (see `tooling/openapi/README.md`).
- **Typed conditions.** A query parameter with the operators `eq`/`ne`/`gt`/`gte`/`lt`/`lte` is a `list<string>` in
  REST syntax; a typed condition model (value + operator) is the next step.
- **Long parameter lists.** The full overload of an endpoint has up to nine parameters; an options object / builder
  per language is open.
- **`@@pattern` on list elements.** The meta-language has no annotation for the elements of a `list<string>`, so the
  patterns of the condition strings are not checked by the SDK.
- **Response-level values of list endpoints** (e.g. the `timestamp` of `/balances`) have no place in `Page<$$T>`.
- **Cursor-based pagination** via `links.next` — no token / cursor concept is documented.

---

## 5. `enterprise` service layer

| Service / method | State |
| --- | --- |
| **ScheduleService** (create / sign / delete + long-term) | missing |
| **AllowanceService** (HBAR / token / NFT approve + delete) | missing |
| **StakingService** (stake to node / account, decline reward, claim rewards) | missing |
| **BatchService** (HIP-551 atomic batch across service calls) | missing |
| **HooksService** (HIP-1195) | missing |
| **TopicService**: `updateTopic()`, `subscribeWithFilter()`, topic custom fees (HIP-991) | missing |
| **TokenService**: custom fees, pause / unpause, freeze / unfreeze, KYC, wipe, fee-schedule update, NFT updates, airdrop / claim / cancel / reject | missing |
| **AccountService**: `transferHbar()`, auto account creation via a transfer | missing |
| **SmartContractService**: update / delete, Ethereum transactions | missing |

Open in the `## Questions & Comments` of [`service.md`](../spec/enterprise/service.md): cross-process propagation of
the consistency token (`exportConsistencyToken` / `importConsistencyToken`), a dedicated `consistency-timeout-error`,
and what `createService(session)` needs from a `Session`.

---

## 6. Client / networking / operator setup (`HieroClient`)

### 6.1 Operator identity is coupled to an in-memory `PrivateKey`

`HieroClient` requires an `operatorAccount: Account`
([`client.md`](../spec/consensus-node-client/client.md)), and `Account` carries a `privateKey: PrivateKey`, so the
operator's private key must sit in process memory. That excludes HSM / KMS-backed keys, hardware wallets,
wallet bridges (HashConnect, WalletConnect) and FIPS custody. v2 has `Client.setOperatorWith(AccountId, PublicKey,
UnaryFunction<byte[], byte[]>)`.

V3 has the building block — `TransactionSigner` on `HieroClient` — but still requires `Account.privateKey`. Needed:

| What | Current | Needed |
| --- | --- | --- |
| Operator identity | `Account { accountId, privateKey }` | `accountId` + `publicKey` (no `PrivateKey`) |
| Operator signing | `transactionSigner: TransactionSigner` | unchanged; the only signing path |
| In-memory shortcut | implicit via `Account.privateKey` | a factory that wraps a `PrivateKey` in a `TransactionSigner` |

Ripple effects: `Account` loses `privateKey` or is split into identity and signing account; `Transaction.sign(payer,
nodes)` and the sponsored-query payer ([ADR-0002](adr/0002-defer-paid-query-payer-customization.md)) must follow; the
one-line CLI / script case must stay one line. The same identity / signer decoupling also blocks the CLPR ownership
proofs for HSM keys.

### 6.2 Other client features

| Feature in v2 | State in V3 |
| --- | --- |
| `forMainnet` / `forTestnet` / `forName` / `forNetwork` / `forMirrorNetwork` (HIP-1064) / `fromConfig` | abstracted via the `NetworkSetting` registry; a client cannot reach the consensus nodes of its `NetworkSetting` yet (see `CLAUDE.md`) |
| `setMaxQueryPayment`, `setDefaultMaxTransactionFee` as client defaults | missing (only per query / transaction) |
| `setNetworkUpdatePeriod` (automatic address-book refresh), `setNetworkFromAddressBook(NodeAddressBook)` | missing |
| `setLogger(Logger)` (pluggable logger) | missing |
| Persistent shard / realm (HIP-1299) | missing |

---

## 7. Other layers

### 7.1 `block-node-client`

- `ProofService` (`getBlockContentsProof`, `getStateProof`) — the state proof is still a placeholder in the protocol.
- `StateService` (`stateSnapshot`).
- Typed access to the transactions of a block (a signed transaction with its result, outputs, traces and state
  changes); it needs the protobuf messages of the consensus node and belongs to a module on top of
  `consensus-node-client`.
- Reading blocks from files (`Block.fromBytes`), retry / failover, and closing the client — open questions in
  [`block-node.md`](../spec/block-node-client/block-node.md).

### 7.2 Sub-layers without content

- `spec/consensus-node-client/proto-accounts.md` and `spec/base/proto.md` are empty placeholders; `grpc.md` holds only
  `MethodDescriptor`.

---

## 8. Suggested prioritization

1. **SDK parity:** `TopicMessageSubmit`, the contract queries (§1.8), the write-side custom fees with
   `TokenFeeScheduleUpdate` (§3.3), the missing receipt and record fields (§3.5).
2. **Architecture:** operator identity without an in-memory key (§6.1), the record design (§3.5).
3. **Modern HIPs:** hooks (HIP-1195), registered nodes (HIP-1137), jumbo transactions (HIP-1300), custom fees on
   topics (HIP-991).
4. **Mirror node:** the gRPC API (topic subscription, address book), the update of the pinned OpenAPI definition,
   typed conditions.
5. **Client / infrastructure:** logger, address-book refresh, shard / realm persistence, client-wide payment and fee
   defaults, `Mnemonic`.
6. **Enterprise:** the missing services (§5).

---

## Sources

- [Hiero Java SDK](https://github.com/hiero-ledger/hiero-sdk-java)
- [Hiero Java SDK CHANGELOG](https://github.com/hiero-ledger/hiero-sdk-java/blob/main/CHANGELOG.md)
- [Hedera SDK transactions overview](https://docs.hedera.com/hedera/sdks-and-apis/sdks/transactions)
- [Hedera Mirror Node REST API](https://docs.hedera.com/hedera/sdks-and-apis/rest-api)
- [HIP-991 Permissionless Revenue-Generating Topics](https://hips.hedera.com/hip/hip-991)
- [HIP-1137 Block Node discoverability via on-chain registry](https://hips.hedera.com/hip/hip-1137)
- [HIP-1195 Hiero Hooks](https://hips.hedera.com/hip/hip-1195)
- [Hedera client setup guide](https://docs.hedera.com/hedera/sdks-and-apis/sdks/client)
