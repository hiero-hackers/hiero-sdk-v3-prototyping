package org.hiero.sdk.v3.ledger;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * An immutable network identity associated with its concrete native-token unit type.
 *
 * <p>The ID is copied on construction and every read. Its bytes, including empty IDs and leading
 * zeros, and the optional name are preserved without normalization. The unit is retained by
 * reference and must be immutable as required by {@link NativeTokenUnit}.
 *
 * <p>Java value equality compares ID contents, name, and the unit's own equality semantics. It does
 * not define network-ID uniqueness or imply that equal IDs have equal names or units. This type
 * stores identity only and performs no metadata lookup or network access.
 *
 * @param <UNIT> the concrete native-token unit type
 * @param id the required ledger ID, copied on construction and access
 * @param name the optional human-readable name, or {@code null} when absent
 * @param nativeTokenUnit the required immutable native-token unit
 */
public record Network<UNIT extends NativeTokenUnit>(
        byte @NonNull [] id, @Nullable String name, @NonNull UNIT nativeTokenUnit) {
    /**
     * Creates a network identity, copying the supplied ID and retaining its name and immutable unit.
     *
     * @param id the required ledger ID, with no imposed length or format restrictions
     * @param name the optional name, preserved as supplied, including {@code null} or an empty string
     * @param nativeTokenUnit the required immutable native-token unit
     * @throws NullPointerException if {@code id} or {@code nativeTokenUnit} is {@code null}
     */
    public Network(final byte @NonNull [] id, @Nullable final String name, @NonNull final UNIT nativeTokenUnit) {
        this.id = Objects.requireNonNull(id, "id must not be null").clone();
        this.name = name;
        this.nativeTokenUnit = Objects.requireNonNull(nativeTokenUnit, "nativeTokenUnit must not be null");
    }

    /**
     * Returns a fresh copy of the ledger ID, preserving its exact byte order and contents.
     *
     * @return an ID copy whose mutation cannot change this network's stored identity
     */
    @Override
    public byte @NonNull [] id() {
        return id.clone();
    }

    /**
     * Returns the supplied human-readable name, or {@code null} when absent.
     *
     * @return the unchanged optional name
     */
    @Override
    public @Nullable String name() {
        return name;
    }

    /**
     * Returns the associated immutable unit with its concrete type preserved.
     *
     * @return the same native-token unit supplied at construction
     */
    @Override
    public @NonNull UNIT nativeTokenUnit() {
        return nativeTokenUnit;
    }

    /**
     * Compares ID contents, optional name, and unit using the unit's own equality semantics.
     *
     * @param other the object to compare, or {@code null}
     * @return {@code true} when all three stored values are equal
     */
    @Override
    public boolean equals(@Nullable final Object other) {
        return this == other || other instanceof Network<?> network
                && Arrays.equals(id, network.id)
                && Objects.equals(name, network.name)
                && nativeTokenUnit.equals(network.nativeTokenUnit);
    }

    /**
     * Returns a hash based on ID contents, optional name, and unit.
     *
     * @return the value hash, consistent with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return 31 * (31 * Arrays.hashCode(id) + Objects.hashCode(name)) + nativeTokenUnit.hashCode();
    }

    /**
     * Returns a representation containing the ID bytes by content, optional name, and unit.
     *
     * @return the network's value representation
     */
    @Override
    public @NonNull String toString() {
        return "Network[id=" + Arrays.toString(id) + ", name=" + name + ", nativeTokenUnit=" + nativeTokenUnit + "]";
    }
}
