# Queries / Contracts

## Scenarios

### `queries.contracts/can-query-contract-bytecode`

> **Implementation:** `ContractBytecodeIntegrationTest.canQueryContractBytecode`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractBytecodeIntegrationTest.java:25`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query contract bytecode.
 - **Then** the operation completes without error and the bytecode size equals 798.

### `queries.contracts/get-cost-big-max-query-contract-bytecode`

> **Implementation:** `ContractBytecodeIntegrationTest.getCostBigMaxQueryContractBytecode`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractBytecodeIntegrationTest.java:64`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error and the bytecode size equals 798.

### `queries.contracts/get-cost-small-max-query-contract-bytecode`

> **Implementation:** `ContractBytecodeIntegrationTest.getCostSmallMaxQueryContractBytecode`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractBytecodeIntegrationTest.java:108`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/get-cost-insufficient-tx-fee-query-contract-bytecode`

> **Implementation:** `ContractBytecodeIntegrationTest.getCostInsufficientTxFeeQueryContractBytecode`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractBytecodeIntegrationTest.java:150`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/cannot-query-contract-bytecode-when-contract-id-is-not-set`

> **Implementation:** `ContractBytecodeIntegrationTest.cannotQueryContractBytecodeWhenContractIDIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractBytecodeIntegrationTest.java:195`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to query contract bytecode when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `queries.contracts/can-call-contract-function`

> **Implementation:** `ContractCallIntegrationTest.canCallContractFunction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCallIntegrationTest.java:26`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to call contract function.
 - **Then** the operation completes without error and the returned result get string(0) equals the expected text.

### `queries.contracts/cannot-call-contract-function-when-contract-function-is-not-set`

> **Implementation:** `ContractCallIntegrationTest.cannotCallContractFunctionWhenContractFunctionIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCallIntegrationTest.java:79`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to call contract function when contract function is not set.
 - **Then** the request is rejected with status CONTRACT_REVERT_EXECUTED.

### `queries.contracts/cannot-call-contract-function-when-gas-is-not-set`

> **Implementation:** `ContractCallIntegrationTest.cannotCallContractFunctionWhenGasIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCallIntegrationTest.java:123`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to call contract function when gas is not set.
 - **Then** the request is rejected with status INSUFFICIENT_GAS.

### `queries.contracts/cannot-call-contract-function-when-contract-id-is-not-set`

> **Implementation:** `ContractCallIntegrationTest.cannotCallContractFunctionWhenContractIDIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCallIntegrationTest.java:167`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to call contract function when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `queries.contracts/get-cost-big-max-contract-call-function`

> **Implementation:** `ContractCallIntegrationTest.getCostBigMaxContractCallFunction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCallIntegrationTest.java:211`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error and the returned result get string(0) equals the expected text.

### `queries.contracts/get-cost-small-max-contract-call-function`

> **Implementation:** `ContractCallIntegrationTest.getCostSmallMaxContractCallFunction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCallIntegrationTest.java:264`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/get-cost-insufficient-tx-fee-contract-call-function`

> **Implementation:** `ContractCallIntegrationTest.getCostInsufficientTxFeeContractCallFunction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCallIntegrationTest.java:317`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/can-call-contract-function-uint8-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint8Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:69`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint8 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint8(0) equals the expected 0x0.

### `queries.contracts/can-call-contract-function-uint8-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint8Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:87`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint8 max value from contract call.
 - **Then** the operation completes without error and the uint8 max from response equals the expected uint8 max.

### `queries.contracts/can-call-contract-function-uint8-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint8Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:110`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint8 array value from contract call.
 - **Then** the operation completes without error, the response result 0 equals the expected uint8 min byte, and the response result 1 equals the expected uint8 max.

### `queries.contracts/can-call-contract-function-uint16-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint16Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:136`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint16 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint32(0) equals 0.

### `queries.contracts/can-call-contract-function-uint16-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint16Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:149`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint16 max value from contract call.
 - **Then** the operation completes without error and the uint16 max int from response equals the expected uint16 max.

### `queries.contracts/can-call-contract-function-uint16-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint16Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:172`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint16 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint16 array.

### `queries.contracts/can-call-contract-function-uint24-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint24Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:197`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint24 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint32(0) equals 0.

