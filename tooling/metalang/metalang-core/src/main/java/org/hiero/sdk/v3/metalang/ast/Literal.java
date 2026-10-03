package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A literal value as used in constants and annotation arguments.
 */
public sealed interface Literal extends Node {

    /**
     * Returns the literal as it is written in the source (normalized whitespace).
     *
     * @return the source text
     */
    String text();

    /**
     * A string literal.
     *
     * @param value    the unescaped value
     * @param location the source location
     */
    record StringLiteral(String value, SourceLocation location) implements Literal {
        /**
         * Creates a string literal.
         *
         * @param value    the unescaped value
         * @param location the source location
         */
        public StringLiteral {
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public String text() {
            return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
        }
    }

    /**
     * A numeric literal. The source text is kept so no precision is lost.
     *
     * @param text     the number as written, e.g. {@code 100_000} or {@code -1.5}
     * @param location the source location
     */
    record NumberLiteral(String text, SourceLocation location) implements Literal {
        /**
         * Creates a number literal.
         *
         * @param text     the number as written
         * @param location the source location
         */
        public NumberLiteral {
            Objects.requireNonNull(text, "text must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }

        /**
         * Returns the numeric value with digit separators removed.
         *
         * @return the value
         */
        public java.math.BigDecimal value() {
            return new java.math.BigDecimal(text.replace("_", ""));
        }
    }

    /**
     * A list literal such as {@code []}.
     *
     * @param items    the items
     * @param location the source location
     */
    record ListLiteral(List<Literal> items, SourceLocation location) implements Literal {
        /**
         * Creates a list literal.
         *
         * @param items    the items
         * @param location the source location
         */
        public ListLiteral {
            items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public String text() {
            return "[" + String.join(", ", items.stream().map(Literal::text).toList()) + "]";
        }
    }

    /**
     * A struct literal such as {@code Address{shard: 0, realm: 0}}.
     *
     * @param typeName the (possibly qualified) type name
     * @param entries  the entries in source order
     * @param location the source location
     */
    record StructLiteral(String typeName, List<StructEntry> entries, SourceLocation location) implements Literal {
        /**
         * Creates a struct literal.
         *
         * @param typeName the type name
         * @param entries  the entries
         * @param location the source location
         */
        public StructLiteral {
            Objects.requireNonNull(typeName, "typeName must not be null");
            entries = List.copyOf(Objects.requireNonNull(entries, "entries must not be null"));
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public String text() {
            return typeName + "{" + String.join(", ",
                    entries.stream().map(e -> e.name() + ": " + e.value().text()).toList()) + "}";
        }
    }

    /**
     * One entry of a {@link StructLiteral}.
     *
     * @param name  the field name
     * @param value the value
     */
    record StructEntry(String name, Literal value) {
        /**
         * Creates a struct entry.
         *
         * @param name  the field name
         * @param value the value
         */
        public StructEntry {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(value, "value must not be null");
        }
    }

    /**
     * A bare or qualified name: {@code true}, {@code null}, an enum value, a field name, or a
     * kebab-case error identifier.
     *
     * @param text     the name
     * @param location the source location
     */
    record NameLiteral(String text, SourceLocation location) implements Literal {
        /**
         * Creates a name literal.
         *
         * @param text     the name
         * @param location the source location
         */
        public NameLiteral {
            Objects.requireNonNull(text, "text must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }
}
