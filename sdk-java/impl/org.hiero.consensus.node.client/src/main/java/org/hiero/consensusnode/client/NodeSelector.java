package org.hiero.consensusnode.client;

import java.util.List;
import org.hiero.ledger.ConsensusNode;
import org.hiero.sdk.annotation.ThreadSafe;

/// Picks the consensus node for an attempt and learns from the attempts of all requests of a client which nodes to
/// avoid. Implement it to use your own strategy. One selector serves all requests of a client concurrently.
@ThreadSafe
public interface NodeSelector {

    /// The node for the next attempt: one of the candidates, which are never empty. For a transaction the candidates
    /// are the nodes it was packed for; `attempts` are the attempts of the request so far, empty for the first one.
    ConsensusNode select(final List<ConsensusNode> candidates, final List<Attempt> attempts);

    /// Called after every attempt of every request of the client.
    void attemptCompleted(final Attempt attempt);
}
