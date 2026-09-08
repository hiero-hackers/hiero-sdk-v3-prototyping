# Transactions / Schedules

## Scenarios

### `transactions.schedules/can-create-schedule`

> **Implementation:** `ScheduleCreateIntegrationTest.canCreateSchedule`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:42`
> **Status:** Disabled — No reason recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create schedule.
 - **Then** the operation completes without error and the returned information executed at is present.

### `transactions.schedules/can-get-transaction-schedule`

> **Implementation:** `ScheduleCreateIntegrationTest.canGetTransactionSchedule`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:67`
> **Status:** Disabled — No reason recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to get Transaction.
 - **Then** the operation completes without error, the returned information executed at is present, and the returned information scheduled transaction is present.

### `transactions.schedules/can-create-with-schedule`

> **Implementation:** `ScheduleCreateIntegrationTest.canCreateWithSchedule`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:93`
> **Status:** Disabled — No reason recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create schedule with schedule.
 - **Then** the operation completes without error, the returned information executed at is present, and the returned information scheduled transaction is present.

### `transactions.schedules/can-sign-schedule2`

> **Implementation:** `ScheduleCreateIntegrationTest.canSignSchedule2`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:119`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign schedule.
 - **Then** the operation completes without error, the returned information executed at is absent, the returned information executed at is present, and the schedule identifier checksum is absent.

### `transactions.schedules/can-schedule-token-transfer`

> **Implementation:** `ScheduleCreateIntegrationTest.canScheduleTokenTransfer`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:202`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to schedule token transfer.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.schedules/cannot-schedule-two-transactions`

> **Implementation:** `ScheduleCreateIntegrationTest.cannotScheduleTwoTransactions`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:270`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to schedule two identical transactions.
 - **Then** the request is rejected with the expected error.

### `transactions.schedules/can-schedule-topic-message`

> **Implementation:** `ScheduleCreateIntegrationTest.canScheduleTopicMessage`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:308`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to schedule topic message.
 - **Then** the operation completes without error, the expected observable result is returned, the returned information is present, and the returned information schedule identifier equals the expected schedule identifier.

### `transactions.schedules/can-sign-schedule`

> **Implementation:** `ScheduleCreateIntegrationTest.canSignSchedule`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:381`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign schedule.
 - **Then** the operation completes without error, the returned information executed at is absent, the returned information executed at is present, and the schedule identifier checksum is absent.

### `transactions.schedules/cannot-schedule-transaction-one-year-into-the-future`

> **Implementation:** `ScheduleCreateIntegrationTest.cannotScheduleTransactionOneYearIntoTheFuture`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:435`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to schedule one year into the future.
 - **Then** the request is rejected with status SCHEDULE_EXPIRATION_TIME_TOO_FAR_IN_FUTURE.

### `transactions.schedules/cannot-schedule-transaction-in-the-past`

> **Implementation:** `ScheduleCreateIntegrationTest.cannotScheduleTransactionInThePast`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:466`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to schedule in the past.
 - **Then** the request is rejected with status SCHEDULE_EXPIRATION_TIME_MUST_BE_HIGHER_THAN_CONSENSUS_TIME.

### `transactions.schedules/can-sign-schedule-and-wait-for-expiry`

> **Implementation:** `ScheduleCreateIntegrationTest.canSignScheduleAndWaitForExpiry`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:498`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign schedule and wait for expiry.
 - **Then** the operation completes without error, the returned information executed at is absent, the schedule identifier checksum is absent, and the schedule identifier hash code has the expected value.

### `transactions.schedules/can-sign-with-multi-sig-and-update-signing-requirements`

> **Implementation:** `ScheduleCreateIntegrationTest.canSignWithMultiSigAndUpdateSigningRequirements`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:553`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign with multisig and update signing requirements.
 - **Then** the operation completes without error, the returned information executed at is absent, and the returned information executed at is present.

### `transactions.schedules/can-sign-with-multi-sig`

> **Implementation:** `ScheduleCreateIntegrationTest.canSignWithMultiSig`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:640`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign with multisig.
 - **Then** the operation completes without error, the returned information executed at is absent, and the returned information executed at is present.

### `transactions.schedules/can-execute-with-short-expiration-time`

> **Implementation:** `ScheduleCreateIntegrationTest.canExecuteWithShortExpirationTime`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleCreateIntegrationTest.java:725`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute with short expiration time.
 - **Then** the operation completes without error, the returned information executed at is absent, and the expected observable result is returned.

### `transactions.schedules/should-charge-hbars-with-limit-using-scheduled-transaction`

> **Implementation:** `ScheduleTransactionIntegrationTest.shouldChargeHbarsWithLimitUsingScheduledTransaction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleTransactionIntegrationTest.java:13`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to charge hbars with limit using scheduled transaction.
 - **Then** the operation completes without error, the schedule identifier is present, and the account balance HBAR balance value in tinybars is less than the expected value.

### `transactions.schedules/should-not-charge-hbars-with-lower-limit-using-scheduled-transaction`

> **Implementation:** `ScheduleTransactionIntegrationTest.shouldNotChargeHbarsWithLowerLimitUsingScheduledTransaction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleTransactionIntegrationTest.java:76`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to not charge hbars with lower limit using scheduled transaction.
 - **Then** the operation completes without error and the account balance HBAR balance value in tinybars is greater than the expected value.

### `transactions.schedules/should-not-charge-tokens-with-lower-limit-using-scheduled-transaction`

> **Implementation:** `ScheduleTransactionIntegrationTest.shouldNotChargeTokensWithLowerLimitUsingScheduledTransaction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ScheduleTransactionIntegrationTest.java:138`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to not charge tokens with lower limit using scheduled transaction.
 - **Then** the operation completes without error and the retrieved value equals the expected value.
