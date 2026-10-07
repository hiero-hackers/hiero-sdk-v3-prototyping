# Crypto service

The TCK methods of the crypto service
([crypto-service](https://github.com/hiero-ledger/hiero-sdk-tck/tree/main/docs/test-specifications/crypto-service)):
accounts and transfers. Results are written in the conventions of the TCK: amounts in tinybar, durations in
seconds and integers as decimal strings.

```bindings
requires {AccountCreateTransaction, AccountUpdateTransaction, AccountDeleteTransaction, TransferTransaction} from consensusnode.transactions.accounts
requires {HbarTransfer, TokenTransfer, NftTransfer} from consensusnode.transactions.accounts
requires {AccountInfoQuery} from consensusnode.queries.accounts

// Creates an account (AccountCreateTransaction.md).
binding createAccount -> AccountCreateTransaction {
    authority                     = key : key
    initialBalance                = initialBalance : tinybar
    receiverSignatureRequired     = receiverSignatureRequired : bool
    autoRenewPeriod               = autoRenewPeriod : seconds
    accountMemo                   = memo : string
    maxAutomaticTokenAssociations = maxAutoTokenAssociations : int32
    stakedAccountId               = stakedAccountId : accountId
    stakedNodeId                  = stakedNodeId : int64
    declineStakingReward          = declineStakingReward : bool
    alias                         = alias : hex
    result accountId = receipt.accountId : accountId
    result status    = receipt.status : status
}

// Updates an account (AccountUpdateTransaction.md).
binding updateAccount -> AccountUpdateTransaction {
    accountId                     = accountId : accountId
    authority                     = key : key
    autoRenewPeriod               = autoRenewPeriod : seconds
    expirationTime                = expirationTime : timestamp
    receiverSignatureRequired     = receiverSignatureRequired : bool
    accountMemo                   = memo : string
    maxAutomaticTokenAssociations = maxAutoTokenAssociations : int32
    stakedAccountId               = stakedAccountId : accountId
    stakedNodeId                  = stakedNodeId : int64
    declineStakingReward          = declineStakingReward : bool
    result status = receipt.status : status
}

// Deletes an account (AccountDeleteTransaction.md).
binding deleteAccount -> AccountDeleteTransaction {
    accountId         = deleteAccountId : accountId
    transferAccountId = transferAccountId : accountId
    result status = receipt.status : status
}

// Transfers hbar, fungible tokens and NFTs (TransferTransaction.md, Transfers.md).
binding transferCrypto -> TransferTransaction {
    hbarTransfers = each transfers.hbar -> HbarTransfer {
        accountId = accountId : accountId | evmAddress : evmAccountId
        amount    = amount : tinybar
        approved  = ^approved : bool
    }
    tokenTransfers = each transfers.token -> TokenTransfer {
        tokenId          = tokenId : address
        accountId        = accountId : accountId
        amount           = amount : int64
        expectedDecimals = decimals : int32
        approved         = ^approved : bool
    }
    nftTransfers = each transfers.nft -> NftTransfer {
        tokenId  = tokenId : address
        serial   = serialNumber : int64
        sender   = senderAccountId : accountId
        receiver = receiverAccountId : accountId
        approved = ^approved : bool
    }
    result status = receipt.status : status
}

// Queries the information of an account (AccountInfoQuery.md).
binding getAccountInfo -> AccountInfoQuery {
    accountId = accountId : accountId
    result accountId                     = response.accountId : accountId
    result contractAccountId             = response.evmAddress : evmAddress
    result isDeleted                     = response.deleted : bool
    result key                           = response.authority : key
    result balance                       = response.balance : tinybar
    result isReceiverSignatureRequired   = response.receiverSignatureRequired : bool
    result expirationTime                = response.expirationTime : timestamp
    result autoRenewPeriod               = response.autoRenewPeriod : seconds
    result accountMemo                   = response.accountMemo : string
    result ownedNfts                     = response.ownedNfts : int64
    result maxAutomaticTokenAssociations = response.maxAutomaticTokenAssociations : int32
    result stakingInfo.declineStakingReward = response.declineStakingReward : bool
    result stakingInfo.stakedAccountId      = response.stakedAccountId : accountId
    result stakingInfo.stakedNodeId         = response.stakedNodeId : int64
    unsupported result proxyAccountId "proxy staking is deprecated in HAPI; AccountInfo has no such attribute"
    unsupported result proxyReceived "proxy staking is deprecated in HAPI; AccountInfo has no such attribute"
    unsupported result sendRecordThreshold "deprecated in HAPI; AccountInfo has no such attribute"
    unsupported result receiveRecordThreshold "deprecated in HAPI; AccountInfo has no such attribute"
    unsupported result liveHashes "live hashes are a retired network feature"
    unsupported result tokenRelationships "AccountInfo has no token relationships"
    unsupported result aliasKey "AccountInfo has no alias key"
    unsupported result ledgerId "AccountInfo has no ledger ID"
    unsupported result hbarAllowances "AccountInfo has no allowances"
    unsupported result tokenAllowances "AccountInfo has no allowances"
    unsupported result nftAllowances "AccountInfo has no allowances"
    unsupported result ethereumNonce "AccountInfo has no Ethereum nonce (the Mirror Node account has one)"
}
```
