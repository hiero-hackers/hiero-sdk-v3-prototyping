package org.hiero.blocknode.status;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// A consensus node in the TSS roster of a ledger.
///
/// @param nodeId the id of the node
/// @param weight the weight of the node
/// @param schnorrPublicKey The Schnorr public key of the node; a placeholder if the node did not take part in the roster election in time.
public record TssRosterEntry(long nodeId, long weight, byte[] schnorrPublicKey) {

    /// Creates a new `TssRosterEntry`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TssRosterEntry {
        Objects.requireNonNull(schnorrPublicKey, "schnorrPublicKey must not be null");
        schnorrPublicKey = schnorrPublicKey.clone();
    }

    /// The Schnorr public key of the node; a placeholder if the node did not take part in the roster election in time.
    ///
    /// Returns a copy of the `schnorrPublicKey` array.
    @Override
    public byte[] schnorrPublicKey() {
        return schnorrPublicKey.clone();
    }

    /// Compares `schnorrPublicKey` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof TssRosterEntry other
            && nodeId == other.nodeId
            && weight == other.weight
            && Arrays.equals(schnorrPublicKey, other.schnorrPublicKey);
    }

    /// Hashes `schnorrPublicKey` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(nodeId, weight, Arrays.hashCode(schnorrPublicKey));
    }

    /// Shows only the length of `schnorrPublicKey` instead of the identity of the array object.
    @Override
    public String toString() {
        return "TssRosterEntry[nodeId=" + nodeId + ", weight=" + weight + ", schnorrPublicKey=" + "byte[" + schnorrPublicKey.length + "]" + "]";
    }
}
