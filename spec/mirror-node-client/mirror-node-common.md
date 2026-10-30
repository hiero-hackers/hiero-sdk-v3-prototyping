# Mirror Node Common API

## Description

Value types shared by several Mirror Node queries: the HBAR, fungible-token, NFT and staking-reward transfers that
make up a transaction, and the fixed custom fees that can be attached to tokens and topics. Transfer amounts are
signed: a negative amount is debited from the account, a positive amount is credited to it. HBAR amounts are given in
tinybars, token amounts in the token's smallest unit.

Also shared: the sort order of a list query (`Order`) and the period an entity or a value is valid for
(`TimestampRange`).

## Design Notes

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **The parties of an NFT transfer are grouped.** `sender_account_id`, `receiver_account_id` and `token_id` of an NFT
  transfer are `NftTransfer.parties` (`NftTransferParties`). The sender of a mint and the receiver of a burn are
  `null`; the token is always set.
- **An absent approval flag is `false`.** `is_approval` is optional for HBAR and token transfers in the REST API;
  `Transfer.isApproval` and `TokenTransfer.isApproval` are `false` when it is absent.
- **`FixedFee` is both the fixed fee of a token (`FixedFee`) and of a topic (`FixedCustomFee`).** Only the token
  schema carries `all_collectors_are_exempt`; for a topic fee `allCollectorsAreExempt` is `false`.

## API Schema

```
namespace mirrornode.common
requires {Address, AccountId} from ledger

// The order in which a list query returns its items, by the attribute the query is sorted by (an id, a consensus
// timestamp, ...). When a query is called without an order, the mirror node uses the default of the endpoint.
enum Order {
    ASC   // ascending
    DESC  // descending
}

// The period an entity or a value is valid for: from the inclusive start to the exclusive end. A period that has not
// ended has no end.
@@finalType
TimestampRange {
    @@immutable from: zonedDateTime             // inclusive start
    @@immutable @@nullable to: zonedDateTime    // exclusive end; absent while the period lasts
}

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
    @@immutable @@nullable senderAccountId: AccountId      // absent for a mint
    @@immutable @@nullable receiverAccountId: AccountId    // absent for a burn
    @@immutable tokenId: Address
}

@@finalType
NftTransfer {
    @@immutable isApproval: bool
    @@immutable parties: NftTransferParties
    @@immutable serialNumber: int64
}

@@finalType
StakingRewardTransfer {
    @@immutable account: AccountId
    @@immutable amount: int64
}

FixedFee {
    @@immutable @@default(false) allCollectorsAreExempt: bool   // whether all fee collectors of the token are exempt from the fee
    @@immutable amount: int64
    @@immutable @@nullable collectorAccountId: AccountId
    @@immutable @@nullable denominatingTokenId: Address // token in which the fee is charged; absent if the fee is charged in HBAR
}
```

## Testing

```tests
// Source: the Python SDK (hbar_transfer_test: test_hbar_transfer_constructor); this spec (a negative amount is debited,
// a positive one credited).
@@testId("an-hbar-transfer-keeps-its-account-its-signed-amount-and-its-approval-flag")
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
@@testId("a-token-transfer-keeps-its-token-account-amount-and-approval-flag")
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
@@testId("an-nft-transfer-keeps-its-sender-receiver-token-serial-number-and-approval-flag")
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

// Source: this spec (the NFT transfer of a mint has no sender, that of a burn has no receiver; the token is always set).
@@testId("the-nft-transfer-of-a-mint-has-no-sender-and-that-of-a-burn-has-no-receiver")
test "the NFT transfer of a mint has no sender and that of a burn has no receiver" {
    // given
    let token = Address.fromString(address: "0.0.5005")
    let treasury = AccountId.fromString(value: "0.0.1001")

    // when
    let mint = NftTransfer{isApproval: false, parties: NftTransferParties{receiverAccountId: treasury, tokenId: token},
            serialNumber: 1}
    let burn = NftTransfer{isApproval: false, parties: NftTransferParties{senderAccountId: treasury, tokenId: token},
            serialNumber: 1}

    // then
    assertNull(actual: mint.parties.senderAccountId)
    assertEquals(expected: treasury, actual: mint.parties.receiverAccountId)
    assertEquals(expected: token, actual: mint.parties.tokenId)
    assertNull(actual: burn.parties.receiverAccountId)
    assertEquals(expected: treasury, actual: burn.parties.senderAccountId)
    assertNotEquals(unexpected: mint, actual: burn)
}

// Source: the Java SDK (CustomFixedFeeTest: amount 4, collector 1.2.3, denominating token 5.6.7); this spec (the
// denominating token is absent if the fee is charged in HBAR).
@@testId("a-fixed-fee-in-hbar-has-no-denominating-token-and-a-fixed-fee-in-a-token-has-one")
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

// Source: the mirror node REST API (TimestampRange: an inclusive `from` and an exclusive, nullable `to`).
@@testId("a-timestamp-range-that-has-not-ended-has-no-end")
test "a timestamp range that has not ended has no end" {
    // given
    let start = toZonedDateTime(text: "2026-08-04T14:00:00Z")

    // when
    let open = TimestampRange{from: start}
    let closed = TimestampRange{from: start, to: toZonedDateTime(text: "2026-08-04T15:00:00Z")}

    // then
    assertEquals(expected: start, actual: open.from)
    assertNull(actual: open.to)
    assertEquals(expected: toZonedDateTime(text: "2026-08-04T15:00:00Z"), actual: closed.to)
    assertNotEquals(unexpected: open, actual: closed)
}
```

## Questions & Comments

- `FixedFee.denominatingTokenId`: does this field make sense, since `FixedFee` is also used by the topic query service?
