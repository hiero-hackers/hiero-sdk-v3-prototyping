package org.hiero.consensusnode.clpr.queries;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The CLPR endpoints of a ledger, with the state proof that a peer ledger passes to its verifier contract.
///
/// @param version the version of the manifest
/// @param serviceAddress the address of the CLPR service of the ledger
/// @param endpoints the endpoints of the ledger; may be empty
/// @param manifestStateProof The opaque state proof of the manifest from a signed snapshot; pass it unchanged to the peer ledger. Empty when
///   no usable snapshot exists, also if the endpoint list is not empty.
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public record ClprEndpointManifest(
        long version,
        byte[] serviceAddress,
        List<ClprEndpoint> endpoints,
        byte[] manifestStateProof) {

    /// Creates a new `ClprEndpointManifest`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ClprEndpointManifest {
        Objects.requireNonNull(serviceAddress, "serviceAddress must not be null");
        serviceAddress = serviceAddress.clone();
        Objects.requireNonNull(endpoints, "endpoints must not be null");
        endpoints = List.copyOf(endpoints);
        Objects.requireNonNull(manifestStateProof, "manifestStateProof must not be null");
        manifestStateProof = manifestStateProof.clone();
    }

    /// the address of the CLPR service of the ledger
    ///
    /// Returns a copy of the `serviceAddress` array.
    @Override
    public byte[] serviceAddress() {
        return serviceAddress.clone();
    }

    /// The opaque state proof of the manifest from a signed snapshot; pass it unchanged to the peer ledger. Empty when
    /// no usable snapshot exists, also if the endpoint list is not empty.
    ///
    /// Returns a copy of the `manifestStateProof` array.
    @Override
    public byte[] manifestStateProof() {
        return manifestStateProof.clone();
    }

    /// Compares `serviceAddress` and `manifestStateProof` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ClprEndpointManifest other
            && version == other.version
            && Arrays.equals(serviceAddress, other.serviceAddress)
            && Objects.equals(endpoints, other.endpoints)
            && Arrays.equals(manifestStateProof, other.manifestStateProof);
    }

    /// Hashes `serviceAddress` and `manifestStateProof` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(version, Arrays.hashCode(serviceAddress), endpoints, Arrays.hashCode(manifestStateProof));
    }

    /// Shows only the length of `serviceAddress` and `manifestStateProof` instead of the identity of the array object.
    @Override
    public String toString() {
        return "ClprEndpointManifest[version=" + version + ", serviceAddress=" + "byte[" + serviceAddress.length + "]" + ", endpoints=" + endpoints + ", manifestStateProof=" + "byte[" + manifestStateProof.length + "]" + "]";
    }
}
