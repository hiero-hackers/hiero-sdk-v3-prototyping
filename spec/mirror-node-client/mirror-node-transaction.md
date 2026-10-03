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

Transaction types are modelled like `consensusnode.transactions.TransactionStatus`: `TransactionType` is an
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

## Questions & Comments

- `BasicTransactionType` is not complete yet: the full list is to be derived from the `TransactionTypes` enum of the
  Mirror Node OpenAPI spec.