### `queries.contracts/can-call-contract-function-uint24-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint24Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:215`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint24 max value from contract call.
 - **Then** the operation completes without error and the uint24 max int from response equals the expected uint24 max.

### `queries.contracts/can-call-contract-function-uint24-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint24Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:238`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint24 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint24 array.

### `queries.contracts/can-call-contract-function-uint32-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint32Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:263`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint32 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint32(0) equals 0.

### `queries.contracts/can-call-contract-function-uint32-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint32Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:276`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint32 max value from contract call.
 - **Then** the operation completes without error and the uint32 max int from response equals the expected uint32 max.

### `queries.contracts/can-call-contract-function-uint32-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint32Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:294`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint32 array value from contract call.
 - **Then** the operation completes without error, the response result 0 equals the expected uint32 min int, and the response result 1 equals the expected long parse unsigned long(uint32 max).

### `queries.contracts/can-call-contract-function-uint40-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint40Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:315`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint40 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals 0.

### `queries.contracts/can-call-contract-function-uint40-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint40Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:328`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint40 max value from contract call.
 - **Then** the operation completes without error and the uint64 max long from response equals the expected uint40 max.

### `queries.contracts/can-call-contract-function-uint40-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint40Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:346`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint40 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint40 array.

### `queries.contracts/can-call-contract-function-uint48-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint48Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:366`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint48 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals 0.

### `queries.contracts/can-call-contract-function-uint48-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint48Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:379`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint48 max value from contract call.
 - **Then** the operation completes without error and the uint64 max long from response equals the expected uint48 max.

### `queries.contracts/can-call-contract-function-uint48-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint48Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:397`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint48 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint48 array.

### `queries.contracts/can-call-contract-function-uint56-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint56Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:417`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint56 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals 0.

### `queries.contracts/can-call-contract-function-uint56-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint56Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:430`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint56 max value from contract call.
 - **Then** the operation completes without error and the uint64 max long from response equals the expected uint56 max.

### `queries.contracts/can-call-contract-function-uint56-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint56Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:448`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint56 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint56 array.

### `queries.contracts/can-call-contract-function-uint64-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint64Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:468`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint64 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals 0.

### `queries.contracts/can-call-contract-function-uint64-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint64Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:481`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint64 max value from contract call.
 - **Then** the operation completes without error and the uint64 max long from response equals the expected uint64 max.

### `queries.contracts/can-call-contract-function-uint64-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint64Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:499`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint64 array value from contract call.
 - **Then** the operation completes without error, the response result 0 equals the expected uint64 min long, and the response result 1 equals the expected long parse unsigned long(uint64 max).

### `queries.contracts/can-call-contract-function-uint72-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint72Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:520`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint72 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint72-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint72Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:533`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint72 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint72 max.

### `queries.contracts/can-call-contract-function-uint72-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint72Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:548`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint72 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint72 array.

### `queries.contracts/can-call-contract-function-uint80-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint80Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:567`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint80 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint80-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint80Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:580`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint80 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint80 max.

### `queries.contracts/can-call-contract-function-uint80-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint80Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:595`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint80 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint80 array.

### `queries.contracts/can-call-contract-function-uint88-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint88Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:614`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint88 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint88-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint88Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:627`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint88 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint88 max.

### `queries.contracts/can-call-contract-function-uint88-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint88Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:642`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint88 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint88 array.

### `queries.contracts/can-call-contract-function-uint96-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint96Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:661`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint96 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint96-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint96Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:674`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint96 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint96 max.

### `queries.contracts/can-call-contract-function-uint96-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint96Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:689`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint96 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint96 array.

### `queries.contracts/can-call-contract-function-uint104-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint104Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:708`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint104 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint104-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint104Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:721`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint104 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint104 max.

### `queries.contracts/can-call-contract-function-uint104-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint104Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:736`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint104 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint104 array.

### `queries.contracts/can-call-contract-function-uint112-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint112Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:755`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint112 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint112-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint112Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:768`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint112 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint112 max.

### `queries.contracts/can-call-contract-function-uint112-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint112Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:783`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint112 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint112 array.

### `queries.contracts/can-call-contract-function-uint120-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint120Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:802`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint120 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint120-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint120Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:815`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint120 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint120 max.

### `queries.contracts/can-call-contract-function-uint120-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint120Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:830`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint120 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint120 array.

### `queries.contracts/can-call-contract-function-uint128-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint128Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:849`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint128 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint128-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint128Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:862`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint128 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint128 max.

