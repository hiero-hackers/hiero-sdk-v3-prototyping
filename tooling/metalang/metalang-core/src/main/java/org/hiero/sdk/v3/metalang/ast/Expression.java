package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * An expression of a default instance or a test: it describes how a value is obtained through the API. Arguments are
 * always named after the parameter or attribute they are passed to. The operators, indexes and maps
 * ({@link Negate}, {@link Binary}, {@link IsNull}, {@link IsType}, {@link Index}, {@link MapValue}) are only allowed in
 * tests.
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

    /**
     * The element of a list or the value of a map: {@code keys[0]}, {@code balances[account]}.
     *
     * @param target   the list or map
     * @param index    the index or key
     * @param location the location of the opening bracket
     */
    record Index(Expression target, Expression index, SourceLocation location) implements Expression {

        /**
         * Creates an index expression.
         *
         * @param target   the list or map
         * @param index    the index or key
         * @param location the location
         */
        public Index {
            Objects.requireNonNull(target, "target must not be null");
            Objects.requireNonNull(index, "index must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * The negation of a number: {@code -amount}. A negated number literal is no {@code Negate} but a negative
     * {@link Literal.NumberLiteral}.
     *
     * @param operand  the number
     * @param location the location of the minus sign
     */
    record Negate(Expression operand, SourceLocation location) implements Expression {

        /**
         * Creates a negation.
         *
         * @param operand  the number
         * @param location the location
         */
        public Negate {
            Objects.requireNonNull(operand, "operand must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * The binary operators, in the notation of the test language.
     */
    enum Operator {
        /** {@code *}. */
        MULTIPLY("*"),
        /** {@code /}. */
        DIVIDE("/"),
        /** {@code %}. */
        REMAINDER("%"),
        /** {@code +}. */
        ADD("+"),
        /** {@code -}. */
        SUBTRACT("-"),
        /** {@code <}. */
        LESS("<"),
        /** {@code <=}. */
        LESS_OR_EQUAL("<="),
        /** {@code >}. */
        GREATER(">"),
        /** {@code >=}. */
        GREATER_OR_EQUAL(">="),
        /** {@code ==}. */
        EQUAL("=="),
        /** {@code !=}. */
        NOT_EQUAL("!="),
        /** {@code &&}. */
        AND("&&"),
        /** {@code ||}. */
        OR("||");

        private final String symbol;

        Operator(final String symbol) {
            this.symbol = symbol;
        }

        /**
         * Returns the operator as written in a test.
         *
         * @return the symbol
         */
        public String symbol() {
            return symbol;
        }
    }

    /**
     * A binary operation: {@code a + b}, {@code a == b}, {@code a && b}.
     *
     * @param operator the operator
     * @param left     the left operand
     * @param right    the right operand
     * @param location the location of the operator
     */
    record Binary(Operator operator, Expression left, Expression right, SourceLocation location)
            implements Expression {

        /**
         * Creates a binary operation.
         *
         * @param operator the operator
         * @param left     the left operand
         * @param right    the right operand
         * @param location the location
         */
        public Binary {
            Objects.requireNonNull(operator, "operator must not be null");
            Objects.requireNonNull(left, "left must not be null");
            Objects.requireNonNull(right, "right must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A check for {@code null}: {@code a is null}, {@code a is not null}.
     *
     * @param operand  the value
     * @param negated  whether it is {@code is not null}
     * @param location the location of {@code is}
     */
    record IsNull(Expression operand, boolean negated, SourceLocation location) implements Expression {

        /**
         * Creates a null check.
         *
         * @param operand  the value
         * @param negated  whether the check is negated
         * @param location the location
         */
        public IsNull {
            Objects.requireNonNull(operand, "operand must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A check for a type: {@code a is PublicKey}, {@code a is not PublicKey}.
     *
     * @param operand  the value
     * @param type     the type
     * @param negated  whether it is {@code is not}
     * @param location the location of {@code is}
     */
    record IsType(Expression operand, TypeRef type, boolean negated, SourceLocation location) implements Expression {

        /**
         * Creates a type check.
         *
         * @param operand  the value
         * @param type     the type
         * @param negated  whether the check is negated
         * @param location the location
         */
        public IsType {
            Objects.requireNonNull(operand, "operand must not be null");
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * An entry of a map.
     *
     * @param key   the key
     * @param value the value
     */
    record Entry(Expression key, Expression value) {

        /**
         * Creates an entry.
         *
         * @param key   the key
         * @param value the value
         */
        public Entry {
            Objects.requireNonNull(key, "key must not be null");
            Objects.requireNonNull(value, "value must not be null");
        }
    }

    /**
     * A map: {@code {"a": 1, "b": 2}}.
     *
     * @param entries  the entries in source order
     * @param location the location of the opening brace
     */
    record MapValue(List<Entry> entries, SourceLocation location) implements Expression {

        /**
         * Creates a map.
         *
         * @param entries  the entries
         * @param location the location
         */
        public MapValue {
            entries = List.copyOf(entries);
            Objects.requireNonNull(location, "location must not be null");
        }
    }
}
