/// Types of the Ethereum Virtual Machine (EVM) that do not depend on a particular network: they mean the same on a Hiero
/// network as on any other EVM network.
///
/// `EvmAddress` is a 20-byte EVM address. It is read from 40 hex digits, with or without `0x`, and written in the
/// mixed-case checksum form of EIP-55 (`0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed`). Digits in mixed case must be the
/// EIP-55 form; a mistyped address is rejected instead of being read as a different one.
///
/// The contract ABI (application binary interface) defines how the arguments of a contract call, its results and the
/// logs of a contract are encoded. `AbiType` is the type of an ABI value — `uint256`, `int8`, `address`, `bool`,
/// `bytes32`, `bytes`, `string`, an array such as `uint256[]` or `bytes3[2]`, or a tuple such as `(address,uint256)` (a
/// struct in Solidity). `AbiValue` is a value of such a type, e.g. `AbiUint{value: 1000, bits: 256}` or
/// `AbiAddress{value: address}`; it knows its type, so a list of values can be encoded without a separate list of types.
///
/// `AbiFunction` describes a function of a contract. It computes the selector, encodes the call data of a call and
/// decodes its result. `AbiEvent` describes an event of a contract and decodes the logs the contract emits; `AbiTuple.encode()` encodes
/// values without a selector, e.g. the arguments of a constructor. Decoding is strict: data that is too short, an offset
/// that points outside the data, or a value with bits set that its type does not have (a `bool` other than 0 or 1, a
/// `uint8` above 255, an address with non-zero padding) is rejected rather than truncated.
package org.hiero.evm;
