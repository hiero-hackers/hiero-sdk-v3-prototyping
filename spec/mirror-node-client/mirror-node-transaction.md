# Mirror Node Transaction Query API

## Description

Query transactions from the Mirror Node. Use the `TransactionRepository` (available as
`MirrorNodeClient.transaction`) to look up a transaction by its `TransactionId`, to page through all transactions or
through the transactions of an account, optionally filtered by transaction type, by result or by whether the
account's balance was credited or debited. A `TransactionInfo` contains the fees, timestamps, result and all HBAR,
token, NFT and staking-reward transfers of the transaction, and the custom fees that were charged for it.

A transaction id can belong to several transactions: a transaction submitted by a user, the child transactions the
network created for it (with a nonce greater than 0) and a scheduled transaction. `findAllById` returns all of them,
`findById` the one with the nonce of the id that is not scheduled.

The type of a transaction is a `TransactionType` that carries the value the Mirror Node REST API uses for it
(`protocolName`). The types known to the SDK are available as constants in `BasicTransactionType`. A type the SDK does
not know yet (for example from a service added to the network later) is still returned as a `TransactionType` with the
received `protocolName`, so no information is lost and the value can be used as a filter again.

## Design Notes

Transaction types are modelled like `consensusnode.status.TransactionStatus`: `TransactionType` is an
abstraction that carries the Mirror Node REST wire value (`protocolName`), and `BasicTransactionType` enumerates the
types known to the SDK - all values of the `TransactionTypes` enum of the REST API. There is no `UNKNOWN` value.

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **The list and the detail form of a transaction are one type.** `TransactionInfo` is the `Transaction` of
  `GET /transactions` and the `TransactionDetail` of `GET /transactions/{transactionId}`; `assessedCustomFees`, which
  only the detail has, is empty for a transaction of the list.
- **Required-ness.** The `Transaction` schema declares no required fields. The fields the mirror node sets for every
  transaction are not nullable here; `batchKey`, `bytes`, `entityId`, `highVolume`, `highVolumePricingMultiplier`,
  `node` and `parentConsensusTimestamp` are. The same holds for `AssessedCustomFee` and `CustomFeeLimit`, whose
  `amount` is required here.
- **Types of string fields.** `transaction_id` is a `TransactionId` (its nonce is the one of the transaction),
  `max_fee` an `int64` (tinybars), `valid_duration_seconds` the `seconds` `validDuration`, `name` a
  `TransactionType`; `entity_id` is an `Address`, `node` an `AccountId`.
- **`memo_base64` is `memo`.** It holds the decoded bytes of the memo; a transaction without memo has an empty memo.
- **The transfers are the common transfer types.** `transfers`, `token_transfers`, `nft_transfers` and
  `staking_reward_transfers` are `Transfer`, `TokenTransfer`, `NftTransfer` and `StakingRewardTransfer` of
  `mirrornode.common` (see its Design Notes).
- **`GET /transactions/{transactionId}` is `findAllById`.** It returns all transactions of the id (a list without
  pagination); `findById` is the convenience form that returns one of them.
- **The filters `result` and `type` are lowercase on the wire.** `TransactionResult` and `BalanceModification` are sent
  as `success` / `fail` and `credit` / `debit`; a `TransactionType` is sent as its `protocolName`.

## API Schema

