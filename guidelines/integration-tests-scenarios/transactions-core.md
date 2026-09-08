# Transactions / Core

## Scenarios

### `transactions.core/can-create-batch-transaction`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create batch transaction.
 - **Then** the operation completes without error and the account identifier inner transaction equals the expected execute account identifier.

### `transactions.core/can-execute-from-to-bytes`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute from/to Bytes.
 - **Then** the operation completes without error and the account identifier inner transaction equals the expected execute account identifier.

### `transactions.core/can-execute-large-batch-transaction-up-to-maximum-request-size`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute a large batch transaction up to maximum request size.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.core/batch-transaction-without-inner-transactions-should-throw-an-error`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “batch Transaction with empty inner transaction's list should throw an error” behavior.
 - **Then** the request is rejected with status BATCH_LIST_EMPTY.

### `transactions.core/batch-transaction-with-blacklisted-inner-transaction-should-throw-an-error`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “blacklisted inner transaction should throw an error” behavior.
 - **Then** the request is rejected with the expected error.

### `transactions.core/batch-transaction-with-invalid-batch-key-inside-inner-transaction-should-throw-an-error`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “invalid batch key set to inner transaction should throw an error” behavior.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.core/chunked-inner-transactions-should-be-executed-successfully`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “chunked inner transactions should be executed successfully” behavior.
 - **Then** the operation completes without error and the returned information sequence number equals 1.

### `transactions.core/can-execute-with-different-batch-keys`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute with different batch keys.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the account1 is present, the account2 is present, and the account3 is present.

### `transactions.core/successful-inner-transactions-should-incur-fees-even-though-one-failed`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “successful inner transaction should incur fees even though one failed” behavior.
 - **Then** the request is rejected with status INNER_TRANSACTION_FAILED.

### `transactions.core/transaction-should-fail-when-batchified`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “transaction should fail when batchified but not part of a batch” behavior.
 - **Then** the request is rejected with the expected error.

### `transactions.core/transaction-hash-in-transaction-record-is-equal-to-the-derived-transaction-hash`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “transaction hash in transaction record is equal to the derived transaction hash” behavior.
 - **Then** the operation completes without error, the expected observable result is returned, and the account identifier is present.

### `transactions.core/can-serialize-deserialize-compare-fields`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “incomplete transaction can be serialized into bytes, deserialized and be equal to the original one” behavior.
 - **Then** the request is rejected with the expected error.

### `transactions.core/can-serialize-with-node-account-ids-deserialize-compare-fields`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “incomplete transaction with node account ids can be serialized into bytes, deserialized and be equal to the original one” behavior.
 - **Then** the request is rejected with the expected error.

### `transactions.core/can-serialize-deserialize-and-execute-incomplete-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “incomplete transaction can be serialized into bytes, deserialized and executed” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.core/can-serialize-deserialize-and-execute-incomplete-transaction-with-node-account-ids`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “incomplete transaction with node account ids can be serialized into bytes, deserialized and executed” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.core/can-serialize-deserialize-edit-execute-compare-fields`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “incomplete transaction can be serialized into bytes, deserialized, edited and executed” behavior.
 - **Then** the operation completes without error and the expected balance equals the expected account create transaction deserialized initial balance.

### `transactions.core/can-serialize-deserialize-edit-execute-compare-fields-incomplete-transaction-with-node-account-ids`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “incomplete transaction with node account ids can be serialized into bytes, deserialized, edited and executed” behavior.
 - **Then** the operation completes without error and the expected balance equals the expected account create transaction deserialized initial balance.

### `transactions.core/can-freeze-sign-serialize-deserialize-reserialize-and-execute`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “complete frozen and signed transaction can be serialized into bytes, deserialized (x2) and executed” behavior.
 - **Then** the operation completes without error and the transaction bytes serialized equals the expected transaction bytes reserialized.

### `transactions.core/can-freeze-serialize-deserialize-add-signature-and-execute`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “complete frozen transaction can be serialized into bytes, deserialized, signature added and executed” behavior.
 - **Then** the operation completes without error, the expected observable result is returned, and the account identifier is present.

### `transactions.core/can-freeze-sign-serialize-deserialize-and-compare-file-append-chunked-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “file append chunked transaction can be frozen, signed, serialized into bytes, deserialized and be equal to the original one” behavior.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.core/can-serialize-deserialize-execute-file-append-chunked-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “incomplete file append chunked transaction can be serialized into bytes, deserialized, edited and executed” behavior.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.core/can-serialize-deserialize-execute-incomplete-file-append-chunked-transaction-with-node-account-ids`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “incomplete file append chunked transaction with node account ids can be serialized into bytes, deserialized, edited and executed” behavior.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.core/can-freeze-sign-serialize-deserialize-and-compare-topic-message-submit-chunked-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “topic message submit chunked transaction can be frozen, signed, serialized into bytes, deserialized and be equal to the original one” behavior.
 - **Then** the operation completes without error, the returned information topic identifier equals the expected topic identifier, the returned information topic memo equals the expected text, and the returned information sequence number equals 0.

### `transactions.core/can-serialize-deserialize-execute-incomplete-topic-message-submit-chunked-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “incomplete topic message submit chunked transaction can be serialized into bytes, deserialized, edited and executed” behavior.
 - **Then** the operation completes without error, the returned information topic identifier equals the expected topic identifier, the returned information topic memo equals the expected text, and the returned information sequence number equals 0.

### `transactions.core/can-serialize-deserialize-execute-incomplete-topic-message-submit-chunked-transaction-with-node-account-ids`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “incomplete topic message submit chunked transaction with node account ids can be serialized into bytes, deserialized, edited and executed” behavior.
 - **Then** the operation completes without error, the returned information topic identifier equals the expected topic identifier, the returned information topic memo equals the expected text, and the returned information sequence number equals 0.

### `transactions.core/transaction-from-to-bytes2`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “transaction can be serialized into bytes, deserialized, signature added and executed” behavior.
 - **Then** the request is rejected with the expected error.

### `transactions.core/can-add-signature-to-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “add Signature - can get signable body bytes, sign externally, and add signatures back” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the signable body list is not empty, and the delete receipt status equals the expected status success.

### `transactions.core/can-remove-signature-from-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “remove Signature - can remove a signature, add it back, and execute” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the expected observable result is returned, the removed signatures is not empty, and the retrieved value equals the expected value.

### `transactions.core/can-remove-all-signatures-from-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “remove All Signatures - can clear all signatures, re-sign, and execute” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the removed signatures contains the expected value, the retrieved value equals the expected value, and the delete transaction signatures is empty.
