// Source: spec/base/ledger.md (ledger#MirrorNode).

package org.hiero.sdk.v3.ledger;

import java.util.Objects;

/** Mirror-node REST endpoint descriptor. */
public record MirrorNode(String restBaseUrl) {
    /** Validates the endpoint text without parsing or network access. */
    public MirrorNode { Objects.requireNonNull(restBaseUrl, "restBaseUrl"); }
}
