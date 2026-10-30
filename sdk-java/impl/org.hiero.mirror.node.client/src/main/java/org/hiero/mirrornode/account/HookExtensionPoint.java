package org.hiero.mirrornode.account;


/// The point of a transaction at which a hook is called.
public enum HookExtensionPoint {

    /// called to approve a transfer from the account in place of an allowance
    ACCOUNT_ALLOWANCE_HOOK;
}
