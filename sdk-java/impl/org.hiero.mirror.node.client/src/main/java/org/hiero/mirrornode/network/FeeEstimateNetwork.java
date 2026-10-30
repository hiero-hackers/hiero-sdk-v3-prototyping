package org.hiero.mirrornode.network;


/// The network component of a fee estimate: the node subtotal multiplied by the network multiplier.
///
/// @param subtotal in tinycents
public record FeeEstimateNetwork(int multiplier, long subtotal) {
}
