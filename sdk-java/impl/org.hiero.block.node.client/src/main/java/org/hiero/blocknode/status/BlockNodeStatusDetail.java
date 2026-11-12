package org.hiero.blocknode.status;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The detailed status of a block node.
///
/// @param versions the versions of the block node
/// @param availableRanges the ranges of blocks the block node serves
/// @param storedRanges the ranges of blocks the block node has stored
/// @param tssData the TSS data of the ledger, or null if the block node has none
/// @param nodeAddressBook The protobuf encoding of the current address book of the consensus nodes (`NodeAddressBook`); empty if the block
///   node has none.
/// @param addressBookHistory The address books of the consensus nodes by block range, oldest first; used to verify older blocks.
public record BlockNodeStatusDetail(
        BlockNodeVersions versions,
        List<BlockRange> availableRanges,
        List<BlockRange> storedRanges,
        @Nullable TssData tssData,
        byte[] nodeAddressBook,
        List<AddressBookRange> addressBookHistory) {

    /// Creates a new `BlockNodeStatusDetail`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public BlockNodeStatusDetail {
        Objects.requireNonNull(versions, "versions must not be null");
        Objects.requireNonNull(availableRanges, "availableRanges must not be null");
        availableRanges = List.copyOf(availableRanges);
        Objects.requireNonNull(storedRanges, "storedRanges must not be null");
        storedRanges = List.copyOf(storedRanges);
        Objects.requireNonNull(nodeAddressBook, "nodeAddressBook must not be null");
        nodeAddressBook = nodeAddressBook.clone();
        Objects.requireNonNull(addressBookHistory, "addressBookHistory must not be null");
        addressBookHistory = List.copyOf(addressBookHistory);
    }

    /// The protobuf encoding of the current address book of the consensus nodes (`NodeAddressBook`); empty if the block
    /// node has none.
    ///
    /// Returns a copy of the `nodeAddressBook` array.
    @Override
    public byte[] nodeAddressBook() {
        return nodeAddressBook.clone();
    }

    /// Compares `nodeAddressBook` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof BlockNodeStatusDetail other
            && Objects.equals(versions, other.versions)
            && Objects.equals(availableRanges, other.availableRanges)
            && Objects.equals(storedRanges, other.storedRanges)
            && Objects.equals(tssData, other.tssData)
            && Arrays.equals(nodeAddressBook, other.nodeAddressBook)
            && Objects.equals(addressBookHistory, other.addressBookHistory);
    }

    /// Hashes `nodeAddressBook` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(versions, availableRanges, storedRanges, tssData, Arrays.hashCode(nodeAddressBook), addressBookHistory);
    }

    /// Shows only the length of `nodeAddressBook` instead of the identity of the array object.
    @Override
    public String toString() {
        return "BlockNodeStatusDetail[versions=" + versions + ", availableRanges=" + availableRanges + ", storedRanges=" + storedRanges + ", tssData=" + tssData + ", nodeAddressBook=" + "byte[" + nodeAddressBook.length + "]" + ", addressBookHistory=" + addressBookHistory + "]";
    }

    /// Returns a builder for a new `BlockNodeStatusDetail`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `BlockNodeStatusDetail`.
    ///
    /// `build()` creates the `BlockNodeStatusDetail` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable BlockNodeVersions versions;
        private @Nullable List<BlockRange> availableRanges;
        private @Nullable List<BlockRange> storedRanges;
        private @Nullable TssData tssData;
        private byte @Nullable [] nodeAddressBook;
        private @Nullable List<AddressBookRange> addressBookHistory;

        private Builder() {
            // created by BlockNodeStatusDetail.builder()
        }

        /// Sets the `versions`.
        ///
        /// the versions of the block node
        ///
        /// @param versions the value
        /// @return this builder
        public Builder versions(final BlockNodeVersions versions) {
            this.versions = versions;
            return this;
        }

        /// Sets the `availableRanges`.
        ///
        /// the ranges of blocks the block node serves
        ///
        /// @param availableRanges the value
        /// @return this builder
        public Builder availableRanges(final List<BlockRange> availableRanges) {
            this.availableRanges = availableRanges;
            return this;
        }

        /// Sets the `storedRanges`.
        ///
        /// the ranges of blocks the block node has stored
        ///
        /// @param storedRanges the value
        /// @return this builder
        public Builder storedRanges(final List<BlockRange> storedRanges) {
            this.storedRanges = storedRanges;
            return this;
        }

        /// Sets the `tssData`.
        ///
        /// the TSS data of the ledger, or null if the block node has none
        ///
        /// @param tssData the value
        /// @return this builder
        public Builder tssData(final @Nullable TssData tssData) {
            this.tssData = tssData;
            return this;
        }

        /// Sets the `nodeAddressBook`.
        ///
        /// The protobuf encoding of the current address book of the consensus nodes (`NodeAddressBook`); empty if the block
        /// node has none.
        ///
        /// @param nodeAddressBook the value
        /// @return this builder
        public Builder nodeAddressBook(final byte[] nodeAddressBook) {
            this.nodeAddressBook = nodeAddressBook;
            return this;
        }

        /// Sets the `addressBookHistory`.
        ///
        /// The address books of the consensus nodes by block range, oldest first; used to verify older blocks.
        ///
        /// @param addressBookHistory the value
        /// @return this builder
        public Builder addressBookHistory(final List<AddressBookRange> addressBookHistory) {
            this.addressBookHistory = addressBookHistory;
            return this;
        }

        /// Creates the `BlockNodeStatusDetail` of the values set.
        ///
        /// @return the new `BlockNodeStatusDetail`
        /// @throws NullPointerException if a required value is not set
        public BlockNodeStatusDetail build() {
            return new BlockNodeStatusDetail(
                    Objects.requireNonNull(versions, "versions must not be null"),
                    Objects.requireNonNull(availableRanges, "availableRanges must not be null"),
                    Objects.requireNonNull(storedRanges, "storedRanges must not be null"),
                    tssData,
                    Objects.requireNonNull(nodeAddressBook, "nodeAddressBook must not be null"),
                    Objects.requireNonNull(addressBookHistory, "addressBookHistory must not be null"));
        }
    }
}
