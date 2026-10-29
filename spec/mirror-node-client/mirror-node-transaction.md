# Mirror Node Transaction Query API

## Description

Query transactions from the Mirror Node. Use the `TransactionRepository` (available as
`MirrorNodeClient.transaction`) to look up a transaction by its `TransactionId` or to page through the transactions of
an account, optionally filtered by transaction type, by result or by whether the account's balance was credited or
debited. A `TransactionInfo` contains the fees, timestamps, result and all HBAR, token, NFT and staking-reward
transfers of the transaction.

The type of a transaction is a `TransactionType` that carries the value the Mirror Node REST API uses for it
(`protocolName`). The types known to the SDK are available as constants in `BasicTransactionType`. A type the SDK does
not know yet (for example from a service added to the network later) is still returned as a `TransactionType` with the
received `protocolName`, so no information is lost and the value can be used as a filter again.

## Design Notes

Transaction types are modelled like `consensusnode.status.TransactionStatus`: `TransactionType` is an
abstraction that carries the Mirror Node REST wire value (`protocolName`), and `BasicTransactionType` enumerates the
types known to the SDK. There is no `UNKNOWN` value.

## API Schema

```
namespace mirrornode.transaction
requires {AccountId, MirrorNode, TransactionId} from ledger
requires {NftTransfer, StakingRewardTransfer, TokenTransfer, Transfer} from mirrornode.common
requires {Page} from common

// The type of a transaction as reported by the Mirror Node. Open for types unknown to the SDK, so that new
// services stay usable without an SDK update.
abstraction TransactionType {
    @@immutable protocolName: string   // the REST API wire value, e.g. "CRYPTOTRANSFER"
}

// The transaction types known to the SDK. protocolName values are the `TransactionTypes` enum of the Mirror Node
// REST OpenAPI spec.
enum BasicTransactionType(protocolName: string) extends TransactionType {
    ACCOUNT_CREATE("CRYPTOCREATEACCOUNT")
    ACCOUNT_DELETE("CRYPTODELETE")
    ACCOUNT_UPDATE("CRYPTOUPDATEACCOUNT")
    CRYPTO_TRANSFER("CRYPTOTRANSFER")
    TOPIC_CREATE("CONSENSUSCREATETOPIC")
    TOPIC_MESSAGE_SUBMIT("CONSENSUSSUBMITMESSAGE")
    TOKEN_CREATE("TOKENCREATION")
    TOKEN_MINT("TOKENMINT")
    TOKEN_BURN("TOKENBURN")
    CONTRACT_CREATE("CONTRACTCREATEINSTANCE")
    CONTRACT_CALL("CONTRACTCALL")
    ETHEREUM("ETHEREUMTRANSACTION")
    // not complete yet: the full list is to be derived from the Mirror Node OpenAPI spec
}

enum TransactionResult {
    SUCCESS
    FAIL
}

enum BalanceModification {
    CREDIT
    DEBIT
}

@@finalType
TransactionInfo {
    @@immutable transactionId: TransactionId
    @@immutable transactionHash: bytes
    @@immutable chargedTxFee: int64
    @@immutable consensusTimestamp: zonedDateTime
    @@immutable @@nullable entityId: string
    @@immutable maxFee: int64
    @@immutable memo: bytes
    @@immutable name: TransactionType
    @@immutable nonce: int32
    @@immutable @@nullable node: string
    @@immutable @@nullable parentConsensusTimestamp: zonedDateTime
    @@immutable result: TransactionResult
    @@immutable scheduled: bool
    @@immutable validDuration: seconds
    @@immutable validStartTimestamp: zonedDateTime
    @@immutable transfers: list<Transfer>
    @@immutable tokenTransfers: list<TokenTransfer>
    @@immutable nftTransfers: list<NftTransfer>
    @@immutable stakingRewardTransfers: list<StakingRewardTransfer>
}

abstraction TransactionRepository {
    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findByAccount(accountId: AccountId)

    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findByAccountAndType(accountId: AccountId, type: TransactionType)

    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findByAccountAndResult(accountId: AccountId, result: TransactionResult)

    @@async @@throws(mirror-node-error)
    Page<TransactionInfo> findByAccountAndModification(accountId: AccountId, modification: BalanceModification)

    @@async @@throws(mirror-node-error)
    @@nullable TransactionInfo findById(transactionId: TransactionId)
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
            nonce: 0, node: "0.0.13", result: TransactionResult.SUCCESS, scheduled: false,
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
    assertEquals(expected: "0.0.13", actual: info.node)
    assertEquals(expected: "CRYPTOTRANSFER", actual: info.name.protocolName)
    assertEquals(expected: TransactionResult.SUCCESS, actual: info.result)
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
            parentConsensusTimestamp: parent, result: TransactionResult.SUCCESS, scheduled: false,
            validDuration: DEFAULT(seconds), validStartTimestamp: DEFAULT(zonedDateTime), transfers: [],
            tokenTransfers: [], nftTransfers: [], stakingRewardTransfers: []}

    // then
    assertEquals(expected: parent, actual: info.parentConsensusTimestamp)
    assertEquals(expected: child, actual: info.consensusTimestamp)
    assertEquals(expected: 1, actual: info.nonce)
}
```

## Questions & Comments

- `BasicTransactionType` is not complete yet: the full list is to be derived from the `TransactionTypes` enum of the
  Mirror Node OpenAPI spec.
