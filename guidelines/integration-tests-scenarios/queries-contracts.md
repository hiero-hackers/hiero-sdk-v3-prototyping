# Queries / Contracts

## Scenarios

### `queries.contracts/can-query-contract-bytecode`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query contract bytecode.
 - **Then** the operation completes without error and the bytecode size equals 798.

### `queries.contracts/get-cost-big-max-query-contract-bytecode`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error and the bytecode size equals 798.

### `queries.contracts/get-cost-small-max-query-contract-bytecode`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/get-cost-insufficient-tx-fee-query-contract-bytecode`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/cannot-query-contract-bytecode-when-contract-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to query contract bytecode when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `queries.contracts/can-call-contract-function`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to call contract function.
 - **Then** the operation completes without error and the returned result get string(0) equals the expected text.

### `queries.contracts/cannot-call-contract-function-when-contract-function-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to call contract function when contract function is not set.
 - **Then** the request is rejected with status CONTRACT_REVERT_EXECUTED.

### `queries.contracts/cannot-call-contract-function-when-gas-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to call contract function when gas is not set.
 - **Then** the request is rejected with status INSUFFICIENT_GAS.

### `queries.contracts/cannot-call-contract-function-when-contract-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to call contract function when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `queries.contracts/get-cost-big-max-contract-call-function`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error and the returned result get string(0) equals the expected text.

### `queries.contracts/get-cost-small-max-contract-call-function`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/get-cost-insufficient-tx-fee-contract-call-function`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/can-call-contract-function-uint8-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint8 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint8(0) equals the expected 0x0.

### `queries.contracts/can-call-contract-function-uint8-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint8 max value from contract call.
 - **Then** the operation completes without error and the uint8 max from response equals the expected uint8 max.

### `queries.contracts/can-call-contract-function-uint8-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint8 array value from contract call.
 - **Then** the operation completes without error, the response result 0 equals the expected uint8 min byte, and the response result 1 equals the expected uint8 max.

### `queries.contracts/can-call-contract-function-uint16-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint16 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint32(0) equals 0.

### `queries.contracts/can-call-contract-function-uint16-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint16 max value from contract call.
 - **Then** the operation completes without error and the uint16 max int from response equals the expected uint16 max.

### `queries.contracts/can-call-contract-function-uint16-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint16 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint16 array.

### `queries.contracts/can-call-contract-function-uint24-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint24 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint32(0) equals 0.

### `queries.contracts/can-call-contract-function-uint24-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint24 max value from contract call.
 - **Then** the operation completes without error and the uint24 max int from response equals the expected uint24 max.

### `queries.contracts/can-call-contract-function-uint24-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint24 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint24 array.

### `queries.contracts/can-call-contract-function-uint32-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint32 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint32(0) equals 0.

### `queries.contracts/can-call-contract-function-uint32-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint32 max value from contract call.
 - **Then** the operation completes without error and the uint32 max int from response equals the expected uint32 max.

### `queries.contracts/can-call-contract-function-uint32-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint32 array value from contract call.
 - **Then** the operation completes without error, the response result 0 equals the expected uint32 min int, and the response result 1 equals the expected long parse unsigned long(uint32 max).

### `queries.contracts/can-call-contract-function-uint40-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint40 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals 0.

### `queries.contracts/can-call-contract-function-uint40-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint40 max value from contract call.
 - **Then** the operation completes without error and the uint64 max long from response equals the expected uint40 max.

### `queries.contracts/can-call-contract-function-uint40-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint40 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint40 array.

### `queries.contracts/can-call-contract-function-uint48-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint48 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals 0.

### `queries.contracts/can-call-contract-function-uint48-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint48 max value from contract call.
 - **Then** the operation completes without error and the uint64 max long from response equals the expected uint48 max.

### `queries.contracts/can-call-contract-function-uint48-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint48 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint48 array.

### `queries.contracts/can-call-contract-function-uint56-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint56 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals 0.

### `queries.contracts/can-call-contract-function-uint56-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint56 max value from contract call.
 - **Then** the operation completes without error and the uint64 max long from response equals the expected uint56 max.

### `queries.contracts/can-call-contract-function-uint56-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint56 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint56 array.

### `queries.contracts/can-call-contract-function-uint64-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint64 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals 0.

### `queries.contracts/can-call-contract-function-uint64-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint64 max value from contract call.
 - **Then** the operation completes without error and the uint64 max long from response equals the expected uint64 max.

