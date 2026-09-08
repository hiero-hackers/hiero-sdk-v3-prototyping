# Transactions / Schedules

## Scenarios

### `transactions.schedules/can-create-schedule`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create schedule.
 - **Then** the operation completes without error and the returned information executed at is present.

### `transactions.schedules/can-get-transaction-schedule`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to get Transaction.
 - **Then** the operation completes without error, the returned information executed at is present, and the returned information scheduled transaction is present.

### `transactions.schedules/can-create-with-schedule`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create schedule with schedule.
 - **Then** the operation completes without error, the returned information executed at is present, and the returned information scheduled transaction is present.

### `transactions.schedules/can-sign-schedule2`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign schedule.
 - **Then** the operation completes without error, the returned information executed at is absent, the returned information executed at is present, and the schedule identifier checksum is absent.

### `transactions.schedules/can-schedule-token-transfer`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to schedule token transfer.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.schedules/cannot-schedule-two-transactions`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to schedule two identical transactions.
 - **Then** the request is rejected with the expected error.

### `transactions.schedules/can-schedule-topic-message`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to schedule topic message.
 - **Then** the operation completes without error, the expected observable result is returned, the returned information is present, and the returned information schedule identifier equals the expected schedule identifier.

### `transactions.schedules/can-sign-schedule`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign schedule.
 - **Then** the operation completes without error, the returned information executed at is absent, the returned information executed at is present, and the schedule identifier checksum is absent.

### `transactions.schedules/cannot-schedule-transaction-one-year-into-the-future`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to schedule one year into the future.
 - **Then** the request is rejected with status SCHEDULE_EXPIRATION_TIME_TOO_FAR_IN_FUTURE.

### `transactions.schedules/cannot-schedule-transaction-in-the-past`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to schedule in the past.
 - **Then** the request is rejected with status SCHEDULE_EXPIRATION_TIME_MUST_BE_HIGHER_THAN_CONSENSUS_TIME.

### `transactions.schedules/can-sign-schedule-and-wait-for-expiry`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign schedule and wait for expiry.
 - **Then** the operation completes without error, the returned information executed at is absent, the schedule identifier checksum is absent, and the schedule identifier hash code has the expected value.

### `transactions.schedules/can-sign-with-multi-sig-and-update-signing-requirements`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign with multisig and update signing requirements.
 - **Then** the operation completes without error, the returned information executed at is absent, and the returned information executed at is present.

### `transactions.schedules/can-sign-with-multi-sig`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to sign with multisig.
 - **Then** the operation completes without error, the returned information executed at is absent, and the returned information executed at is present.

### `transactions.schedules/can-execute-with-short-expiration-time`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute with short expiration time.
 - **Then** the operation completes without error, the returned information executed at is absent, and the expected observable result is returned.

### `transactions.schedules/should-charge-hbars-with-limit-using-scheduled-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to charge hbars with limit using scheduled transaction.
 - **Then** the operation completes without error, the schedule identifier is present, and the account balance HBAR balance value in tinybars is less than the expected value.

### `transactions.schedules/should-not-charge-hbars-with-lower-limit-using-scheduled-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to not charge hbars with lower limit using scheduled transaction.
 - **Then** the operation completes without error and the account balance HBAR balance value in tinybars is greater than the expected value.

### `transactions.schedules/should-not-charge-tokens-with-lower-limit-using-scheduled-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to not charge tokens with lower limit using scheduled transaction.
 - **Then** the operation completes without error and the retrieved value equals the expected value.
