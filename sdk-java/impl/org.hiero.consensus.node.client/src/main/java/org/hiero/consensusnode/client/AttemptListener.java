package org.hiero.consensusnode.client;

import org.hiero.sdk.annotation.ThreadSafe;

/// Is told about every attempt, e.g. for logging and metrics; it cannot change the course of a request.
@ThreadSafe
public interface AttemptListener {

    /// Called after every attempt of every request of the client.
    void attemptCompleted(final Submittable<?> request, final Attempt attempt);
}
