package org.hiero.sdk.v3.metalang.generator.java;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Maps meta-language types to Java types (see "Type Mapping", "Numeric Types" and "Null handling" in
 * {@code guidelines/api-best-practices-java.md}).
 *
 * <ul>
 *   <li>{@code intX}/{@code uintX}: {@code byte} (X &le; 8), {@code short} (&le; 16), {@code int} (&le; 32),
 *       {@code long} (&le; 64), otherwise {@code BigInteger}; {@code double}, {@code bool} likewise. The primitive
 *       is used unless the value is nullable or a type argument; then the wrapper class is used.</li>
 *   <li>Reference types of a declaration get {@code @NonNull} or {@code @Nullable} (jspecify, type-use syntax).</li>
 * </ul>
 */
final class JavaTypes {

    /** The jspecify package of {@code @NonNull} / {@code @Nullable}. */
    static final String JSPECIFY = "org.jspecify.annotations";

    private JavaTypes() {
    }

    /**
     * Returns the Java type of a declaration (field, parameter, return value) including its nullness annotation.
     *
     * @param type     the meta-language type
     * @param nullable whether the declaration is {@code @@nullable}
     * @param imports  the imports of the file
     * @return the Java type, e.g. {@code int}, {@code @Nullable Integer}, {@code @NonNull String},
     *         {@code byte @NonNull []}
     */
    static String declaration(final Type type, final boolean nullable, final Imports imports) {
        final String java = type(type, nullable, imports);
        if (isPrimitive(java) || java.equals("void")) {
            return java;
        }
        return annotate(java, imports.use(JSPECIFY, nullable ? "Nullable" : "NonNull"));
    }

    /**
     * Returns the Java type without nullness annotation.
     *
     * @param type    the meta-language type
     * @param boxed   whether a primitive must be replaced by its wrapper class
     * @param imports the imports of the file
     * @return the Java type
     * @throws UnsupportedTypeException if the type has no Java mapping yet
     */
    static String type(final Type type, final boolean boxed, final Imports imports) {
        Objects.requireNonNull(type, "type must not be null");
        return switch (type) {
            case Type.BasicType basic -> basic(basic, boxed, imports);
            case Type.DeclaredType declared -> imports.use(JavaNames.packageName(declared.name().namespace()),
                    declared.name().name()) + arguments(declared.arguments(), imports);
            case Type.TypeVariable variable -> variable.name().substring(2);
            case Type.WildcardType wildcard -> wildcard.upperBound() == null ? "?"
                    : "? extends " + type(wildcard.upperBound(), true, imports);
            case Type.AnyType ignored -> "Object";
            case Type.VoidType ignored -> boxed ? "Void" : "void";
            case Type.FunctionType function -> throw new UnsupportedTypeException(function.text());
            case Type.UnresolvedType unresolved -> throw new UnsupportedTypeException(unresolved.text());
        };
    }

    private static String arguments(final List<Type> arguments, final Imports imports) {
        return arguments.isEmpty() ? ""
                : "<" + String.join(", ", arguments.stream().map(a -> type(a, true, imports)).toList()) + ">";
    }

    private static String basic(final Type.BasicType basic, final boolean boxed, final Imports imports) {
        final BuiltinType builtin = basic.builtin();
        return switch (builtin.category()) {
            case INTEGER -> integer(builtin.bits(), boxed, imports);
            case FLOAT -> boxed ? "Double" : "double";
            case DECIMAL -> imports.use("java.math", "BigDecimal");
            case BOOL -> boxed ? "Boolean" : "boolean";
            case STRING -> "String";
            case BYTES -> "byte[]";
            case COLLECTION -> imports.use("java.util", builtin.name().equals("set") ? "Set" : "List")
                    + arguments(basic.arguments(), imports);
            case MAP -> imports.use("java.util", "Map") + arguments(basic.arguments(), imports);
            case TYPE -> "Class<" + (basic.arguments().isEmpty() ? "?"
                    : "? extends " + type(basic.arguments().getFirst(), true, imports)) + ">";
            case UUID -> imports.use("java.util", "UUID");
            case TEMPORAL -> imports.use("java.time", switch (builtin.name()) {
                case "date" -> "LocalDate";
                case "time" -> "LocalTime";
                case "dateTime" -> "LocalDateTime";
                default -> "ZonedDateTime";
            });
            case DURATION -> imports.use("java.time", "Duration");
            case STREAM_RESULT -> throw new UnsupportedTypeException(basic.text());
        };
    }

    private static String integer(final int bits, final boolean boxed, final Imports imports) {
        if (bits <= 8) {
            return boxed ? "Byte" : "byte";
        } else if (bits <= 16) {
            return boxed ? "Short" : "short";
        } else if (bits <= 32) {
            return boxed ? "Integer" : "int";
        } else if (bits <= 64) {
            return boxed ? "Long" : "long";
        }
        return imports.use("java.math", "BigInteger");
    }

    /**
     * Whether the Java type is a primitive type.
     *
     * @param java the Java type
     * @return {@code true} for primitives
     */
    static boolean isPrimitive(final String java) {
        return switch (java) {
            case "byte", "short", "int", "long", "double", "boolean" -> true;
            default -> false;
        };
    }

    /**
     * Puts a type-use annotation at the right place: in front of the simple type ({@code @NonNull String}) or in
     * front of the array brackets ({@code byte @NonNull []}).
     */
    static String annotate(final String java, final String annotation) {
        if (java.endsWith("[]")) {
            return java.substring(0, java.length() - 2) + " @" + annotation + " []";
        }
        final int lastDot = java.contains("<") ? java.substring(0, java.indexOf('<')).lastIndexOf('.')
                : java.lastIndexOf('.');
        if (lastDot >= 0) {
            return java.substring(0, lastDot + 1) + "@" + annotation + " " + java.substring(lastDot + 1);
        }
        return "@" + annotation + " " + java;
    }

    /**
     * Thrown for meta-language types that have no Java mapping yet.
     */
    static final class UnsupportedTypeException extends RuntimeException {

        UnsupportedTypeException(final String type) {
            super("Type '" + type + "' has no Java mapping yet");
        }
    }
}
