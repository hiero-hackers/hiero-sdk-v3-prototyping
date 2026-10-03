package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * An expression of a default instance: it describes how a value is obtained through the API. Arguments are always
 * named after the parameter or attribute they are passed to.
 */
public sealed interface Expression extends Node {

    /**
     * A named argument.
     *
     * @param name     the name of the parameter or attribute
     * @param value    the value
     * @param location the location of the name
     */
    record Argument(String name, Expression value, SourceLocation location) implements Node {

        /**
         * Creates an argument.
         *
         * @param name     the name
         * @param value    the value
         * @param location the location
         */
        public Argument {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * The default instance of a type: {@code DEFAULT} (of the expected type) or {@code DEFAULT(Type)}.
     *
     * @param type     the explicit type, {@code null} for the expected type
     * @param location the location
     */
    record Default(@Nullable TypeRef type, SourceLocation location) implements Expression {
    }

    /**
     * Creates a complex type with its attributes: {@code AccountId{shard: 0, realm: 0, num: 2}}.
     *
     * @param type     the type with its type arguments
     * @param values   the attribute values
     * @param location the location
     */
    record Construct(TypeRef.Named type, List<Argument> values, SourceLocation location) implements Expression {

        /**
         * Creates a construction.
         *
         * @param type     the type
         * @param values   the attribute values
         * @param location the location
         */
        public Construct {
            Objects.requireNonNull(type, "type must not be null");
            values = List.copyOf(values);
        }
    }

    /**
     * Calls a namespace-level function or a static method: {@code createClient(...)},
     * {@code TransactionId.generateTransactionId(...)}, {@code keys.generatePrivateKey(...)}.
     *
     * @param name      the (qualified) name of the function or {@code Type.method}
     * @param arguments the arguments
     * @param location  the location
     */
    record Call(String name, List<Argument> arguments, SourceLocation location) implements Expression {

        /**
         * Creates a call.
         *
         * @param name      the name
         * @param arguments the arguments
         * @param location  the location
         */
        public Call {
            Objects.requireNonNull(name, "name must not be null");
            arguments = List.copyOf(arguments);
        }
    }

    /**
     * Calls a method of a value: {@code DEFAULT(PrivateKey).createPublicKey()}.
     *
     * @param target    the value
     * @param method    the method name
     * @param arguments the arguments
     * @param location  the location of the method name
     */
    record MethodCall(Expression target, String method, List<Argument> arguments, SourceLocation location)
            implements Expression {

        /**
         * Creates a method call.
         *
         * @param target    the value
         * @param method    the method name
         * @param arguments the arguments
         * @param location  the location
         */
        public MethodCall {
            Objects.requireNonNull(target, "target must not be null");
            Objects.requireNonNull(method, "method must not be null");
            arguments = List.copyOf(arguments);
        }
    }

    /**
     * Reads an attribute of a value: {@code DEFAULT(HieroClient<ANY>).transactionSigner}.
     *
     * @param target    the value
     * @param attribute the attribute name
     * @param location  the location of the attribute name
     */
    record Access(Expression target, String attribute, SourceLocation location) implements Expression {
    }

    /**
     * A literal: string, number, {@code true}/{@code false}/{@code null}, an enum constant ({@code KeyAlgorithm.ED25519})
     * or a constant ({@code ZERO_ACCOUNT_ID}).
     *
     * @param literal  the literal
     * @param location the location
     */
    record Value(Literal literal, SourceLocation location) implements Expression {
    }

    /**
     * A list: elements of a {@code list}, {@code set} or {@code bytes} ({@code [127, 0, 0, 1]}).
     *
     * @param items    the elements
     * @param location the location
     */
    record ListValue(List<Expression> items, SourceLocation location) implements Expression {

        /**
         * Creates a list.
         *
         * @param items    the elements
         * @param location the location
         */
        public ListValue {
            items = List.copyOf(items);
        }
    }
}
