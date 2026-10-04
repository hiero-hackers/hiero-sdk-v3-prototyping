package org.hiero.tck.runtime;

import org.hiero.consensusnode.client.HieroClient;
import org.hiero.tck.contract.Session;
import org.jspecify.annotations.Nullable;

/**
 * The state of one test file of the TCK: the client created by {@code setup}.
 */
final class RuntimeSession implements Session {

    private final String id;
    private volatile @Nullable HieroClient<?> client;

    /**
     * Creates an empty session.
     *
     * @param id the session ID sent by the TCK
     */
    RuntimeSession(final String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    /**
     * Returns the client.
     *
     * @return the client
     * @throws RpcError if {@code setup} was not called
     */
    HieroClient<?> client() {
        final HieroClient<?> current = client;
        if (current == null) {
            throw RpcError.internal("setup has not been called for session '" + id + "'");
        }
        return current;
    }

    /**
     * Sets the client.
     *
     * @param client the client, {@code null} to reset
     */
    void client(final @Nullable HieroClient<?> client) {
        this.client = client;
    }

    /**
     * Returns the session of the runtime behind a session of the contract.
     *
     * @param session the session passed to a handler
     * @return the runtime session
     */
    static RuntimeSession of(final Session session) {
        if (session instanceof RuntimeSession runtime) {
            return runtime;
        }
        throw new IllegalArgumentException("Session " + session.id() + " was not created by this runtime");
    }
}
