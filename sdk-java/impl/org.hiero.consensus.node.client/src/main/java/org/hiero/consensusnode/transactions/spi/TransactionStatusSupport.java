package org.hiero.consensusnode.transactions.spi;

import java.util.Set;
import org.hiero.consensusnode.status.TransactionStatus;

/// The status codes of a service: those of the protocol (`BasicTransactionStatus`, registered by the SDK itself), of an
/// optional service such as CLPR, or of a custom service.
public interface TransactionStatusSupport {

    /// the statuses of the service; no two with the same code
    Set<TransactionStatus> getStatuses();
}
