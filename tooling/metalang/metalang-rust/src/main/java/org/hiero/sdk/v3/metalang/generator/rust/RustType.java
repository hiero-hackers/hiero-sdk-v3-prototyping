package org.hiero.sdk.v3.metalang.generator.rust;

import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.model.QualifiedName;

/**
 * The Rust form of a meta-language type, as the generator stores, passes and returns it. Rendering needs the imports
 * of the file; the properties (copy, equality, hash, debug) decide which traits a struct can derive and how a getter
 * returns the value.
 */
sealed interface RustType {

    /**
     * Renders the owned type, e.g. {@code Option<Vec<AccountId>>}.
     *
     * @param imports the imports of the file
     * @return the Rust type
     */
    String render(RustImports imports);

    /** A primitive or library type that is {@code Copy} or cheap to compare: integers, {@code f64}, dates, ... */
    record Primitive(String path, boolean copy, boolean eq, boolean hash) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return path; // the full path of a library type is clearer than an import
        }
    }

    /** {@code String}. */
    record Text() implements RustType {
        @Override
        public String render(final RustImports imports) {
            return "String";
        }
    }

    /** {@code Vec<u8>}. */
    record Bytes() implements RustType {
        @Override
        public String render(final RustImports imports) {
            return "Vec<u8>";
        }
    }

    /** {@code Vec<T>}. */
    record VecOf(RustType element) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return "Vec<" + element.render(imports) + ">";
        }
    }

    /** {@code HashSet<T>}. */
    record SetOf(RustType element) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return imports.external("std::collections::HashSet") + "<" + element.render(imports) + ">";
        }
    }

    /** {@code HashMap<K, V>}. */
    record MapOf(RustType key, RustType value) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return imports.external("std::collections::HashMap") + "<" + key.render(imports) + ", "
                    + value.render(imports) + ">";
        }
    }

    /** {@code Vec<(K, V)>}: a map whose keys cannot be hashed. */
    record Pairs(RustType key, RustType value) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return "Vec<(" + key.render(imports) + ", " + value.render(imports) + ")>";
        }
    }

    /** {@code Option<T>}: a nullable value. */
    record Optional(RustType inner) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return "Option<" + inner.render(imports) + ">";
        }
    }

    /** A generated struct, by value. */
    record Struct(QualifiedName name, List<RustType> arguments) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return imports.type(name) + RustType.arguments(arguments, imports);
        }
    }

    /** A generated enum without data (a meta-language enum), by value. */
    record Enum(QualifiedName name) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return imports.type(name);
        }
    }

    /** A generated enum with one variant per permitted type (a sealed abstraction), by value. */
    record Sealed(QualifiedName name) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return imports.type(name);
        }
    }

    /** A shared trait object of a generated trait: {@code Arc<dyn Trait<..>>}. */
    record Dyn(QualifiedName name, List<RustType> arguments) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return imports.external("std::sync::Arc") + "<dyn " + imports.type(name)
                    + RustType.arguments(arguments, imports) + ">";
        }
    }

    /** A value of any type: {@code Arc<dyn Any + Send + Sync>}. */
    record AnyValue() implements RustType {
        @Override
        public String render(final RustImports imports) {
            return imports.external("std::sync::Arc") + "<dyn " + imports.external("std::any::Any")
                    + " + Send + Sync>";
        }
    }

    /** A type parameter. */
    record Parameter(String name) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return name;
        }
    }

    /** {@code Self} in a trait. */
    record SelfType() implements RustType {
        @Override
        public String render(final RustImports imports) {
            return "Self";
        }
    }

    /** A function value: {@code Arc<dyn Fn(A) -> R + Send + Sync>}. */
    record Function(List<RustType> parameters, RustType result) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return imports.external("std::sync::Arc") + "<dyn Fn(" + parameters.stream()
                    .map(p -> p.render(imports)).collect(Collectors.joining(", ")) + ")"
                    + (result instanceof Unit ? "" : " -> " + result.render(imports)) + " + Send + Sync>";
        }
    }

    /** One item of a stream that can fail: {@code StreamItem<T>}. */
    record StreamItem(RustType value) implements RustType {
        @Override
        public String render(final RustImports imports) {
            return imports.support("StreamItem") + "<" + value.render(imports) + ">";
        }
    }

    /** {@code ()}: no value. */
    record Unit() implements RustType {
        @Override
        public String render(final RustImports imports) {
            return "()";
        }
    }

    /** Whether values of the type can be constants ({@code const}): integers, {@code f64}, {@code bool}, durations. */
    static boolean isConstant(final RustType type) {
        return type instanceof Primitive primitive && (!primitive.path().contains("::")
                || primitive.path().startsWith("ethnum::") || primitive.path().equals("std::time::Duration"));
    }

    private static String arguments(final List<RustType> arguments, final RustImports imports) {
        return arguments.isEmpty() ? "" : "<" + arguments.stream().map(a -> a.render(imports))
                .collect(Collectors.joining(", ")) + ">";
    }
}
