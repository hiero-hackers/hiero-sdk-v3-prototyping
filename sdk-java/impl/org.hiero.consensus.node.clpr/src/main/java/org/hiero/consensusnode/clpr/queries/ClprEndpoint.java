package org.hiero.consensusnode.clpr.queries;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// A CLPR relay endpoint of a ledger, as it appears in its endpoint manifest.
///
/// @param serviceEndpoint where the endpoint is reached
/// @param tlsCertificate the DER-encoded certificate of the endpoint for mutual TLS
/// @param accountId the account on the ledger that the endpoint belongs to
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public record ClprEndpoint(ClprServiceEndpoint serviceEndpoint, byte[] tlsCertificate, byte[] accountId) {

    /// Creates a new `ClprEndpoint`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ClprEndpoint {
        Objects.requireNonNull(serviceEndpoint, "serviceEndpoint must not be null");
        Objects.requireNonNull(tlsCertificate, "tlsCertificate must not be null");
        tlsCertificate = tlsCertificate.clone();
        Objects.requireNonNull(accountId, "accountId must not be null");
        accountId = accountId.clone();
    }

    /// the DER-encoded certificate of the endpoint for mutual TLS
    ///
    /// Returns a copy of the `tlsCertificate` array.
    @Override
    public byte[] tlsCertificate() {
        return tlsCertificate.clone();
    }

    /// the account on the ledger that the endpoint belongs to
    ///
    /// Returns a copy of the `accountId` array.
    @Override
    public byte[] accountId() {
        return accountId.clone();
    }

    /// Compares `tlsCertificate` and `accountId` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ClprEndpoint other
            && Objects.equals(serviceEndpoint, other.serviceEndpoint)
            && Arrays.equals(tlsCertificate, other.tlsCertificate)
            && Arrays.equals(accountId, other.accountId);
    }

    /// Hashes `tlsCertificate` and `accountId` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(serviceEndpoint, Arrays.hashCode(tlsCertificate), Arrays.hashCode(accountId));
    }

    /// Shows only the length of `tlsCertificate` and `accountId` instead of the identity of the array object.
    @Override
    public String toString() {
        return "ClprEndpoint[serviceEndpoint=" + serviceEndpoint + ", tlsCertificate=" + "byte[" + tlsCertificate.length + "]" + ", accountId=" + "byte[" + accountId.length + "]" + "]";
    }
}
