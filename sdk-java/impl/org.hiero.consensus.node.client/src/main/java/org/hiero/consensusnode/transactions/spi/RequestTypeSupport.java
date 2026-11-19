package org.hiero.consensusnode.transactions.spi;

import java.util.Set;
import org.hiero.consensusnode.requestTypes.RequestType;

/// The request types of a service: those of the protocol (`BasicRequestType`, registered by the SDK itself), of an
/// optional service such as CLPR, or of a custom service.
public interface RequestTypeSupport {

    /// the request types of the service; no two with the same code
    Set<RequestType> getRequestTypes();
}
