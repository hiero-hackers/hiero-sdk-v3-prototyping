package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.ast.Literal;

/**
 * A resolved expression of a default instance: how a value is obtained through the API. Every expression knows its
 * type; arguments are ordered like the parameters or attributes they are passed to.
 */
public sealed interface InstanceExpression {

    /**
     * Returns the type of the value.
     *
     * @return the type
     */
    Type type();

    /**
     * The default instance of a type: its own default instance if it has one, otherwise any way to obtain one.
     *
     * @param type the type
     */
    record Default(Type type) implements InstanceExpression {
    }

    /**
     * A complex type created with its attributes.
     *
     * @param type   the type
     * @param values the attribute values by attribute name (attributes that are not given are absent)
     */
    record Construct(Type.DeclaredType type, Map<String, InstanceExpression> values) implements InstanceExpression {

        /**
         * Creates a construction.
         *
         * @param type   the type
         * @param values the attribute values
         */
        public Construct {
            Objects.requireNonNull(type, "type must not be null");
            values = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(values));
        }
    }

    /**
     * A call of a namespace-level function or a static method of a type.
     *
     * @param owner     the type of a static method, {@code null} for a namespace-level function
     * @param namespace the namespace of the function or type
     * @param method    the function or method
     * @param arguments the arguments by parameter name (parameters that are not given are absent)
     * @param type      the type of the result
     */
    record Call(QualifiedName owner, String namespace, MethodDefinition method, Map<String, InstanceExpression> arguments,
                Type type) implements InstanceExpression {

        /**
         * Creates a call.
         *
         * @param owner     the type or {@code null}
         * @param namespace the namespace
         * @param method    the method
         * @param arguments the arguments
         * @param type      the result type
         */
        public Call {
            Objects.requireNonNull(namespace, "namespace must not be null");
            Objects.requireNonNull(method, "method must not be null");
            arguments = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(arguments));
        }
    }

    /**
     * A call of a method of a value.
     *
     * @param target    the value
     * @param method    the method
     * @param arguments the arguments by parameter name
     * @param type      the type of the result
     */
    record MethodCall(InstanceExpression target, MethodDefinition method, Map<String, InstanceExpression> arguments,
                      Type type) implements InstanceExpression {

        /**
         * Creates a method call.
         *
         * @param target    the value
         * @param method    the method
         * @param arguments the arguments
         * @param type      the result type
         */
        public MethodCall {
            arguments = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(arguments));
        }
    }

    /**
     * An attribute of a value.
     *
     * @param target the value
     * @param field  the attribute
     * @param type   the type of the attribute
     */
    record Access(InstanceExpression target, FieldDefinition field, Type type) implements InstanceExpression {
    }

    /**
     * A literal: string, number, {@code true}/{@code false}/{@code null} or an enum constant.
     *
     * @param literal the literal
     * @param type    the type it is used as
     */
    record Value(Literal literal, Type type) implements InstanceExpression {
    }

    /**
     * A constant of a namespace.
     *
     * @param constant the constant
     */
    record ConstantValue(ConstantDefinition constant) implements InstanceExpression {

        @Override
        public Type type() {
            return constant.type();
        }
    }

    /**
     * The elements of a {@code list}, {@code set} or {@code bytes}.
     *
     * @param items the elements
     * @param type  the type of the value
     */
    record ListValue(List<InstanceExpression> items, Type type) implements InstanceExpression {

        /**
         * Creates a list.
         *
         * @param items the elements
         * @param type  the type
         */
        public ListValue {
            items = List.copyOf(items);
        }
    }
}
