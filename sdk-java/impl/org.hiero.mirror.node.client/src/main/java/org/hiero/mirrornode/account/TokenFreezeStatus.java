package org.hiero.mirrornode.account;


/// Whether an account's balance of a token is frozen.
public enum TokenFreezeStatus {

    /// the token has no freeze key
    NOT_APPLICABLE,
    FROZEN,
    UNFROZEN;
}
