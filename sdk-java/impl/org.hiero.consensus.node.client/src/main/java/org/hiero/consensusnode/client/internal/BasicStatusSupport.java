package org.hiero.consensusnode.client.internal;

import java.util.Set;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.spi.TransactionStatusSupport;

/// The status codes of the protocol (`BasicTransactionStatus`), the support the SDK registers itself.
public final class BasicStatusSupport implements TransactionStatusSupport {

    private static final Set<TransactionStatus> STATUSES = Set.of(BasicTransactionStatus.values());

    /// Creates the support.
    public BasicStatusSupport() {
        // nothing to set up
    }

    @Override
    public Set<TransactionStatus> getStatuses() {
        return STATUSES;
    }
}
