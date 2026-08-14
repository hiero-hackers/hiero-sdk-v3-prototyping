// Source: spec/base/solo.md (solo.SoloNetworkSetting).

package org.hiero.sdk.v3.solo;

import java.util.Set;
import org.hiero.sdk.v3.ledger.ConsensusNode;
import org.hiero.sdk.v3.ledger.MirrorNode;
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.ledger.config.NetworkSetting;

/** Base for Solo network settings. */
public abstract class SoloNetworkSetting extends NetworkSetting {
    /** Creates immutable Solo network-setting data. */
    protected SoloNetworkSetting(Network<?> network, Set<ConsensusNode> consensusNodes, Set<MirrorNode> mirrorNodes) {
        super(network, consensusNodes, mirrorNodes);
    }
}
