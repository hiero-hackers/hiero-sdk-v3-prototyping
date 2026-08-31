// Source: spec/base/ledger.md (ledger#TransactionId).

package org.hiero.sdk.v3.ledger;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Immutable transaction-identifier state with provider-supplied representations. */
public abstract class TransactionId {
    private final AccountId accountId;
    private final ZonedDateTime validStart;
    private final @Nullable Integer nonce;

    /** Creates validated transaction-identifier state. */
    protected TransactionId(AccountId accountId, ZonedDateTime validStart, @Nullable Integer nonce) {
        this.accountId = Objects.requireNonNull(accountId, "accountId");
        this.validStart = Objects.requireNonNull(validStart, "validStart");
        this.nonce = nonce;
    }

    /** Returns the payer account. */
    public final AccountId accountId() { return accountId; }

    /** Returns the valid-start time. */
    public final ZonedDateTime validStart() { return validStart; }

    /** Returns the optional internal-transaction nonce. */
    public final @Nullable Integer nonce() { return nonce; }

    /** Returns the implementation-defined canonical transaction identifier. */
    @Override
    public abstract String toString();

    /** Returns the implementation-defined checksum representation. */
    public abstract String toStringWithChecksum();
}
