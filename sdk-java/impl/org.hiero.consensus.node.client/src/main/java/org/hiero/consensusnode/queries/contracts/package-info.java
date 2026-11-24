/// Queries for reading smart contracts directly from a consensus node. All three are paid, so you can bound the spend
/// with `maxQueryPayment`, ask for the price up front with `getCost(client)`, and read the actually paid `cost` from the
/// `PaidQueryResponse`.
///
/// - **`ContractCallQuery`** — calls a function of a contract on the node that answers the query, without consensus. The
///   call must not change any state: it is executed like an EVM `STATICCALL`, so it suits view and pure functions. It
///   creates no transaction, no receipt and no record, and it cannot transfer native tokens. It returns the
///   `ContractFunctionResult` of the call.
/// - **`ContractInfoQuery`** — returns the properties of a contract: its account, EVM address, admin authority, memo,
///   expiration, renewal, storage, balance and staking. It does not return the code; use `ContractBytecodeQuery` for that.
/// - **`ContractBytecodeQuery`** — returns the runtime bytecode of a contract: the code the EVM executes, not the
///   initcode that deployed it.
///
/// ### The local call
///
/// A `ContractCallQuery` names the contract (`contractId`), the call data (`callData`, the selector of the function
/// followed by its ABI-encoded arguments, see `AbiFunction.encodeCall`) and the gas the call may use (`gas`). Decode
/// what the function returned with `AbiFunction.decodeResult(result.result)` of the namespace `evm`.
///
/// Unlike a `ContractExecuteTransaction`, the local call is charged for **all** of its `gas`, not for the gas it used, so
/// `gas` should be close to what the call needs. The price that `getCost(client)` returns and that `submit(client)` pays
/// already contains the gas.
///
/// The call is sent by the operator account of the client, which pays for the query. Set `senderId` to execute the call
/// as another account (`msg.sender` in Solidity); the payment of the query must then be signed by that account as well,
/// which the SDK does not do today (see *Questions & Comments*).
///
/// A call that reverts, runs out of gas or fails otherwise is rejected by the node: `submit(client)` fails as described
/// in "The outcome of an attempt" of the client, with the status of the node (for example `CONTRACT_REVERT_EXECUTED` or
/// `INSUFFICIENT_GAS`).
///
/// For calls that do not need the answer of a consensus node, the mirror node offers a free, simulated call of a contract
/// and a gas estimation (`ContractRepository.call` of `mirrornode.contract`).
///
/// ### Deleted contracts
///
/// Querying a deleted contract does not fail: `ContractInfoQuery` returns a `ContractInfo` with `deleted = true`. Check
/// `ContractInfo.deleted` instead of treating empty bytecode as proof of deletion.
package org.hiero.consensusnode.queries.contracts;
