package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * A statement of a test (see "Statements" in {@code guidelines/test-language.md}).
 */
public sealed interface Statement extends Node {

    /**
     * Binds a value to a name: {@code let id = AccountId.fromString(value: "0.0.1234")}.
     *
     * @param name     the name
     * @param type     the declared type, {@code null} if the type is inferred
     * @param value    the value
     * @param location the location of {@code let}
     */
    record Let(String name, @Nullable TypeRef type, Expression value, SourceLocation location) implements Statement {

        /**
         * Creates a binding.
         *
         * @param name     the name
         * @param type     the declared type or {@code null}
         * @param value    the value
         * @param location the location
         */
        public Let {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * Evaluates an expression and ignores its result: a call such as {@code assertEquals(expected: 1, actual: n)},
     * optionally annotated, e.g. with {@code @@expectThrows(illegal-format)}.
     *
     * @param annotations the annotations of the statement
     * @param expression  the expression
     * @param location    the location of the statement's first token
     */
    record Evaluate(List<Annotation> annotations, Expression expression, SourceLocation location)
            implements Statement {

        /**
         * Creates an expression statement.
         *
         * @param annotations the annotations
         * @param expression  the expression
         * @param location    the location
         */
        public Evaluate {
            annotations = List.copyOf(annotations);
            Objects.requireNonNull(expression, "expression must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * Runs one of two blocks: {@code if condition { … } else { … }}. An {@code else if} is an {@code If} as the only
     * statement of {@code otherwise}.
     *
     * @param condition the condition
     * @param then      the statements run if the condition is true
     * @param otherwise the statements run if it is false (empty without {@code else})
     * @param location  the location of {@code if}
     */
    record If(Expression condition, List<Statement> then, List<Statement> otherwise, SourceLocation location)
            implements Statement {

        /**
         * Creates a conditional statement.
         *
         * @param condition the condition
         * @param then      the statements of the true branch
         * @param otherwise the statements of the false branch
         * @param location  the location
         */
        public If {
            Objects.requireNonNull(condition, "condition must not be null");
            then = List.copyOf(then);
            otherwise = List.copyOf(otherwise);
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * Runs a block once per element of a list or set: {@code for key in keys { … }}.
     *
     * @param variable the name bound to the element
     * @param iterable the list or set
     * @param body     the statements
     * @param location the location of {@code for}
     */
    record For(String variable, Expression iterable, List<Statement> body, SourceLocation location)
            implements Statement {

        /**
         * Creates a loop.
         *
         * @param variable the variable
         * @param iterable the list or set
         * @param body     the statements
         * @param location the location
         */
        public For {
            Objects.requireNonNull(variable, "variable must not be null");
            Objects.requireNonNull(iterable, "iterable must not be null");
            body = List.copyOf(body);
            Objects.requireNonNull(location, "location must not be null");
        }
    }
}