### `queries.contracts/can-call-contract-function-uint128-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint128Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:877`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint128 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint128 array.

### `queries.contracts/can-call-contract-function-uint136-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint136Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:896`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint136 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint136-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint136Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:909`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint136 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint136 max.

### `queries.contracts/can-call-contract-function-uint136-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint136Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:924`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint136 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint136 array.

### `queries.contracts/can-call-contract-function-uint144-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint144Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:943`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint144 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint144-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint144Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:956`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint144 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint144 max.

### `queries.contracts/can-call-contract-function-uint144-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint144Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:971`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint144 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint144 array.

### `queries.contracts/can-call-contract-function-uint152-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint152Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:990`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint152 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint152-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint152Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1003`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint152 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint152 max.

### `queries.contracts/can-call-contract-function-uint152-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint152Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1018`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint152 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint152 array.

### `queries.contracts/can-call-contract-function-uint160-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint160Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1037`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint160 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint160-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint160Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1050`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint160 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint160 max.

### `queries.contracts/can-call-contract-function-uint160-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint160Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1065`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint160 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint160 array.

### `queries.contracts/can-call-contract-function-uint168-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint168Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1084`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint168 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint168-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint168Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1097`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint168 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint168 max.

### `queries.contracts/can-call-contract-function-uint168-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint168Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1112`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint168 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint168 array.

### `queries.contracts/can-call-contract-function-uint176-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint176Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1131`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint176 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint176-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint176Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1144`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint176 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint176 max.

### `queries.contracts/can-call-contract-function-uint176-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint176Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1159`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint176 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint176 array.

### `queries.contracts/can-call-contract-function-uint184-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint184Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1178`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint184 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint184-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint184Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1191`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint184 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint184 max.

### `queries.contracts/can-call-contract-function-uint184-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint184Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1206`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint184 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint184 array.

### `queries.contracts/can-call-contract-function-uint192-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint192Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1225`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint192 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint192-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint192Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1238`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint192 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint192 max.

### `queries.contracts/can-call-contract-function-uint192-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint192Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1253`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint192 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint192 array.

### `queries.contracts/can-call-contract-function-uint200-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint200Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1272`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint200 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint200-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint200Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1285`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint200 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint200 max.

### `queries.contracts/can-call-contract-function-uint200-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint200Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1300`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint200 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint200 array.

### `queries.contracts/can-call-contract-function-uint208-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint208Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1319`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint208 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint208-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint208Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1332`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint208 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint208 max.

### `queries.contracts/can-call-contract-function-uint208-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint208Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1347`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint208 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint208 array.

### `queries.contracts/can-call-contract-function-uint216-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint216Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1366`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint216 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint216-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint216Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1379`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint216 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint216 max.

### `queries.contracts/can-call-contract-function-uint216-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint216Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1394`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint216 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint216 array.

### `queries.contracts/can-call-contract-function-uint224-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint224Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1413`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint224 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint224-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint224Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1426`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint224 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint224 max.

### `queries.contracts/can-call-contract-function-uint224-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint224Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1441`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint224 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint224 array.

### `queries.contracts/can-call-contract-function-uint232-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint232Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1460`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint232 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint232-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint232Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1473`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint232 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint232 max.

### `queries.contracts/can-call-contract-function-uint232-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint232Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1489`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint232 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint232 array.

### `queries.contracts/can-call-contract-function-uint240-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint240Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1509`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint240 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint240-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint240Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1522`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint240 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint240 max.

### `queries.contracts/can-call-contract-function-uint240-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint240Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1538`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint240 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint240 array.

### `queries.contracts/can-call-contract-function-uint248-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint248Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1558`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint248 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint248-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint248Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1571`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint248 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint248 max.

### `queries.contracts/can-call-contract-function-uint248-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint248Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1587`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint248 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint248 array.

### `queries.contracts/can-call-contract-function-uint256-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint256Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1607`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint256 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint256-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint256Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1620`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint256 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint256 max.

### `queries.contracts/can-call-contract-function-uint256-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionUint256Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1635`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint256 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint256 array.

### `queries.contracts/can-call-contract-function-int8-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt8Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1654`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int8 min value from contract call.
 - **Then** the operation completes without error and the returned response get int8(0) equals the expected byte min value.

### `queries.contracts/can-call-contract-function-int8-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt8Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1667`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int8 max value from contract call.
 - **Then** the operation completes without error and the returned response get int8(0) equals the expected byte max value.

