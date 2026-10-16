package org.hiero.ledger;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// Single IP address representation, stored as raw network-order bytes. Today the type
/// only accepts IPv4 (exactly 4 bytes), matching the consensus-node wire format, which is
/// IPv4-only. IPv6 support may be added later without changing the type. Until then, IPv6
/// reachability for a node is achieved via a domain name in `ServiceEndpoint.domainName`
/// whose DNS AAAA record resolves to the v6 address.
///
/// @param bytes 4 bytes, network byte order (IPv4); constraint widens to allow 16 bytes once IPv6 lands
public record IpAddress(byte[] bytes) {

    /// Creates a new `IpAddress`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public IpAddress {
        Objects.requireNonNull(bytes, "bytes must not be null");
        bytes = bytes.clone();
        if (bytes.length < 4) {
            throw new IllegalArgumentException("bytes must contain at least 4 element(s)");
        }
        if (bytes.length > 4) {
            throw new IllegalArgumentException("bytes must contain at most 4 element(s)");
        }
    }

    /// 4 bytes, network byte order (IPv4); constraint widens to allow 16 bytes once IPv6 lands
    ///
    /// Returns a copy of the `bytes` array.
    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof IpAddress other
            && Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(bytes));
    }

    /// Dotted-quad form ("10.0.0.7") today; once IPv6 lands, this returns the RFC 5952
    /// canonical form for 16-byte values.
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            text.append(i == 0 ? "" : ".").append(bytes[i] & 0xff);
        }
        return text.toString();
    }

    /// Parses an IpAddress from its textual form. Today only dotted-quad IPv4 ("10.0.0.7") is
    /// accepted; everything else throws.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static IpAddress fromString(final String value) {
        if (!value.matches("(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}")) {
            throw new IllegalArgumentException("'" + value + "' is no IPv4 address in dotted-quad form");
        }
        final String[] parts = value.split("\\.");
        final byte[] address = new byte[4];
        for (int i = 0; i < 4; i++) {
            address[i] = (byte) Integer.parseInt(parts[i]);
        }
        return new IpAddress(address);
    }

    /// Wraps raw network-order bytes. `value.length` must equal 4; otherwise throws.
    /// Will also accept 16 bytes once IPv6 support is added.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static IpAddress fromBytes(final byte[] value) {
        if (value.length != 4) {
            throw new IllegalArgumentException("An IPv4 address has 4 bytes, not " + value.length);
        }
        return new IpAddress(value);
    }
}
