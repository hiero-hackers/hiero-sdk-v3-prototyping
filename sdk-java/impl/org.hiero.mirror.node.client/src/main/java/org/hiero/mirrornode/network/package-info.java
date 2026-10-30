/// Query network-wide data from the Mirror Node. Use the `NetworkRepository` (available as `MirrorNodeClient.network`)
/// to read the current and next HBAR exchange rate, the current fee schedule (`fees`), the staking parameters (`stake`),
/// the HBAR supply (`supplies`), the address book of consensus nodes (`nodes`) and the other nodes registered in the
/// network, such as block nodes (`registeredNodes`). `estimateFees` estimates the fee of a candidate transaction before
/// it is submitted (HIP-1313).
package org.hiero.mirrornode.network;
