# Mirror Node Transaction Query API

## Description

## API Schema

```
namespace mirrornode.transaction
requires {AccountId, MirrorNode, TransactionId} from ledger
requires {NftTransfer, StakingRewardTransfer, TokenTransfer, Transfer} from mirrornode.common
requires {Page} from common

// All known transaction types. Each carries a protocolName matching the REST API wire value.
//TODO: That must be changed in future to make new services pluggable.
// protocolName values are the `TransactionTypes` enum of the Mirror Node REST OpenAPI spec.
enum TransactionType(protocolName: string) {
    ACCOUNT_CREATE("CRYPTOCREATEACCOUNT")
    ACCOUNT_DELETE("CRYPTODELETE")
    ACCOUNT_UPDATE("CRYPTOUPDATEACCOUNT")
    CRYPTO_TRANSFER("CRYPTOTRANSFER")
    TOPIC_CREATE("CONSENSUSCREATETOPIC")
    TOPIC_MESSAGE_SUBMIT("CONSENSUSSUBMITMESSAGE")
    TOKEN_CREATE("TOKENCREATION")
    TOKEN_MINT("TOKENMINT")
    TOKEN_BURN("TOKENBURN")
    TOKEN_TRANSFER      // open: the Mirror Node API has no own type for token transfers (part of CRYPTOTRANSFER)
    CONTRACT_CREATE("CONTRACTCREATEINSTANCE")
    CONTRACT_CALL("CONTRACTCALL")
    ETHEREUM("ETHEREUMTRANSACTION")
    // not complete yet: the full list is to be derived from the Mirror Node OpenAPI spec
    UNKNOWN             // open: no wire value; fallback for types unknown to the SDK
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

- **`TransactionType.TOKEN_TRANSFER` and `TransactionType.UNKNOWN` have no `protocolName`.** The `TransactionTypes`
  enum of the Mirror Node REST OpenAPI spec has no own value for token transfers (they are part of `CRYPTOTRANSFER`)
  and no `UNKNOWN` value. Should `TOKEN_TRANSFER` be removed, and should `UNKNOWN` become a `@@nullable` protocolName
  (or be modelled differently, e.g. as an SDK-side fallback outside the enum)?
