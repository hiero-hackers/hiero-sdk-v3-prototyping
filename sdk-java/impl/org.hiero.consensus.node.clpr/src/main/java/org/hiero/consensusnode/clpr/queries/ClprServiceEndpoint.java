package org.hiero.consensusnode.clpr.queries;

import java.util.Objects;

/// The network address of a CLPR endpoint: a numeric IPv4 or IPv6 address, no host name.
///
/// @param ipAddress the numeric IPv4 or IPv6 address
/// @param port the port
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public record ClprServiceEndpoint(String ipAddress, int port) {

    /// Creates a new `ClprServiceEndpoint`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ClprServiceEndpoint {
        Objects.requireNonNull(ipAddress, "ipAddress must not be null");
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
    }
}