### `queries.contracts/can-call-contract-function-int8-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt8Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1680`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int8 array value from contract call.
 - **Then** the operation completes without error, the response result 0 equals the expected int8 array 0, and the response result 1 equals the expected int8 array 1.

### `queries.contracts/can-call-contract-function-int16-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt16Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1698`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int16 min value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected int16 min.

### `queries.contracts/can-call-contract-function-int16-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt16Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1713`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int16 max value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected int16 max.

### `queries.contracts/can-call-contract-function-int16-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt16Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1728`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int16 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int16 array.

### `queries.contracts/can-call-contract-function-int24-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt24Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1747`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int24 min value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected int24 min.

### `queries.contracts/can-call-contract-function-int24-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt24Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1762`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int24 max value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected int24 max.

### `queries.contracts/can-call-contract-function-int24-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt24Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1777`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int24 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int24 array.

### `queries.contracts/can-call-contract-function-int32-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt32Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1796`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int32 min value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected integer min value.

### `queries.contracts/can-call-contract-function-int32-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt32Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1809`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int32 max value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected integer max value.

### `queries.contracts/can-call-contract-function-int32-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt32Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1822`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int32 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int32 array.

### `queries.contracts/can-call-contract-function-int40-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt40Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1841`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int40 min value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int40 min.

### `queries.contracts/can-call-contract-function-int40-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt40Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1856`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int40 max value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int40 max.

### `queries.contracts/can-call-contract-function-int40-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt40Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1871`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int40 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int40 array.

### `queries.contracts/can-call-contract-function-int48-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt48Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1890`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int48 min value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int48 min.

### `queries.contracts/can-call-contract-function-int48-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt48Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1905`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int48 max value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int48 max.

### `queries.contracts/can-call-contract-function-int48-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt48Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1920`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int48 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int48 array.

### `queries.contracts/can-call-contract-function-int56-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt56Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1939`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int56 min value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int56 min.

### `queries.contracts/can-call-contract-function-int56-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt56Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1954`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int56 max value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int56 max.

### `queries.contracts/can-call-contract-function-int56-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt56Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1969`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int56 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int56 array.

### `queries.contracts/can-call-contract-function-int64-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt64Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:1988`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int64 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals the expected long min value.

### `queries.contracts/can-call-contract-function-int64-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt64Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2001`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int64 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals the expected long max value.

### `queries.contracts/can-call-contract-function-int64-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt64Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2014`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int64 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int64 array.

### `queries.contracts/can-call-contract-function-int72-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt72Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2033`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int72 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int72 min.

### `queries.contracts/can-call-contract-function-int72-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt72Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2048`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int72 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int72 max.

### `queries.contracts/can-call-contract-function-int72-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt72Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2063`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int72 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int72 array.

### `queries.contracts/can-call-contract-function-int80-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt80Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2082`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int80 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int80 min.

### `queries.contracts/can-call-contract-function-int80-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt80Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2097`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int80 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int80 max.

### `queries.contracts/can-call-contract-function-int80-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt80Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2112`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int80 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int80 array.

### `queries.contracts/can-call-contract-function-int88-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt88Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2131`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int88 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int88 min.

### `queries.contracts/can-call-contract-function-int88-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt88Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2146`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int88 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int88 max.

### `queries.contracts/can-call-contract-function-int88-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt88Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2161`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int88 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int88 array.

### `queries.contracts/can-call-contract-function-int96-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt96Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2180`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int96 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int96 min.

### `queries.contracts/can-call-contract-function-int96-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt96Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2195`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int96 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int96 max.

### `queries.contracts/can-call-contract-function-int96-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt96Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2210`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int96 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int96 array.

### `queries.contracts/can-call-contract-function-int104-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt104Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2229`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int104 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int104 min.

### `queries.contracts/can-call-contract-function-int104-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt104Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2244`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int104 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int104 max.

### `queries.contracts/can-call-contract-function-int104-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt104Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2259`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int104 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int104 array.

### `queries.contracts/can-call-contract-function-int112-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt112Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2278`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int112 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int112 min.

### `queries.contracts/can-call-contract-function-int112-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt112Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2293`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int112 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int112 max.

### `queries.contracts/can-call-contract-function-int112-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt112Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2308`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int112 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int112 array.

### `queries.contracts/can-call-contract-function-int120-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt120Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2327`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int120 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int120 min.

