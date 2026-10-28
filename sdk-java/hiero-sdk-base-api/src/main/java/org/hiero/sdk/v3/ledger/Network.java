package org.hiero.sdk.v3.ledger;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.jspecify.annotations.Nullable;

/**
 * The identity of a network: its ledger ID, an optional human-readable name, and the native token unit it uses.
 *
 * <p>This is an immutable value type. The ID array is copied on construction and on every call to {@link #id()}, so
 * neither the supplied array nor a returned array can modify the stored identity. The unit is stored as supplied;
 * unit providers must themselves be immutable. No restriction is placed on the ID length, format, or contents, or on
 * the name.
 *
 * <p>Two networks are equal when their IDs have equal contents, their names are equal (or both absent), and their
 * units are equal. {@link #toString()} renders the ID as lowercase hexadecimal.
 *
 * @param id the network identifier; copied, never {@code null}, may be empty
 * @param name the human-readable name, or {@code null} when absent; preserved exactly
 * @param nativeTokenUnit the native token unit of the network, never {@code null}
 * @param <UNIT> the concrete native token unit type
 */
public record Network<UNIT extends NativeTokenUnit>(byte[] id, @Nullable String name, UNIT nativeTokenUnit) {
    /**
     * Creates a network identity.
     *
     * @throws NullPointerException if {@code id} or {@code nativeTokenUnit} is {@code null}
     */
    public Network {
        id = Objects.requireNonNull(id, "id").clone();
        Objects.requireNonNull(nativeTokenUnit, "nativeTokenUnit");
    }

    /**
     * Returns a copy of the network identifier.
     *
     * @return a new array holding the identifier; modifying it does not affect this network
     */
    @Override
    public byte[] id() {
        return id.clone();
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof Network<?> that
                && Arrays.equals(id, that.id)
                && Objects.equals(name, that.name)
                && nativeTokenUnit.equals(that.nativeTokenUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(id), name, nativeTokenUnit);
    }

    @Override
    public String toString() {
        return "Network[id=" + HexFormat.of().formatHex(id) + ", name=" + name + ", nativeTokenUnit="
                + nativeTokenUnit + "]";
    }
}
