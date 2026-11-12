/// A block node stores the block stream of a network: every block as the consensus nodes produced it, with its block
/// proof. Create a `BlockNodeClient` for a `BlockNode` with `createBlockNodeClient` and use it to
///
/// - read the status of the block node (`status`, `statusDetail`) — which blocks it has, which versions it runs and the
///   data needed to verify its blocks,
/// - read a single block (`findByNumber`, `findLatest`),
/// - stream a range of blocks, or all blocks from a start block on as they are produced (`subscribe`).
///
/// Reading from a block node needs no account, no payment and no signature.
///
/// A `Block` is the sequence of its `BlockItem`s, from the block header to the block proofs: the transactions of the
/// block with their results and outputs, the state changes, and the hashes and signatures that prove the block. Every item
/// has a kind (`BlockItemKind`) and its content as bytes, in the protobuf encoding of the block stream of the network
/// release that produced the block. The SDK decodes only the `BlockHeader` (number, consensus time, versions, hash
/// algorithm) and the `BlockFooter` (the hashes that chain the block to its predecessor); decode the other items with the
/// block stream definitions when you need them. The SDK does not verify a block. A block can be many megabytes in size.
///
/// Ask the block node for its `status` before reading blocks: a block outside its available range is not served.
/// `findByNumber` returns `null` for a block that the block node does not have. Every method fails with a
/// `block-node-error` if the block node cannot be reached, rejects the request or fails.
package org.hiero.blocknode;