### `queries.contracts/can-call-contract-function-int120-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt120Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2342`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int120 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int120 max.

### `queries.contracts/can-call-contract-function-int120-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt120Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2357`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int120 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int120 array.

### `queries.contracts/can-call-contract-function-int128-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt128Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2376`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int128 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int128 min.

### `queries.contracts/can-call-contract-function-int128-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt128Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2391`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int128 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int128 max.

### `queries.contracts/can-call-contract-function-int128-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt128Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2406`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int128 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int128 array.

### `queries.contracts/can-call-contract-function-int136-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt136Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2425`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int136 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int136 min.

### `queries.contracts/can-call-contract-function-int136-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt136Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2440`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int136 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int136 max.

### `queries.contracts/can-call-contract-function-int136-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt136Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2455`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int136 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int136 array.

### `queries.contracts/can-call-contract-function-int144-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt144Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2474`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int144 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int144 min.

### `queries.contracts/can-call-contract-function-int144-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt144Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2489`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int144 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int144 max.

### `queries.contracts/can-call-contract-function-int144-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt144Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2504`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int144 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int144 array.

### `queries.contracts/can-call-contract-function-int152-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt152Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2523`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int152 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int152 min.

### `queries.contracts/can-call-contract-function-int152-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt152Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2538`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int152 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int152 max.

### `queries.contracts/can-call-contract-function-int152-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt152Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2553`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int152 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int152 array.

### `queries.contracts/can-call-contract-function-int160-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt160Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2572`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int160 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int160 min.

### `queries.contracts/can-call-contract-function-int160-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt160Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2587`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int160 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int160 max.

### `queries.contracts/can-call-contract-function-int160-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt160Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2602`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int160 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int160 array.

### `queries.contracts/can-call-contract-function-int168-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt168Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2621`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int168 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int168 min.

### `queries.contracts/can-call-contract-function-int168-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt168Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2636`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int168 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int168 max.

### `queries.contracts/can-call-contract-function-int168-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt168Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2651`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int168 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int168 array.

### `queries.contracts/can-call-contract-function-int176-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt176Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2670`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int176 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int176 min.

### `queries.contracts/can-call-contract-function-int176-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt176Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2685`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int176 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int176 max.

### `queries.contracts/can-call-contract-function-int176-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt176Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2700`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int176 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int176 array.

### `queries.contracts/can-call-contract-function-int184-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt184Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2719`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int184 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int184 min.

### `queries.contracts/can-call-contract-function-int184-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt184Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2734`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int184 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int184 max.

### `queries.contracts/can-call-contract-function-int184-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt184Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2749`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int184 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int184 array.

### `queries.contracts/can-call-contract-function-int192-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt192Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2768`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int192 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int192 min.

### `queries.contracts/can-call-contract-function-int192-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt192Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2783`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int192 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int192 max.

### `queries.contracts/can-call-contract-function-int192-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt192Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2798`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int192 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int192 array.

### `queries.contracts/can-call-contract-function-int200-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt200Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2817`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int200 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int200 min.

### `queries.contracts/can-call-contract-function-int200-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt200Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2832`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int200 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int200 max.

### `queries.contracts/can-call-contract-function-int200-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt200Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2847`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int200 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int200 array.

### `queries.contracts/can-call-contract-function-int208-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt208Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2866`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int208 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int208 min.

### `queries.contracts/can-call-contract-function-int208-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt208Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2881`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int208 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int208 max.

### `queries.contracts/can-call-contract-function-int208-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt208Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2896`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int208 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int208 array.

### `queries.contracts/can-call-contract-function-int216-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt216Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2915`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int216 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int216 min.

### `queries.contracts/can-call-contract-function-int216-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt216Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2930`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int216 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int216 max.

### `queries.contracts/can-call-contract-function-int216-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt216Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2945`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int216 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int216 array.

### `queries.contracts/can-call-contract-function-int224-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt224Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2964`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int224 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int224 min.

### `queries.contracts/can-call-contract-function-int224-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt224Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2979`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int224 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int224 max.

### `queries.contracts/can-call-contract-function-int224-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt224Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:2994`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int224 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int224 array.

### `queries.contracts/can-call-contract-function-int232-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt232Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3013`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int232 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int232 min.

### `queries.contracts/can-call-contract-function-int232-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt232Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3029`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int232 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int232 max.

