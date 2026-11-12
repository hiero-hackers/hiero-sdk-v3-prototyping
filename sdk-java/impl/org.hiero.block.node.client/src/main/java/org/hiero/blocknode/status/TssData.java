package org.hiero.blocknode.status;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The TSS data of a ledger, used to verify the proofs of its blocks.
///
/// @param ledgerId the id of the ledger
/// @param wrapsVerificationKey the WRAPS verification key of the ledger
/// @param currentRoster the current TSS roster
/// @param validFromBlock the number of the first block this data is valid for
public record TssData(
        byte[] ledgerId,
        byte[] wrapsVerificationKey,
        List<TssRosterEntry> currentRoster,
        long validFromBlock) {

    /// Creates a new `TssData`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TssData {
        Objects.requireNonNull(ledgerId, "ledgerId must not be null");
        ledgerId = ledgerId.clone();
        Objects.requireNonNull(wrapsVerificationKey, "wrapsVerificationKey must not be null");
        wrapsVerificationKey = wrapsVerificationKey.clone();
        Objects.requireNonNull(currentRoster, "currentRoster must not be null");
        currentRoster = List.copyOf(currentRoster);
    }

    /// the id of the ledger
    ///
    /// Returns a copy of the `ledgerId` array.
    @Override
    public byte[] ledgerId() {
        return ledgerId.clone();
    }

    /// the WRAPS verification key of the ledger
    ///
    /// Returns a copy of the `wrapsVerificationKey` array.
    @Override
    public byte[] wrapsVerificationKey() {
        return wrapsVerificationKey.clone();
    }

    /// Compares `ledgerId` and `wrapsVerificationKey` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof TssData other
            && Arrays.equals(ledgerId, other.ledgerId)
            && Arrays.equals(wrapsVerificationKey, other.wrapsVerificationKey)
            && Objects.equals(currentRoster, other.currentRoster)
            && validFromBlock == other.validFromBlock;
    }

    /// Hashes `ledgerId` and `wrapsVerificationKey` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(ledgerId), Arrays.hashCode(wrapsVerificationKey), currentRoster, validFromBlock);
    }

    /// Shows only the length of `ledgerId` and `wrapsVerificationKey` instead of the identity of the array object.
    @Override
    public String toString() {
        return "TssData[ledgerId=" + "byte[" + ledgerId.length + "]" + ", wrapsVerificationKey=" + "byte[" + wrapsVerificationKey.length + "]" + ", currentRoster=" + currentRoster + ", validFromBlock=" + validFromBlock + "]";
    }
}
