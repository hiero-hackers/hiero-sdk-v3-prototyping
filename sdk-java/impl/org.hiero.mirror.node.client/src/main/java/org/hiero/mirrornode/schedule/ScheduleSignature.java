package org.hiero.mirrornode.schedule;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// A signature collected by a schedule.
///
/// @param consensusTimestamp consensus time at which the signature was added
/// @param publicKeyPrefix prefix of the public key the signature belongs to
public record ScheduleSignature(
        ZonedDateTime consensusTimestamp,
        byte[] publicKeyPrefix,
        byte[] signature,
        SignatureType type) {

    /// Creates a new `ScheduleSignature`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ScheduleSignature {
        Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null");
        Objects.requireNonNull(publicKeyPrefix, "publicKeyPrefix must not be null");
        publicKeyPrefix = publicKeyPrefix.clone();
        Objects.requireNonNull(signature, "signature must not be null");
        signature = signature.clone();
        Objects.requireNonNull(type, "type must not be null");
    }

    /// prefix of the public key the signature belongs to
    ///
    /// Returns a copy of the `publicKeyPrefix` array.
    @Override
    public byte[] publicKeyPrefix() {
        return publicKeyPrefix.clone();
    }

    /// Returns a copy of the `signature` array.
    @Override
    public byte[] signature() {
        return signature.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ScheduleSignature other
            && Objects.equals(consensusTimestamp, other.consensusTimestamp)
            && Arrays.equals(publicKeyPrefix, other.publicKeyPrefix)
            && Arrays.equals(signature, other.signature)
            && Objects.equals(type, other.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consensusTimestamp, Arrays.hashCode(publicKeyPrefix), Arrays.hashCode(signature), type);
    }

    @Override
    public String toString() {
        return "ScheduleSignature[consensusTimestamp=" + consensusTimestamp + ", publicKeyPrefix=" + "byte[" + publicKeyPrefix.length + "]" + ", signature=" + "byte[" + signature.length + "]" + ", type=" + type + "]";
    }
}
