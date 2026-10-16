package org.hiero.mirrornode.network;


/// Estimated fee for a candidate transaction (HIP-1313, congestion-adjusted). Returned by
/// NetworkRepository.estimateFees().
///
/// @param estimatedFee estimated total fee in tinybars
public record FeeEstimate(long estimatedFee) {
}
