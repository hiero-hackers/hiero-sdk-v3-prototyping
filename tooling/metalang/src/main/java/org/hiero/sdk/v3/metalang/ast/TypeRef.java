package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * A reference to a type, e.g. {@code string}, {@code list<AccountId>}, {@code $$T}, {@code ANY},
 * {@code void} or {@code function<void run()>}.
 */
public sealed interface TypeRef extends Node {

    /**
     * Returns the canonical textual form of the type reference.
     *
     * @return the text
     */
    String text();

    /**
     * A reference to a basic data type or a complex type, possibly qualified and with type arguments.
     *
     * @param name      the simple or qualified name ({@code ns.Type})
     * @param arguments the type arguments (empty if none)
     * @param location  the source location
     */
    record Named(String name, List<TypeArgument> arguments, SourceLocation location) implements TypeRef {
        /**
         * Creates a named type reference.
         *
         * @param name      the name
         * @param arguments the type arguments
         * @param location  the source location
         */
        public Named {
            Objects.requireNonNull(name, "name must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
            Objects.requireNonNull(location, "location must not be null");
        }

        /**
         * Returns whether the name is qualified with a namespace.
         *
         * @return {@code true} if qualified
         */
        public boolean isQualified() {
            return name.contains(".");
        }

        /**
         * Returns the simple name (last segment).
         *
         * @return the simple name
         */
        public String simpleName() {
            return name.substring(name.lastIndexOf('.') + 1);
        }

        @Override
        public String text() {
            if (arguments.isEmpty()) {
                return name;
            }
            return name + "<" + String.join(", ", arguments.stream().map(TypeArgument::text).toList()) + ">";
        }
    }

    /**
     * A reference to a generic type parameter, e.g. {@code $$T}.
     *
     * @param name     the name including the {@code $$} prefix
     * @param location the source location
     */
    record GenericParameter(String name, SourceLocation location) implements TypeRef {
        /**
         * Creates a generic parameter reference.
         *
         * @param name     the name including prefix
         * @param location the source location
         */
        public GenericParameter {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public String text() {
            return name;
        }
    }

    /**
     * The top type {@code ANY} used as a standalone type.
     *
     * @param location the source location
     */
    record Any(SourceLocation location) implements TypeRef {
        /**
         * Creates an ANY reference.
         *
         * @param location the source location
         */
        public Any {
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public String text() {
            return "ANY";
        }
    }

    /**
     * The {@code void} return type.
     *
     * @param location the source location
     */
    record Void(SourceLocation location) implements TypeRef {
        /**
         * Creates a void reference.
         *
         * @param location the source location
         */
        public Void {
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public String text() {
            return "void";
        }
    }

    /**
     * A function type {@code function<R m(p: T, ...)>}.
     *
     * @param returnType the return type
     * @param name       the descriptive function name
     * @param parameters the parameters
     * @param location   the source location
     */
    record Function(TypeRef returnType, String name, List<Parameter> parameters, SourceLocation location)
            implements TypeRef {
        /**
         * Creates a function type.
         *
         * @param returnType the return type
         * @param name       the function name
         * @param parameters the parameters
         * @param location   the source location
         */
        public Function {
            Objects.requireNonNull(returnType, "returnType must not be null");
            Objects.requireNonNull(name, "name must not be null");
            parameters = List.copyOf(Objects.requireNonNull(parameters, "parameters must not be null"));
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public String text() {
            return "function<" + returnType.text() + " " + name + "("
                    + String.join(", ", parameters.stream().map(Parameter::text).toList()) + ")>";
        }
    }

    /**
     * A type argument inside angle brackets.
     */
    sealed interface TypeArgument extends Node {

        /**
         * Returns the canonical textual form.
         *
         * @return the text
         */
        String text();
    }

    /**
     * A concrete type argument, e.g. {@code AccountId} in {@code list<AccountId>}.
     *
     * @param type the type
     */
    record Concrete(TypeRef type) implements TypeArgument {
        /**
         * Creates a concrete type argument.
         *
         * @param type the type
         */
        public Concrete {
            Objects.requireNonNull(type, "type must not be null");
        }

        @Override
        public SourceLocation location() {
            return type.location();
        }

        @Override
        public String text() {
            return type.text();
        }
    }

    /**
     * A wildcard argument {@code ANY} or {@code ANY extends T}.
     *
     * @param upperBound the upper bound or {@code null}
     * @param location   the source location
     */
    record Wildcard(@Nullable TypeRef upperBound, SourceLocation location) implements TypeArgument {
        /**
         * Creates a wildcard argument.
         *
         * @param upperBound the upper bound or {@code null}
         * @param location   the source location
         */
        public Wildcard {
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public String text() {
            return upperBound == null ? "ANY" : "ANY extends " + upperBound.text();
        }
    }

    /**
     * A generic parameter with a bound at the use site, e.g. {@code $$R extends Receipt} in a return
     * type. Not part of the guideline (bounds belong to the declaration); kept so the validator can
     * report it precisely.
     *
     * @param name     the generic parameter name including prefix
     * @param bound    the bound
     * @param location the source location
     */
    record BoundedGeneric(String name, TypeRef bound, SourceLocation location) implements TypeArgument {
        /**
         * Creates a bounded generic argument.
         *
         * @param name     the parameter name
         * @param bound    the bound
         * @param location the source location
         */
        public BoundedGeneric {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(bound, "bound must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public String text() {
            return name + " extends " + bound.text();
        }
    }
}
