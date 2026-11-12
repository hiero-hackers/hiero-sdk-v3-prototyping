package org.hiero.ledger;

import java.util.Objects;

/// Represents a block node on a network, reached over gRPC.
///
/// @param host the host name or IP address of the block node
/// @param port the gRPC port of the block node
/// @param requiresTls whether the block node is reached over TLS
public record BlockNode(String host, int port, boolean requiresTls) {

    /// Creates a new `BlockNode`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public BlockNode {
        Objects.requireNonNull(host, "host must not be null");
        if (host.length() < 1) {
            throw new IllegalArgumentException("host must be at least 1 characters long");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
    }
}
