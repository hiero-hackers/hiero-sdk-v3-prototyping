package org.hiero.blocknode.status;


/// A contiguous range of blocks.
///
/// @param start the number of the first block of the range
/// @param end the number of the last block of the range (inclusive)
public record BlockRange(long start, long end) {
}
