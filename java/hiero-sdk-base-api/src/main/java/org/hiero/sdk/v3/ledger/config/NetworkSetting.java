// Generated from spec/base/ledger-config.md (ledger.config#NetworkSetting); DO NOT EDIT.

package org.hiero.sdk.v3.ledger.config;

import java.util.Set;
import org.hiero.sdk.v3.ledger.ConsensusNode;
import org.hiero.sdk.v3.ledger.MirrorNode;
import org.hiero.sdk.v3.ledger.Network;

/** Immutable network configuration contract. */
public interface NetworkSetting {
    /** Returns the configured network. */ Network<?> network();
    /** Returns an immutable consensus-node snapshot. */ Set<ConsensusNode> getConsensusNodes();
    /** Returns an immutable mirror-node snapshot. */ Set<MirrorNode> getMirrorNodes();
}
