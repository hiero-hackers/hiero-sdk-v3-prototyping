// Source: spec/base/hedera.md (hedera.Hbar).

package org.hiero.sdk.v3.hedera;

import org.hiero.sdk.v3.nativetoken.NativeToken;

/** An HBAR amount whose conversions are supplied by a later implementation. */
public abstract class Hbar extends NativeToken<Hbar, HbarUnit> {
    /** Creates structurally valid HBAR amount data. */
    protected Hbar(long amount, HbarUnit unit) { super(amount, unit); }
    /** Returns this amount in tinybars. */
    public abstract long toTinybars();
}
