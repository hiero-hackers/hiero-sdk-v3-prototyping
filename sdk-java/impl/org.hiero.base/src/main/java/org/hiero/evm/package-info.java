/// Types of the Ethereum Virtual Machine (EVM) that do not depend on a particular network: they mean the same on a Hiero
/// network as on any other EVM network.
///
/// `EvmAddress` is a 20-byte EVM address. It is read from 40 hex digits, with or without `0x`, and written in the
/// mixed-case checksum form of EIP-55 (`0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed`). Digits in mixed case must be the
/// EIP-55 form; a mistyped address is rejected instead of being read as a different one.
package org.hiero.evm;
