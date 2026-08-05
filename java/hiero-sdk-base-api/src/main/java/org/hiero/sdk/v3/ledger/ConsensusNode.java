// Generated from spec/base/ledger.md (ledger#ConsensusNode); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

import java.util.Objects;

/** Consensus-node routing and fee identity. */
public record ConsensusNode(IpAddress ip, int port, AccountId account) {
    /** Validates node values and unsigned-16-bit port semantics. */
    public ConsensusNode {
        Objects.requireNonNull(ip, "ip"); Objects.requireNonNull(account, "account");
        if (port < 0 || port > 65_535) throw new IllegalArgumentException("port must be between 0 and 65535");
    }
}
