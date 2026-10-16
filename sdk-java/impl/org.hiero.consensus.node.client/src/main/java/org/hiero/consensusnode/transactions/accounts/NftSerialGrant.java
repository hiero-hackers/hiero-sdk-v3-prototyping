package org.hiero.consensusnode.transactions.accounts;

import java.util.List;
import java.util.Objects;

/// Per-serial rights to the listed serials. Additive across multiple approve transactions.
public record NftSerialGrant(List<Long> serials) implements NftGrant {

    /// Creates a new `NftSerialGrant`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public NftSerialGrant {
        Objects.requireNonNull(serials, "serials must not be null");
        serials = List.copyOf(serials);
        if (serials.size() < 1) {
            throw new IllegalArgumentException("serials must contain at least 1 element(s)");
        }
    }
}
