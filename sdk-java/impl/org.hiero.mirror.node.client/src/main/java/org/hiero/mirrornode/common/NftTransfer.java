package org.hiero.mirrornode.common;

import java.util.Objects;

public record NftTransfer(boolean isApproval, NftTransferParties parties, long serialNumber) {

    /// Creates a new `NftTransfer`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NftTransfer {
        Objects.requireNonNull(parties, "parties must not be null");
    }
}
