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

## Testing

```tests
// Source: the Python SDK (hbar_transfer_test: test_hbar_transfer_constructor); this spec (a negative amount is debited,
// a positive one credited).
test "an HBAR transfer keeps its account, its signed amount and its approval flag" {
    // given
    let account = DEFAULT(AccountId)

    // when
    let debit = Transfer{account: account, amount: -1000, isApproval: false}
    let credit = Transfer{account: account, amount: 1000, isApproval: true}

    // then
    assertEquals(expected: account, actual: debit.account)
    assertEquals(expected: -1000, actual: debit.amount)
    assertFalse(condition: debit.isApproval)
    assertEquals(expected: 1000, actual: credit.amount)
    assertTrue(condition: credit.isApproval)
    assertNotEquals(unexpected: debit, actual: credit)
}

// Source: the Python SDK (token_transfer_test: test_token_transfer_constructor).
test "a token transfer keeps its token, account, amount and approval flag" {
    // given
    let token = Address.fromString(address: "0.0.5005")
    let account = DEFAULT(AccountId)

    // when
    let transfer = TokenTransfer{tokenId: token, account: account, amount: -10, isApproval: false}

    // then
    assertEquals(expected: token, actual: transfer.tokenId)
    assertEquals(expected: account, actual: transfer.account)
    assertEquals(expected: -10, actual: transfer.amount)
    assertFalse(condition: transfer.isApproval)
    assertEquals(expected: TokenTransfer{tokenId: token, account: account, amount: -10, isApproval: false},
            actual: transfer)
}

// Source: the Python SDK (token_nft_transfer_test: test_token_nft_transfer_constructor, approved and not approved).
test "an NFT transfer keeps its sender, receiver, token, serial number and approval flag" {
    // given
    let parties = NftTransferParties{senderAccountId: AccountId.fromString(value: "0.0.1001"),
            receiverAccountId: AccountId.fromString(value: "0.0.1002"), tokenId: Address.fromString(address: "0.0.5005")}

    for approved in [false, true] {
        // when
        let transfer = NftTransfer{isApproval: approved, parties: parties, serialNumber: 1}

        // then
        assertEquals(expected: approved, actual: transfer.isApproval)
        assertEquals(expected: parties, actual: transfer.parties)
        assertEquals(expected: 1, actual: transfer.serialNumber)
    }
}

// Source: this spec (`parties` is @@nullable).
test "an NFT transfer can be created without its parties" {
    // when
    let transfer = NftTransfer{isApproval: false, serialNumber: 1}

    // then
    assertNull(actual: transfer.parties)
}

// Source: the Java SDK (CustomFixedFeeTest: amount 4, collector 1.2.3, denominating token 5.6.7); this spec (the
// denominating token is absent if the fee is charged in HBAR).
test "a fixed fee in HBAR has no denominating token and a fixed fee in a token has one" {
    // given
    let collector = AccountId.fromString(value: "1.2.3")
    let token = Address.fromString(address: "5.6.7")

    // when
    let inHbar = FixedFee{amount: 4, collectorAccountId: collector}
    let inToken = FixedFee{amount: 4, collectorAccountId: collector, denominatingTokenId: token}

    // then
    assertEquals(expected: 4, actual: inHbar.amount)
    assertEquals(expected: collector, actual: inHbar.collectorAccountId)
    assertNull(actual: inHbar.denominatingTokenId)
    assertEquals(expected: token, actual: inToken.denominatingTokenId)
}
```

## Questions & Comments

- `FixedFee.denominatingTokenId`: does this field make sense, since `FixedFee` is also used by the topic query service?

- **`NftTransfer.parties` of a mint or burn is `null`, and the token id is lost with it.** `NftTransferParties`
  requires both a sender and a receiver, but the NFT transfer of a mint has no sender and that of a burn has no
  receiver. The Java implementation sets `parties` to `null` for these transfers, which also drops the `tokenId` that
  the REST API does return. Should `senderAccountId` / `receiverAccountId` be `@@nullable`, and should `tokenId` move
  to `NftTransfer` itself?