### `queries.contracts/can-call-contract-function-uint64-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint64 array value from contract call.
 - **Then** the operation completes without error, the response result 0 equals the expected uint64 min long, and the response result 1 equals the expected long parse unsigned long(uint64 max).

### `queries.contracts/can-call-contract-function-uint72-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint72 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint72-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint72 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint72 max.

### `queries.contracts/can-call-contract-function-uint72-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint72 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint72 array.

### `queries.contracts/can-call-contract-function-uint80-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint80 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint80-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint80 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint80 max.

### `queries.contracts/can-call-contract-function-uint80-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint80 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint80 array.

### `queries.contracts/can-call-contract-function-uint88-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint88 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint88-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint88 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint88 max.

### `queries.contracts/can-call-contract-function-uint88-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint88 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint88 array.

### `queries.contracts/can-call-contract-function-uint96-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint96 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint96-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint96 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint96 max.

### `queries.contracts/can-call-contract-function-uint96-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint96 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint96 array.

### `queries.contracts/can-call-contract-function-uint104-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint104 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint104-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint104 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint104 max.

### `queries.contracts/can-call-contract-function-uint104-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint104 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint104 array.

### `queries.contracts/can-call-contract-function-uint112-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint112 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint112-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint112 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint112 max.

### `queries.contracts/can-call-contract-function-uint112-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint112 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint112 array.

### `queries.contracts/can-call-contract-function-uint120-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint120 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint120-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint120 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint120 max.

### `queries.contracts/can-call-contract-function-uint120-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint120 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint120 array.

### `queries.contracts/can-call-contract-function-uint128-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint128 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint128-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint128 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint128 max.

### `queries.contracts/can-call-contract-function-uint128-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint128 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint128 array.

### `queries.contracts/can-call-contract-function-uint136-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint136 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint136-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint136 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint136 max.

### `queries.contracts/can-call-contract-function-uint136-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint136 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint136 array.

### `queries.contracts/can-call-contract-function-uint144-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint144 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint144-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint144 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint144 max.

### `queries.contracts/can-call-contract-function-uint144-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint144 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint144 array.

### `queries.contracts/can-call-contract-function-uint152-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint152 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint152-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint152 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint152 max.

### `queries.contracts/can-call-contract-function-uint152-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint152 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint152 array.

### `queries.contracts/can-call-contract-function-uint160-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint160 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint160-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint160 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint160 max.

### `queries.contracts/can-call-contract-function-uint160-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint160 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint160 array.

### `queries.contracts/can-call-contract-function-uint168-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint168 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint168-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint168 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint168 max.

### `queries.contracts/can-call-contract-function-uint168-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint168 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint168 array.

### `queries.contracts/can-call-contract-function-uint176-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint176 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint176-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint176 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint176 max.

### `queries.contracts/can-call-contract-function-uint176-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint176 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint176 array.

### `queries.contracts/can-call-contract-function-uint184-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint184 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint184-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint184 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint184 max.

### `queries.contracts/can-call-contract-function-uint184-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint184 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint184 array.

### `queries.contracts/can-call-contract-function-uint192-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint192 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint192-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint192 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint192 max.

### `queries.contracts/can-call-contract-function-uint192-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint192 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint192 array.

### `queries.contracts/can-call-contract-function-uint200-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint200 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint200-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint200 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint200 max.

### `queries.contracts/can-call-contract-function-uint200-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint200 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint200 array.

### `queries.contracts/can-call-contract-function-uint208-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint208 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint208-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint208 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint208 max.

### `queries.contracts/can-call-contract-function-uint208-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint208 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint208 array.

### `queries.contracts/can-call-contract-function-uint216-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint216 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint216-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint216 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint216 max.

### `queries.contracts/can-call-contract-function-uint216-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint216 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint216 array.

### `queries.contracts/can-call-contract-function-uint224-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint224 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint224-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint224 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint224 max.

### `queries.contracts/can-call-contract-function-uint224-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint224 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint224 array.

### `queries.contracts/can-call-contract-function-uint232-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint232 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint232-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint232 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint232 max.

### `queries.contracts/can-call-contract-function-uint232-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint232 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint232 array.

### `queries.contracts/can-call-contract-function-uint240-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint240 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint240-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint240 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint240 max.

### `queries.contracts/can-call-contract-function-uint240-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint240 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint240 array.

### `queries.contracts/can-call-contract-function-uint248-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint248 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint248-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint248 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint248 max.

### `queries.contracts/can-call-contract-function-uint248-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint248 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint248 array.

