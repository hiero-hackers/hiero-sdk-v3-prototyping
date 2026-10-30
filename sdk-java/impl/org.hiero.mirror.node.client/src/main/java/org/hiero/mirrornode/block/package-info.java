/// Query the blocks of the network from the Mirror Node. Use the `BlockRepository` (available as
/// `MirrorNodeClient.blocks`) to look up a single block by its number (`findByNumber`) or its hash (`findByHash`), and to
/// list the blocks of the network (`findAll`), optionally filtered by block number and consensus time.
///
/// A `Block` describes one block of the network: its number, its hash and the hash of the block before it, the number of
/// transactions it contains, the consensus time range it covers, and its EVM-related data (the gas used and the logs
/// bloom filter).
package org.hiero.mirrornode.block;
