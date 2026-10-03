package org.hiero.sdk.v3.metalang.generator.java;

import java.util.Map;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;

/**
 * The custom functional interfaces of the function types that no {@code java.util.function} interface matches
 * (three or more parameters, varargs), by the text of the function type ({@code function<bool onMessage(...)>}).
 *
 * @param names    the generated interface of every function type
 * @param types    the function type of every generated interface, by the text of the function type
 * @param problems why a function type has no interface (e.g. a name clash), by the text of the function type
 */
record FunctionInterfaces(Map<String, QualifiedName> names, Map<String, Type.FunctionType> types,
                          Map<String, String> problems) {

    /** No custom functional interfaces. */
    static final FunctionInterfaces NONE = new FunctionInterfaces(Map.of(), Map.of(), Map.of());

    /**
     * Creates the custom functional interfaces.
     *
     * @param names    the interface names
     * @param types    the function types
     * @param problems the problems
     */
    FunctionInterfaces {
        names = Map.copyOf(Objects.requireNonNull(names, "names must not be null"));
        types = Map.copyOf(Objects.requireNonNull(types, "types must not be null"));
        problems = Map.copyOf(Objects.requireNonNull(problems, "problems must not be null"));
    }

    /**
     * Returns the generated interface of a function type.
     *
     * @param function the function type
     * @return the interface
     * @throws JavaTypes.UnsupportedTypeException if the function type has no interface
     */
    QualifiedName of(final Type.FunctionType function) {
        final QualifiedName name = names.get(function.text());
        if (name == null) {
            throw JavaTypes.UnsupportedTypeException.withMessage(problems.getOrDefault(function.text(),
                    "Function type '" + function.text() + "' has no functional interface"));
        }
        return name;
    }
}