```
namespace mirrornode.transaction
requires {Address, AccountId, MirrorNode, TransactionId} from ledger
requires {Authority} from authority
requires {NftTransfer, Order, StakingRewardTransfer, TokenTransfer, Transfer} from mirrornode.common
requires {Page} from common

// The type of a transaction as reported by the Mirror Node. Open for types unknown to the SDK, so that new
// services stay usable without an SDK update.
abstraction TransactionType {
    @@immutable protocolName: string   // the REST API wire value, e.g. "CRYPTOTRANSFER"
}

// The transaction types known to the SDK. protocolName values are the `TransactionTypes` enum of the Mirror Node
// REST OpenAPI spec.
enum BasicTransactionType(protocolName: string) extends TransactionType {
    ATOMIC_BATCH("ATOMICBATCH")
    TOPIC_CREATE("CONSENSUSCREATETOPIC")
    TOPIC_DELETE("CONSENSUSDELETETOPIC")
    TOPIC_MESSAGE_SUBMIT("CONSENSUSSUBMITMESSAGE")
    TOPIC_UPDATE("CONSENSUSUPDATETOPIC")
    CONTRACT_CALL("CONTRACTCALL")
    CONTRACT_CREATE("CONTRACTCREATEINSTANCE")
    CONTRACT_DELETE("CONTRACTDELETEINSTANCE")
    CONTRACT_UPDATE("CONTRACTUPDATEINSTANCE")
    CRS_PUBLICATION("CRSPUBLICATION")
    LIVE_HASH_ADD("CRYPTOADDLIVEHASH")
    ACCOUNT_ALLOWANCE_APPROVE("CRYPTOAPPROVEALLOWANCE")
    ACCOUNT_CREATE("CRYPTOCREATEACCOUNT")
    ACCOUNT_DELETE("CRYPTODELETE")
    ACCOUNT_ALLOWANCE_DELETE("CRYPTODELETEALLOWANCE")
    LIVE_HASH_DELETE("CRYPTODELETELIVEHASH")
    CRYPTO_TRANSFER("CRYPTOTRANSFER")
    ACCOUNT_UPDATE("CRYPTOUPDATEACCOUNT")
    ETHEREUM("ETHEREUMTRANSACTION")
    FILE_APPEND("FILEAPPEND")
    FILE_CREATE("FILECREATE")
    FILE_DELETE("FILEDELETE")
    FILE_UPDATE("FILEUPDATE")
    FREEZE("FREEZE")
    HINTS_KEY_PUBLICATION("HINTSKEYPUBLICATION")
    HINTS_PARTIAL_SIGNATURE("HINTSPARTIALSIGNATURE")
    HINTS_PREPROCESSING_VOTE("HINTSPREPROCESSINGVOTE")
    HISTORY_PROOF_KEY_PUBLICATION("HISTORYPROOFKEYPUBLICATION")
    HISTORY_PROOF_SIGNATURE("HISTORYPROOFSIGNATURE")
    HISTORY_PROOF_VOTE("HISTORYPROOFVOTE")
    HOOK_STORE("HOOKSTORE")
    LEDGER_ID_PUBLICATION("LEDGERIDPUBLICATION")
    MIGRATION_ROOT_HASH_VOTE("MIGRATIONROOTHASHVOTE")
    NODE_CREATE("NODECREATE")
    NODE_DELETE("NODEDELETE")
    NODE_STAKE_UPDATE("NODESTAKEUPDATE")
    NODE_UPDATE("NODEUPDATE")
    REGISTERED_NODE_CREATE("REGISTEREDNODECREATE")
    REGISTERED_NODE_DELETE("REGISTEREDNODEDELETE")
    REGISTERED_NODE_UPDATE("REGISTEREDNODEUPDATE")
    SCHEDULE_CREATE("SCHEDULECREATE")
    SCHEDULE_DELETE("SCHEDULEDELETE")
    SCHEDULE_SIGN("SCHEDULESIGN")
    STATE_SIGNATURE("STATESIGNATURETRANSACTION")
    SYSTEM_DELETE("SYSTEMDELETE")
    SYSTEM_UNDELETE("SYSTEMUNDELETE")
    TOKEN_AIRDROP("TOKENAIRDROP")
    TOKEN_ASSOCIATE("TOKENASSOCIATE")
    TOKEN_BURN("TOKENBURN")
    TOKEN_CANCEL_AIRDROP("TOKENCANCELAIRDROP")
    TOKEN_CLAIM_AIRDROP("TOKENCLAIMAIRDROP")
    TOKEN_CREATE("TOKENCREATION")
    TOKEN_DELETE("TOKENDELETION")
    TOKEN_DISSOCIATE("TOKENDISSOCIATE")
    TOKEN_FEE_SCHEDULE_UPDATE("TOKENFEESCHEDULEUPDATE")
    TOKEN_FREEZE("TOKENFREEZE")
    TOKEN_GRANT_KYC("TOKENGRANTKYC")
    TOKEN_MINT("TOKENMINT")
    TOKEN_PAUSE("TOKENPAUSE")
    TOKEN_REJECT("TOKENREJECT")
    TOKEN_REVOKE_KYC("TOKENREVOKEKYC")
    TOKEN_UNFREEZE("TOKENUNFREEZE")
    TOKEN_UNPAUSE("TOKENUNPAUSE")
    TOKEN_UPDATE("TOKENUPDATE")
    TOKEN_UPDATE_NFTS("TOKENUPDATENFTS")
    TOKEN_WIPE("TOKENWIPE")
    UNCHECKED_SUBMIT("UNCHECKEDSUBMIT")
    PRNG("UTILPRNG")
}

// Whether a transaction succeeded, as a filter of a transaction query.
enum TransactionResult {
    SUCCESS
    FAIL
}

// Whether the balance of an account was credited or debited by a transaction, as a filter of a transaction query.
enum BalanceModification {
    CREDIT
    DEBIT
}

// A custom fee that was charged for a transaction.
@@finalType
AssessedCustomFee {
    @@immutable amount: int64                                   // in tinybars or in the smallest unit of the token
    @@immutable @@nullable collectorAccountId: AccountId
    @@immutable @@default([]) effectivePayerAccountIds: list<AccountId>   // the accounts that paid the fee
    @@immutable @@nullable tokenId: Address                     // the token the fee was charged in; absent for HBAR
}

// The maximum custom fee the payer of a transaction was willing to pay in one unit (HIP-991).
@@finalType
CustomFeeLimit {
    @@immutable @@nullable accountId: AccountId
    @@immutable amount: int64
    @@immutable @@nullable denominatingTokenId: Address         // the token of the limit; absent for HBAR
}

@@finalType
TransactionInfo {
    @@immutable transactionId: TransactionId
    @@immutable transactionHash: bytes
    @@immutable @@nullable batchKey: Authority                  // the key of the atomic batch of the transaction
    @@immutable @@nullable bytes: bytes
    @@immutable chargedTxFee: int64
    @@immutable consensusTimestamp: zonedDateTime
    @@immutable @@nullable entityId: Address                    // the entity the transaction created or changed
    @@immutable @@nullable highVolume: bool                     // whether high-volume throttles and pricing were used
    @@immutable @@nullable highVolumePricingMultiplier: int64   // the fee multiplier of high-volume pricing, times 1000
    @@immutable @@default([]) maxCustomFees: list<CustomFeeLimit>
    @@immutable maxFee: int64
    @@immutable memo: bytes
    @@immutable name: TransactionType
    @@immutable nonce: int32
    @@immutable @@nullable node: AccountId                      // the node the transaction was submitted to
    @@immutable @@nullable parentConsensusTimestamp: zonedDateTime
    @@immutable result: string                                  // the response code, e.g. "SUCCESS"
    @@immutable scheduled: bool
    @@immutable validDuration: seconds
    @@immutable validStartTimestamp: zonedDateTime
    @@immutable @@default([]) transfers: list<Transfer>
    @@immutable @@default([]) tokenTransfers: list<TokenTransfer>
    @@immutable @@default([]) nftTransfers: list<NftTransfer>
    @@immutable @@default([]) stakingRewardTransfers: list<StakingRewardTransfer>
    @@immutable @@default([]) assessedCustomFees: list<AssessedCustomFee>   // empty for a transaction of a list
}

abstraction TransactionRepository {
    // All transactions. Maps to GET /api/v1/transactions.
    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findAll()

    // The transactions, filtered by account, consensus time, type, result and balance modification.
    // Maps to GET /api/v1/transactions.
    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findAll(accountId: list<string>, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, timestamp: list<string>, @@nullable transactionType: TransactionType,
            @@nullable result: TransactionResult, @@nullable type: BalanceModification)

    // The transactions of an account. Maps to GET /api/v1/transactions.
    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findByAccount(accountId: AccountId)

    // The transactions of an account of the given type. Maps to GET /api/v1/transactions.
    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findByAccountAndType(accountId: AccountId, type: TransactionType)

    // The transactions of an account with the given result. Maps to GET /api/v1/transactions.
    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findByAccountAndResult(accountId: AccountId, result: TransactionResult)

    // The transactions that credited or debited the balance of an account. Maps to GET /api/v1/transactions.
    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findByAccountAndModification(accountId: AccountId, modification: BalanceModification)

    // The transaction with the given id and its nonce that is not scheduled.
    // Maps to GET /api/v1/transactions/{transactionId}.
    @@async @@throws(mirror-node-error)
    @@nullable TransactionInfo findById(transactionId: TransactionId)

    // All transactions with the payer and the valid start of the given id: the transaction, its child transactions
    // and the scheduled transaction. Empty if there is none. Maps to GET /api/v1/transactions/{transactionId}.
    @@async @@throws(mirror-node-error)
    list<TransactionInfo> findAllById(transactionId: TransactionId)

    // The transactions with the payer and the valid start of the given id, filtered by nonce and by whether they are
    // scheduled. Empty if there is none. Maps to GET /api/v1/transactions/{transactionId}.
    @@async @@throws(mirror-node-error)
    list<TransactionInfo> findAllById(transactionId: TransactionId, @@nullable @@min(0) nonce: int32,
            @@nullable scheduled: bool)
}

@@static TransactionRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `TransactionRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: this spec (BasicTransactionType, whose protocol names are those of the `TransactionTypes` enum of the Mirror
// Node REST OpenAPI spec).
@@testId("the-basic-transaction-types-carry-the-names-of-the-mirror-node-rest-api")
test "the basic transaction types carry the names of the Mirror Node REST API" {
    // when / then
    assertEquals(expected: "CRYPTOCREATEACCOUNT", actual: BasicTransactionType.ACCOUNT_CREATE.protocolName)
    assertEquals(expected: "CRYPTODELETE", actual: BasicTransactionType.ACCOUNT_DELETE.protocolName)
    assertEquals(expected: "CRYPTOUPDATEACCOUNT", actual: BasicTransactionType.ACCOUNT_UPDATE.protocolName)
    assertEquals(expected: "CRYPTOTRANSFER", actual: BasicTransactionType.CRYPTO_TRANSFER.protocolName)
    assertEquals(expected: "CONSENSUSCREATETOPIC", actual: BasicTransactionType.TOPIC_CREATE.protocolName)
    assertEquals(expected: "CONSENSUSSUBMITMESSAGE", actual: BasicTransactionType.TOPIC_MESSAGE_SUBMIT.protocolName)
    assertEquals(expected: "TOKENCREATION", actual: BasicTransactionType.TOKEN_CREATE.protocolName)
    assertEquals(expected: "TOKENMINT", actual: BasicTransactionType.TOKEN_MINT.protocolName)
    assertEquals(expected: "TOKENBURN", actual: BasicTransactionType.TOKEN_BURN.protocolName)
    assertEquals(expected: "CONTRACTCREATEINSTANCE", actual: BasicTransactionType.CONTRACT_CREATE.protocolName)
    assertEquals(expected: "CONTRACTCALL", actual: BasicTransactionType.CONTRACT_CALL.protocolName)
    assertEquals(expected: "ETHEREUMTRANSACTION", actual: BasicTransactionType.ETHEREUM.protocolName)
}

// Source: this spec (a BasicTransactionType is a TransactionType and can be used as a filter).
@@testId("a-basic-transaction-type-is-a-transaction-type-with-its-protocol-name")
test "a basic transaction type is a transaction type with its protocol name" {
    // when
    let type: TransactionType = BasicTransactionType.CRYPTO_TRANSFER

    // then
    assertInstanceOf(expected: BasicTransactionType, actual: type)
    assertEquals(expected: "CRYPTOTRANSFER", actual: type.protocolName)
}

// Source: the Hiero Enterprise Java test fixture transaction-list.json, first transaction (CRYPTOTRANSFER, SUCCESS,
// charged fee 140686, max fee 1000000, node 0.0.13, consensus timestamp "1785848389.057282104", valid start
// "1785848378.760000732", valid duration 120 seconds, no parent, no entity); millisecond precision.
@@testId("a-transaction-info-keeps-its-timestamps-and-its-valid-duration")
test "a transaction info keeps its timestamps and its valid duration" {
    // given
    let consensus = toZonedDateTime(text: "2026-08-04T12:59:49.057Z")
    let validStart = toZonedDateTime(text: "2026-08-04T12:59:38.760Z")

    // when
    let info = TransactionInfo{transactionId: DEFAULT(TransactionId), transactionHash: [], chargedTxFee: 140686,
            consensusTimestamp: consensus, maxFee: 1000000, memo: [], name: BasicTransactionType.CRYPTO_TRANSFER,
            nonce: 0, node: AccountId.fromString(value: "0.0.13"), result: "SUCCESS", scheduled: false,
            validDuration: toSeconds(value: 120), validStartTimestamp: validStart, transfers: [], tokenTransfers: [],
            nftTransfers: [], stakingRewardTransfers: []}

    // then
    assertEquals(expected: toZonedDateTime(text: "2026-08-04T12:59:49.057Z"), actual: info.consensusTimestamp)
    assertEquals(expected: toZonedDateTime(text: "2026-08-04T12:59:38.760Z"), actual: info.validStartTimestamp)
    assertEquals(expected: toSeconds(value: 120), actual: info.validDuration)
    assertNull(actual: info.parentConsensusTimestamp)
    assertNull(actual: info.entityId)
    assertEquals(expected: 140686, actual: info.chargedTxFee)
    assertEquals(expected: 1000000, actual: info.maxFee)
    assertEquals(expected: AccountId.fromString(value: "0.0.13"), actual: info.node)
    assertEquals(expected: "CRYPTOTRANSFER", actual: info.name.protocolName)
    assertEquals(expected: "SUCCESS", actual: info.result)
    assertEquals(expected: 0, actual: length(value: info.assessedCustomFees))
}

// Source: this spec (`parentConsensusTimestamp` is nullable); the Mirror Node REST API (`parent_consensus_timestamp`
// is set for a child transaction, with its `nonce` greater than 0).
@@testId("a-child-transaction-info-keeps-the-consensus-timestamp-of-its-parent")
test "a child transaction info keeps the consensus timestamp of its parent" {
    // given
    let parent = toZonedDateTime(text: "2026-08-04T12:59:49.057Z")
    let child = toZonedDateTime(text: "2026-08-04T12:59:49.058Z")

    // when
    let info = TransactionInfo{transactionId: DEFAULT(TransactionId), transactionHash: [], chargedTxFee: 0,
            consensusTimestamp: child, maxFee: 0, memo: [], name: BasicTransactionType.CRYPTO_TRANSFER, nonce: 1,
            parentConsensusTimestamp: parent, result: "SUCCESS", scheduled: false,
            validDuration: DEFAULT(seconds), validStartTimestamp: DEFAULT(zonedDateTime), transfers: [],
            tokenTransfers: [], nftTransfers: [], stakingRewardTransfers: []}

    // then
    assertEquals(expected: parent, actual: info.parentConsensusTimestamp)
    assertEquals(expected: child, actual: info.consensusTimestamp)
    assertEquals(expected: 1, actual: info.nonce)
}
```

## Questions & Comments

- **`TransactionInfo.result` is the name of a response code.** The consensus node client has `TransactionStatus` for
  it, but the mirror node client does not depend on the consensus node client. Should the response codes move to a
  namespace both can use?
