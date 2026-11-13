/// Foundational types that identify a Hiero network and the entities that live on it. They are used
/// throughout the SDK wherever a network, a node, an account, a contract or another ledger entity is
/// referenced.
///
/// `Network` identifies a specific network instance and the unit of its native token. `ConsensusNode`,
/// `MirrorNode` and `BlockNode` describe the nodes of a network that a client talks to; `IpAddress` holds
/// the address of a consensus node.
///
/// Every entity on a Hiero ledger lives in a `(shard, realm)` space and is selected within that space by
/// a third part — usually a number. `BaseAddress` is the common base of all entity identifiers and
/// provides the shard, the realm, the checksum and the string forms. The concrete identifier types are:
///
/// - `Address` — entities that are always identified by `shard.realm.num`: tokens, topics, files and
///   schedules. `num` is always set.
/// - `ContractId` — a smart contract, identified either by its number or by its 20-byte EVM address
///   (exactly one of the two is set).
/// - `AccountId` — an account, identified by its number, by an EVM-address alias, or by a public-key
///   alias (exactly one of the three is set).
///
/// The EVM-address form of `ContractId` and `AccountId` is an `EvmAddress` of the namespace `evm`.
///
/// All identifier types can be parsed from and formatted to their canonical string form
/// (e.g. `0.0.1234` or `0.0.1234-abcde` with checksum). `TransactionId` identifies a transaction by its
/// payer account and valid-start time. The constants `ZERO_ADDRESS`, `ZERO_ACCOUNT_ID` and
/// `ZERO_CONTRACT_ID` are used in update transactions to remove a previously configured value.
///
/// The text forms and the checksum of an entity id are the same in every SDK:
///
/// - `rule:entity-id-text-form` The text form of an entity id is `shard.realm.num`, each number in decimal without
///   leading zeros, optionally followed by `-` and a checksum of five lowercase letters, as defined by HIP-15. Text in
///   any other form, including whitespace, a sign or upper-case letters, is rejected. `toString()` writes an id without
///   its checksum, `toStringWithChecksum()` with it.
/// - `rule:evm-entity-id-text-form` A `ContractId` or `AccountId` addressed by an EVM address has the text form
///   `shard.realm.<address>`, the address as 40 lowercase hexadecimal digits without `0x`, as defined by HIP-583. It has
///   no checksum.
/// - `rule:entity-id-checksum` The checksum of an entity id on a network is the HIP-15 checksum of its text form
///   `shard.realm.num`, computed with the `id` of the `Network` as the ledger id.
/// - `rule:checksum-validation` `validateChecksum(network)` is `true` for an id without a checksum and for an id without
///   a number (addressed by an EVM address or a key alias), for which HIP-15 defines no checksum; otherwise it is `true`
///   exactly if the checksum is the checksum of the id on that network. Parsing an id never validates its checksum.
package org.hiero.ledger;
