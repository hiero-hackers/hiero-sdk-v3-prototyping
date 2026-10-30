# Crypto service

The TCK methods of the crypto service
([crypto-service](https://github.com/hiero-ledger/hiero-sdk-tck/tree/main/docs/test-specifications/crypto-service)):
accounts, transfers and allowances. Results are written in the conventions of the TCK: amounts in tinybar, durations in
seconds and integers as decimal strings.

## Methods without binding

- `executeDeprecatedAccountBalanceQuery`: the TCK checks the deprecation warning and error texts of the V2 SDKs; the
  result (`constructionWarning`, `executionError`) is no value of the API. `AccountBalanceQuery` itself exists.
- `ping`, `pingAll`: the V3 client has no ping of a consensus node.
- `getMirrorNodeAccountBalance`: reads the Mirror Node; not bound yet. A call of the Mirror Node client
  (`AccountRepository::...`, as `getAddressBook` in `node-service.md`) can bind it; open is how its `accountId`, which
  may also be an EVM address or a key alias, maps to the parameters of the repository.

```bindings
requires {AccountCreateTransaction, AccountUpdateTransaction, AccountDeleteTransaction, TransferTransaction} from consensusnode.transactions.accounts
requires {HbarTransfer, TokenTransfer, NftTransfer} from consensusnode.transactions.accounts
requires {AccountAllowanceApproveTransaction, AccountAllowanceDeleteTransaction} from consensusnode.transactions.accounts
requires {HbarAllowance, TokenAllowance, NftAllowance, NftSerialGrant, NftCollectionGrant} from consensusnode.transactions.accounts
requires {NftAllowanceDeletion} from consensusnode.transactions.accounts
requires {AccountInfoQuery} from consensusnode.queries.accounts
requires {TransactionReceiptQuery} from consensusnode.queries.receipts

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
    // not in AccountCreateTransaction.md, but test-transaction-receipt-query.ts reads it
    undocumented result transactionId = receipt.transactionId : transactionId
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

// Approves hbar, token and NFT allowances (AccountAllowanceApproveTransaction.md, Allowances.md). An NFT allowance
// grants the listed serial numbers if `serialNumbers` is sent, otherwise the whole collection (`approvedForAll`).
binding approveAllowance -> AccountAllowanceApproveTransaction {
    hbarAllowances = each allowances.hbar -> HbarAllowance {
        ownerAccountId   = ^ownerAccountId : accountId
        spenderAccountId = ^spenderAccountId : accountId
        amount           = amount : tinybar
    }
    tokenAllowances = each allowances.token -> TokenAllowance {
        tokenId          = tokenId : address
        ownerAccountId   = ^ownerAccountId : accountId
        spenderAccountId = ^spenderAccountId : accountId
        amount           = amount : int64
    }
    nftAllowances = each allowances.nft -> NftAllowance {
        tokenId           = tokenId : address
        ownerAccountId    = ^ownerAccountId : accountId
        spenderAccountId  = ^spenderAccountId : accountId
        grant             = when serialNumbers -> NftSerialGrant {
                                serials = serialNumbers : int64
                            }
                            else -> NftCollectionGrant {
                                approved = approvedForAll : bool
                            }
        delegatingSpender = delegateSpenderAccountId : accountId
    }
    result status = receipt.status : status
}

// Deletes NFT allowances (AccountAllowanceDeleteTransaction.md). The tests send every allowance flat -
// {ownerAccountId, tokenId, serialNumbers} - and not nested in `nft` as the specification of the method and
// Allowances.md describe it (test-account-allowance-delete-transaction.ts, revision a3ea777).
binding deleteAllowance -> AccountAllowanceDeleteTransaction {
    nftAllowanceDeletions = each allowances -> NftAllowanceDeletion {
        tokenId        = tokenId : address
        ownerAccountId = ownerAccountId : accountId
        serials        = serialNumbers : int64
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

// Reads the receipt of a transaction by its id (TransactionReceiptQuery.md), untyped: the TCK does not name the
// transaction type. The query returns the receipt also for a failed transaction; the V3 API has no validateStatus.
binding getTransactionReceipt -> TransactionReceiptQuery {
    transactionId     = transactionId : transactionId
    includeDuplicates = includeDuplicates : bool
    includeChildren   = includeChildren : bool
    // validateStatus false is what the V3 query does: it returns the receipt of a failed transaction too
    unsupported validateStatus "the V3 receipt query does not fail on the status of a failed transaction; the status is a value, not an error" unless false
    result status                 = response.receipt.status : status
    result accountId              = response.receipt.accountId : accountId
    result fileId                 = response.receipt.fileId : address
    result contractId             = response.receipt.contractId : contractId
    result topicId                = response.receipt.topicId : address
    result tokenId                = response.receipt.tokenId : address
    result scheduleId             = response.receipt.scheduleId : address
    result exchangeRate           = response.receipt.exchangeRate : exchangeRate
    result topicSequenceNumber    = response.receipt.topicSequenceNumber : int64
    result topicRunningHash       = response.receipt.topicRunningHash : hex
    result totalSupply            = response.receipt.totalSupply : int64
    result scheduledTransactionId = response.receipt.scheduledTransactionId : transactionId
    result serials                = response.receipt.serials : int64
    result nodeId                 = response.receipt.nodeId : int64
    result duplicates             = response.duplicates : receipt
    result children               = response.children : receipt
}
```
