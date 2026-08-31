// Generated from spec/base/ledger.md (ledger#TransactionId); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

import java.time.ZonedDateTime;
import org.jspecify.annotations.Nullable;

/** Provider-owned transaction identifier data contract. */
public interface TransactionId {
    /** Returns the payer account. */ AccountId accountId();
    /** Returns the valid-start time. */ ZonedDateTime validStart();
    /** Returns the optional internal-transaction nonce. */ @Nullable Integer nonce();
}
