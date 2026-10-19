# Receipt Queries API

This namespace defines the query for the receipt of a transaction by its transaction id, for the cases in which the
typed `Response.queryReceipt(client)` of [`transactions.md`](transactions.md) does not fit: the type of the
transaction is not known, or the receipts of duplicate and child transactions are needed.

## Description

The receipt of a transaction tells whether it succeeded and which entity it created. You usually read it through the
`Response` you got when you submitted the transaction: `response.queryReceipt(client)` returns the receipt typed for
the transaction, e.g. an `AccountCreateReceipt` with the id of the new account.

**`TransactionReceiptQuery`** reads the receipt by the transaction id alone. Use it when you do not know the type of
the transaction, and to read the receipts of related transactions:

- **Duplicates** (`includeDuplicates`) — the same signed transaction can reach the network more than once, e.g. when
  it was sent to several nodes or sent again after a node did not answer. All copies carry the same transaction id.
  The network executes only the first one; every further copy that reaches consensus gets a receipt of its own,
  normally with the status `DUPLICATE_TRANSACTION`. Ask for them to see whether and how often that happened.
- **Children** (`includeChildren`) — transactions the network creates itself while it executes this one, because the
  transaction needs further transactions to complete. The most common case is a smart contract call that transfers
  value, creates accounts or calls other contracts. Each child has a receipt of its own; its transaction type is not
  known to the caller.

The query is free. The network keeps a receipt for about three minutes after consensus; before consensus and after
that period it answers that the receipt is unknown. The query waits while the receipt is not yet available and while
the node is busy, within the retry settings of the query, and fails when the receipt does not become available.
For a transaction older than about three minutes, read its record or ask the mirror node.

The result contains **`UntypedReceipt`s**: receipts with every attribute the network reports, each of them optional.
When you know the type of the transaction, turn an untyped receipt into the typed one with
`typed(transactionType)`:

```
TransactionReceipts receipts = TransactionReceiptQuery{transactionId: id, includeChildren: true}
    .submit(client).value;
AccountCreateReceipt receipt = receipts.receipt.typed(transactionType: AccountCreateTransaction);
for (UntypedReceipt child : receipts.children) { ... }
```

The query returns the receipt also when the transaction failed: read its `status`.

## Design Notes

- **Two kinds of receipts.** Receipts are typed per transaction type everywhere in this API. The receipt of the
  protocol does not say which transaction type it belongs to, so a query that knows only the transaction id cannot
  produce a typed receipt, and the type of a child transaction is unknown to the caller anyway. `UntypedReceipt` is
  the one place where the receipt of the protocol appears as it is: every attribute optional, as in the current
  Hiero SDKs, whose `TransactionReceipt` has the same shape. See ADR-0013.
- **`typed(...)` is an instance method with its own type parameter** and is not overridable — `UntypedReceipt`
  is final — as the guideline requires for generic methods.
- **`typed(...)` needs no network request.** `UntypedReceipt` holds every attribute of the protocol's
  `TransactionReceipt`, so the SDK can rebuild the protocol message and hand it to the `TransactionSupport` of the
  given transaction type (`consensusnode.transactions.spi`), which creates the typed receipt. A receipt can therefore
  be typed for any transaction type the SDK knows, including those of custom services.
- **`Response.queryReceipt(client)` is built on this query**: it sends the query to the node the transaction was sent
  to and types the receipt with the transaction type the `Response` carries.
- **Polling.** While the receipt is not final the query is repeated: when the node answers with `BUSY`, `UNKNOWN`,
  `RECEIPT_NOT_FOUND` or `PLATFORM_NOT_ACTIVE`, and when the receipt's status is `UNKNOWN` or `OK`. These are the
  statuses the Java, Go, Python and C++ SDKs repeat on.
- **No `validateStatus`.** The current SDKs throw on a failed status depending on a flag whose default differs between
  the SDKs and between the query and the response. The query here returns the receipt in every case: the status is a
  value, not an error. Whether the typed `Response.queryReceipt(client)` throws on a failed status is the open
  question in [`transactions.md`](transactions.md).

## API Schema

