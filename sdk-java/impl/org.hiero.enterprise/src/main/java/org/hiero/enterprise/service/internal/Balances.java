package org.hiero.enterprise.service.internal;

import org.hiero.enterprise.service.ServiceException;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.Network;
import org.hiero.nativeToken.NativeToken;

/// Amounts of the native token of a network.
public final class Balances {

    private Balances() {
    }

    /// The amount of base units of the native token of the network, as a native token.
    ///
    /// @param network   the network
    /// @param baseUnits the amount in base units
    /// @return the amount
    /// @throws ServiceException if the native token of the network has no type the SDK knows
    public static NativeToken<?, ?> ofBaseUnits(final Network<?> network, final long baseUnits) {
        if (network.nativeTokenUnit() instanceof HbarUnit) {
            return new Hbar(baseUnits, HbarUnit.TINYBAR);
        }
        throw new ServiceException("The native token unit " + network.nativeTokenUnit()
                + " of the network has no NativeToken type", null);
    }
}
