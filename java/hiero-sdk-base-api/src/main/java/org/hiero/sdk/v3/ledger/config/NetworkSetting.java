// Source: spec/base/ledger-config.md (ledger.config.NetworkSetting).

package org.hiero.sdk.v3.ledger.config;

import java.util.Objects;
import java.util.Set;
import org.hiero.sdk.v3.ledger.ConsensusNode;
import org.hiero.sdk.v3.ledger.MirrorNode;
import org.hiero.sdk.v3.ledger.Network;

/** Immutable network configuration base. */
public abstract class NetworkSetting {
    private final Network<?> network;
    private final Set<ConsensusNode> consensusNodes;
    private final Set<MirrorNode> mirrorNodes;

    /** Creates a network setting and snapshots both node sets. */
    protected NetworkSetting(Network<?> network, Set<ConsensusNode> consensusNodes, Set<MirrorNode> mirrorNodes) {
        this.network = Objects.requireNonNull(network, "network");
        this.consensusNodes = Set.copyOf(Objects.requireNonNull(consensusNodes, "consensusNodes"));
        this.mirrorNodes = Set.copyOf(Objects.requireNonNull(mirrorNodes, "mirrorNodes"));
    }

    /** Returns the configured network. */ public final Network<?> network() { return network; }
    /** Returns the immutable consensus-node snapshot. */ public final Set<ConsensusNode> getConsensusNodes() { return consensusNodes; }
    /** Returns the immutable mirror-node snapshot. */ public final Set<MirrorNode> getMirrorNodes() { return mirrorNodes; }
}