### `queries.contracts/can-call-contract-function-int232-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt232Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3044`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int232 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int232 array.

### `queries.contracts/can-call-contract-function-int240-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt240Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3064`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int240 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int240 min.

### `queries.contracts/can-call-contract-function-int240-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt240Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3080`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int240 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int240 max.

### `queries.contracts/can-call-contract-function-int240-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt240Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3096`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int240 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int240 array.

### `queries.contracts/can-call-contract-function-int248-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt248Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3117`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int248 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int248 min.

### `queries.contracts/can-call-contract-function-int248-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt248Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3133`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int248 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int248 max.

### `queries.contracts/can-call-contract-function-int248-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt248Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3149`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int248 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int248 array.

### `queries.contracts/can-call-contract-function-int256-min`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt256Min`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3170`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int256 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int256 min.

### `queries.contracts/can-call-contract-function-int256-max`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt256Max`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3186`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int256 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int256 max.

### `queries.contracts/can-call-contract-function-int256-array`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionInt256Array`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3202`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int256 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int256 array.

### `queries.contracts/can-call-contract-function-multiple-int8`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionMultipleInt8`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3223`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive multiple int8 values from contract call.
 - **Then** the operation completes without error, the returned response get int8(0) equals the expected byte min value, and the returned response get int8(1) equals the expected -108.

### `queries.contracts/can-call-contract-function-multiple-int40`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionMultipleInt40`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3237`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive multiple int40 values from contract call.
 - **Then** the operation completes without error, the returned response get int64(0) equals the expected int40, and the returned response get int64(1) equals the expected int40 + 1.

### `queries.contracts/can-call-contract-function-multiple-int256`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionMultipleInt256`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3253`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive multiple int256 values from contract call.
 - **Then** the operation completes without error, the returned response get int256(0) equals the expected int256 min, and the retrieved value equals the expected value.

### `queries.contracts/can-call-contract-function-multiple-types`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionMultipleTypes`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3270`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive multiple types of values from contract call.
 - **Then** the operation completes without error, the integer to unsigned string(returned response get uint32(0)) equals the expected uint32 max, the returned response get uint64(1) equals the expected long parse unsigned long(uint32 max) - 1, and the returned response get string(2) equals the expected text.

### `queries.contracts/can-call-contract-function-string-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionStringType`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3288`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive string value from contract call.
 - **Then** the operation completes without error and the returned response get string(0) equals the expected test string.

### `queries.contracts/can-call-contract-function-string-array-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionStringArrayType`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3303`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive string array value from contract call.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `queries.contracts/can-call-contract-function-string-array-type-get-result`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionStringArrayType_getResult`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3319`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive string array value from contract call with get Result function.
 - **Then** the operation completes without error and the response result equals the expected test string array.

### `queries.contracts/can-call-contract-function-address-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionAddressType`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3335`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive address value from contract call.
 - **Then** the operation completes without error and the returned response get address(0) equals the expected test address.

### `queries.contracts/can-call-contract-function-address-array-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionAddressArrayType`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3350`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive address array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected test address array address.

### `queries.contracts/can-call-contract-function-boolean-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionBooleanType`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3373`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive boolean value from contract call.
 - **Then** the operation completes without error and the returned response get bool(0) equals the expected test boolean.

### `queries.contracts/can-call-contract-function-boolean-array-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionBooleanArrayType`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3388`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive boolean array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected test boolean array.

### `queries.contracts/can-call-contract-function-bytes-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionBytesType`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3405`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive bytes value from contract call.
 - **Then** the operation completes without error and the returned response get bytes(0) equals the expected test bytes.

### `queries.contracts/can-call-contract-function-bytes-array-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionBytesArrayType`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3420`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive bytes array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected test bytes.

### `queries.contracts/can-call-contract-function-bytes32-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionBytes32Type`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3437`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive bytes32 value from contract call.
 - **Then** the operation completes without error and the returned response get bytes32(0) equals the expected test bytes len32.

### `queries.contracts/can-call-contract-function-bytes32-array-type`

