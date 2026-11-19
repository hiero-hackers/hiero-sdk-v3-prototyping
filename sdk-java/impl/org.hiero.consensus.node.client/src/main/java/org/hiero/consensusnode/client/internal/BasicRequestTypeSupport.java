package org.hiero.consensusnode.client.internal;

import java.util.Set;
import org.hiero.consensusnode.requestTypes.BasicRequestType;
import org.hiero.consensusnode.requestTypes.RequestType;
import org.hiero.consensusnode.transactions.spi.RequestTypeSupport;

/// The request types of the protocol (`BasicRequestType`), the support the SDK registers itself.
public final class BasicRequestTypeSupport implements RequestTypeSupport {

    private static final Set<RequestType> REQUEST_TYPES = Set.of(BasicRequestType.values());

    /// Creates the support.
    public BasicRequestTypeSupport() {
        // nothing to set up
    }

    @Override
    public Set<RequestType> getRequestTypes() {
        return REQUEST_TYPES;
    }
}
