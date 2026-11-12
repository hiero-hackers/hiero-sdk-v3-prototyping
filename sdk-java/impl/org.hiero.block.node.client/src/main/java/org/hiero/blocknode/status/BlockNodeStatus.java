package org.hiero.blocknode.status;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The status of a block node.
///
/// @param firstAvailableBlock the number of the first block the block node serves
/// @param lastAvailableBlock The number of the last block the block node serves, or null if the block node accepts any block next and the
///   last available block is not known.
/// @param onlyLatestState True if the block node serves only the latest state snapshot.
/// @param nextExpectedBlock The number of the next block the block node expects from the network, or null if it accepts any block next.
public record BlockNodeStatus(
        long firstAvailableBlock,
        @Nullable Long lastAvailableBlock,
        boolean onlyLatestState,
        @Nullable Long nextExpectedBlock) {

    /// Returns a builder for a new `BlockNodeStatus`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `BlockNodeStatus`.
    ///
    /// `build()` creates the `BlockNodeStatus` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long firstAvailableBlock;
        private @Nullable Long lastAvailableBlock;
        private @Nullable Boolean onlyLatestState;
        private @Nullable Long nextExpectedBlock;

        private Builder() {
            // created by BlockNodeStatus.builder()
        }

        /// Sets the `firstAvailableBlock`.
        ///
        /// the number of the first block the block node serves
        ///
        /// @param firstAvailableBlock the value
        /// @return this builder
        public Builder firstAvailableBlock(final long firstAvailableBlock) {
            this.firstAvailableBlock = firstAvailableBlock;
            return this;
        }

        /// Sets the `lastAvailableBlock`.
        ///
        /// The number of the last block the block node serves, or null if the block node accepts any block next and the
        /// last available block is not known.
        ///
        /// @param lastAvailableBlock the value
        /// @return this builder
        public Builder lastAvailableBlock(final @Nullable Long lastAvailableBlock) {
            this.lastAvailableBlock = lastAvailableBlock;
            return this;
        }

        /// Sets the `onlyLatestState`.
        ///
        /// True if the block node serves only the latest state snapshot.
        ///
        /// @param onlyLatestState the value
        /// @return this builder
        public Builder onlyLatestState(final boolean onlyLatestState) {
            this.onlyLatestState = onlyLatestState;
            return this;
        }

        /// Sets the `nextExpectedBlock`.
        ///
        /// The number of the next block the block node expects from the network, or null if it accepts any block next.
        ///
        /// @param nextExpectedBlock the value
        /// @return this builder
        public Builder nextExpectedBlock(final @Nullable Long nextExpectedBlock) {
            this.nextExpectedBlock = nextExpectedBlock;
            return this;
        }

        /// Creates the `BlockNodeStatus` of the values set.
        ///
        /// @return the new `BlockNodeStatus`
        /// @throws NullPointerException if a required value is not set
        public BlockNodeStatus build() {
            return new BlockNodeStatus(
                    Objects.requireNonNull(firstAvailableBlock, "firstAvailableBlock must not be null"),
                    lastAvailableBlock,
                    Objects.requireNonNull(onlyLatestState, "onlyLatestState must not be null"),
                    nextExpectedBlock);
        }
    }
}