> **Implementation:** `ContractFunctionParametersIntegrationTest.canCallContractFunctionBytes32ArrayType`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractFunctionParametersIntegrationTest.java:3454`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive bytes32 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected test bytes len32.

### `queries.contracts/can-populate-contract-id-num-sync`

> **Implementation:** `ContractIdPopulationIntegrationTest.canPopulateContractIdNumSync`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractIdPopulationIntegrationTest.java:16`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to populate Contract identifier num from mirror node (using synchronous operation).
 - **Then** the operation completes without error and the contract identifier num equals the expected new contract identifier num.

### `queries.contracts/can-populate-contract-id-num-async`

> **Implementation:** `ContractIdPopulationIntegrationTest.canPopulateContractIdNumAsync`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractIdPopulationIntegrationTest.java:54`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to populate Contract identifier num from mirror node (using asynchronous operation).
 - **Then** the operation completes without error and the contract identifier num equals the expected new contract identifier num.

### `queries.contracts/can-query-contract-info`

> **Implementation:** `ContractInfoIntegrationTest.canQueryContractInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractInfoIntegrationTest.java:15`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query contract info.
 - **Then** the operation completes without error, the contract identifier hash code is greater than the expected value, the expected observable result is returned, and the returned information contract identifier equals the expected contract identifier.

### `queries.contracts/can-query-contract-info-when-admin-key-is-null`

> **Implementation:** `ContractInfoIntegrationTest.canQueryContractInfoWhenAdminKeyIsNull`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractInfoIntegrationTest.java:65`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query contract info when admin key is null.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text equals the expected contract identifier text.

### `queries.contracts/cannot-query-contract-info-when-contract-id-is-not-set`

> **Implementation:** `ContractInfoIntegrationTest.cannotQueryContractInfoWhenContractIDIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractInfoIntegrationTest.java:99`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to query contract info when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `queries.contracts/get-cost-big-max-contract-info-function`

> **Implementation:** `ContractInfoIntegrationTest.getCostBigMaxContractInfoFunction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractInfoIntegrationTest.java:112`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error.

### `queries.contracts/get-cost-small-max-contract-info-function`

> **Implementation:** `ContractInfoIntegrationTest.getCostSmallMaxContractInfoFunction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractInfoIntegrationTest.java:154`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/get-cost-insufficient-tx-fee-contract-info-function`

> **Implementation:** `ContractInfoIntegrationTest.getCostInsufficientTxFeeContractInfoFunction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractInfoIntegrationTest.java:195`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/can-increment-nonce-through-contract-constructor`

> **Implementation:** `ContractNonceInfoIntegrationTest.canIncrementNonceThroughContractConstructor`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractNonceInfoIntegrationTest.java:19`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “contract Create of A nonce, which deploys contract B in CONSTRUCTOR” behavior.
 - **Then** the operation completes without error, the contract a nonce info nonce equals 2, the contract b nonce info nonce equals 1, and the contract function result signer nonce equals 0.

### `queries.contracts/can-simulate-transaction`

> **Implementation:** `MirrorNodeContractQueryIntegrationTest.canSimulateTransaction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/MirrorNodeContractQueryIntegrationTest.java:29`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to estimate and simulate transaction.
 - **Then** the operation completes without error and the returned result get address(0) equals the expected simulation result substring(26).

### `queries.contracts/returns-default-values-when-contract-is-not-deployed`

> **Implementation:** `MirrorNodeContractQueryIntegrationTest.returnsDefaultValuesWhenContractIsNotDeployed`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/MirrorNodeContractQueryIntegrationTest.java:102`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “returns default values when contract is not deployed” behavior.
 - **Then** the operation completes without error, the gas equals the expected default gas, and the returned result equals the expected text.

### `queries.contracts/fails-when-gas-limit-is-low`

> **Implementation:** `MirrorNodeContractQueryIntegrationTest.failsWhenGasLimitIsLow`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/MirrorNodeContractQueryIntegrationTest.java:124`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “fails when gas limit is low” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/fails-when-sender-is-not-set`

> **Implementation:** `MirrorNodeContractQueryIntegrationTest.failsWhenSenderIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/MirrorNodeContractQueryIntegrationTest.java:170`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “fails when sender is not set” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/can-simulate-with-sender-set`

> **Implementation:** `MirrorNodeContractQueryIntegrationTest.canSimulateWithSenderSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/MirrorNodeContractQueryIntegrationTest.java:219`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to simulate with sender set.
 - **Then** the operation completes without error.
