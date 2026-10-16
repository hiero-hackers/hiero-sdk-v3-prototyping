package org.hiero.mirrornode.common;

import org.jspecify.annotations.Nullable;

public record NftTransfer(boolean isApproval, @Nullable NftTransferParties parties, long serialNumber) {
}
