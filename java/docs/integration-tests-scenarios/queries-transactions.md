# Queries / Transactions

## Scenarios

### `queries.transactions/token-create-transaction-fee-estimate`

> **Implementation:** `FeeEstimateQueryIntegrationTest.tokenCreateTransactionFeeEstimate`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:29`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “token Create Transaction Fee Estimate” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/transfer-transaction-state-mode-fee-estimate`

> **Implementation:** `FeeEstimateQueryIntegrationTest.transferTransactionStateModeFeeEstimate`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:60`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Transfer Transaction, when fee estimate is requested in STATE mode, then all components are returned” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/transfer-transaction-intrinsic-mode-fee-estimate`

> **Implementation:** `FeeEstimateQueryIntegrationTest.transferTransactionIntrinsicModeFeeEstimate`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:83`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Transfer Transaction, when fee estimate is requested in INTRINSIC mode, then components are returned without state dependencies” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/transfer-transaction-default-mode-is-intrinsic`

> **Implementation:** `FeeEstimateQueryIntegrationTest.transferTransactionDefaultModeIsIntrinsic`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:105`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a Transfer Transaction without explicit mode, when fee estimate is requested, then INTRINSIC mode is used by default” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/fee-estimate-query-with-high-volume-throttle`

> **Implementation:** `FeeEstimateQueryIntegrationTest.feeEstimateQueryWithHighVolumeThrottle`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:125`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Transfer Transaction with high volume throttle, when fee estimate is requested, then high volume multiplier is returned” behavior.
 - **Then** the operation completes without error and the returned response high volume multiplier is at least the expected value.

### `queries.transactions/token-mint-transaction-fee-estimate`

> **Implementation:** `FeeEstimateQueryIntegrationTest.tokenMintTransactionFeeEstimate`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:149`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Token Mint Transaction, when fee estimate is requested, then extras are returned for minting” behavior.
 - **Then** the operation completes without error and the returned response node extras is present.

### `queries.transactions/topic-create-transaction-fee-estimate`

> **Implementation:** `FeeEstimateQueryIntegrationTest.topicCreateTransactionFeeEstimate`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:171`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Topic Create Transaction, when fee estimate is requested, then service fees are included” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/contract-create-transaction-fee-estimate`

> **Implementation:** `FeeEstimateQueryIntegrationTest.contractCreateTransactionFeeEstimate`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:192`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Contract Create Transaction, when fee estimate is requested, then execution fees are returned” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/file-create-transaction-fee-estimate`

> **Implementation:** `FeeEstimateQueryIntegrationTest.fileCreateTransactionFeeEstimate`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:215`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a File Create Transaction, when fee estimate is requested, then storage fees are included” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/file-append-transaction-fee-estimate-aggregates-chunks`

> **Implementation:** `FeeEstimateQueryIntegrationTest.fileAppendTransactionFeeEstimateAggregatesChunks`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:237`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a File Append Transaction spanning multiple chunks, when fee estimate is requested, then aggregated totals are returned” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/topic-message-submit-single-chunk-fee-estimate`

> **Implementation:** `FeeEstimateQueryIntegrationTest.topicMessageSubmitSingleChunkFeeEstimate`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:259`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a Topic Message Submit Transaction smaller than a chunk, when fee estimate is requested, then a single chunk estimate is returned” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/topic-message-submit-multiple-chunk-fee-estimate`

> **Implementation:** `FeeEstimateQueryIntegrationTest.topicMessageSubmitMultipleChunkFeeEstimate`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:281`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a Topic Message Submit Transaction larger than a chunk, when fee estimate is requested, then multi-chunk totals are aggregated” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/malformed-transaction-returns-invalid-argument-error`

> **Implementation:** `FeeEstimateQueryIntegrationTest.malformedTransactionReturnsInvalidArgumentError`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:303`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a Fee Estimate Query with a malformed transaction, when the query is executed, then it returns an INVALID_ARGUMENT error and does not retry” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/query-without-transaction-throws-error`

> **Implementation:** `FeeEstimateQueryIntegrationTest.queryWithoutTransactionThrowsError`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:327`
> **Status:** Disabled — Temporarily disabled. Will be enabled with PR for refactoring

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a Fee Estimate Query without a transaction, when executed, then it throws an error” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/actual-fees-match-estimate-within-tolerance`

> **Implementation:** `FeeEstimateQueryIntegrationTest.actualFeesMatchEstimateWithinTolerance`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeEstimateQueryIntegrationTest.java:339`
> **Status:** Disabled — No reason recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a fee estimate is obtained, when transaction is executed, then actual fees are within reasonable range” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.transactions/can-get-transaction-receipt`

> **Implementation:** `ReceiptQueryIntegrationTest.canGetTransactionReceipt`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ReceiptQueryIntegrationTest.java:18`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to get Receipt.
 - **Then** the operation completes without error.

### `queries.transactions/can-get-transaction-record`

> **Implementation:** `ReceiptQueryIntegrationTest.canGetTransactionRecord`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ReceiptQueryIntegrationTest.java:33`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to get Record.
 - **Then** the operation completes without error.

### `queries.transactions/get-cost-transaction-record`

> **Implementation:** `ReceiptQueryIntegrationTest.getCostTransactionRecord`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ReceiptQueryIntegrationTest.java:52`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to get Record cost.
 - **Then** the operation completes without error.

### `queries.transactions/get-cost-big-max-transaction-record`

> **Implementation:** `ReceiptQueryIntegrationTest.getCostBigMaxTransactionRecord`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ReceiptQueryIntegrationTest.java:73`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to get Record cost with big max set.
 - **Then** the operation completes without error.

### `queries.transactions/get-cost-small-max-transaction-record`

> **Implementation:** `ReceiptQueryIntegrationTest.getCostSmallMaxTransactionRecord`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ReceiptQueryIntegrationTest.java:97`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “error at very small max, get Record” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.transactions/get-cost-insufficient-tx-fee-transaction-record`

> **Implementation:** `ReceiptQueryIntegrationTest.getCostInsufficientTxFeeTransactionRecord`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ReceiptQueryIntegrationTest.java:125`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “insufficient transaction fee error for transaction record query” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.transactions/next-exchange-rate-property-is-not-null-in-transaction-receipt`

> **Implementation:** `TransactionReceiptIntegrationTest.nextExchangeRatePropertyIsNotNullInTransactionReceipt`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TransactionReceiptIntegrationTest.java:12`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “next Exchange Rate property is not null in Transaction Receipt” behavior.
 - **Then** the operation completes without error and the next exchange rate is present.

### `queries.transactions/transaction-hash-in-transaction-record-is-equal-to-the-transaction-response-transaction-hash`

> **Implementation:** `TransactionResponseTest.transactionHashInTransactionRecordIsEqualToTheTransactionResponseTransactionHash`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TransactionResponseTest.java:12`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “transaction hash in transaction record is equal to the transaction response transaction hash” behavior.
 - **Then** the operation completes without error, the returned record transaction hash to byte array contains the expected value, and the account identifier is present.
