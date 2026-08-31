// Generated from spec/base/ledger-config.md (ledger.config#NetworkSettingRegistry); DO NOT EDIT.

package org.hiero.sdk.v3.ledger.config;

import java.util.NoSuchElementException;

/** Body-free network-setting registry contract with no global state. */
public interface NetworkSettingRegistry {
    /** Registers a setting under an identifier. */ void registerNetworkSetting(String identifier, NetworkSetting setting);
    /** Returns a setting or throws when the identifier is absent. */ NetworkSetting getNetworkSetting(String identifier) throws NoSuchElementException;
}
