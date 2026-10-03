package org.hiero.sdk.v3.metalang.semantic;

import java.util.Optional;
import java.util.Set;

/**
 * Names that attributes and methods must not have (guideline section "Reserved names"): they clash with members that
 * every type inherits in a target language and cannot be renamed by escaping, unlike keywords.
 */
public final class ReservedNames {

    /** Attribute names of every type: the accessor would clash with a method of {@code java.lang.Object}. */
    public static final Set<String> ATTRIBUTES = Set.of("clone", "finalize", "getClass", "hashCode", "notify",
            "notifyAll", "toString", "wait");

    /** Additional attribute names of enums: the accessor would clash with a method of {@code java.lang.Enum}. */
    public static final Set<String> ENUM_ATTRIBUTES = Set.of("describeConstable", "getDeclaringClass", "name",
            "ordinal", "values");

    /** Method names of every type: final methods of {@code java.lang.Object} (or with fixed semantics). */
    public static final Set<String> METHODS = Set.of("clone", "finalize", "getClass", "notify", "notifyAll", "wait");

    /**
     * Additional method names of enums: final or generated methods of {@code java.lang.Enum}. An explicit
     * {@code values()} is reported by its own rule ({@code enum.explicit-values-method}).
     */
    public static final Set<String> ENUM_METHODS = Set.of("compareTo", "describeConstable", "equals",
            "getDeclaringClass", "hashCode", "name", "ordinal", "valueOf");

    private ReservedNames() {
    }

    /**
     * Returns why an attribute name is reserved.
     *
     * @param name   the attribute name
     * @param inEnum whether the attribute belongs to an enum
     * @return the clashing member (e.g. {@code Object.hashCode()}), if the name is reserved
     */
    public static Optional<String> attribute(final String name, final boolean inEnum) {
        if (ATTRIBUTES.contains(name)) {
            return Optional.of("Object." + name + "()");
        }
        return inEnum && ENUM_ATTRIBUTES.contains(name) ? Optional.of("Enum." + name + "()") : Optional.empty();
    }

    /**
     * Returns why a method name is reserved.
     *
     * @param name   the method name
     * @param inEnum whether the method belongs to an enum
     * @return the clashing member, if the name is reserved
     */
    public static Optional<String> method(final String name, final boolean inEnum) {
        if (METHODS.contains(name)) {
            return Optional.of("Object." + name + "()");
        }
        return inEnum && ENUM_METHODS.contains(name) ? Optional.of("Enum." + name + "()") : Optional.empty();
    }
}
