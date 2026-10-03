# Mirror Node Common API

## Description

Value types shared by several Mirror Node queries: the HBAR, fungible-token, NFT and staking-reward transfers that
make up a transaction, and the fixed custom fees that can be attached to tokens and topics. Transfer amounts are
signed: a negative amount is debited from the account, a positive amount is credited to it. HBAR amounts are given in
tinybars, token amounts in the token's smallest unit.

## API Schema

```
namespace mirrornode.common
requires {Address, AccountId} from ledger

@@finalType
Transfer {
    @@immutable account: AccountId
    @@immutable amount: int64
    @@immutable isApproval: bool
}

@@finalType
TokenTransfer {
    @@immutable tokenId: Address
    @@immutable account: AccountId
    @@immutable amount: int64
    @@immutable isApproval: bool
}

// Grouping of sender, receiver, and token for an NFT transfer
@@finalType
NftTransferParties {
    @@immutable senderAccountId: AccountId
    @@immutable receiverAccountId: AccountId
    @@immutable tokenId: Address
}

@@finalType
NftTransfer {
    @@immutable isApproval: bool
    @@immutable @@nullable parties: NftTransferParties
    @@immutable serialNumber: int64
}

@@finalType
StakingRewardTransfer {
    @@immutable account: AccountId
    @@immutable amount: int64
}

FixedFee {
    @@immutable amount: int64
    @@immutable @@nullable collectorAccountId: AccountId
    @@immutable @@nullable denominatingTokenId: Address // token in which the fee is charged; absent if the fee is charged in HBAR
}
```

## Questions & Comments

- `FixedFee.denominatingTokenId`: does this field make sense, since `FixedFee` is also used by the topic query service?
