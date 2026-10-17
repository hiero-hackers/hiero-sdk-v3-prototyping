# Schedule service

The TCK methods of the schedule service
([schedule-service](https://github.com/hiero-ledger/hiero-sdk-tck/tree/main/docs/test-specifications/schedule-service)):
creating, signing and deleting schedules, and the schedule query.

The scheduled transaction of `createSchedule` is a nested TCK request (`method` and `params`,
ScheduledTransaction.md): `request` builds the transaction of that method's binding without sending it, so every
bound transaction method can be scheduled.

## Gaps

- **`getCost`** / **`cost`** of `getScheduleInfo`: the cost is the result of `PaidQuery.getCost`, a method of the
  API, not an attribute the binding can read. **`queryPayment`**: a paid query has only a maximum payment.

```bindings
requires {ScheduleCreateTransaction, ScheduleSignTransaction, ScheduleDeleteTransaction} from consensusnode.transactions.schedule
requires {ScheduleInfoQuery} from consensusnode.queries.schedule

// Creates a schedule (ScheduleCreateTransaction.md, ScheduledTransaction.md).
binding createSchedule -> ScheduleCreateTransaction {
    scheduledTransaction = request scheduledTransaction
    scheduleMemo         = memo : string
    adminKey             = adminKey : key
    payerAccountId       = payerAccountId : accountId
    expirationTime       = expirationTime : timestamp
    waitForExpiry        = waitForExpiry : bool
    result scheduleId    = receipt.scheduleId : address
    result transactionId = receipt.scheduledTransactionId : transactionId
    result status        = receipt.status : status
}

// Adds the signatures of the signers to a schedule (ScheduleSignTransaction.md).
binding signSchedule -> ScheduleSignTransaction {
    scheduleId = scheduleId : address
    result status = receipt.status : status
}

// Deletes a schedule (ScheduleDeleteTransaction.md).
binding deleteSchedule -> ScheduleDeleteTransaction {
    scheduleId = scheduleId : address
    result status = receipt.status : status
}

// Queries the information of a schedule (ScheduleInfoQuery.md).
binding getScheduleInfo -> ScheduleInfoQuery {
    scheduleId      = scheduleId : address
    maxQueryPayment = maxQueryPayment : tinybar
    unsupported queryPayment "a paid query has only a maximum payment (PaidQuery.maxQueryPayment)"
    unsupported getCost "the cost is the result of PaidQuery.getCost, not an attribute"
    result scheduleId             = response.scheduleId : address
    result creatorAccountId       = response.creatorAccountId : accountId
    result payerAccountId         = response.payerAccountId : accountId
    result scheduledTransactionId = response.scheduledTransactionId : transactionId
    result signers                = response.signers : publicKey
    result adminKey               = response.adminKey : key
    result expirationTime         = response.expirationTime : timestamp
    result executedAt             = response.executionTime : timestamp
    result deletedAt              = response.deletionTime : timestamp
    result scheduleMemo           = response.scheduleMemo : string
    result waitForExpiry          = response.waitForExpiry : bool
    unsupported result cost "the cost is the result of PaidQuery.getCost, not an attribute"
}
```
