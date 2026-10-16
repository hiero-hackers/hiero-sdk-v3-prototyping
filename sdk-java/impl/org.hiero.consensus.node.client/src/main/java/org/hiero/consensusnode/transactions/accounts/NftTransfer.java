package org.hiero.consensusnode.transactions.accounts;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// One NFT transfer (single serial). NFTs are non-fungible so each serial moves exactly once
/// per transaction.
///
/// @param tokenId the NFT collection
/// @param serial the serial number being transferred
/// @param sender current owner
/// @param receiver new owner
/// @param approved when true, the transfer is drawn from an NFT allowance granted to the transaction payer; the sender need not sign
public record NftTransfer(
        Address tokenId,
        long serial,
        AccountId sender,
        AccountId receiver,
        boolean approved) {

    /// Creates a new `NftTransfer`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NftTransfer {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(sender, "sender must not be null");
        Objects.requireNonNull(receiver, "receiver must not be null");
    }

    /// Creates a new `NftTransfer` with the default value of `approved`.
    public NftTransfer(
            final Address tokenId,
            final long serial,
            final AccountId sender,
            final AccountId receiver) {
        this(tokenId, serial, sender, receiver, false);
    }

    /// Returns a builder for a new `NftTransfer`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NftTransfer`.
    ///
    /// `build()` creates the `NftTransfer` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address tokenId;
        private @Nullable Long serial;
        private @Nullable AccountId sender;
        private @Nullable AccountId receiver;
        private boolean approved = false;

        private Builder() {
            // created by NftTransfer.builder()
        }

        /// Sets the `tokenId`.
        ///
        /// the NFT collection
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Sets the `serial`.
        ///
        /// the serial number being transferred
        ///
        /// @param serial the value
        /// @return this builder
        public Builder serial(final long serial) {
            this.serial = serial;
            return this;
        }

        /// Sets the `sender`.
        ///
        /// current owner
        ///
        /// @param sender the value
        /// @return this builder
        public Builder sender(final AccountId sender) {
            this.sender = sender;
            return this;
        }

        /// Sets the `receiver`.
        ///
        /// new owner
        ///
        /// @param receiver the value
        /// @return this builder
        public Builder receiver(final AccountId receiver) {
            this.receiver = receiver;
            return this;
        }

        /// Sets the `approved`.
        ///
        /// when true, the transfer is drawn from an NFT allowance granted to the transaction payer; the sender need not sign
        ///
        /// @param approved the value
        /// @return this builder
        public Builder approved(final boolean approved) {
            this.approved = approved;
            return this;
        }

        /// Creates the `NftTransfer` of the values set.
        ///
        /// @return the new `NftTransfer`
        /// @throws NullPointerException if a required value is not set
        public NftTransfer build() {
            return new NftTransfer(
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    Objects.requireNonNull(serial, "serial must not be null"),
                    Objects.requireNonNull(sender, "sender must not be null"),
                    Objects.requireNonNull(receiver, "receiver must not be null"),
                    approved);
        }
    }
}
