/// Query smart contracts and their executions from the Mirror Node. Use the `ContractRepository` (available as
/// `MirrorNodeClient.contracts`) to
///
/// - look up a single `Contract` by its `ContractId` or page through all contracts (`findAll`, `findById`),
/// - page through the results of contract executions, of one contract (`findResults`) or of all contracts
///   (`findAllResults`), and look up a single `ContractResult` by the contract and its consensus time (`findResult`) or by
///   the transaction id or Ethereum transaction hash of its transaction (`findResultByTransaction`),
/// - read the storage slots of a contract (`findState`), the log events it emitted (`findLogs`, `findAllLogs`), the
///   actions (calls) of a transaction (`findActions`) and the opcode trace of a transaction (`findOpcodes`),
/// - and simulate a call of a contract without submitting a transaction or estimate its gas (`call`).
///
/// A `Contract` describes the contract entity: its keys, auto-renew settings, memo, EVM address, lifecycle timestamps
/// and, if available, its bytecode. Data of the EVM - bytecode, call data, results, hashes, storage slots, log topics -
/// is given as hexadecimal text with the prefix `0x`, as the Mirror Node returns it.
///
/// A transaction is identified either by its transaction id in the form of the Mirror Node, `shard.realm.num-seconds-nanos`
/// (e.g. `0.0.1001-1700000000-123456789`), or by the 32-byte hash of an Ethereum transaction in hexadecimal.
package org.hiero.mirrornode.contract;
