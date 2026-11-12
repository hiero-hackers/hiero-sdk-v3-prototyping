/// `BlockNodeClient.status()` returns a `BlockNodeStatus`, a short summary: the first and the last block the block node
/// serves and the next block it expects from the network. A request for a block between `firstAvailableBlock` and
/// `lastAvailableBlock` should succeed; a request outside this range fails.
///
/// `BlockNodeClient.statusDetail()` returns a `BlockNodeStatusDetail`. It lists the exact ranges of blocks the block node
/// serves — a block node can have gaps — and the versions of its software, of the block stream it supports and of its
/// plugins. It also holds the data that a client needs to verify the proofs of the blocks: the TSS data of the ledger
/// (`TssData`) and the address books of the consensus nodes that signed older blocks. The list of ranges can be very long.
package org.hiero.blocknode.status;
