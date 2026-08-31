// Generated from spec/base/ledger.md (ledger#Network); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.sdk.v3.nativetoken.NativeTokenUnit;
import org.jspecify.annotations.Nullable;

/** Immutable identity and native-unit description of a network. */
public final class Network<Unit extends NativeTokenUnit> {
    private final byte[] id;
    private final @Nullable String name;
    private final Unit nativeTokenUnit;
    /** Creates a network value and copies its identifier. */
    public Network(byte[] id, @Nullable String name, Unit nativeTokenUnit) {
        this.id = Objects.requireNonNull(id, "id").clone();
        this.name = name;
        this.nativeTokenUnit = Objects.requireNonNull(nativeTokenUnit, "nativeTokenUnit");
    }
    /** Returns a copy of the network identifier. */
    public byte[] id() { return id.clone(); }
    /** Returns the optional display name. */
    public @Nullable String name() { return name; }
    /** Returns the network native-token unit. */
    public Unit nativeTokenUnit() { return nativeTokenUnit; }
    @Override public boolean equals(Object other) {
        return other instanceof Network<?> value && Arrays.equals(id, value.id)
                && Objects.equals(name, value.name) && nativeTokenUnit.equals(value.nativeTokenUnit);
    }
    @Override public int hashCode() { return Objects.hash(Arrays.hashCode(id), name, nativeTokenUnit); }
}
