// Generated from spec/base/hedera.md (hedera#Hbar); DO NOT EDIT.

package org.hiero.sdk.v3.hedera;

import org.hiero.sdk.v3.nativetoken.NativeToken;

/** An HBAR amount. */
public interface Hbar extends NativeToken<Hbar, HbarUnit> {
    /** Returns this amount in tinybars. */
    long toTinybars();
}
