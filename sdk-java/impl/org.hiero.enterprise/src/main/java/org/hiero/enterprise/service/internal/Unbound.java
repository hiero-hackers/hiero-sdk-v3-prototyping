package org.hiero.enterprise.service.internal;

import org.hiero.enterprise.service.ServiceException;

/// The failure of a service that was created with its public constructor instead of its factory: such a service has no
/// session, so it can neither execute transactions nor read from the Mirror Node.
public final class Unbound {

    private Unbound() {
    }

    /// The error of a service without a session.
    ///
    /// @param service the simple name of the service type
    /// @param factory the simple name of the factory that creates it
    /// @return the error to throw
    public static ServiceException error(final String service, final String factory) {
        return new ServiceException("This " + service + " is not bound to a session; create it with " + factory
                + ".createService(session)", null);
    }
}
