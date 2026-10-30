# Token service

The TCK methods of the token service
([token-service](https://github.com/hiero-ledger/hiero-sdk-tck/tree/main/docs/test-specifications/token-service)):
creating and managing tokens, minting, burning and wiping, freeze, KYC and pause, associations, airdrops and the
token queries. Results are written in the conventions of the TCK: amounts in the smallest unit and integers as decimal
strings.

Where the V3 API splits one TCK method into two transactions by token type (mint, burn and wipe of fungible tokens
or NFTs), the binding has two variants: the NFT variant is used when the NFT parameter (`metadata` or
`serialNumbers`) is sent, otherwise the fungible one. A request without either parameter therefore fails in the SDK
(the fungible transaction requires `amount`) instead of being rejected by the network.

The V3 API requires a token type, while the TCK (like the V2 SDKs) defaults to a fungible token, so `createToken`
uses `"ft"` when `tokenType` is not sent.

## Gaps

- **Custom fees** (`customFees` of `createToken` and `getTokenInfo`) and `updateTokenFeeSchedule`: the V3
  token transactions and `TokenInfo` have no custom fees (only the Mirror Node client models them).
- **`ledgerId`** of the queries: the query results have no ledger ID.
- **`queryPayment`**: a paid query has only `maxQueryPayment` in V3; an exact payment cannot be set.
- **Duplicate token IDs** of `associateToken`, `dissociateToken`: the V3 attribute is a set, so duplicates collapse
  before the network can reject them (`TOKEN_ID_REPEATED_IN_TOKEN_LIST`).
- **`getMirrorNodeTokenBalance`** is not bound yet: it reads the Mirror Node, which a call of a repository of the
  Mirror Node client (`TokenRepository::...`, as `getAddressBook` in `node-service.md`) can bind.

```bindings
requires {TokenCreateTransaction, TokenUpdateTransaction, TokenDeleteTransaction} from consensusnode.transactions.tokens
requires {TokenAssociateTransaction, TokenDissociateTransaction} from consensusnode.transactions.tokens
requires {TokenMintFungibleTransaction, TokenMintNftTransaction} from consensusnode.transactions.tokens
requires {TokenBurnFungibleTransaction, TokenBurnNftTransaction} from consensusnode.transactions.tokens
requires {TokenWipeFungibleTransaction, TokenWipeNftTransaction} from consensusnode.transactions.tokens
requires {TokenFreezeTransaction, TokenUnfreezeTransaction} from consensusnode.transactions.tokens
requires {TokenGrantKycTransaction, TokenRevokeKycTransaction} from consensusnode.transactions.tokens
requires {TokenPauseTransaction, TokenUnpauseTransaction} from consensusnode.transactions.tokens
requires {TokenRejectTransaction, TokenReference} from consensusnode.transactions.tokens
requires {TokenAirdropTransaction, TokenClaimAirdropTransaction, TokenCancelAirdropTransaction} from consensusnode.transactions.tokens
requires {PendingAirdrop} from consensusnode.transactions.tokens
requires {TokenTransfer, NftTransfer} from consensusnode.transactions.accounts
requires {TokenInfoQuery, TokenNftInfoQuery} from consensusnode.queries.tokens

// Creates a token (TokenCreateTransaction.md).
binding createToken -> TokenCreateTransaction {
    name                 = name : string
    symbol               = symbol : string
    decimals             = decimals : int32
    initialSupply        = initialSupply : int64
    treasuryAccountId    = treasuryAccountId : accountId
    adminAuthority       = adminKey : key
    kycAuthority         = kycKey : key
    freezeAuthority      = freezeKey : key
    wipeAuthority        = wipeKey : key
    supplyAuthority      = supplyKey : key
    freezeDefault        = freezeDefault : bool
    expirationTime       = expirationTime : timestamp
    autoRenewAccount     = autoRenewAccountId : accountId
    autoRenewPeriod      = autoRenewPeriod : seconds
    tokenMemo            = memo : string
    tokenType            = tokenType : tokenType | "ft" : tokenType
    supplyType           = supplyType : tokenSupplyType
    maxSupply            = maxSupply : int64
    feeScheduleAuthority = feeScheduleKey : key
    pauseAuthority       = pauseKey : key
    metadata             = metadata : hex
    metadataAuthority    = metadataKey : key
    unsupported customFees "TokenCreateTransaction has no custom fees"
    result tokenId = receipt.tokenId : address
    result status  = receipt.status : status
}

// Updates a token (TokenUpdateTransaction.md).
binding updateToken -> TokenUpdateTransaction {
    tokenId              = tokenId : address
    symbol               = symbol : string
    name                 = name : string
    treasuryAccountId    = treasuryAccountId : accountId
    adminAuthority       = adminKey : key
    kycAuthority         = kycKey : key
    freezeAuthority      = freezeKey : key
    wipeAuthority        = wipeKey : key
    supplyAuthority      = supplyKey : key
    autoRenewAccount     = autoRenewAccountId : accountId
    autoRenewPeriod      = autoRenewPeriod : seconds
    expirationTime       = expirationTime : timestamp
    tokenMemo            = memo : string
    feeScheduleAuthority = feeScheduleKey : key
    pauseAuthority       = pauseKey : key
    metadata             = metadata : hex
    metadataAuthority    = metadataKey : key
    result status = receipt.status : status
}

// Deletes a token (TokenDeleteTransaction.md).
binding deleteToken -> TokenDeleteTransaction {
    tokenId = tokenId : address
    result status = receipt.status : status
}

// Associates an account with tokens (TokenAssociateTransaction.md).
binding associateToken -> TokenAssociateTransaction {
    accountId = accountId : accountId
    tokens    = tokenIds : address
    result status = receipt.status : status
}

// Dissociates an account from tokens (TokenDissociateTransaction.md).
binding dissociateToken -> TokenDissociateTransaction {
    accountId = accountId : accountId
    tokens    = tokenIds : address
    result status = receipt.status : status
}

// Mints fungible tokens or NFTs (TokenMintTransaction.md): NFTs if `metadata` is sent.
binding mintToken
    when metadata -> TokenMintNftTransaction {
        tokenId  = tokenId : address
        metadata = metadata : hex
        result status         = receipt.status : status
        result newTotalSupply = receipt.totalSupply : int64
        result serialNumbers  = receipt.serials : int64
    }
    else -> TokenMintFungibleTransaction {
        tokenId = tokenId : address
        amount  = amount : int64
        result status         = receipt.status : status
        result newTotalSupply = receipt.totalSupply : int64
    }

// Burns fungible tokens or NFTs (TokenBurnTransaction.md): NFTs if `serialNumbers` is sent.
binding burnToken
    when serialNumbers -> TokenBurnNftTransaction {
        tokenId = tokenId : address
        serials = serialNumbers : int64
        result status         = receipt.status : status
        result newTotalSupply = receipt.totalSupply : int64
    }
    else -> TokenBurnFungibleTransaction {
        tokenId = tokenId : address
        amount  = amount : int64
        result status         = receipt.status : status
        result newTotalSupply = receipt.totalSupply : int64
    }

// Wipes fungible tokens or NFTs from an account (TokenWipeTransaction.md): NFTs if `serialNumbers` is sent.
binding wipeToken
    when serialNumbers -> TokenWipeNftTransaction {
        tokenId   = tokenId : address
        accountId = accountId : accountId
        serials   = serialNumbers : int64
        result status = receipt.status : status
    }
    else -> TokenWipeFungibleTransaction {
        tokenId   = tokenId : address
        accountId = accountId : accountId
        amount    = amount : int64
        result status = receipt.status : status
    }

// Freezes an account for a token (TokenFreezeTransaction.md).
binding freezeToken -> TokenFreezeTransaction {
    tokenId   = tokenId : address
    accountId = accountId : accountId
    result status = receipt.status : status
}

// Unfreezes an account for a token (TokenUnfreezeTransaction.md).
binding unfreezeToken -> TokenUnfreezeTransaction {
    tokenId   = tokenId : address
    accountId = accountId : accountId
    result status = receipt.status : status
}

// Grants KYC to an account for a token (TokenGrantKycTransaction.md).
binding grantTokenKyc -> TokenGrantKycTransaction {
    tokenId   = tokenId : address
    accountId = accountId : accountId
    result status = receipt.status : status
}

// Revokes the KYC of an account for a token (TokenRevokeKycTransaction.md).
binding revokeTokenKyc -> TokenRevokeKycTransaction {
    tokenId   = tokenId : address
    accountId = accountId : accountId
    result status = receipt.status : status
}

// Pauses a token (TokenPauseTransaction.md).
binding pauseToken -> TokenPauseTransaction {
    tokenId = tokenId : address
    result status = receipt.status : status
}

// Unpauses a token (TokenUnpauseTransaction.md).
binding unpauseToken -> TokenUnpauseTransaction {
    tokenId = tokenId : address
    result status = receipt.status : status
}

// Rejects tokens (TokenRejectTransaction.md): every token of `tokenIds`, or with `serialNumbers` every serial of
// every token.
binding rejectToken -> TokenRejectTransaction {
    owner      = ownerId : accountId
    rejections = each token in tokenIds, serial in serialNumbers? -> TokenReference {
        tokenId = token : address
        serial  = serial : int64
    }
    result status = receipt.status : status
}

// Airdrops fungible tokens and NFTs (TokenAirdropTransaction.md, Transfers.md).
binding airdropToken -> TokenAirdropTransaction {
    tokenTransfers = each tokenTransfers.token -> TokenTransfer {
        tokenId          = tokenId : address
        accountId        = accountId : accountId
        amount           = amount : int64
        expectedDecimals = decimals : int32
        approved         = ^approved : bool
    }
    nftTransfers = each tokenTransfers.nft -> NftTransfer {
        tokenId  = tokenId : address
        serial   = serialNumber : int64
        sender   = senderAccountId : accountId
        receiver = receiverAccountId : accountId
        approved = ^approved : bool
    }
    result status = receipt.status : status
}

// Claims a pending airdrop (TokenAirdropClaimTransaction.md): one pending airdrop, or one per serial number.
binding claimToken -> TokenClaimAirdropTransaction {
    pendingAirdrops = each serial in serialNumbers? -> PendingAirdrop {
        senderId   = senderAccountId : accountId
        receiverId = receiverAccountId : accountId
        tokenId    = tokenId : address
        serial     = serial : int64
    }
    result status = receipt.status : status
}

// Cancels pending airdrops (TokenAirdropCancelTransaction.md, PendingAirdrop.md): one per pending airdrop, or one
// per serial number of it.
binding cancelAirdrop -> TokenCancelAirdropTransaction {
    pendingAirdrops = each pendingAirdrops, serial in serialNumbers? -> PendingAirdrop {
        senderId   = senderAccountId : accountId
        receiverId = receiverAccountId : accountId
        tokenId    = tokenId : address
        serial     = serial : int64
    }
    result status = receipt.status : status
}

// Queries the information of a token (TokenInfoQuery.md).
binding getTokenInfo -> TokenInfoQuery {
    tokenId         = tokenId : address
    maxQueryPayment = maxQueryPayment : tinybar
    unsupported queryPayment "a paid query has only a maximum payment (PaidQuery.maxQueryPayment)"
    result tokenId             = response.tokenId : address
    result name                = response.name : string
    result symbol              = response.symbol : string
    result decimals            = response.decimals : number
    result totalSupply         = response.totalSupply : int64
    result treasuryAccountId   = response.treasuryAccountId : accountId
    result adminKey            = response.adminAuthority : key
    result kycKey              = response.kycAuthority : key
    result freezeKey           = response.freezeAuthority : key
    result pauseKey            = response.pauseAuthority : key
    result wipeKey             = response.wipeAuthority : key
    result supplyKey           = response.supplyAuthority : key
    result feeScheduleKey      = response.feeScheduleAuthority : key
    result metadataKey         = response.metadataAuthority : key
    result defaultFreezeStatus = response.defaultFreezeStatus : bool
    result defaultKycStatus    = response.defaultKycStatus : bool
    result pauseStatus         = response.paused : bool
    result isDeleted           = response.deleted : bool
    result autoRenewAccountId  = response.autoRenewAccount : accountId
    result autoRenewPeriod     = response.autoRenewPeriod : seconds
    result expirationTime      = response.expirationTime : timestamp
    result tokenMemo           = response.tokenMemo : string
    result tokenType           = response.type : tokenType
    result supplyType          = response.supplyType : tokenSupplyType
    result maxSupply           = response.maxSupply : int64
    result metadata            = response.metadata : hex
    unsupported result customFees "TokenInfo has no custom fees"
    unsupported result ledgerId "TokenInfo has no ledger ID"
}

// Queries the information of an NFT (TokenNftInfoQuery.md).
binding getTokenNftInfo -> TokenNftInfoQuery {
    tokenId         = nftId : nftIdToken
    serial          = nftId : nftIdSerial
    maxQueryPayment = maxQueryPayment : tinybar
    unsupported queryPayment "a paid query has only a maximum payment (PaidQuery.maxQueryPayment)"
    result nftId        = response : nftId
    result accountId    = response.accountId : accountId
    result creationTime = response.creationTime : timestamp
    result metadata     = response.metadata : hex
    result spenderId    = response.spenderId : accountId
    unsupported result ledgerId "TokenNftInfo has no ledger ID"
}
```