### `queries.contracts/can-call-contract-function-uint256-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint256 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected big integer zero.

### `queries.contracts/can-call-contract-function-uint256-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint256 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint256(0) equals the expected uint256 max.

### `queries.contracts/can-call-contract-function-uint256-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive uint256 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected uint256 array.

### `queries.contracts/can-call-contract-function-int8-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int8 min value from contract call.
 - **Then** the operation completes without error and the returned response get int8(0) equals the expected byte min value.

### `queries.contracts/can-call-contract-function-int8-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int8 max value from contract call.
 - **Then** the operation completes without error and the returned response get int8(0) equals the expected byte max value.

### `queries.contracts/can-call-contract-function-int8-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int8 array value from contract call.
 - **Then** the operation completes without error, the response result 0 equals the expected int8 array 0, and the response result 1 equals the expected int8 array 1.

### `queries.contracts/can-call-contract-function-int16-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int16 min value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected int16 min.

### `queries.contracts/can-call-contract-function-int16-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int16 max value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected int16 max.

### `queries.contracts/can-call-contract-function-int16-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int16 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int16 array.

### `queries.contracts/can-call-contract-function-int24-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int24 min value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected int24 min.

### `queries.contracts/can-call-contract-function-int24-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int24 max value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected int24 max.

### `queries.contracts/can-call-contract-function-int24-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int24 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int24 array.

### `queries.contracts/can-call-contract-function-int32-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int32 min value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected integer min value.

### `queries.contracts/can-call-contract-function-int32-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int32 max value from contract call.
 - **Then** the operation completes without error and the returned response get int32(0) equals the expected integer max value.

### `queries.contracts/can-call-contract-function-int32-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int32 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int32 array.

### `queries.contracts/can-call-contract-function-int40-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int40 min value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int40 min.

### `queries.contracts/can-call-contract-function-int40-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int40 max value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int40 max.

### `queries.contracts/can-call-contract-function-int40-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int40 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int40 array.

### `queries.contracts/can-call-contract-function-int48-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int48 min value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int48 min.

### `queries.contracts/can-call-contract-function-int48-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int48 max value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int48 max.

### `queries.contracts/can-call-contract-function-int48-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int48 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int48 array.

### `queries.contracts/can-call-contract-function-int56-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int56 min value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int56 min.

### `queries.contracts/can-call-contract-function-int56-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int56 max value from contract call.
 - **Then** the operation completes without error and the returned response get int64(0) equals the expected int56 max.

### `queries.contracts/can-call-contract-function-int56-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int56 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int56 array.

### `queries.contracts/can-call-contract-function-int64-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int64 min value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals the expected long min value.

### `queries.contracts/can-call-contract-function-int64-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int64 max value from contract call.
 - **Then** the operation completes without error and the returned response get uint64(0) equals the expected long max value.

### `queries.contracts/can-call-contract-function-int64-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int64 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int64 array.

### `queries.contracts/can-call-contract-function-int72-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int72 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int72 min.

### `queries.contracts/can-call-contract-function-int72-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int72 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int72 max.

### `queries.contracts/can-call-contract-function-int72-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int72 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int72 array.

### `queries.contracts/can-call-contract-function-int80-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int80 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int80 min.

### `queries.contracts/can-call-contract-function-int80-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int80 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int80 max.

### `queries.contracts/can-call-contract-function-int80-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int80 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int80 array.

### `queries.contracts/can-call-contract-function-int88-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int88 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int88 min.

### `queries.contracts/can-call-contract-function-int88-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int88 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int88 max.

### `queries.contracts/can-call-contract-function-int88-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int88 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int88 array.

### `queries.contracts/can-call-contract-function-int96-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int96 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int96 min.

### `queries.contracts/can-call-contract-function-int96-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int96 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int96 max.

### `queries.contracts/can-call-contract-function-int96-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int96 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int96 array.

### `queries.contracts/can-call-contract-function-int104-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int104 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int104 min.

### `queries.contracts/can-call-contract-function-int104-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int104 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int104 max.

### `queries.contracts/can-call-contract-function-int104-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int104 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int104 array.

### `queries.contracts/can-call-contract-function-int112-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int112 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int112 min.

### `queries.contracts/can-call-contract-function-int112-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int112 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int112 max.

### `queries.contracts/can-call-contract-function-int112-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int112 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int112 array.

### `queries.contracts/can-call-contract-function-int120-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int120 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int120 min.

### `queries.contracts/can-call-contract-function-int120-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int120 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int120 max.

