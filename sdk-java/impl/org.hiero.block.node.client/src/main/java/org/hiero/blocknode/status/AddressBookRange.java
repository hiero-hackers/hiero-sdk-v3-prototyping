package org.hiero.blocknode.status;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The address book of the consensus nodes that was in effect for a range of blocks.
///
/// @param addressBook The protobuf encoding of the address book (`NodeAddressBook`) of the range.
/// @param startBlock the number of the first block of the range
/// @param endBlock The number of the last block of the range (inclusive), or null if the range is open-ended.
public record AddressBookRange(byte[] addressBook, long startBlock, @Nullable Long endBlock) {

    /// Creates a new `AddressBookRange`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AddressBookRange {
        Objects.requireNonNull(addressBook, "addressBook must not be null");
        addressBook = addressBook.clone();
    }

    /// The protobuf encoding of the address book (`NodeAddressBook`) of the range.
    ///
    /// Returns a copy of the `addressBook` array.
    @Override
    public byte[] addressBook() {
        return addressBook.clone();
    }

    /// Compares `addressBook` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof AddressBookRange other
            && Arrays.equals(addressBook, other.addressBook)
            && startBlock == other.startBlock
            && Objects.equals(endBlock, other.endBlock);
    }

    /// Hashes `addressBook` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(addressBook), startBlock, endBlock);
    }

    /// Shows only the length of `addressBook` instead of the identity of the array object.
    @Override
    public String toString() {
        return "AddressBookRange[addressBook=" + "byte[" + addressBook.length + "]" + ", startBlock=" + startBlock + ", endBlock=" + endBlock + "]";
    }
}
