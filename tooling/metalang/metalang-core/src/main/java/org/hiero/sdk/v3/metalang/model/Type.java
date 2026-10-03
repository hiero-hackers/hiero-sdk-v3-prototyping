package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.jspecify.annotations.Nullable;

/**
 * A fully resolved type. Unlike the syntactic {@link org.hiero.sdk.v3.metalang.ast.TypeRef}, every name is already
 * resolved: declared types are identified by their {@link QualifiedName}, generic parameters by their owner.
 *
 * <p>Two types are equal if they denote the same type, so {@code equals} can be used for type comparisons.
 */
public sealed interface Type {

    /**
     * Returns the canonical textual form with qualified names, e.g.
     * {@code consensusnode.transactions.PackedTransaction<consensusnode.transactions.accounts.AccountCreateReceipt, ...>}.
     *
     * @return the text
     */
    String text();

    private static String argumentText(final List<Type> arguments) {
        return arguments.isEmpty() ? ""
                : "<" + String.join(", ", arguments.stream().map(Type::text).toList()) + ">";
    }

    /**
     * A basic data type of the meta-language, possibly with type arguments ({@code list<int8>}).
     *
     * @param builtin   the basic type
     * @param arguments the type arguments
     */
    record BasicType(BuiltinType builtin, List<Type> arguments) implements Type {
        /**
         * Creates a basic type.
         *
         * @param builtin   the basic type
         * @param arguments the type arguments
         */
        public BasicType {
            Objects.requireNonNull(builtin, "builtin must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        }

        @Override
        public String text() {
            return builtin.name() + argumentText(arguments);
        }
    }

    /**
     * A type declared in a spec, possibly with type arguments.
     *
     * @param name      the qualified name of the declaration
     * @param arguments the type arguments
     */
    record DeclaredType(QualifiedName name, List<Type> arguments) implements Type {
        /**
         * Creates a declared type.
         *
         * @param name      the qualified name
         * @param arguments the type arguments
         */
        public DeclaredType {
            Objects.requireNonNull(name, "name must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        }

        @Override
        public String text() {
            return name + argumentText(arguments);
        }
    }

    /**
     * A generic type parameter. The owner distinguishes parameters with the same name declared by different types
     * or methods.
     *
     * @param name  the name including the {@code $$} prefix
     * @param owner the declaring type ({@code ns.Type}) or method ({@code ns.Type#method(...)})
     */
    record TypeVariable(String name, String owner) implements Type {
        /**
         * Creates a type variable.
         *
         * @param name  the name
         * @param owner the owner
         */
        public TypeVariable {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(owner, "owner must not be null");
        }

        @Override
        public String text() {
            return name;
        }
    }

    /**
     * A wildcard type argument {@code ANY} or {@code ANY extends T}.
     *
     * @param upperBound the upper bound or {@code null}
     */
    record WildcardType(@Nullable Type upperBound) implements Type {
        @Override
        public String text() {
            return upperBound == null ? "ANY" : "ANY extends " + upperBound.text();
        }
    }

    /**
     * The top type {@code ANY} used as a standalone type.
     */
    record AnyType() implements Type {
        @Override
        public String text() {
            return "ANY";
        }
    }

    /**
     * The {@code void} return type.
     */
    record VoidType() implements Type {
        @Override
        public String text() {
            return "void";
        }
    }

    /**
     * A function type {@code function<R name(p: T, ...)>}.
     *
     * @param returnType the return type
     * @param name       the descriptive function name
     * @param parameters the parameters
     */
    record FunctionType(Type returnType, String name, List<ParameterDefinition> parameters) implements Type {
        /**
         * Creates a function type.
         *
         * @param returnType the return type
         * @param name       the function name
         * @param parameters the parameters
         */
        public FunctionType {
            Objects.requireNonNull(returnType, "returnType must not be null");
            Objects.requireNonNull(name, "name must not be null");
            parameters = List.copyOf(Objects.requireNonNull(parameters, "parameters must not be null"));
        }

        @Override
        public String text() {
            return "function<" + returnType.text() + " " + name + "("
                    + String.join(", ", parameters.stream().map(p -> p.name() + ": " + p.type().text()
                    + (p.varargs() ? "..." : "")).toList()) + ")>";
        }
    }

    /**
     * A reference that could not be resolved. Only occurs in specs with validation errors; generators must not run
     * on such models.
     *
     * @param reference the reference as written in the spec
     */
    record UnresolvedType(String reference) implements Type {
        /**
         * Creates an unresolved type.
         *
         * @param reference the reference text
         */
        public UnresolvedType {
            Objects.requireNonNull(reference, "reference must not be null");
        }

        @Override
        public String text() {
            return "?" + reference;
        }
    }
}