### `queries.contracts/can-call-contract-function-int120-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int120 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int120 array.

### `queries.contracts/can-call-contract-function-int128-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int128 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int128 min.

### `queries.contracts/can-call-contract-function-int128-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int128 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int128 max.

### `queries.contracts/can-call-contract-function-int128-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int128 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int128 array.

### `queries.contracts/can-call-contract-function-int136-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int136 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int136 min.

### `queries.contracts/can-call-contract-function-int136-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int136 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int136 max.

### `queries.contracts/can-call-contract-function-int136-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int136 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int136 array.

### `queries.contracts/can-call-contract-function-int144-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int144 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int144 min.

### `queries.contracts/can-call-contract-function-int144-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int144 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int144 max.

### `queries.contracts/can-call-contract-function-int144-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int144 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int144 array.

### `queries.contracts/can-call-contract-function-int152-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int152 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int152 min.

### `queries.contracts/can-call-contract-function-int152-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int152 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int152 max.

### `queries.contracts/can-call-contract-function-int152-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int152 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int152 array.

### `queries.contracts/can-call-contract-function-int160-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int160 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int160 min.

### `queries.contracts/can-call-contract-function-int160-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int160 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int160 max.

### `queries.contracts/can-call-contract-function-int160-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int160 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int160 array.

### `queries.contracts/can-call-contract-function-int168-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int168 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int168 min.

### `queries.contracts/can-call-contract-function-int168-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int168 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int168 max.

### `queries.contracts/can-call-contract-function-int168-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int168 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int168 array.

### `queries.contracts/can-call-contract-function-int176-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int176 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int176 min.

### `queries.contracts/can-call-contract-function-int176-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int176 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int176 max.

### `queries.contracts/can-call-contract-function-int176-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int176 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int176 array.

### `queries.contracts/can-call-contract-function-int184-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int184 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int184 min.

### `queries.contracts/can-call-contract-function-int184-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int184 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int184 max.

### `queries.contracts/can-call-contract-function-int184-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int184 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int184 array.

### `queries.contracts/can-call-contract-function-int192-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int192 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int192 min.

### `queries.contracts/can-call-contract-function-int192-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int192 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int192 max.

### `queries.contracts/can-call-contract-function-int192-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int192 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int192 array.

### `queries.contracts/can-call-contract-function-int200-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int200 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int200 min.

### `queries.contracts/can-call-contract-function-int200-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int200 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int200 max.

### `queries.contracts/can-call-contract-function-int200-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int200 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int200 array.

### `queries.contracts/can-call-contract-function-int208-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int208 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int208 min.

### `queries.contracts/can-call-contract-function-int208-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int208 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int208 max.

### `queries.contracts/can-call-contract-function-int208-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int208 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int208 array.

### `queries.contracts/can-call-contract-function-int216-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int216 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int216 min.

### `queries.contracts/can-call-contract-function-int216-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int216 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int216 max.

### `queries.contracts/can-call-contract-function-int216-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int216 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int216 array.

### `queries.contracts/can-call-contract-function-int224-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int224 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int224 min.

### `queries.contracts/can-call-contract-function-int224-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int224 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int224 max.

### `queries.contracts/can-call-contract-function-int224-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int224 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int224 array.

### `queries.contracts/can-call-contract-function-int232-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int232 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int232 min.

### `queries.contracts/can-call-contract-function-int232-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int232 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int232 max.

### `queries.contracts/can-call-contract-function-int232-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int232 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int232 array.

### `queries.contracts/can-call-contract-function-int240-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int240 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int240 min.

### `queries.contracts/can-call-contract-function-int240-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int240 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int240 max.

### `queries.contracts/can-call-contract-function-int240-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int240 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int240 array.

### `queries.contracts/can-call-contract-function-int248-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int248 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int248 min.

### `queries.contracts/can-call-contract-function-int248-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int248 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int248 max.

### `queries.contracts/can-call-contract-function-int248-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int248 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int248 array.

### `queries.contracts/can-call-contract-function-int256-min`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int256 min value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int256 min.

### `queries.contracts/can-call-contract-function-int256-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int256 max value from contract call.
 - **Then** the operation completes without error and the returned response get int256(0) equals the expected int256 max.

### `queries.contracts/can-call-contract-function-int256-array`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive int256 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected int256 array.

### `queries.contracts/can-call-contract-function-multiple-int8`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive multiple int8 values from contract call.
 - **Then** the operation completes without error, the returned response get int8(0) equals the expected byte min value, and the returned response get int8(1) equals the expected -108.

