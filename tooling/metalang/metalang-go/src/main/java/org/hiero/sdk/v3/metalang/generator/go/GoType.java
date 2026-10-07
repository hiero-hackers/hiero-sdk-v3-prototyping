package org.hiero.sdk.v3.metalang.generator.go;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The Go form of a meta-language type.
 *
 * <p>Rendering needs the imports of the file, because Go qualifies a foreign name by its package. Two properties
 * drive decisions elsewhere in the generator:
 *
 * <ul>
 *   <li>{@link #comparable()} - whether Go allows the type as a map key. A {@code set<T>} becomes
 *       {@code map[T]struct{}} only if {@code T} is comparable; a struct with a slice attribute is not.</li>
 *   <li>{@link #nilable()} - whether the zero value is already {@code nil}. An {@code @@nullable} attribute of
 *       such a type needs no pointer.</li>
 * </ul>
 */
sealed interface GoType {

    /**
     * Renders the type.
     *
     * @param imports the imports of the file, extended by what the type needs
     * @return the Go type
     */
    String render(GoImports imports);

    /**
     * Whether Go allows the type as a map key or set element.
     *
     * @return {@code true} if comparable
     */
    default boolean comparable() {
        return true;
    }

    /**
     * Whether the zero value of the type is {@code nil}.
     *
     * @return {@code true} if nilable
     */
    default boolean nilable() {
        return false;
    }

    /** A predeclared type: {@code string}, {@code int64}, {@code bool}, {@code float64}. */
    record Predeclared(String name) implements GoType {
        @Override
        public String render(final GoImports imports) {
            return name;
        }
    }

    /**
     * A type from another package: {@code time.Time}, {@code uuid.UUID}.
     *
     * @param path       the import path
     * @param name       the exported name
     * @param comparable whether Go allows it as a map key
     */
    record Library(String path, String name, boolean comparable) implements GoType {
        @Override
        public String render(final GoImports imports) {
            return imports.qualify(path, name);
        }
    }

    /** {@code []byte}. */
    record Bytes() implements GoType {
        @Override
        public String render(final GoImports imports) {
            return "[]byte";
        }

        @Override
        public boolean comparable() {
            return false;
        }

        @Override
        public boolean nilable() {
            return true;
        }
    }

    /** {@code []T}. */
    record Slice(GoType element) implements GoType {
        @Override
        public String render(final GoImports imports) {
            return "[]" + element.render(imports);
        }

        @Override
        public boolean comparable() {
            return false;
        }

        @Override
        public boolean nilable() {
            return true;
        }
    }

    /** {@code map[K]V}; a {@code set<T>} is a map to the empty struct. */
    record MapOf(GoType key, GoType value) implements GoType {
        /** The value type of a set: {@code struct{}} occupies no memory. */
        static final GoType UNIT = new Predeclared("struct{}");

        @Override
        public String render(final GoImports imports) {
            return "map[" + key.render(imports) + "]" + value.render(imports);
        }

        @Override
        public boolean comparable() {
            return false;
        }

        @Override
        public boolean nilable() {
            return true;
        }
    }

    /**
     * A type generated from the specs.
     *
     * @param path       the import path of its package
     * @param name       the exported name
     * @param iface      whether it is an interface (an abstraction) rather than a struct
     * @param comparable whether Go allows it as a map key
     * @param arguments  the type arguments
     */
    record Declared(String path, String name, boolean iface, boolean comparable, List<GoType> arguments)
            implements GoType {
        @Override
        public String render(final GoImports imports) {
            final String base = imports.qualify(path, name);
            return arguments.isEmpty() ? base : base + "[" + arguments.stream()
                    .map(a -> a.render(imports)).collect(Collectors.joining(", ")) + "]";
        }

        @Override
        public boolean nilable() {
            return iface;
        }
    }

    /** {@code *T}: an {@code @@nullable} attribute of a type whose zero value is not {@code nil}. */
    record Pointer(GoType target) implements GoType {
        @Override
        public String render(final GoImports imports) {
            return "*" + target.render(imports);
        }

        @Override
        public boolean nilable() {
            return true;
        }
    }

    /** {@code func(A, B) R}. */
    record Func(List<GoType> parameters, GoType result) implements GoType {
        @Override
        public String render(final GoImports imports) {
            final String params = parameters.stream().map(p -> p.render(imports))
                    .collect(Collectors.joining(", "));
            return "func(" + params + ")" + (result instanceof Void ? "" : " " + result.render(imports));
        }

        @Override
        public boolean comparable() {
            return false;
        }

        @Override
        public boolean nilable() {
            return true;
        }
    }

    /** {@code any}: the empty interface. */
    record Any() implements GoType {
        @Override
        public String render(final GoImports imports) {
            return "any";
        }

        @Override
        public boolean nilable() {
            return true;
        }
    }

    /** A type parameter of the enclosing type or method. */
    record Variable(String name) implements GoType {
        @Override
        public String render(final GoImports imports) {
            return name;
        }
    }

    /** {@code iter.Seq2[T, error]}: the result of a {@code streamResult<T>}. */
    record StreamResult(GoType item) implements GoType {
        @Override
        public String render(final GoImports imports) {
            return imports.qualify("iter", "Seq2") + "[" + item.render(imports) + ", error]";
        }

        @Override
        public boolean comparable() {
            return false;
        }

        @Override
        public boolean nilable() {
            return true;
        }
    }

    /** The absent return type; it renders as nothing. */
    record Void() implements GoType {
        @Override
        public String render(final GoImports imports) {
            return "";
        }
    }
}
