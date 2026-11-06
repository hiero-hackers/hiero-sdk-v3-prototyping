/// The smart contract service runs contracts written for the Ethereum Virtual Machine (EVM) on the consensus node. A
/// contract is addressed by a `ContractId`, either by its number or by its 20-byte EVM address.
///
/// A contract's lifecycle is:
///
/// ```
///   create ── (execute | update)* ── delete
/// ```
///
/// - `ContractCreateTransaction` deploys a contract. Its initcode (the compiled bytecode that runs the constructor and
///   returns the code of the contract) is either given inline as `bytecode` or read from a file of the file service
///   (`bytecodeFileId`), which is the way for initcode that does not fit into one transaction. The arguments of the
///   constructor are passed as `constructorParameters`.
/// - `ContractExecuteTransaction` calls a function of a contract and can transfer native tokens to it. The function and
///   its arguments are the `callData`.
/// - `ContractUpdateTransaction` changes the properties of a contract: its admin authority, memo, expiration, renewal
///   and staking. It does not change the code of the contract.
/// - `ContractDeleteTransaction` deletes a contract and transfers its remaining balance to an account or another
///   contract.
/// - `EthereumTransaction` submits a transaction that was signed by an Ethereum account (with its ECDSA secp256k1 key),
///   in the encoding of the Ethereum network. The account of the client pays the transaction fee and, up to
///   `maxGasAllowance`, the gas the Ethereum account does not pay itself.
///
/// ### Arguments and results in the contract ABI
///
/// The call data, the constructor arguments, the result of a call and the logs of a contract are encoded in the
/// contract ABI. The types of the namespace `evm` produce and read them:
///
/// - `AbiFunction.encodeCall(arguments)` returns the `callData` of a `ContractExecuteTransaction`: the 4-byte selector
///   of the function followed by its encoded arguments.
/// - `AbiTuple{values}.encode()` returns the `constructorParameters` of a `ContractCreateTransaction`: the encoded
///   arguments without a selector, as a constructor has none.
/// - The record of a contract transaction (`Response.queryRecord(client)`) holds a `ContractFunctionResult`.
///   `AbiFunction.decodeResult(result.result)` decodes what the function returned, and
///   `AbiEvent.decodeLog(log.topics, log.data)` decodes each of its `logs`.
///
/// ### Gas
///
/// Every contract transaction names the maximum amount of gas its execution may use (`gas`). The gas actually used is
/// charged in addition to the transaction fee; gas that is reserved but not used is refunded only in part, so `gas`
/// should be close to what the execution needs. An execution that runs out of gas fails with `INSUFFICIENT_GAS` and its
/// changes are reverted, but the gas is charged.
///
/// ### Signing requirements (contract)
///
/// | Transaction          | Signers required                                                                                         |
/// |----------------------|----------------------------------------------------------------------------------------------------------|
/// | `ContractCreate`     | the *payer*; **and** the `adminAuthority` if it is set (anti-spoofing); **and** the `autoRenewAccount` if it is set |
/// | `ContractExecute`    | the *payer* only; what the contract allows the caller to do is decided by its code                       |
/// | `ContractUpdate`     | the *payer*; **and** the contract's *current* `adminAuthority`; **and** the *new* `adminAuthority` if it changes; **and** the new `autoRenewAccount` if it changes. Extending only `expirationTime` needs no admin signature. |
/// | `ContractDelete`     | the *payer*; **and** the contract's current `adminAuthority`                                             |
/// | `EthereumTransaction` | the *payer* (the relay); the Ethereum account has already signed `ethereumData`                         |
///
/// A contract without an `adminAuthority` is immutable: it can neither be updated (except for extending its expiration)
/// nor deleted. An `adminAuthority` can be a contract (`Authority.ofContract(...)`), so that a contract manages itself.
///
/// ### Clearing optional values
///
/// `ContractUpdate` distinguishes "leave unchanged" from "clear":
///
/// - An optional field that is left unset means *leave unchanged*.
/// - The auto-renew account is removed by setting `autoRenewAccount` to `ZERO_ACCOUNT_ID`.
package org.hiero.consensusnode.transactions.contracts;