### `queries.contracts/can-call-contract-function-multiple-int40`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive multiple int40 values from contract call.
 - **Then** the operation completes without error, the returned response get int64(0) equals the expected int40, and the returned response get int64(1) equals the expected int40 + 1.

### `queries.contracts/can-call-contract-function-multiple-int256`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive multiple int256 values from contract call.
 - **Then** the operation completes without error, the returned response get int256(0) equals the expected int256 min, and the retrieved value equals the expected value.

### `queries.contracts/can-call-contract-function-multiple-types`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive multiple types of values from contract call.
 - **Then** the operation completes without error, the integer to unsigned string(returned response get uint32(0)) equals the expected uint32 max, the returned response get uint64(1) equals the expected long parse unsigned long(uint32 max) - 1, and the returned response get string(2) equals the expected text.

### `queries.contracts/can-call-contract-function-string-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive string value from contract call.
 - **Then** the operation completes without error and the returned response get string(0) equals the expected test string.

### `queries.contracts/can-call-contract-function-string-array-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive string array value from contract call.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `queries.contracts/can-call-contract-function-string-array-type-get-result`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive string array value from contract call with get Result function.
 - **Then** the operation completes without error and the response result equals the expected test string array.

### `queries.contracts/can-call-contract-function-address-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive address value from contract call.
 - **Then** the operation completes without error and the returned response get address(0) equals the expected test address.

### `queries.contracts/can-call-contract-function-address-array-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive address array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected test address array address.

### `queries.contracts/can-call-contract-function-boolean-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive boolean value from contract call.
 - **Then** the operation completes without error and the returned response get bool(0) equals the expected test boolean.

### `queries.contracts/can-call-contract-function-boolean-array-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive boolean array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected test boolean array.

### `queries.contracts/can-call-contract-function-bytes-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive bytes value from contract call.
 - **Then** the operation completes without error and the returned response get bytes(0) equals the expected test bytes.

### `queries.contracts/can-call-contract-function-bytes-array-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive bytes array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected test bytes.

### `queries.contracts/can-call-contract-function-bytes32-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive bytes32 value from contract call.
 - **Then** the operation completes without error and the returned response get bytes32(0) equals the expected test bytes len32.

### `queries.contracts/can-call-contract-function-bytes32-array-type`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to receive bytes32 array value from contract call.
 - **Then** the operation completes without error and the response result equals the expected test bytes len32.

### `queries.contracts/can-populate-contract-id-num-sync`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to populate Contract identifier num from mirror node (using synchronous operation).
 - **Then** the operation completes without error and the contract identifier num equals the expected new contract identifier num.

### `queries.contracts/can-populate-contract-id-num-async`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to populate Contract identifier num from mirror node (using asynchronous operation).
 - **Then** the operation completes without error and the contract identifier num equals the expected new contract identifier num.

### `queries.contracts/can-query-contract-info`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query contract info.
 - **Then** the operation completes without error, the contract identifier hash code is greater than the expected value, the expected observable result is returned, and the returned information contract identifier equals the expected contract identifier.

### `queries.contracts/can-query-contract-info-when-admin-key-is-null`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query contract info when admin key is null.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text equals the expected contract identifier text.

### `queries.contracts/cannot-query-contract-info-when-contract-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to query contract info when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `queries.contracts/get-cost-big-max-contract-info-function`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error.

### `queries.contracts/get-cost-small-max-contract-info-function`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/get-cost-insufficient-tx-fee-contract-info-function`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/can-increment-nonce-through-contract-constructor`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “contract Create of A nonce, which deploys contract B in CONSTRUCTOR” behavior.
 - **Then** the operation completes without error, the contract a nonce info nonce equals 2, the contract b nonce info nonce equals 1, and the contract function result signer nonce equals 0.

### `queries.contracts/can-simulate-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to estimate and simulate transaction.
 - **Then** the operation completes without error and the returned result get address(0) equals the expected simulation result substring(26).

### `queries.contracts/returns-default-values-when-contract-is-not-deployed`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “returns default values when contract is not deployed” behavior.
 - **Then** the operation completes without error, the gas equals the expected default gas, and the returned result equals the expected text.

### `queries.contracts/fails-when-gas-limit-is-low`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “fails when gas limit is low” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/fails-when-sender-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “fails when sender is not set” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.contracts/can-simulate-with-sender-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to simulate with sender set.
 - **Then** the operation completes without error.
