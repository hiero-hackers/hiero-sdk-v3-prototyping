// Source: spec/base/hedera.md (hedera.HederaNetworkSetting).

package org.hiero.sdk.v3.hedera;

import java.util.Set;
import org.hiero.sdk.v3.ledger.ConsensusNode;
import org.hiero.sdk.v3.ledger.MirrorNode;
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.ledger.config.NetworkSetting;

/** Base for Hedera network settings. */
public abstract class HederaNetworkSetting extends NetworkSetting {
    /** Creates immutable Hedera network-setting data. */
    protected HederaNetworkSetting(Network<?> network, Set<ConsensusNode> consensusNodes, Set<MirrorNode> mirrorNodes) {
        super(network, consensusNodes, mirrorNodes);
    }
}