```
namespace consensusnode.queries.receipts
requires {AccountId, Address, ContractId, TransactionId} from ledger
requires {FreeQuery} from consensusnode.queries
requires {Receipt, Transaction} from consensusnode.transactions

// Free query for the receipt of a transaction by its transaction id, optionally with the receipts of its duplicates
// and children. The receipt is available for about three minutes after consensus; the query waits until it is.
@@finalType
TransactionReceiptQuery extends FreeQuery<TransactionReceipts> {
    @@immutable transactionId: TransactionId                    // the id of the transaction
    @@immutable @@default(false) includeDuplicates: bool        // also return the receipts of further copies (duplicates)
    @@immutable @@default(false) includeChildren: bool          // also return the receipts of child transactions
    @@immutable @@nullable nodeAccountId: AccountId             // the node to ask; null: any node of the client
}

// The answer of a `TransactionReceiptQuery`: the receipt of the transaction and, when requested, the receipts of its
// duplicates and children. Both lists are empty unless the query asked for them, and empty while the receipt itself is
// not yet available.
@@finalType
TransactionReceipts {
    // The receipt of the transaction. When the same transaction reached consensus more than once (see `duplicates`),
    // this is the receipt of the one that counts: the first to reach consensus that was not rejected for being sent
    // to the wrong node (`INVALID_NODE_ACCOUNT`) or for a wrong payer signature (`INVALID_PAYER_SIGNATURE`).
    @@immutable receipt: UntypedReceipt

    // The receipts of the further submissions of the same transaction, in the order they reached consensus.
    // A signed transaction can reach the network more than once with the same transaction id: when it was sent to
    // several nodes, or sent again after a node did not answer. The network executes it only once; every further
    // copy that reaches consensus gets its own receipt, normally with the status `DUPLICATE_TRANSACTION`, and is
    // listed here. Only returned when `includeDuplicates` is set.
    @@immutable @@default([]) duplicates: list<UntypedReceipt>

    // The receipts of the transactions the network created itself while it executed this one, in the order they
    // reached consensus. A transaction can need further transactions to complete; most commonly a smart contract call
    // that transfers value, creates accounts or calls other contracts, each of which becomes a child transaction with
    // a receipt of its own. Their transaction types are not known to the caller, which is why every receipt here is
    // untyped. Empty when the transaction created no children. Only returned when `includeChildren` is set.
    @@immutable @@default([]) children: list<UntypedReceipt>
}

// A receipt whose transaction type is not known: every attribute the network reports for a transaction, each of them
// optional. Use `typed(transactionType)` to get the receipt of a known transaction type.
@@finalType
UntypedReceipt extends Receipt {
    @@immutable @@nullable accountId: AccountId                 // the account created
    @@immutable @@nullable fileId: Address                      // the file created
    @@immutable @@nullable contractId: ContractId               // the smart contract created
    @@immutable @@nullable topicId: Address                     // the topic created
    @@immutable @@nullable topicSequenceNumber: int64           // the sequence number of a submitted topic message
    @@immutable @@nullable topicRunningHash: bytes              // the running hash of a topic after a submitted message
    @@immutable @@nullable topicRunningHashVersion: int64       // the version of the running hash algorithm
    @@immutable @@nullable tokenId: Address                     // the token created
    @@immutable @@nullable totalSupply: int64                   // the total supply of a token after a mint, burn or wipe
    @@immutable @@nullable scheduleId: Address                  // the schedule created
    @@immutable @@nullable scheduledTransactionId: TransactionId  // the id of the transaction a schedule executes
    @@immutable @@default([]) serials: list<int64>              // the serial numbers of the NFTs minted
    @@immutable @@nullable nodeId: int64                        // the consensus node created
    @@immutable @@nullable registeredNodeId: int64              // the registered node created
    @@immutable @@nullable blockNumber: int64                   // the number of the block the transaction is in

    // The receipt typed for the given transaction type, e.g. an `AccountCreateReceipt` for
    // `AccountCreateTransaction`. Makes no network request. Fails when the receipt lacks an attribute the typed
    // receipt requires, e.g. an `accountId` for an `AccountCreateReceipt`; a receipt can be typed for a transaction
    // type it does not belong to when nothing is missing.
    @@finalMethod @@throws(invalid-argument-error)
    $$R typed<$$R extends Receipt>(transactionType: type<Transaction<$$R, ANY>>)
}
```

## Default Instances for Testing

```
// The receipt of the default transaction id, without any created entity.
instance UntypedReceipt = UntypedReceipt{transactionId: DEFAULT, status: DEFAULT, exchangeRate: DEFAULT,
        nextExchangeRate: DEFAULT}
```

## Testing

Tests of turning an untyped receipt into a typed one, written as given / when / then. They need no network: the
untyped receipt is built from its attributes.

```tests
// Source: this spec (UntypedReceipt.typed: the typed receipt takes the attributes the untyped one carries).
@@testId("an-untyped-receipt-with-an-account-id-is-typed-as-the-receipt-of-an-account-creation")
test "an untyped receipt with an account id is typed as the receipt of an account creation" {
    // given
    let account = DEFAULT(AccountId)
    let untyped = UntypedReceipt{transactionId: DEFAULT, status: DEFAULT, exchangeRate: DEFAULT,
            nextExchangeRate: DEFAULT, accountId: account}

    // when
    let receipt = untyped.typed(transactionType: consensusnode.transactions.accounts.AccountCreateTransaction)

    // then
    assertEquals(expected: account, actual: receipt.accountId)
    assertEquals(expected: untyped.transactionId, actual: receipt.transactionId)
    assertEquals(expected: untyped.status, actual: receipt.status)
}

// Source: this spec (UntypedReceipt.typed fails when the receipt lacks an attribute the typed receipt requires).
@@testId("an-untyped-receipt-without-an-account-id-is-not-the-receipt-of-an-account-creation")
test "an untyped receipt without an account id is not the receipt of an account creation" {
    // when / then
    @@expectThrows(invalid-argument-error)
    DEFAULT(UntypedReceipt).typed(transactionType: consensusnode.transactions.accounts.AccountCreateTransaction)
}
```

## Questions & Comments

- **Which statuses are polled on?** The current SDKs differ: Java, Go, Python and C++ also repeat while the receipt's
  status is `OK`, JavaScript, Rust and Swift do not; Go and Python also repeat on
  `PLATFORM_TRANSACTION_NOT_CREATED`. The list in the design notes follows the majority — open whether the spec
  should name the statuses at all or leave them to the implementation.
- **Should the node failover of the current SDKs be offered?** Java, JavaScript, Go, Rust and C++ let a client ask
  other nodes when the node a transaction was sent to does not answer (off by default). `nodeAccountId: null` asks
  any node; a failover after a pinned node failed is not expressible. — open
- **Can any node answer a receipt query?** Every current SDK pins the query to the node the transaction was sent to,
  but neither HAPI nor the SDKs say why. If any node answers reliably, `Response.nodeAccountId` could be dropped.
  — open, to be measured against a network with more than one node
