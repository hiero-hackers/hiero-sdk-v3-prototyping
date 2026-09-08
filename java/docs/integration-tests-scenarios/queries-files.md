# Queries / Files

## Scenarios

### `queries.files/can-fetch-fee-schedules`

> **Implementation:** `FeeSchedulesTest.canFetchFeeSchedules`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FeeSchedulesTest.java:14`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “fee Schedules (Current And Next Fee Schedule) is fetched and parsed from file 0.0.111” behavior.
 - **Then** the operation completes without error and the fee schedules current is present.

### `queries.files/can-query-file-contents`

> **Implementation:** `FileContentsIntegrationTest.canQueryFileContents`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileContentsIntegrationTest.java:20`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query file contents.
 - **Then** the operation completes without error and the contents to string utf8 equals the expected text.

### `queries.files/can-query-empty-file-contents`

> **Implementation:** `FileContentsIntegrationTest.canQueryEmptyFileContents`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileContentsIntegrationTest.java:43`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query empty file contents.
 - **Then** the operation completes without error and the contents size equals 0.

### `queries.files/cannot-query-file-contents-when-file-id-is-not-set`

> **Implementation:** `FileContentsIntegrationTest.cannotQueryFileContentsWhenFileIDIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileContentsIntegrationTest.java:64`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to query file contents when file identifier is not set.
 - **Then** the request is rejected with status INVALID_FILE_ID.

### `queries.files/get-cost-big-max-query-file-contents`

> **Implementation:** `FileContentsIntegrationTest.getCostBigMaxQueryFileContents`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileContentsIntegrationTest.java:77`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error and the contents to string utf8 equals the expected text.

### `queries.files/get-cost-small-max-query-file-contents`

> **Implementation:** `FileContentsIntegrationTest.getCostSmallMaxQueryFileContents`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileContentsIntegrationTest.java:102`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.files/get-cost-insufficient-tx-fee-query-file-contents`

> **Implementation:** `FileContentsIntegrationTest.getCostInsufficientTxFeeQueryFileContents`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileContentsIntegrationTest.java:127`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.files/can-query-file-info`

> **Implementation:** `FileInfoIntegrationTest.canQueryFileInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileInfoIntegrationTest.java:20`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to query file info.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `queries.files/can-query-file-info-with-no-admin-key-or-contents`

> **Implementation:** `FileInfoIntegrationTest.canQueryFileInfoWithNoAdminKeyOrContents`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileInfoIntegrationTest.java:47`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query file info with no admin key or contents.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 0, and the returned information is deleted is false.

### `queries.files/get-cost-big-max-query-file-info`

> **Implementation:** `FileInfoIntegrationTest.getCostBigMaxQueryFileInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileInfoIntegrationTest.java:65`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error.

### `queries.files/get-cost-small-max-query-file-info`

> **Implementation:** `FileInfoIntegrationTest.getCostSmallMaxQueryFileInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileInfoIntegrationTest.java:91`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.files/get-cost-insufficient-tx-fee-query-file-info`

> **Implementation:** `FileInfoIntegrationTest.getCostInsufficientTxFeeQueryFileInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileInfoIntegrationTest.java:116`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.
