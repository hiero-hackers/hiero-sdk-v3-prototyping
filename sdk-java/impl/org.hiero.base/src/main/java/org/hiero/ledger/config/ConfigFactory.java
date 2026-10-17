package org.hiero.ledger.config;

import java.util.NoSuchElementException;
import org.hiero.ledger.config.internal.NetworkSettings;

/// Factory methods of the package `org.hiero.ledger.config`.
public final class ConfigFactory {

    /// Registers a network setting under the given identifier.
    public static void registerNetworkSetting(final String identifier, final NetworkSetting setting) {
        NetworkSettings.register(identifier, setting);
    }

    /// Returns the network setting registered for the given identifier; throws if none exists.
    /// Network settings can be added as plug and play by external modules
    ///
    /// @throws NoSuchElementException if a not found error occurs
    public static NetworkSetting getNetworkSetting(final String identifier) {
        return NetworkSettings.get(identifier);
    }

    private ConfigFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
